/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNConflictDescriptor;
import org.eclipse.team.svn.core.connector.SVNConflictResolution;
import org.eclipse.team.svn.core.connector.SVNConflictVersion;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.connector.SVNEntry;
import org.eclipse.team.svn.core.connector.SVNEntryReference;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.connector.SVNConflictDescriptor.Action;
import org.eclipse.team.svn.core.connector.SVNConflictDescriptor.Operation;
import org.eclipse.team.svn.core.connector.SVNConflictDescriptor.Reason;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.SVNNullProgressMonitor;
import org.eclipse.team.svn.core.operation.local.MarkResolvedOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RevertOperation;
import org.eclipse.team.svn.core.operation.local.UpdateOperation;
import org.eclipse.team.svn.core.operation.local.refactor.DeleteResourceOperation;
import org.eclipse.team.svn.core.operation.remote.CopyRemoteResourcesToWcOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;

/**
 * Helper class for <class>EditTreeConflictsPanel</class>
 * 
 * @author Igor Burilo
 */
public class EditTreeConflictsHelper {

	protected ILocalResource local;
	protected SVNConflictDescriptor treeConflict;
	
	public EditTreeConflictsHelper(ILocalResource local) {
		this.local = local;
		this.treeConflict = this.local.getTreeConflictDescriptor();
	}
	
	public String getOperationAsString() {
		String operation;
		switch (this.treeConflict.operation) {
			case Operation.UPDATE:
				operation = SVNUIMessages.EditTreeConflictsPanel_Update_Operation;
				break;
			case Operation.MERGE:
				operation = SVNUIMessages.EditTreeConflictsPanel_Merge_Operation;
				break;
			case Operation.SWITCHED:
				operation = SVNUIMessages.EditTreeConflictsPanel_Switch_Operation;
				break;	
			default:
				operation = SVNUIMessages.EditTreeConflictsPanel_None_Operation;
		}
		return operation;
	}
	
	public String getReasonAsString() {
		String reason = ""; //$NON-NLS-1$
		switch (this.treeConflict.reason) {
			case Reason.ADDED:
				reason = SVNUIMessages.EditTreeConflictsPanel_Add_Reason;
				break;
			case Reason.DELETED:
				reason = SVNUIMessages.EditTreeConflictsPanel_Delete_Reason;
				break;
			case Reason.MISSING:
				reason = SVNUIMessages.EditTreeConflictsPanel_Missing_Reason;
				break;
			case Reason.MODIFIED:
				reason = SVNUIMessages.EditTreeConflictsPanel_Modified_Reason;
				break;
			case Reason.OBSTRUCTED:
				reason = SVNUIMessages.EditTreeConflictsPanel_Obstructed_Reason;
				break;
			case Reason.UNVERSIONED:
				reason = SVNUIMessages.EditTreeConflictsPanel_Unversioned_Reason;
				break;
		}
		return reason;
	}
	
	public String getActionAsString() {
		String action = ""; //$NON-NLS-1$
		if (this.treeConflict.action == Action.MODIFY) {
			action = SVNUIMessages.EditTreeConflictsPanel_Modify_Action;
		} else if (this.treeConflict.action == Action.ADD) {
			action = SVNUIMessages.EditTreeConflictsPanel_Add_Action;
		} else if (this.treeConflict.action == Action.DELETE) {
			action = SVNUIMessages.EditTreeConflictsPanel_Delete_Action;
		}
		return action;
	}
		
	public String getTip() {
		String tip = null;
		/*	
		 * Add it ?
		 *  	
		 * [Merge: local missing incoming edit]
		 * 		In case alpha was renamed, rename alpha in the branch
         * 		and run the merge again. 	
		 */
		if (this.treeConflict.action == Action.DELETE && this.treeConflict.reason == Reason.DELETED) {
			tip = SVNUIMessages.EditTreeConflictsPanel_Tip1;
		} else if (this.treeConflict.reason == Reason.MODIFIED &&  this.treeConflict.action == Action.DELETE) {
			 tip = SVNUIMessages.EditTreeConflictsPanel_Tip2;
		} else if (this.treeConflict.reason == Reason.DELETED || this.treeConflict.action == Action.DELETE) {
			tip = SVNUIMessages.EditTreeConflictsPanel_Tip3;
		}					
		return tip;
	}
	
	public String getSrcUrl(boolean isLeft) {
		SVNConflictVersion version = isLeft ? this.treeConflict.srcLeftVersion : this.treeConflict.srcRightVersion;
		String url = version.reposURL + "/" + version.pathInRepos; //$NON-NLS-1$
		url = SVNUtility.normalizeURL(url);
		return url;
	}
	
