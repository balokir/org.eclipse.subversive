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

import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.operation.OpenRemoteFileOperation;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.editors.text.EditorsUI;

/**
 * The action will open the selected file with the system external editor if available
 * 
 * @author Igor Burilo
 */
public class OpenFileWithAction extends BaseRevisionGraphAction {

	public final static String OpenFileWithAction_ID = "OpenFileWith";	 //$NON-NLS-1$
	
	protected String editorId;
	protected boolean allowsMultiple;
	
	public OpenFileWithAction(IWorkbenchPart part) {
		this(part, EditorsUI.DEFAULT_TEXT_EDITOR_ID, true);				
	}
		
	public OpenFileWithAction(IWorkbenchPart part, String editorId, boolean allowsMultiple) {
		super(part);
		this.editorId = editorId;
		this.allowsMultiple = allowsMultiple;
				
		setId(OpenFileWithAction_ID);		
	}	
	
	@Override
	protected boolean calculateEnabled() {
		if (this.allowsMultiple) {
			return this.getSelectedEditParts(BaseRevisionGraphAction.NOT_DELETED_ACTION_FILTER).length > 0;
		} else {
			return this.isEnable(BaseRevisionGraphAction.NOT_DELETED_ACTION_FILTER, 1);
		}								
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.ui.actions.SelectionAction#handleSelectionChanged()
	 */
	@Override
	protected void handleSelectionChanged() {		
		super.handleSelectionChanged();
		
		IEditorDescriptor descriptor = SVNTeamUIPlugin.instance().getWorkbench().getEditorRegistry().findEditor(this.editorId);
		this.setImageDescriptor(descriptor == null ? null : descriptor.getImageDescriptor());		
	}
	
	@Override
	public void run() {
		IRepositoryResource[] resources = BaseRevisionGraphAction.convertToResources(this.getSelectedEditParts(BaseRevisionGraphAction.NOT_DELETED_ACTION_FILTER));
	    IRepositoryFile []files = new IRepositoryFile[resources.length];
	    for (int i = 0; i < resources.length; i++) {
	    	files[i] = (IRepositoryFile) resources[i];
	    }	    
	    this.runOperation(new OpenRemoteFileOperation(files, OpenRemoteFileOperation.OPEN_SPECIFIED, this.editorId));
	}

}
