/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.mylyn;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.IResourceSelector;
import org.eclipse.team.svn.ui.action.local.CommitAction;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.extension.factory.ICommitDialog;
import org.eclipse.team.svn.ui.panel.local.CommitPanel;
import org.eclipse.team.svn.ui.synchronize.SVNChangeSetCapability;
import org.eclipse.team.svn.ui.utility.CommitActionUtility;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Commit integration for the Mylar project.
 * 
 * @author Alexander Gurov
 */
public class SVNCommitWorkflowProvider /*extends AbstractCommitWorkflowProvider*/ {
	//NOTE for future use only
	public boolean hasOutgoingChanges(IResource[] resources) {
		return FileUtility.checkForResourcesPresence(resources, CommitAction.SF_ANY_CHANGE, IResource.DEPTH_ZERO);
	}
	
	public void commit(final IResource[] resources) {
		CommitActionUtility commitUtility = new CommitActionUtility(new IResourceSelector() {
			public IResource []getSelectedResources() {
				return resources;
			}
			public IResource []getSelectedResourcesRecursive(IStateFilter filter) {
				return this.getSelectedResources(filter);
			}
			public IResource []getSelectedResourcesRecursive(IStateFilter filter, int depth) {
				return this.getSelectedResources(filter);
			}
			public IResource []getSelectedResources(IStateFilter filter) {
				return FileUtility.getResourcesRecursive(this.getSelectedResources(), filter, IResource.DEPTH_ZERO);
			}
		});	
	    String proposedComment = SVNChangeSetCapability.getProposedComment(commitUtility.getAllResources());
        CommitPanel commitPanel = new CommitPanel(commitUtility.getAllResources(), CommitPanel.MSG_COMMIT, proposedComment);
        Shell shell = UIMonitorUtility.getShell();
        ICommitDialog commitDialog = ExtensionsManager.getInstance().getCurrentCommitFactory().getCommitDialog(shell, commitUtility.getAllResourcesSet(), commitPanel);
		
		if (commitDialog.open() == 0) {
			IResource []selectedResources = commitPanel.getSelectedResources();
			IWorkbenchWindow window = SVNTeamUIPlugin.instance().getWorkbench().getActiveWorkbenchWindow();
			IWorkbenchPart part = window == null ? null : window.getPartService().getActivePart();
			CompositeOperation op = commitUtility.getCompositeCommitOperation(selectedResources, commitDialog.getMessage(), commitPanel.getKeepLocks(), shell, part);
			UIMonitorUtility.doTaskScheduledActive(op);
		}
	}
	
}
