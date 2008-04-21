/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.action.local;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.IStateFilter;
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
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
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
		
		ArrayList<ILocalResource> localOperateResources = new ArrayList<ILocalResource>();
		ArrayList<IRepositoryResource> remoteOperateResources = new ArrayList<IRepositoryResource>();
		ArrayList<IResource> operateResources = new ArrayList<IResource>();
		IRepositoryLocation first = SVNRemoteStorage.instance().asRepositoryResource(resources[0]).getRepositoryLocation(); 
		
		for (int i = 0; i < resources.length; i++) {
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resources[i]);
			IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(resources[i]);
			if (local == null || remote == null) {
				continue;
			}
			
			// prevent branch or tag operation for the resources from the different repositories
			if (remote.getRepositoryLocation() != first) {
				new OperationErrorDialog(this.getShell(), SVNTeamUIPlugin.instance().getResource(this.actionType == BRANCH_ACTION ? "BranchTagAction.Error.Branch" : "BranchTagAction.Error.Tag"), OperationErrorDialog.ERR_DIFFREPOSITORIES).open();
				return;
			}
			operateResources.add(resources[i]);
			localOperateResources.add(local);
			remoteOperateResources.add(remote);
		}
		
		//process problems preventing successful branch/tag operation execution
		if (remoteOperateResources.size() > 0) {
			IRepositoryResource []remoteResources = remoteOperateResources.toArray(new IRepositoryResource[remoteOperateResources.size()]);
			
			if (!OperationErrorDialog.isAcceptableAtOnce(remoteResources, SVNTeamUIPlugin.instance().getResource(this.actionType == BRANCH_ACTION ? "BranchTagAction.Error.Branch" : "BranchTagAction.Error.Tag"), this.getShell())) {
				return;
			}
			
			AbstractBranchTagPanel panel = null;
			Set<String> nodeNames = new HashSet<String>();
			boolean respectProjectStructure = SVNTeamPreferences.getRepositoryBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_NAME);
			if (respectProjectStructure && remoteResources[0].getRepositoryLocation().isStructureEnabled()) {
				nodeNames = org.eclipse.team.svn.ui.action.remote.BranchTagAction.getExistingNodeNames(this.actionType == BranchTagAction.BRANCH_ACTION ? SVNUtility.getBranchesLocation(remoteResources[0]) : SVNUtility.getTagsLocation(remoteResources[0]));
			}
			if (this.actionType == BranchTagAction.BRANCH_ACTION) {
				panel = new BranchPanel(SVNUtility.getBranchesLocation(remoteResources[0]), true, nodeNames, resources);
			}
			else {
				panel = new TagPanel(SVNUtility.getTagsLocation(remoteResources[0]), true, nodeNames, resources);
			}
			DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
			
			if (dialog.open() == 0) {
				IRepositoryResource destination = panel.getDestination();
				IResource []operateResourcesArr = operateResources.toArray(new IResource[operateResources.size()]);
				IResource []newResources = panel.getSelectedResources();
				
				boolean multipleLayout = this.isMultipleLayout(resources, remoteOperateResources);
				PreparedBranchTagOperation mainOp = new PreparedBranchTagOperation((this.actionType == BRANCH_ACTION ? "Branch" : "Tag"), operateResourcesArr, remoteResources, destination, panel.getMessage(), multipleLayout);
				CompositeOperation op = new CompositeOperation(mainOp.getId());
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
					IResource []switchedResources = new IResource[localOperateResources.size()];
					int i = 0;
					for (Iterator<ILocalResource> iter = localOperateResources.iterator(); iter.hasNext(); i++) {
						switchedResources[i] = iter.next().getResource();
					}
					SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(switchedResources);
					op.add(saveOp);
				    op.add(new SwitchOperation(switchedResources, mainOp), new IActionOperation[] {mainOp});
					op.add(new RestoreProjectMetaOperation(saveOp));
					op.add(new RefreshResourcesOperation(operateResourcesArr));
				}
				this.runScheduled(op);
			}
		} 
	}
	
	protected boolean isMultipleLayout(IResource []resources, List<IRepositoryResource> remoteOperateResources) {
		boolean multipleLayout = false;
		if (resources.length == 1 && resources[0] instanceof IProject) {
			IRepositoryResource remote = remoteOperateResources.get(0);
			return !(remote instanceof IRepositoryRoot) || ((IRepositoryRoot)remote).getKind() != IRepositoryRoot.KIND_TRUNK;
		}
		return multipleLayout;
	}
	
	public boolean isEnabled() {
		return this.checkForResourcesPresence(IStateFilter.SF_EXCLUDE_DELETED);
	}

	protected boolean needsToSaveDirtyEditors() {
		return true;
	}
	
}
