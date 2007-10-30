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

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RemoveNonVersionedResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.RevertOperation;
import org.eclipse.team.svn.core.operation.local.SaveProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.UpdateOperation;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractNonRecursiveTeamAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.dialog.ReplaceWarningDialog;
import org.eclipse.team.svn.ui.panel.common.InputRevisionPanel;

/**
 * Team services menu "replace with revision" action implementation
 *
 * @author Sergiy Logvin
 */
public class ReplaceWithRevisionAction extends AbstractNonRecursiveTeamAction {

	public ReplaceWithRevisionAction() {
		super();
	}
	
	public void runImpl(IAction action) {
	    IRemoteStorage storage = SVNRemoteStorage.instance();
		IResource resource = this.getSelectedResources(IStateFilter.SF_ONREPOSITORY)[0];
		IRepositoryResource remote = storage.asRepositoryResource(resource);
		
		InputRevisionPanel panel = new InputRevisionPanel(remote, SVNTeamUIPlugin.instance().getResource("ReplaceWithRevisionAction.InputRevisionPanel.Title"));
		DefaultDialog selectionDialog = new DefaultDialog(this.getShell(), panel);
		
		if (selectionDialog.open() == Dialog.OK) {
			ReplaceWarningDialog dialog = new ReplaceWarningDialog(this.getShell());
			if (dialog.open() == 0) {
				IResource []resources = this.getSelectedResources(IStateFilter.SF_ONREPOSITORY);
				CompositeOperation op = new CompositeOperation("Operation.ReplaceWithRevision");
				SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(resources);
				op.add(saveOp);
				IActionOperation revertOp = new RevertOperation(resources, true);
				op.add(revertOp);
				IActionOperation removeOp = new RemoveNonVersionedResourcesOperation(resources, true);
				op.add(removeOp, new IActionOperation[] {revertOp});
				op.add(new UpdateOperation(resources, panel.getSelectedRevision(), true), new IActionOperation[] {revertOp, removeOp});
				op.add(new RestoreProjectMetaOperation(saveOp));
				op.add(new RefreshResourcesOperation(resources));
				this.runScheduled(op);
			}
		}
	}

	public boolean isEnabled() {
		return 
			this.getSelectedResources().length == 1 && 
			this.checkForResourcesPresence(IStateFilter.SF_ONREPOSITORY);
	}

}