/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Alessandro Nistico - Change Set's implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize.update.action;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.extension.factory.ICommitDialog;
import org.eclipse.team.svn.ui.panel.local.CommitPanel;
import org.eclipse.team.svn.ui.synchronize.SVNChangeSetCapability;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeModelAction;
import org.eclipse.team.svn.ui.synchronize.update.UpdateSyncInfo;
import org.eclipse.team.svn.ui.utility.CommitActionUtility;

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
                if (super.select(info)) {
                    UpdateSyncInfo sync = (UpdateSyncInfo)info;
                    return !IStateFilter.SF_OBSTRUCTED.accept(sync.getLocal(), sync.getLocalResource().getStatus(), sync.getLocalResource().getChangeMask());
                }
                return false;
            }		    
		};
	}

	protected IActionOperation execute(final FilteredSynchronizeModelOperation operation) {
		final CommitActionUtility commitUtility = new CommitActionUtility(operation);
		final String []msg = new String[1];
		final boolean []keepLocks = new boolean[1];
		final IResource [][]selectedResources = new IResource[1][];
		operation.getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
			    String proposedComment = SVNChangeSetCapability.getProposedComment(commitUtility.getAllResources());
                IResource[] resources = commitUtility.getAllResources();
			    CommitPanel commitPanel = new CommitPanel(resources, resources, CommitPanel.MSG_COMMIT, proposedComment); 
                ICommitDialog dialog = ExtensionsManager.getInstance().getCurrentCommitFactory().getCommitDialog(operation.getShell(), commitUtility.getAllResourcesSet(), commitPanel);				
				if (dialog.open() != 0) {
					msg[0] = null;
					keepLocks[0] = false;
					selectedResources[0] = null;
				}
				else {
					msg[0] = dialog.getMessage();
					keepLocks[0] = commitPanel.getKeepLocks();
					selectedResources[0] = commitPanel.getSelectedResources();
				}
			}
			
		});
		
		if (msg[0] == null) {
		    return null;
		}
		
		CompositeOperation op = commitUtility.getCompositeCommitOperation(selectedResources[0], msg[0], keepLocks[0], operation.getShell(), operation.getPart());
		
		return op;
	}
	
}
