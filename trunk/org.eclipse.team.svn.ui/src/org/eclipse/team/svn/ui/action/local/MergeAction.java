/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
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
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.JavaHLMergeOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.SaveProjectMetaOperation;
import org.eclipse.team.svn.core.operation.remote.LocateResourceURLInHistoryOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.action.AbstractNonRecursiveTeamAction;
import org.eclipse.team.svn.ui.dialog.AdvancedDialog;
import org.eclipse.team.svn.ui.dialog.OperationErrorDialog;
import org.eclipse.team.svn.ui.operation.ShowMergeViewOperation;
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

	public void runImpl(IAction action) {
	    IResource []resources = this.getSelectedResources(IStateFilter.SF_EXCLUDE_DELETED);
		
		if (!OperationErrorDialog.isAcceptableAtOnce(resources, SVNUIMessages.MergeAction_MergeError, this.getShell())) {
			return;
		}
	    
		IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(resources[0]);
		long revision = SVNRemoteStorage.instance().asLocalResourceAccessible(resources[0]).getRevision();
		
		if (resources.length > 1) {
			revision = SVNRevision.INVALID_REVISION_NUMBER;
			remote = remote.getRoot();
		}

		MergePanel panel = new MergePanel(resources, remote, revision);
	    AdvancedDialog dialog = new AdvancedDialog(this.getShell(), panel);
	    if (dialog.open() == 0) {
			// 2URL mode requires peg as revision
			LocateResourceURLInHistoryOperation locateFirst = new LocateResourceURLInHistoryOperation(panel.getFirstSelection());
			LocateResourceURLInHistoryOperation locateSecond = null;
			IRepositoryResourceProvider firstSet = locateFirst;
			IRepositoryResourceProvider secondSet = null;
			if (panel.getMode() == MergePanel.MODE_1URL) {
				firstSet = new IRepositoryResourceProvider.DefaultRepositoryResourceProvider(panel.getFirstSelection());
			}
			else if (panel.getMode() == MergePanel.MODE_2URL) {
				secondSet = locateSecond = new LocateResourceURLInHistoryOperation(panel.getSecondSelection());
			}
			
			IActionOperation mergeOp = null;
	    	if (SVNTeamPreferences.getMergeBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.MERGE_USE_JAVAHL_NAME) /*|| panel.getMode() == MergePanel.MODE_REINTEGRATE*/) {
		    	JavaHLMergeOperation mainOp = null;
				if (panel.getMode() == MergePanel.MODE_2URL) {
					mainOp = new JavaHLMergeOperation(resources, firstSet, secondSet, false, panel.getIgnoreAncestry(), panel.getDepth()); 
				}
				else if (panel.getMode() == MergePanel.MODE_1URL) {
					mainOp = new JavaHLMergeOperation(resources, firstSet, panel.getSelectedRevisions(), false, panel.getIgnoreAncestry(), panel.getDepth());
				}
				else {
					mainOp = new JavaHLMergeOperation(resources, firstSet, false);
				}
				mainOp.setRecordOnly(panel.getRecordOnly());
		    	CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
	    		SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(resources);
	    		op.add(saveOp);
		    	op.add(mainOp);
	    		op.add(new RestoreProjectMetaOperation(saveOp));
		    	op.add(new RefreshResourcesOperation(resources));
		    	mergeOp = op;		    	
	    	}
	    	else if (panel.getMode() == MergePanel.MODE_2URL) {
				mergeOp = new ShowMergeViewOperation(resources, firstSet, secondSet, panel.getIgnoreAncestry(), panel.getDepth(), this.getTargetPart());
				((ShowMergeViewOperation)mergeOp).setRecordOnly(panel.getRecordOnly());
			}
			else if (panel.getMode() == MergePanel.MODE_1URL) {
				mergeOp = new ShowMergeViewOperation(resources, firstSet, panel.getSelectedRevisions(), panel.getIgnoreAncestry(), panel.getDepth(), this.getTargetPart());
				((ShowMergeViewOperation)mergeOp).setRecordOnly(panel.getRecordOnly());
			}
			else {
				mergeOp = new ShowMergeViewOperation(resources, firstSet, this.getTargetPart());
			}
	    	if (panel.getMode() != MergePanel.MODE_1URL) {
	    		CompositeOperation op = new CompositeOperation(mergeOp.getId(), mergeOp.getMessagesClass());
	    		op.add(locateFirst);
		    	if (panel.getMode() == MergePanel.MODE_2URL) {
		    		op.add(locateSecond);
		    	}
	    		op.add(mergeOp);
		    	this.runScheduled(op);
	    	}
	    	else {
		    	this.runScheduled(mergeOp);
	    	}
	    }
	}

	public boolean isEnabled() {
		return this.checkForResourcesPresence(IStateFilter.SF_EXCLUDE_DELETED);
	}

	protected boolean needsToSaveDirtyEditors() {
		return true;
	}
	
}
