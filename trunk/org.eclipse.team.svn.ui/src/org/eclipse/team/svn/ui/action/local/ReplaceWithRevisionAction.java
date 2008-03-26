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

import java.util.HashMap;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.GetRemoteContentsOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.SaveProjectMetaOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
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
		IResource []resources = this.getSelectedResources(IStateFilter.SF_ONREPOSITORY);
		IActionOperation op = ReplaceWithRevisionAction.getReplaceOperation(resources, this.getShell());
		if (op != null) {
			this.runScheduled(op);
		}
	}

	public boolean isEnabled() {
		return this.getSelectedResources().length == 1 && this.checkForResourcesPresence(IStateFilter.SF_ONREPOSITORY);
	}

	public static IActionOperation getReplaceOperation(IResource []resources, Shell shell) {
		ReplaceWarningDialog dialog = new ReplaceWarningDialog(shell);
		if (dialog.open() == 0) {
			IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(resources[0]);
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resources[0]);
			
			ReplaceWithUrlPanel panel = new ReplaceWithUrlPanel(remote, local.getRevision());
			DefaultDialog selectionDialog = new DefaultDialog(shell, panel);
			
			if (selectionDialog.open() == Dialog.OK) {
				IRepositoryResource selected = panel.getSelectedResource();
				HashMap<String, String> remote2local = new HashMap<String, String>();
				remote2local.put(SVNUtility.encodeURL(selected.getUrl()), FileUtility.getWorkingCopyPath(resources[0]));
				CompositeOperation op = new CompositeOperation("Operation.ReplaceWithRevision");
				SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(resources);
				op.add(saveOp);
				op.add(new GetRemoteContentsOperation(resources, new IRepositoryResource [] {selected}, remote2local));
				op.add(new RestoreProjectMetaOperation(saveOp));
				op.add(new RefreshResourcesOperation(resources));
				return op;
			}
		}
		return null;
	}
	
}
