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

import org.eclipse.team.svn.core.operation.remote.PreparedBranchTagOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionEditPart;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.action.remote.BranchTagAction;
import org.eclipse.ui.IWorkbenchPart;

/**
 * 
 * @author Igor Burilo
 */
public class CreateBranchTagAction extends BaseRevisionGraphAction {

	public final static String CreateBranchAction_ID = "CreateBranch"; //$NON-NLS-1$
	public final static String CreateTagAction_ID = "CreateTag"; //$NON-NLS-1$
	
	protected int action;
	
	public CreateBranchTagAction(IWorkbenchPart part, int action) {
		super(part);	
		
		this.action = action;				
		if (this.action == BranchTagAction.BRANCH_ACTION) {
			setText(SVNUIMessages.HistoryView_BranchFromRevision);
			setId(CreateBranchAction_ID);
			setToolTipText(SVNUIMessages.HistoryView_BranchFromRevision);		
			setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/branch.gif")); //$NON-NLS-1$
		} else {
			setText(SVNUIMessages.HistoryView_TagFromRevision);
			setId(CreateTagAction_ID);
			setToolTipText(SVNUIMessages.HistoryView_TagFromRevision);		
			setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/tag.gif")); //$NON-NLS-1$
		}		
	}

	@Override
	protected boolean calculateEnabled() {	
		RevisionEditPart[] editParts = null;		
		if (this.getSelectedEditParts().length == 1 && 
			(editParts = this.getSelectedEditParts(BaseRevisionGraphAction.NOT_DELETED_ACTION_FILTER)).length == 1) {
			
			String revision = String.valueOf(editParts[0].getCastedModel().getRevision());
			String title = this.action == BranchTagAction.BRANCH_ACTION ? 
				SVNUIMessages.format(SVNUIMessages.HistoryView_BranchFrom, new String[] {revision}) :
				SVNUIMessages.format(SVNUIMessages.HistoryView_TagFrom, new String[] {revision});				
			this.setText(title);
			this.setToolTipText(title);		
			return true;
		}
		
		String title = this.action == BranchTagAction.BRANCH_ACTION ? 
			SVNUIMessages.HistoryView_BranchFromRevision :
			SVNUIMessages.HistoryView_TagFromRevision;				
		this.setText(title);
		this.setToolTipText(title);		
		
		return false;
	}
	
	@Override
	public void run() {
		IRepositoryResource[] resources = BaseRevisionGraphAction.convertToResources(this.getSelectedEditParts(BaseRevisionGraphAction.NOT_DELETED_ACTION_FILTER));
		PreparedBranchTagOperation op = BranchTagAction.getBranchTagOperation(resources, getWorkbenchPart().getSite().getShell(), CreateBranchTagAction.this.action);
		this.runOperation(op);									
	}

}
