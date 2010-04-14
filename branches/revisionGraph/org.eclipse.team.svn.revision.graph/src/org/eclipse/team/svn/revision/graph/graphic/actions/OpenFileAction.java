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
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.operation.OpenRemoteFileOperation;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Open remote file action implementation
 * 
 * @author Igor Burilo
 */
public class OpenFileAction extends BaseRevisionGraphAction {

	public final static String OpenFileAction_ID = "OpenFile"; //$NON-NLS-1$
	
	public OpenFileAction(IWorkbenchPart part) {
		super(part);
		
		this.setId(OpenFileAction_ID);
	}
	
	@Override
	protected boolean calculateEnabled() {
		return this.getSelectedEditParts(BaseRevisionGraphAction.NOT_DELETED_ACTION_FILTER).length > 0;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.ui.actions.SelectionAction#handleSelectionChanged()
	 */
	@Override
	protected void handleSelectionChanged() {		
		super.handleSelectionChanged();		
		
		IRepositoryResource[] resources = BaseRevisionGraphAction.convertToResources(this.getSelectedEditParts(BaseRevisionGraphAction.NOT_DELETED_ACTION_FILTER));
		ImageDescriptor imgDescriptor = resources.length > 0 ? SVNTeamUIPlugin.instance().getWorkbench().getEditorRegistry().getImageDescriptor(resources[0].getName()) : null;
		this.setImageDescriptor(imgDescriptor);
	}
	
	@Override
	public void run() {	    
	    IRepositoryResource[] resources = BaseRevisionGraphAction.convertToResources(this.getSelectedEditParts(BaseRevisionGraphAction.NOT_DELETED_ACTION_FILTER));
	    IRepositoryFile []files = new IRepositoryFile[resources.length];
	    for (int i = 0; i < resources.length; i++) {
	    	files[i] = (IRepositoryFile) resources[i];
	    }	    
		this.runOperation(new OpenRemoteFileOperation(files, OpenRemoteFileOperation.OPEN_DEFAULT));
	}
	
}
