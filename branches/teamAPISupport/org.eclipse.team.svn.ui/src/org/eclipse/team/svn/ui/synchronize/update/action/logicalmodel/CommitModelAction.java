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

package org.eclipse.team.svn.ui.synchronize.update.action.logicalmodel;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.synchronize.UpdateSyncInfo;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.dialog.TagModifyWarningDialog;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.extension.factory.ICommitDialog;
import org.eclipse.team.svn.ui.mapping.ModelHelper;
import org.eclipse.team.svn.ui.mapping.SVNModelParticipantChangeSetCapability;
import org.eclipse.team.svn.ui.panel.local.CommitPanel;
import org.eclipse.team.svn.ui.synchronize.SVNChangeSetCapability;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeLogicalModelAction;
import org.eclipse.team.svn.ui.utility.CommitActionUtility;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Synchronize view commit action logical model implementation
 * 
 * @author Igor Burilo
 */
public class CommitModelAction extends AbstractSynchronizeLogicalModelAction {

	public CommitModelAction(String text, ISynchronizePageConfiguration configuration) {		
		super(text, configuration);
	}
	
	protected FastSyncInfoFilter getFastSyncInfoFilter() {
		return new FastSyncInfoFilter.SyncInfoDirectionFilter(new int[] {SyncInfo.OUTGOING}) {
            public boolean select(SyncInfo info) {
                UpdateSyncInfo sync = (UpdateSyncInfo)info;
                return super.select(info) && !IStateFilter.SF_OBSTRUCTED.accept(sync.getLocalResource());
            }		    
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeLogicalModelAction#getOperation()
	 */	
	protected IActionOperation getOperation() {
		CommitActionUtility commitUtility = new CommitActionUtility(this.syncInfoSelector);
		IResource[] resources = commitUtility.getAllResources();
		if (SVNUtility.isTagOperated(resources)) {
			TagModifyWarningDialog dlg = new TagModifyWarningDialog(this.getConfiguration().getSite().getShell());
        	if (dlg.open() != 0) {
        		return null;
        	}
		}
		String proposedComment = ModelHelper.isShowModelSync() ? SVNModelParticipantChangeSetCapability.getProposedComment(resources) : SVNChangeSetCapability.getProposedComment(resources);                
	    CommitPanel commitPanel = new CommitPanel(resources, resources, CommitPanel.MSG_COMMIT, proposedComment); 
        ICommitDialog dialog = ExtensionsManager.getInstance().getCurrentCommitFactory().getCommitDialog(this.getConfiguration().getSite().getShell(), commitUtility.getAllResourcesSet(), commitPanel);				
		if (dialog.open() != 0) {
			return null;
		}
		
		return commitUtility.getCompositeCommitOperation(commitPanel.getSelectedResources(), dialog.getMessage(), commitPanel.getKeepLocks(), this.getConfiguration().getSite().getShell(), this.getConfiguration().getSite().getPart());
	}
		
}
