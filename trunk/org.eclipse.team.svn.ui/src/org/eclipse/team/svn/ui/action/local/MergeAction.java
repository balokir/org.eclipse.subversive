/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.action.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.local.JavaHLMergeOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.SaveProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.UpdateOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.ResourcesParentsProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractNonRecursiveTeamAction;
import org.eclipse.team.svn.ui.dialog.AdvancedDialog;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.dialog.OperationErrorDialog;
import org.eclipse.team.svn.ui.operation.ClearUpdateStatusesOperation;
import org.eclipse.team.svn.ui.operation.NotifyUnresolvedConflictOperation;
import org.eclipse.team.svn.ui.operation.ShowMergeViewOperation;
import org.eclipse.team.svn.ui.panel.local.JavaHLMergePanel;
import org.eclipse.team.svn.ui.panel.local.MergePanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;

/**
 * Merge action implementation
 * 
 * @author Alexander Gurov
 */
public class MergeAction extends AbstractNonRecursiveTeamAction {

	public MergeAction() {
		super();
	}

	public void run(IAction action) {
	    IResource []resources = this.getSelectedResources(IStateFilter.SF_EXCLUDE_DELETED);
		
		if (!OperationErrorDialog.isAcceptableAtOnce(resources, SVNTeamUIPlugin.instance().getResource("MergeAction.MergeError"), this.getShell())) {
			return;
		}
	    
	    if (!CoreExtensionsManager.instance().getSVNClientWrapperFactory().isInteractiveMergeAllowed() || 
	    	SVNTeamPreferences.getMergeBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.MERGE_USE_JAVAHL_NAME)) {
	    	this.performJavaHLMerge(resources);
	    }
	    else {
		    this.performInteractiveMerge(resources);
	    }
	}
	
	protected void performJavaHLMerge(IResource []resources) {
		IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(resources[0]);
	    ILocalResource localTo = SVNRemoteStorage.instance().asLocalResource(resources[0]);
	    if (localTo == null) {
	    	return;
	    }
		long revision = localTo.getRevision();
		
		if (resources.length > 1) {
			revision = Revision.SVN_INVALID_REVNUM;
			remote = remote.getRoot();
		}

		JavaHLMergePanel panel = new JavaHLMergePanel(resources, remote, revision);
	    AdvancedDialog dialog = new AdvancedDialog(this.getShell(), panel);
	    if (dialog.open() == 0) {
	    	JavaHLMergeOperation mainOp = new JavaHLMergeOperation(resources, panel.getFirstSelection(), panel.getSecondSelection(), false, panel.getIgnoreAncestry());
	    	CompositeOperation op = new CompositeOperation(mainOp.getId());
    		SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(resources);
    		op.add(saveOp);
	    	op.add(mainOp);
    		op.add(new RestoreProjectMetaOperation(saveOp));
	    	op.add(new RefreshResourcesOperation(new ResourcesParentsProvider(resources)));
	    	this.runScheduled(op);
	    }
	}
	
	protected void performInteractiveMerge(IResource []resources) {
		IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(resources[0]);
	    ILocalResource localTo = SVNRemoteStorage.instance().asLocalResource(resources[0]);
	    if (localTo == null) {
	    	return;
	    }
		long revision = localTo.getRevision();
		
		if (resources.length > 1) {
			revision = Revision.SVN_INVALID_REVNUM;
			remote = remote.getRoot();
		}
	    
		MergePanel panel = new MergePanel(remote, revision);
	    DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
	    if (dialog.open() == 0) {
	    	IRepositoryResource remoteResource = panel.getSelectedResource();
	    	remoteResource.setSelectedRevision(panel.getSecondSelectedRevision());
	    	
	    	final ShowMergeViewOperation mainOp = new ShowMergeViewOperation(resources, panel.getSelection(resources), this.getTargetPart(), panel.getStartRevision());
	    	CompositeOperation op = new CompositeOperation(mainOp.getId());
	    	if (panel.isUpdateFirstSelected()) {
	    		SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(resources);
	    		op.add(saveOp);
	    		final UpdateOperation updateOp = new UpdateOperation(resources, true);
	    		op.add(updateOp);
	    		op.add(new RestoreProjectMetaOperation(saveOp));
	    		op.add(new ClearUpdateStatusesOperation(updateOp));
	    		op.add(new RefreshResourcesOperation(new ResourcesParentsProvider(updateOp)));
	    		op.add(new AbstractActionOperation(mainOp.getId()) {
	    			public ISchedulingRule getSchedulingRule() {
	    				return mainOp.getSchedulingRule();
	    			}
	    			protected void runImpl(IProgressMonitor monitor) throws Exception {
	    				if (!updateOp.hasUnresolvedConflicts()) {
	    					mainOp.run(monitor);
	    					this.reportStatus(mainOp.getStatus());
	    				}
	    			}
	    		}, new AbstractActionOperation[] {updateOp});
	    		op.add(new NotifyUnresolvedConflictOperation(updateOp), new AbstractActionOperation[] {updateOp});
	    	}
	    	else {
		    	op.add(mainOp);
	    	}
		    this.runScheduled(op);
	    }
	}

	protected boolean isEnabled() {
		//FIXME enable multi-project for "Interactive Merge" when implemented
		return
			(SVNTeamPreferences.getMergeBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.MERGE_USE_JAVAHL_NAME) || this.getSelectedResources().length == 1) &&
			this.checkForResourcesPresence(IStateFilter.SF_EXCLUDE_DELETED);
	}

}
