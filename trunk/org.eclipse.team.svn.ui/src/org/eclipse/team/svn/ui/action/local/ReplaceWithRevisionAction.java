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
import org.eclipse.team.svn.core.operation.local.ClearLocalStatusesOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.SaveProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.SwitchOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.action.AbstractNonRecursiveTeamAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.dialog.ReplaceWarningDialog;
import org.eclipse.team.svn.ui.panel.local.ReplaceWithUrlPanel;

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
		ILocalResource local = storage.asLocalResource(resource);
		
		ReplaceWithUrlPanel panel = new ReplaceWithUrlPanel(remote, local.getRevision());
		DefaultDialog selectionDialog = new DefaultDialog(this.getShell(), panel);
		
		if (selectionDialog.open() == Dialog.OK) {
			ReplaceWarningDialog dialog = new ReplaceWarningDialog(this.getShell());
			if (dialog.open() == 0) {
				IResource [] wcResources = new IResource[] {resource};
				SwitchOperation mainOp = new SwitchOperation(wcResources, new IRepositoryResource [] {panel.getSelectedResource()});
				CompositeOperation op = new CompositeOperation(mainOp.getId());
				SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(wcResources);
				op.add(saveOp);
				op.add(mainOp);
				op.add(new RestoreProjectMetaOperation(saveOp));
				op.add(new ClearLocalStatusesOperation(wcResources));
				op.add(new RefreshResourcesOperation(wcResources));
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