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
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.operation.RemoteShowAnnotationOperation;
import org.eclipse.ui.IWorkbenchPart;

/**
 * 
 * @author Igor Burilo
 */
public class ShowAnnotationAction extends BaseRevisionGraphAction {

	public final static String ShowAnnotationAction_ID = "ShowAnnotation"; //$NON-NLS-1$
	
	public ShowAnnotationAction(IWorkbenchPart part) {
		super(part);
		
		setText(SVNUIMessages.ShowAnnotationCommand_label);
		setId(ShowAnnotationAction_ID);
		setToolTipText(SVNUIMessages.ShowAnnotationCommand_label);		
	}

	@Override
	protected boolean calculateEnabled() {	
		return this.isEnable(BaseRevisionGraphAction.NOT_DELETED_ACTION_FILTER, 1);
	}
	
	@Override
	public void run() {
		IRepositoryResource resource = BaseRevisionGraphAction.convertToResource(this.getSelectedEditPart());
		this.runOperation(new RemoteShowAnnotationOperation(resource));
	}

}
