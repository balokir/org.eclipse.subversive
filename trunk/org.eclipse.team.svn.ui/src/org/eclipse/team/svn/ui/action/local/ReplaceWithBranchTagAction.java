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

package org.eclipse.team.svn.ui.action.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.ClearLocalStatusesOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RemoveNonVersionedResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.RevertOperation;
import org.eclipse.team.svn.core.operation.local.SaveProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.SwitchOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.action.AbstractWorkingCopyAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.dialog.ReplaceWarningDialog;
import org.eclipse.team.svn.ui.panel.local.ReplaceBranchTagPanel;

/**
 * Replace with branch/tag action implementation
 * 
 * @author Alexei Goncharov
 */
public class ReplaceWithBranchTagAction extends AbstractWorkingCopyAction {

	protected int type;
	
	public ReplaceWithBranchTagAction(int type) {
		super();
		this.type = type;
	}
	
	public boolean isEnabled() {
		return 
		this.getSelectedResources().length == 1 && 
		this.checkForResourcesPresence(IStateFilter.SF_ONREPOSITORY);
	}

	public void runImpl(IAction action) {
		IResource resource = this.getSelectedResources()[0];
		ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
		IRepositoryResource remote = local.isCopied() ? SVNUtility.getCopiedFrom(resource) : SVNRemoteStorage.instance().asRepositoryResource(resource);
		ReplaceBranchTagPanel panel = new ReplaceBranchTagPanel(remote, local.getRevision(), this.type, true);
		DefaultDialog dlg = new DefaultDialog(this.getShell(), panel);
		if (dlg.open() == 0){
			ReplaceWarningDialog dialog = new ReplaceWarningDialog(this.getShell());
			if (dialog.open() == 0) {
				IResource [] wcResources = new IResource[] {resource};
				SwitchOperation mainOp = new SwitchOperation(wcResources, new IRepositoryResource [] {panel.getSelectedResoure()});
				CompositeOperation op = new CompositeOperation(mainOp.getId());
				SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(wcResources);
				op.add(saveOp);
				IActionOperation revertOp = new RevertOperation(wcResources, true);
				op.add(revertOp);
				IActionOperation removeOp = new RemoveNonVersionedResourcesOperation(wcResources, true);
				op.add(removeOp, new IActionOperation[] {revertOp});
				op.add(mainOp);
				op.add(new RestoreProjectMetaOperation(saveOp));
				op.add(new ClearLocalStatusesOperation(wcResources));
				op.add(new RefreshResourcesOperation(wcResources));
				this.runScheduled(op);
			}
		}
	}

}
