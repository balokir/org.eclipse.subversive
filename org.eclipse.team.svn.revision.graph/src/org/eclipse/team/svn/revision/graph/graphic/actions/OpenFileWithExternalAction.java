/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.revision.graph.graphic.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionEditPart;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.operation.OpenRemoteFileOperation;
import org.eclipse.ui.IWorkbenchPart;

/**
 * The action will open the selected file with the system external editor if available
 * 
 * @author Igor Burilo
 */
public class OpenFileWithExternalAction extends BaseRevisionGraphAction {

	public final static String OpenFileWithExternalAction_ID = "OpenFileWithExternal";	 //$NON-NLS-1$
	
	public OpenFileWithExternalAction(IWorkbenchPart part) {
		super(part);
		this.setId(OpenFileWithExternalAction_ID);
	}

	@Override
	protected boolean calculateEnabled() {
		RevisionEditPart[] editParts = null;
		if (this.getSelectedEditParts().length == 1 && 
			(editParts = this.getSelectedEditParts(BaseRevisionGraphAction.NOT_DELETED_ACTION_FILTER)).length == 1) {
			IRepositoryResource resource = BaseRevisionGraphAction.convertToResource(editParts[0]);
			return SVNTeamUIPlugin.instance().getWorkbench().getEditorRegistry().isSystemExternalEditorAvailable(resource.getName());
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.ui.actions.SelectionAction#handleSelectionChanged()
	 */
	@Override
	protected void handleSelectionChanged() {		
		super.handleSelectionChanged();		
		
		IRepositoryResource[] resources = BaseRevisionGraphAction.convertToResources(this.getSelectedEditParts(BaseRevisionGraphAction.NOT_DELETED_ACTION_FILTER));
		ImageDescriptor imgDescriptor = resources.length > 0 ? SVNTeamUIPlugin.instance().getWorkbench().getEditorRegistry().getSystemExternalEditorImageDescriptor(resources[0].getName()) : null;
		this.setImageDescriptor(imgDescriptor);
	}
	
	@Override
	public void run() {
		IRepositoryResource[] resources = BaseRevisionGraphAction.convertToResources(this.getSelectedEditParts(BaseRevisionGraphAction.NOT_DELETED_ACTION_FILTER));
		this.runOperation(new OpenRemoteFileOperation(new IRepositoryFile[] {(IRepositoryFile)resources[0]}, OpenRemoteFileOperation.OPEN_EXTERNAL));
	}

}