	/*
	 * When we apply incoming changes we're trying not to make the conflict 
	 * as resolved (e.g. because of another conflicts may exist or user after this may need
	 * to perform other additional steps), but it's not always possible, e.g.
	 * some resolutions require 'revert' operation (which clears conflict markers) 
	 */
	public boolean isRemoteOperationResolveTheConflict() {
		return this.treeConflict.action == Action.MODIFY && (this.treeConflict.operation == Operation.UPDATE || this.treeConflict.operation == Operation.SWITCHED);
	}
	
	public IActionOperation getOperation(boolean isRemoteResolution, boolean isLocalResolution, boolean markAsMerged) {
		CompositeOperation cmpOp = null;
		String opName = ""; //$NON-NLS-1$
		//used as parameter to operations, e.g. update, revert
		boolean isRecursive = true;
		if (isLocalResolution) {
			//resolved
			cmpOp = new CompositeOperation(opName);			
			IActionOperation resolvedOp = this.getResolvedOperation();			
			cmpOp.add(resolvedOp);						
		} else if (isRemoteResolution) {
			if (this.treeConflict.action == Action.ADD) {
				cmpOp = this.getRemoteAddOperation(opName, isRecursive);										
			} else if (this.treeConflict.action == Action.DELETE) {
				cmpOp = this.getRemoteDeleteOperation(opName);
			} else if (this.treeConflict.action == Action.MODIFY) {																
				cmpOp = this.getRemoteModifyOperation(opName, isRecursive);								
			}						
		}
		
		//add resolved operation
		boolean isManual = !isRemoteResolution && !isLocalResolution;
		if (markAsMerged && (isRemoteResolution && !this.isRemoteOperationResolveTheConflict() || isManual)) {			
			if (isManual) {
				cmpOp = new CompositeOperation(opName);		
			}			
			cmpOp.add(this.getResolvedOperation());
		}
		
		if (cmpOp != null) {
			//TODO refresh parent ?
			cmpOp.add(new RefreshResourcesOperation(new IResource[]{this.local.getResource()}));
		}						
		return cmpOp;
	}
	
	protected CompositeOperation getRemoteDeleteOperation(String opName) {
		/* 
		 * If item doesn't exist locally, i.e. missing or deleted then: resolved
		 * otherwise: delete + resolved
		 */
		CompositeOperation cmpOp = new CompositeOperation(opName);				
		
		DeleteResourceOperation deleteOp = null;				
		if (IStateFilter.ST_DELETED != this.local.getStatus() && IStateFilter.ST_MISSING != this.local.getStatus()) {
			deleteOp = new DeleteResourceOperation(this.local.getResource());
			cmpOp.add(deleteOp);
		}				
		
//		IActionOperation resolveOp = this.getResolvedOperation();
//		cmpOp.add(resolveOp, deleteOp == null ? null : new IActionOperation[]{deleteOp});		
		return cmpOp;
	}
	
