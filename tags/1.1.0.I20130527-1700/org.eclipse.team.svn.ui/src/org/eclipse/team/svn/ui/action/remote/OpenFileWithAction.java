/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
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
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.operation.OpenRemoteFileOperation;
import org.eclipse.team.svn.ui.repository.model.RepositoryFile;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.editors.text.EditorsUI;

/**
 * The action will open the selected file with the system external editor if available
 * 
 * @author Alexander Gurov
 */
public class OpenFileWithAction extends AbstractRepositoryTeamAction {
	protected String editorId;
	protected boolean allowsMultiple;
	
	public OpenFileWithAction() {
		this(EditorsUI.DEFAULT_TEXT_EDITOR_ID, true);
	}

	public OpenFileWithAction(String editorId, boolean allowsMultiple) {
		super();
		this.editorId = editorId;
		this.allowsMultiple = allowsMultiple;
	}

	public void runImpl(IAction action) {
		RepositoryFile []resources = (RepositoryFile [])this.getAdaptedSelection(RepositoryFile.class);
	    IRepositoryFile []files = new IRepositoryFile[resources.length];
	    for (int i = 0; i < resources.length; i++) {
	    	files[i] = (IRepositoryFile)resources[i].getRepositoryResource();
	    }
		this.runScheduled(new OpenRemoteFileOperation(files, OpenRemoteFileOperation.OPEN_SPECIFIED, this.editorId));
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		if (action.getImageDescriptor() == null) {
			IEditorDescriptor descriptor = SVNTeamUIPlugin.instance().getWorkbench().getEditorRegistry().findEditor(this.editorId);
			action.setImageDescriptor(descriptor == null ? null : descriptor.getImageDescriptor());
		}
	}
	
	public boolean isEnabled() {
		Object []items = this.getAdaptedSelection(RepositoryFile.class);
		return this.allowsMultiple ? items.length > 0 : items.length == 1;
	}

}
