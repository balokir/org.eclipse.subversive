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
package org.eclipse.team.svn.revision.graph.operation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.revision.graph.operation.PathRevision.RevisionNodeAction;

/**
 * Validate revision graph model:
 *  - node can't contain next and rename nodes at the same time
 *  - node can't contain previous and copied from nodes at the same time 
 * 
 * @author Igor Burilo
 */
public class ValidateRevionGraphModelOperation extends AbstractActionOperation {

	protected CreateRevisionGraphModelOperation createModelOp;
	
	public ValidateRevionGraphModelOperation(CreateRevisionGraphModelOperation createModelOp) {
		//TODO
		super("ValidateRevionGraphModel");
		this.createModelOp = createModelOp;
	}

	@Override
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		PathRevision model = this.createModelOp.getModel();
		if (model != null) {
			new TopRightTraverseVisitor() {
				public void visit(NodeConnections rawNode) {
					PathRevision node = (PathRevision) rawNode;
															
					if (node.getCopiedFrom() != null && node.getPrevious() != null) {
						ValidateRevionGraphModelOperation.this.reportError(
							new Exception(
								"Not valid node: " + node + ". " + 
								"It contains previous and copied from nodes. " +
								"Previous node: " + node.getPrevious() + ", " +
								"copied from node: " + node.getCopiedFrom()));
					}
					
					if (node.getNext() != null) {
						PathRevision[] copiedToNodes = node.getCopiedTo();				
						for (PathRevision copiedToNode : copiedToNodes) {
							if (copiedToNode.action == RevisionNodeAction.RENAME) {
								ValidateRevionGraphModelOperation.this.reportError(
										new Exception(
											"Not valid node: " + node + ". " + 
											"It contains next and rename nodes. " +
											"Next node: " + node.getNext() + ", " +
											"rename node: " + copiedToNode));
								break;
							}
						}		
					}				
				}				
			}.traverse(model.getStartNodeInGraph());
		}		
	}

}
