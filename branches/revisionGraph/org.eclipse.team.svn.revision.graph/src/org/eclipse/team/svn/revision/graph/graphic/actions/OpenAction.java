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

import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.operation.OpenRemoteFileOperation;
import org.eclipse.ui.IWorkbenchPart;

/**
 * 
 * @author Igor Burilo
 */
public class OpenAction extends BaseRevisionGraphAction {

	public final static String OpenAction_ID = "Open"; //$NON-NLS-1$
	
	public OpenAction(IWorkbenchPart part) {
		super(part);
					
		setText(SVNUIMessages.HistoryView_Open);
		setId(OpenAction_ID);
		setToolTipText(SVNUIMessages.HistoryView_Open);				
	}

	@Override
	protected boolean calculateEnabled() {
		return this.isEnable(BaseRevisionGraphAction.NOT_DELETED_ACTION_FILTER, 1);				
	}
	
	@Override
	public void run() {
		IRepositoryResource[] resources = BaseRevisionGraphAction.convertToResources(this.getSelectedEditParts(BaseRevisionGraphAction.NOT_DELETED_ACTION_FILTER));
		IActionOperation op = new OpenRemoteFileOperation(new IRepositoryResourceProvider.DefaultRepositoryResourceProvider(resources), OpenRemoteFileOperation.OPEN_DEFAULT);	
		this.runOperation(op);			
	}

}
