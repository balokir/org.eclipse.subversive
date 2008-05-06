/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.action.remote;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.operation.OpenRemoteFileOperation;
import org.eclipse.team.svn.ui.repository.model.RepositoryFile;

/**
 * Open remote file action implementation
 * 
 * @author Alexander Gurov
 */
public class OpenFileAction extends AbstractRepositoryTeamAction {
	public OpenFileAction() {
		super();
	}

	public void runImpl(IAction action) {
		RepositoryFile []resources = (RepositoryFile [])this.getAdaptedSelection(RepositoryFile.class);
	    IRepositoryFile []files = new IRepositoryFile[resources.length];
	    for (int i = 0; i < resources.length; i++) {
	    	files[i] = (IRepositoryFile)resources[i].getRepositoryResource();
	    }
		this.runScheduled(new OpenRemoteFileOperation(files, OpenRemoteFileOperation.OPEN_DEFAULT));
	}

	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		if (this.isEnabled()) {
			IRepositoryResource []resources = this.getSelectedRepositoryResources();
			action.setImageDescriptor(SVNTeamUIPlugin.instance().getWorkbench().getEditorRegistry().getImageDescriptor(resources[0].getName()));
		}
		else {
			action.setImageDescriptor(null);
		}
	}
	
	public boolean isEnabled() {
		return this.getAdaptedSelection(RepositoryFile.class).length > 0;
	}

}
