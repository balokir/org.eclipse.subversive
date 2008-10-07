/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Alessandro Nistico - [patch] Change Set's implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize.update.action;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.dialog.TagModifyWarningDialog;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.extension.factory.ICommitDialog;
import org.eclipse.team.svn.ui.panel.local.CommitPanel;
import org.eclipse.team.svn.ui.synchronize.SVNChangeSetCapability;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeModelAction;
import org.eclipse.team.svn.ui.synchronize.update.UpdateSyncInfo;
import org.eclipse.team.svn.ui.utility.CommitActionUtility;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Synchronize view commit action implementation
 * 
 * @author Alexander Gurov
 */
public class CommitAction extends AbstractSynchronizeModelAction {
	public CommitAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	public CommitAction(String text, ISynchronizePageConfiguration configuration, ISelectionProvider selectionProvider) {
		super(text, configuration, selectionProvider);
	}

	protected FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter.SyncInfoDirectionFilter(new int[] {SyncInfo.OUTGOING}) {
            public boolean select(SyncInfo info) {
                UpdateSyncInfo sync = (UpdateSyncInfo)info;
                return super.select(info) && !IStateFilter.SF_OBSTRUCTED.accept(sync.getLocalResource());
            }		    
		};
	}

	protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		CommitActionUtility commitUtility = new CommitActionUtility(this.syncInfoSelector);
		
		IResource[] resources = commitUtility.getAllResources();
		if (SVNUtility.isTagOperated(resources)) {
			TagModifyWarningDialog dlg = new TagModifyWarningDialog(configuration.getSite().getShell());
        	if (dlg.open() != 0) {
        		return null;
        	}
		}
		
	    String proposedComment = SVNChangeSetCapability.getProposedComment(resources);                
	    CommitPanel commitPanel = new CommitPanel(resources, resources, CommitPanel.MSG_COMMIT, proposedComment); 
        ICommitDialog dialog = ExtensionsManager.getInstance().getCurrentCommitFactory().getCommitDialog(configuration.getSite().getShell(), commitUtility.getAllResourcesSet(), commitPanel);				
		if (dialog.open() != 0) {
			return null;
		}
		
		return commitUtility.getCompositeCommitOperation(commitPanel.getSelectedResources(), dialog.getMessage(), commitPanel.getKeepLocks(), configuration.getSite().getShell(), configuration.getSite().getPart());
	}
	
}
