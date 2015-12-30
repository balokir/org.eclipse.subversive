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

package org.eclipse.team.svn.ui.action.local;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.action.AbstractRecursiveTeamAction;
import org.eclipse.team.svn.ui.dialog.TagModifyWarningDialog;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.extension.factory.ICommitDialog;
import org.eclipse.team.svn.ui.panel.local.CommitPanel;
import org.eclipse.team.svn.ui.synchronize.SVNChangeSetCapability;
import org.eclipse.team.svn.ui.utility.CommitActionUtility;

/**
 * Team services commit action implementation
 * 
 * @author Alexander Gurov
 */
public class CommitAction extends AbstractRecursiveTeamAction { 
	public CommitAction() {
		super();
	}

	public void runImpl(IAction action) {
		CommitActionUtility commitUtility = new CommitActionUtility(this);
        IResource [] allResources = commitUtility.getAllResources();
        
        IProject[] tagOperatedProjects = SVNUtility.getTagOperatedProjects(allResources);
        if (tagOperatedProjects.length != 0) {
        	TagModifyWarningDialog dlg = new TagModifyWarningDialog(this.getShell(), tagOperatedProjects);
        	if (dlg.open() != 0) {
        		return;
        	}
        }
	    String proposedComment = SVNChangeSetCapability.getProposedComment(commitUtility.getAllResources());
        CommitPanel commitPanel = new CommitPanel(allResources, this.getSelectedResources(), CommitPanel.MSG_COMMIT, proposedComment);
        ICommitDialog commitDialog = ExtensionsManager.getInstance().getCurrentCommitFactory().getCommitDialog(this.getShell(), commitUtility.getAllResourcesSet(), commitPanel);
        if (commitDialog.open() == 0) {
			if (commitPanel.getResourcesChanged()) {
				commitUtility.initialize(this);
			}
			CompositeOperation op = commitUtility.getCompositeCommitOperation(commitPanel.getSelectedResources(), commitPanel.getNotSelectedResources(), commitPanel.getTreatAsEdits(), commitDialog.getMessage(), commitPanel.getKeepLocks(), this.getShell(), this.getTargetPart(), true);			
			this.runScheduled(op);
		}
	}
	
	public boolean isEnabled() {
		return this.checkForResourcesPresenceRecursive(CommitAction.SF_ANY_CHANGE);
	}
	
	protected boolean needsToSaveDirtyEditors() {
		return true;
	}
	
	public static final IStateFilter SF_ANY_CHANGE = new IStateFilter.AbstractStateFilter() {
		protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
			return IStateFilter.SF_ANY_CHANGE.accept(resource, state, mask) && state != IStateFilter.ST_CONFLICTING;
		}
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return true;
		}
	};

}
