/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.action.local;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.AddToSVNOperation;
import org.eclipse.team.svn.core.operation.local.FreezeExternalsOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RestoreExternalsOperation;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.RevertOperation;
import org.eclipse.team.svn.core.operation.local.SaveProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.SwitchOperation;
import org.eclipse.team.svn.core.operation.remote.PreparedBranchTagOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.action.AbstractNonRecursiveTeamAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.dialog.OperationErrorDialog;
import org.eclipse.team.svn.ui.panel.common.AbstractBranchTagPanel;
import org.eclipse.team.svn.ui.panel.common.BranchPanel;
import org.eclipse.team.svn.ui.panel.common.TagPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;

/**
 * Branch or tag action
 *
 * @author Sergiy Logvin
 */
public class BranchTagAction extends AbstractNonRecursiveTeamAction {	
	public static final int BRANCH_ACTION = 0;
	public static final int TAG_ACTION = 1;
	
	protected int actionType;
	
	public BranchTagAction(int actionType) {
		this.actionType = actionType;
	}

	public void runImpl(IAction action) {
		IResource []resources = this.getSelectedResources(IStateFilter.SF_EXCLUDE_DELETED);
		
		IActionOperation op = BranchTagAction.getBranchTagOperation(this.getShell(), this.actionType, resources);
		if (op != null) {
			this.runScheduled(op);
		}
	}
	
	public static IActionOperation getBranchTagOperation(Shell shell, int actionType, IResource []resources) {
		IRepositoryResource []remoteResources = new IRepositoryResource[resources.length];
		
		for (int i = 0; i < resources.length; i++) {
			remoteResources[i] = SVNRemoteStorage.instance().asRepositoryResource(resources[i]);
		}
		
		if (!OperationErrorDialog.isAcceptableAtOnce(remoteResources, actionType == BRANCH_ACTION ? SVNUIMessages.BranchTagAction_Error_Branch : SVNUIMessages.BranchTagAction_Error_Tag, shell)) {
			return null;
		}
		
		Set<String> nodeNames = new HashSet<String>();
		boolean isStructureEnabled = remoteResources[0].getRepositoryLocation().isStructureEnabled()&& SVNTeamPreferences.getRepositoryBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_NAME);
		if (isStructureEnabled) {
			nodeNames = org.eclipse.team.svn.ui.action.remote.BranchTagAction.getExistingNodeNames(actionType == BranchTagAction.BRANCH_ACTION ? SVNUtility.getBranchesLocation(remoteResources[0]) : SVNUtility.getTagsLocation(remoteResources[0]));
		}
		
		AbstractBranchTagPanel panel = 
			actionType == BranchTagAction.BRANCH_ACTION ? 
			new BranchPanel(SVNUtility.getBranchesLocation(remoteResources[0]), true, nodeNames, resources) : 
			new TagPanel(SVNUtility.getTagsLocation(remoteResources[0]), true, nodeNames, resources);
		DefaultDialog dialog = new DefaultDialog(shell, panel);
		
		if (dialog.open() == 0) {
			IRepositoryResource destination = panel.getDestination();
			
			boolean forceCreate =
				isStructureEnabled &&
				resources.length == 1 &&
				resources[0].getType() == IResource.PROJECT;
			
			if (forceCreate) {
				/*
				 * In order to determine that project has a single project layout,
				 * we check existence of .project file in repository for trunk folder.
				 * But there can be a situation that .project file doesn't exist in repository
				 * for trunk folder(probably this can be done intentionally)
				 * but in workspace we have a checked out trunk folder which is a project root.
				 * For more details, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=246268
				 */
				boolean isSingleProjectLayout = 				 
					remoteResources[0] instanceof IRepositoryRoot && ((IRepositoryRoot) (remoteResources[0])).getKind() == IRepositoryRoot.KIND_TRUNK ||
					org.eclipse.team.svn.ui.action.remote.BranchTagAction.isSingleProjectLayout(remoteResources[0]);
				forceCreate &= !isSingleProjectLayout;
			} 
								
			PreparedBranchTagOperation mainOp = new PreparedBranchTagOperation((actionType == BRANCH_ACTION ? "Branch" : "Tag"), resources, remoteResources, destination, panel.getMessage(), forceCreate);
			
			CompositeOperation op = new CompositeOperation(mainOp.getId());
			
			IResource []newResources = panel.getSelectedResources();
			if (newResources != null && newResources.length > 0) {
				op.add(new AddToSVNOperation(newResources, false));
			}
			
			if (panel.isFreezeExternals()) {
				FreezeExternalsOperation freezeOp = new FreezeExternalsOperation(resources);
				op.add(freezeOp);
				op.add(mainOp, new IActionOperation[] {freezeOp});
				op.add(new RestoreExternalsOperation(freezeOp));
			}
			else {
				op.add(mainOp);
			}
			
			if (newResources != null && newResources.length > 0) {
				op.add(new RevertOperation(newResources, false));
			}
			
			if (panel.isStartWithSelected()) {
				SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(resources);
				op.add(saveOp);
			    op.add(new SwitchOperation(resources, mainOp, Depth.INFINITY), new IActionOperation[] {mainOp});
				op.add(new RestoreProjectMetaOperation(saveOp));
				op.add(new RefreshResourcesOperation(resources));
			}
			
			return op;
		}
		return null;
	}
	
	public boolean isEnabled() {
		return this.checkForResourcesPresence(IStateFilter.SF_EXCLUDE_DELETED);
	}

	protected boolean needsToSaveDirtyEditors() {
		return true;
	}
	
}
