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

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionEditPart;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.wizard.CreatePatchWizard;
import org.eclipse.ui.IWorkbenchPart;

/**
 * 
 * @author Igor Burilo
 */
public class CreatePatchAction extends BaseRevisionGraphAction {

	public final static String CreatePatchAction_ID = "CreatePatch"; //$NON-NLS-1$
	
	public CreatePatchAction(IWorkbenchPart part) {
		super(part);
		
		setText(SVNUIMessages.CreatePatchCommand_label);
		setId(CreatePatchAction_ID);
		setToolTipText(SVNUIMessages.CreatePatchCommand_label);		
	}

	@Override
	protected boolean calculateEnabled() {
		if (this.isEnable(BaseRevisionGraphAction.EXIST_IN_PREVIOUS_FILTER, 1)) {
			return true;
		} else {
			RevisionEditPart[] editParts = this.getSelectedEditParts(BaseRevisionGraphAction.NOT_DELETED_ACTION_FILTER);
			//check that resources have the same path
			if (editParts.length == 2 && editParts[0].getCastedModel().getPath().equals(editParts[1].getCastedModel().getPath())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void run() {		
		IRepositoryResource[] resources = BaseRevisionGraphAction.convertToResources(this.getSelectedEditParts());
		
		CreatePatchWizard wizard = new CreatePatchWizard(resources[0].getName());
		WizardDialog dialog = new WizardDialog(this.getWorkbenchPart().getSite().getShell(), wizard);
		if (dialog.open() == DefaultDialog.OK) {															
			IRepositoryResource next = resources[0];
			IRepositoryResource prev = null;
			if (resources.length == 1) {
				//FIXME peg revisions for renamed resources: (rev - 1) works only if the revision really exists
				//in repository for current resource. Use LocateUrlInHistory						
				prev = SVNUtility.copyOf(next);
				prev.setSelectedRevision(SVNRevision.fromNumber(((SVNRevision.Number)next.getSelectedRevision()).getNumber() - 1));
				prev.setPegRevision(next.getPegRevision());
			}
			else {						
				prev = resources[1];									
				if (((SVNRevision.Number)next.getSelectedRevision()).getNumber() < ((SVNRevision.Number) prev.getSelectedRevision()).getNumber()) {
					IRepositoryResource tmp = next;
					next = prev;
					prev = tmp;
				}						
			}									
			
			IActionOperation op = org.eclipse.team.svn.ui.action.remote.CreatePatchAction.getCreatePatchOperation(prev, next, wizard);
			this.runOperation(op);	
		}		
	}

}
