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
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionEditPart;
import org.eclipse.team.svn.revision.graph.operation.GetRepositoryResourcesForCompareOperation;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.operation.CompareRepositoryResourcesOperation;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Compare two revision nodes 
 * 
 * @author Igor Burilo
 */
public class CompareWithEachOtherAction extends BaseRevisionGraphAction {

	public final static String CompareWithEachOtherAction_ID = "CompareWithEachOther";	
	
	public CompareWithEachOtherAction(IWorkbenchPart part) {
		super(part);
		
		setText(SVNUIMessages.HistoryView_CompareEachOther);
		setId(CompareWithEachOtherAction_ID);
		setToolTipText(SVNUIMessages.HistoryView_CompareEachOther);
	}

	@Override
	protected boolean calculateEnabled() {
		RevisionEditPart[] editParts = this.getSelectedEditParts();
		if (editParts.length == 2 && 
			editParts[0].getCastedModel().pathRevision.action != RevisionNodeAction.DELETE &&
			editParts[1].getCastedModel().pathRevision.action != RevisionNodeAction.DELETE) {
			return true;
		}
		return false;
	}
	
	@Override
	public void run() {
		CompositeOperation op = new CompositeOperation("Operation_CompareRepository");

		RevisionEditPart[] editParts = this.getSelectedEditParts();
		GetRepositoryResourcesForCompareOperation getResourcesOp = new GetRepositoryResourcesForCompareOperation(editParts);				
		op.add(getResourcesOp);
		
		CompareRepositoryResourcesOperation compareOp = new CompareRepositoryResourcesOperation(getResourcesOp);
		compareOp.setForceId(this.toString());
		op.add(compareOp, new IActionOperation[] {getResourcesOp});
		
		this.runOperation(op);
	}

}
