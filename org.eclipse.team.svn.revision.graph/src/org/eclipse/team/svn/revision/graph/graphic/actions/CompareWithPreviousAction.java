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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.revision.graph.PathRevision.RevisionNodeAction;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionEditPart;
import org.eclipse.team.svn.revision.graph.operation.GetRepositoryResourcesForCompareOperation;
import org.eclipse.team.svn.ui.operation.CompareRepositoryResourcesOperation;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Compare revision node with previous revision 
 * 
 * @author Igor Burilo
 */
public class CompareWithPreviousAction extends BaseRevisionGraphAction {

	public final static String CompareWithPreviousAction_ID = "CompareWithPrevious";	
	
	public CompareWithPreviousAction(IWorkbenchPart part) {
		super(part);
		
		setText("Compare with Previous Revision");
		setId(CompareWithPreviousAction_ID);
		setToolTipText("Compare with Previous Revision");
	}

	@Override
	protected boolean calculateEnabled() {	
		RevisionEditPart[] editParts = this.getSelectedEditParts();
		if (editParts.length == 1) {
			RevisionEditPart editPart = editParts[0];
			RevisionNode node = editPart.getCastedModel();
			RevisionNodeAction action = node.pathRevision.action;
			if (action == RevisionNodeAction.MODIFY || action == RevisionNodeAction.NONE) {
				return true;
			}			
		}
		return false;
	}
	
	@Override
	public void run() {
		CompositeOperation op = new CompositeOperation("Operation_CompareRepository");

		RevisionEditPart editPart = this.getSelectedEditPart();
		GetRepositoryResourcesForCompareOperation getResourcesOp = new GetRepositoryResourcesForCompareOperation(new RevisionEditPart[]{editPart}) {
			@Override
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				super.runImpl(monitor);
				
				IRepositoryResource next = this.getRepositoryResources()[0];						
				IRepositoryResource prev = SVNUtility.copyOf(next);
				prev.setSelectedRevision(SVNRevision.fromNumber(((SVNRevision.Number)next.getSelectedRevision()).getNumber() - 1));
				this.resources =  new IRepositoryResource[] {prev, next};
			}
		};			
		op.add(getResourcesOp);
		
		CompareRepositoryResourcesOperation compareOp = new CompareRepositoryResourcesOperation(getResourcesOp);
		compareOp.setForceId(this.toString());
		op.add(compareOp, new IActionOperation[]{getResourcesOp});
		
		this.runOperation(op);			
	}

}
