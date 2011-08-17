/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.action.remote;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.team.svn.core.connector.ISVNConnector.Options;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.AbstractCopyMoveResourcesOperation;
import org.eclipse.team.svn.core.operation.remote.SetRevisionAuthorNameOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.operation.RefreshRemoteResourcesOperation;
import org.eclipse.team.svn.ui.wizard.copymove.CopyMoveWizard;

/**
 * Abstract class for the copy and move remote resources actions
 *
 * @author Sergiy Logvin
 */
public abstract class AbstractCopyMoveAction extends AbstractRepositoryTeamAction {
	protected String operationId;
	
	public AbstractCopyMoveAction(String operationId) {
		super();
		this.operationId = operationId;
	}
	
	public void runImpl(IAction action) {
		CopyMoveWizard copyMoveWizard = new CopyMoveWizard(this.getSelectedRepositoryResources(), this.operationId.toLowerCase().contains("move"));
		WizardDialog dlg = new WizardDialog(this.getShell(), copyMoveWizard);
		if (dlg.open() == 0) {
			String message = copyMoveWizard.getComment();
			IRepositoryResource []selected = this.getSelectedRepositoryResources();
			IRepositoryResource destination = copyMoveWizard.getDestination();
			
			AbstractCopyMoveResourcesOperation moveOp = this.makeCopyOperation(destination, selected, message, copyMoveWizard.getNewName());
			CompositeOperation op = new CompositeOperation(moveOp.getId(), moveOp.getMessagesClass());
			op.add(moveOp);
			op.add(new SetRevisionAuthorNameOperation(moveOp, Options.FORCE), new IActionOperation[] {moveOp});
			op.add(this.makeRefreshOperation(destination, selected));
			
			this.runScheduled(op);
		}
		
	}
	
	protected abstract AbstractCopyMoveResourcesOperation makeCopyOperation(IRepositoryResource destination, IRepositoryResource[] selected, String message, String name);
	protected abstract RefreshRemoteResourcesOperation makeRefreshOperation(IRepositoryResource destination, IRepositoryResource []selected);
	
}
