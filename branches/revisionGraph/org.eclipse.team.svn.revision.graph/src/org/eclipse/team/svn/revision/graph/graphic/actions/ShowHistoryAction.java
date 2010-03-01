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

import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.revision.graph.PathRevision.RevisionNodeAction;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionEditPart;
import org.eclipse.team.svn.revision.graph.operation.GetRepositoryResourcesOperation;
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

	public final static String ShowHistoryAction_ID = "ShowHistory";	
	
	public ShowHistoryAction(IWorkbenchPart part) {
		super(part);
		
		setText(SVNUIMessages.ShowResourceHistoryCommand_label);
		setId(ShowHistoryAction_ID);
		setToolTipText("Show History");		
		setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history.gif"));		
	}

	@Override
	protected boolean calculateEnabled() {		
		RevisionEditPart[] editParts = this.getSelectedEditParts();
		if (editParts.length == 1) {
			RevisionEditPart editPart = editParts[0];
			RevisionNode node = editPart.getCastedModel();
			return node.pathRevision.action != RevisionNodeAction.DELETE;
		}
		return false;
	}
	
	@Override
	public void run() {					
		CompositeOperation op = new CompositeOperation("Operation_ShowHistory");

		RevisionEditPart editPart = this.getSelectedEditPart();
		GetRepositoryResourcesOperation getResourcesOp = new GetRepositoryResourcesOperation(new RevisionEditPart[]{editPart});
		op.add(getResourcesOp);
		
		ShowHistoryViewOperation showHistoryOp = new ShowHistoryViewOperation(getResourcesOp, 0, 0);
		op.add(showHistoryOp, new IActionOperation[] {getResourcesOp});
		
		this.runOperation(op);			
	}	
		
}
