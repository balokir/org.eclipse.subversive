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

import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.operation.ShowHistoryViewOperation;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Show History for revision node action
 * 
 * @author Igor Burilo
 */
public class ShowHistoryAction extends BaseRevisionGraphAction {

	public final static String ShowHistoryAction_ID = "ShowHistory"; //$NON-NLS-1$
	
	public ShowHistoryAction(IWorkbenchPart part) {
		super(part);
		
		setText(SVNUIMessages.ShowResourceHistoryCommand_label);
		setId(ShowHistoryAction_ID);
		setToolTipText(SVNUIMessages.ShowResourceHistoryCommand_label);		
		setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history.gif")); //$NON-NLS-1$
	}

	@Override
	protected boolean calculateEnabled() {		
		return this.isEnable(BaseRevisionGraphAction.NOT_DELETED_ACTION_FILTER, 1);
	}
	
	@Override
	public void run() {					
		IRepositoryResource resource = BaseRevisionGraphAction.convertToResource(this.getSelectedEditPart());
		ShowHistoryViewOperation op = new ShowHistoryViewOperation(resource, 0, 0);				
		this.runOperation(op);			
	}	
		
}
