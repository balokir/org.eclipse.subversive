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
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.revision.graph.PathRevision.RevisionNodeAction;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionEditPart;
import org.eclipse.team.svn.revision.graph.operation.GetRepositoryResourcesForCompareOperation;
import org.eclipse.team.svn.ui.operation.CompareRepositoryResourcesOperation;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Compare revision node with HEAD
 * 
 * @author Igor Burilo
 */
public class CompareWithHeadAction extends BaseRevisionGraphAction {

	public final static String CompareWithHeadAction_ID = "CompareWithHead";	
	
	public CompareWithHeadAction(IWorkbenchPart part) {
		super(part);
		
		setText("Compare Current with HEAD");
		setId(CompareWithHeadAction_ID);
		setToolTipText("Compare Current with HEAD");
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
		CompositeOperation op = new CompositeOperation("Operation_CompareRepository");

		RevisionEditPart editPart = this.getSelectedEditPart();
		GetRepositoryResourcesForCompareOperation getResourcesOp = new GetRepositoryResourcesForCompareOperation(new RevisionEditPart[]{editPart}) {
			@Override
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				super.runImpl(monitor);
				
				IRepositoryResource resource = this.resources[0];
				
				IRepositoryResource headResource =
					resource instanceof IRepositoryFile ?
					(IRepositoryResource)((IRepositoryRoot)resource.getRoot()).asRepositoryFile(resource.getUrl(), false) : 
					((IRepositoryRoot)resource.getRoot()).asRepositoryContainer(resource.getUrl(), false);
				headResource.setSelectedRevision(SVNRevision.HEAD);
				headResource.setPegRevision(SVNRevision.HEAD);
				
				this.resources = new IRepositoryResource[]{headResource, resource};
			}
		};			
		op.add(getResourcesOp);
		
		CompareRepositoryResourcesOperation compareOp = new CompareRepositoryResourcesOperation(getResourcesOp);
		compareOp.setForceId(this.toString());
		op.add(compareOp, new IActionOperation[]{getResourcesOp});
		
		this.runOperation(op);			
	}

}