	protected CompositeOperation getRemoteModifyOperation(String opName, boolean isRecursive) {
		/*
		 * For 'update' operation:
		 * 		revert + update
		 * 
		 * For 'merge' operation:
		 * 		[delete] + copy + resolved
		 * 		Result: R+
		 */
		
		CompositeOperation cmpOp = null;
		IResource resource = this.local.getResource();			
		if (this.treeConflict.operation == Operation.UPDATE || this.treeConflict.operation == Operation.SWITCHED) {
			cmpOp = new CompositeOperation(opName);									
			
			IActionOperation resolveOp = new RevertOperation(new IResource[]{resource}, isRecursive);
			cmpOp.add(resolveOp);						
			
			SVNRevision rev = this.treeConflict.operation == Operation.UPDATE ? SVNRevision.HEAD : SVNRevision.fromNumber(this.treeConflict.srcRightVersion.pegRevision);
			boolean ignoreExternals = SVNTeamPreferences.getBehaviourBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BEHAVIOUR_IGNORE_EXTERNALS_NAME);
			UpdateOperation updateOp = new UpdateOperation(new IResource[]{resource}, rev, ignoreExternals);
			cmpOp.add(updateOp, new IActionOperation[]{resolveOp});	
		} else if (this.treeConflict.operation == Operation.MERGE) {
			cmpOp = new CompositeOperation(opName);			
			
			DeleteResourceOperation deleteOp = null;				
			if (resource.exists()) {
				deleteOp = new DeleteResourceOperation(resource);
				cmpOp.add(deleteOp);
			}						
			
			IActionOperation copyOp = this.getCopyResourceOperation();
			cmpOp.add(copyOp, deleteOp == null ? null : new IActionOperation[]{deleteOp});
			
//			IActionOperation resolvedOp = this.getResolvedOperation();
//			cmpOp.add(resolvedOp, new IActionOperation[]{copyOp});														
		}
		return cmpOp;
	}
	
	protected CompositeOperation getRemoteAddOperation(String opName, boolean isRecursive) {		
		/*
		 * 'Update' operation: resolved + [delete[ + update
		 * 'Merge' operation:  resolved + [delete] + copy   
		 * 
		 * For 'merge' operation we perform a copy
		 * For 'update' operation we perform update because file already exists in repository		 
		 */						
		CompositeOperation cmpOp = new CompositeOperation(opName);						
			
		DeleteResourceOperation deleteOp = null;
		//if resource exists on file system we delete it
		if (this.local.getResource().exists()) {
			deleteOp = new DeleteResourceOperation(this.local.getResource());
			cmpOp.add(deleteOp);			
		}
		
		IActionOperation updateOp;
		if (this.treeConflict.operation == Operation.MERGE) {
			updateOp = this.getCopyResourceOperation();		
		} else {
			SVNRevision rev = this.treeConflict.operation == Operation.UPDATE ? SVNRevision.HEAD : SVNRevision.fromNumber(this.treeConflict.srcRightVersion.pegRevision);
			boolean ignoreExternals = SVNTeamPreferences.getBehaviourBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BEHAVIOUR_IGNORE_EXTERNALS_NAME);
			updateOp = new UpdateOperation(new IResource[]{this.local.getResource()}, rev, ignoreExternals);				
		}																		
		cmpOp.add(updateOp, deleteOp == null ? null : new IActionOperation[]{deleteOp});
		
//		IActionOperation resolveOp = this.getResolvedOperation();
//		cmpOp.add(resolveOp, new IActionOperation[]{updateOp});
		
		return cmpOp;					
	}
	
	protected IActionOperation getCopyResourceOperation() {
		String url = this.getSrcUrl(false);
		long pegRev = this.treeConflict.srcRightVersion.pegRevision;
		boolean isFolder = this.treeConflict.nodeKind == SVNEntry.Kind.DIR;		
		
		return new CopyRemoteResourcesToWcOperation(new SVNEntryReference(url, SVNRevision.fromNumber(pegRev)), isFolder, this.local.getResource());
	}
	
	protected IActionOperation getResolvedOperation() {
		return new MarkResolvedOperation(new IResource[] {this.local.getResource()}, SVNConflictResolution.CHOOSE_LOCAL_FULL, ISVNConnector.Depth.INFINITY);		
	}
	
	public IRepositoryResource getRepositoryResourceForHistory(boolean isLeft) {
		SVNConflictVersion version = isLeft ? this.treeConflict.srcLeftVersion : this.treeConflict.srcRightVersion;		
		String url = this.getSrcUrl(isLeft);		
		String repos = version.reposURL;
		repos = SVNUtility.normalizeURL(repos);		
		SVNRevision revision = SVNRevision.fromNumber(this.treeConflict.srcRightVersion.pegRevision);
		
		//find the first parent of resource which exists in repository in end revision (srcRightSource's pegRevision)
		while (true) {
			IPath path = SVNUtility.createPathForSVNUrl(url);
			path = path.removeLastSegments(1);
			url = path.toString();
			
			IRepositoryLocation location = null;
			ISVNConnector proxy = null;
			try {				
				SVNEntryRevisionReference ref = new SVNEntryRevisionReference(url, revision, revision);								
				IRepositoryResource rr = SVNUtility.asRepositoryResource(url, true);
				location = rr.getRepositoryLocation();
				proxy = location.acquireSVNProxy();				
				SVNUtility.info(proxy, ref, Depth.EMPTY, new SVNNullProgressMonitor());
				break;
			} catch (SVNConnectorException e) {
				if (repos.equals(url)) {
					break;
				}
			} finally {
				if (location != null && proxy != null) {
					location.releaseSVNProxy(proxy);	
				}				
			}
		}			
				
		IRepositoryResource repositoryResource = SVNUtility.asRepositoryResource(url, true);
		repositoryResource.setPegRevision(revision);
		repositoryResource.setSelectedRevision(revision);
		return repositoryResource;
	}
}
