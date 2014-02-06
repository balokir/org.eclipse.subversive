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

import org.eclipse.team.svn.core.operation.IResourcePropertyProvider;
import org.eclipse.team.svn.core.operation.remote.GetRemotePropertiesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.operation.ShowPropertiesOperation;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/**
 * 
 * @author Igor Burilo
 */
public class ShowPropertiesAction extends BaseRevisionGraphAction {

	public final static String ShowPropertiesAction_ID = "ShowProperties";	 //$NON-NLS-1$
	
	public ShowPropertiesAction(IWorkbenchPart part) {
		super(part);
		
		setText(SVNUIMessages.ShowPropertiesAction_label);
		setId(ShowPropertiesAction_ID);
		setToolTipText(SVNUIMessages.ShowPropertiesAction_label);		
		setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/propertiesedit.gif")); //$NON-NLS-1$
	}

	@Override
	protected boolean calculateEnabled() {
		return this.isEnable(BaseRevisionGraphAction.NOT_DELETED_ACTION_FILTER, 1);
	}
	
	@Override
	public void run() {
		IRepositoryResource resource = BaseRevisionGraphAction.convertToResource(this.getSelectedEditPart());
		IResourcePropertyProvider provider = new GetRemotePropertiesOperation(resource);
		ShowPropertiesOperation op = new ShowPropertiesOperation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), resource, provider);
		this.runOperation(op);				
	}

}
