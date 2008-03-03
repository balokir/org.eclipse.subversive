/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
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
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.dialog.OperationErrorDialog;
import org.eclipse.team.svn.ui.panel.common.AbstractBranchTagPanel;
import org.eclipse.team.svn.ui.panel.common.BranchPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Create branch action for synchronize view
 * 
 * @author Alexei Goncharov
 */
public class CreateBranchAction extends AbstractSynchronizeModelAction {
	public CreateBranchAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		for (Iterator it = selection.iterator(); it.hasNext(); ) {
			ISynchronizeModelElement element = (ISynchronizeModelElement)it.next();
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(element.getResource());
			// null for change set nodes
			if (local != null && IStateFilter.SF_EXCLUDE_DELETED.accept(local)) {
				return true;
			}
		}
	    return false;
	}
	
	protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		IResource []resources = FileUtility.getResourcesRecursive(this.treeNodeSelector.getSelectedResources(), IStateFilter.SF_EXCLUDE_DELETED, IResource.DEPTH_ZERO);
		ArrayList localOperateResources = new ArrayList();
		ArrayList remoteOperateResources = new ArrayList();
		ArrayList operateResources = new ArrayList();
		IRepositoryLocation first = SVNRemoteStorage.instance().asRepositoryResource(resources[0]).getRepositoryLocation();
		for (int i = 0; i < resources.length; i++) {
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resources[i]);
			IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(resources[i]);
			if (local == null || remote == null) {
				continue;
			}
			
			// prevent branch or tag operation for the resources from the different repositories
			if (remote.getRepositoryLocation() != first) {
				new OperationErrorDialog(configuration.getSite().getShell(), SVNTeamUIPlugin.instance().getResource("BranchTagAction.Error.Branch"), OperationErrorDialog.ERR_DIFFREPOSITORIES).open();
				return null;
			}
			operateResources.add(resources[i]);
			localOperateResources.add(local);
			remoteOperateResources.add(remote);
		}
		if (remoteOperateResources.size() > 0) {
			IRepositoryResource []remoteResources = (IRepositoryResource[])remoteOperateResources.toArray(new IRepositoryResource[remoteOperateResources.size()]);
			
			if (!OperationErrorDialog.isAcceptableAtOnce(remoteResources, SVNTeamUIPlugin.instance().getResource("BranchTagAction.Error.Branch"), configuration.getSite().getShell())) {
				return null;
			}
			
			AbstractBranchTagPanel panel = null;
			Set nodeNames = new HashSet();
			boolean respectProjectStructure = SVNTeamPreferences.getRepositoryBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_NAME);
			if (respectProjectStructure && remoteResources[0].getRepositoryLocation().isStructureEnabled()) {
				nodeNames = org.eclipse.team.svn.ui.action.remote.BranchTagAction.getExistingNodeNames(SVNUtility.getBranchesLocation(remoteResources[0]));
			}
			panel = new BranchPanel(SVNUtility.getBranchesLocation(remoteResources[0]), true, nodeNames, resources);
			DefaultDialog dialog = new DefaultDialog(configuration.getSite().getShell(), panel);
			
			if (dialog.open() == 0) {
				IRepositoryResource destination = panel.getDestination();
				IResource []operateResourcesArr = (IResource[])operateResources.toArray(new IResource[operateResources.size()]);
				IResource []newResources = panel.getSelectedResources();
				
				boolean multipleLayout = this.isMultipleLayout(resources, remoteOperateResources);
				PreparedBranchTagOperation mainOp = new PreparedBranchTagOperation("Branch", operateResourcesArr, remoteResources, destination, panel.getMessage(), multipleLayout);
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
					for (Iterator iter = localOperateResources.iterator(); iter.hasNext(); i++) {
						switchedResources[i] = ((ILocalResource)iter.next()).getResource();
					}
					SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(switchedResources);
					op.add(saveOp);
				    op.add(new SwitchOperation(switchedResources, mainOp), new IActionOperation[] {mainOp});
					op.add(new RestoreProjectMetaOperation(saveOp));
					op.add(new RefreshResourcesOperation(operateResourcesArr));
				}
				return op;
			}
		}
		return null;
	}

	protected boolean isMultipleLayout(IResource []resources, List remoteOperateResources) {
		boolean multipleLayout = false;
		if (resources.length == 1 && resources[0] instanceof IProject) {
			IRepositoryResource remote = (IRepositoryResource)remoteOperateResources.get(0);
			return !(remote instanceof IRepositoryRoot) || ((IRepositoryRoot)remote).getKind() != IRepositoryRoot.KIND_TRUNK;
		}
		return multipleLayout;
	}
}
