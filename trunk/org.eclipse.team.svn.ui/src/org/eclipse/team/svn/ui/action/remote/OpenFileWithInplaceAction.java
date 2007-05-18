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

package org.eclipse.team.svn.ui.action.remote;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.operation.OpenRemoteFileOperation;

/**
 * The action will open the selected file with the system external in-place editor if available
 * 
 * @author Alexander Gurov
 */
public class OpenFileWithInplaceAction extends AbstractRepositoryTeamAction {
	public OpenFileWithInplaceAction() {
		super();
	}

	public void run(IAction action) {
		IRepositoryResource []resources = this.getSelectedRepositoryResources();
		this.runScheduled(new OpenRemoteFileOperation(new IRepositoryFile[] {(IRepositoryFile)resources[0]}, OpenRemoteFileOperation.OPEN_INPLACE));
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		if (this.isEnabled()) {
			IRepositoryResource []resources = this.getSelectedRepositoryResources();
			action.setImageDescriptor(SVNTeamUIPlugin.instance().getWorkbench().getEditorRegistry().getSystemExternalEditorImageDescriptor(resources[0].getName()));
		}
		else {
			action.setImageDescriptor(null);
		}
	}
	
	protected boolean isEnabled() {
		IRepositoryResource []resources = this.getSelectedRepositoryResources();
		if (resources.length != 1 || !(resources[0] instanceof IRepositoryFile)) {
			return false;
		}
		return SVNTeamUIPlugin.instance().getWorkbench().getEditorRegistry().isSystemInPlaceEditorAvailable(resources[0].getName());
	}

}
