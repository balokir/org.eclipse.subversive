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

import org.eclipse.team.svn.revision.graph.PathRevision;
import org.eclipse.team.svn.revision.graph.PathRevision.RevisionNodeAction;
import org.eclipse.team.svn.revision.graph.cache.RevisionDataContainer;

/** 
 * Validate revision graph node:
 *  - nodes in the same chain must have the same path
 *  - node can't contain next and rename nodes at the same time
 *  - node can't contain previous and copied from nodes at the same time
 *  - there can be only one rename node
 *  
 * Following fail-fast approach if there're problems with model
 * we report a problem.
 * 
 * @author Igor Burilo
 */
public class PathRevisionConnectionsValidator {

	protected RevisionDataContainer dataContainer;
	
	public PathRevisionConnectionsValidator(RevisionDataContainer dataContainer) {
		this.dataContainer = dataContainer;
	}
	
	public void validate(PathRevision node) {		
		//nodes in the same chain should have the same path
		if (node.getNext() != null && node.getPathIndex() != node.getNext().getPathIndex()) {
			this.reportProblem(node, 
				"Its path and next node path are not equal. " +
				"Next node: " + node.getNext().toString(this.dataContainer));						
		}
		if (node.getPrevious() != null && node.getPathIndex() != node.getPrevious().getPathIndex()) {
			this.reportProblem(node, 
				"Its path and previous node path are not equal. " +
				"Previous node: " + node.getPrevious().toString(this.dataContainer));						
		}
		
		//check copy from and not previous
		if (node.getCopiedFrom() != null && node.getPrevious() != null) {
			this.reportProblem(node,							 
				"It contains previous and copied from nodes. " +
				"Previous node: " + node.getPrevious().toString(this.dataContainer) + ", " +
				"copied from node: " + node.getCopiedFrom().toString(this.dataContainer));
		}				
		
		//check rename					
		PathRevision[] copiedToNodes = node.getCopiedTo();
		PathRevision renameNode = null;
		for (PathRevision copiedToNode : copiedToNodes) {
			if (copiedToNode.action == RevisionNodeAction.RENAME) {
				
				//check rename and not next
				if (node.getNext() != null) {
					this.reportProblem(node,
						"It contains next and rename nodes. " +
						"Next node: " + node.getNext().toString(this.dataContainer) + ", " +
						"rename node: " + copiedToNode.toString(this.dataContainer));								
				}
				
				//check that there's only one rename
				if (renameNode != null) {
					this.reportProblem(node,										 
						"It contains several rename nodes. " +
						"Rename node1: " + renameNode.toString(this.dataContainer) + ", " +
						"rename node2: " + copiedToNode.toString(this.dataContainer));
				}
				
				renameNode = copiedToNode;
			}
		}
	}

	protected void reportProblem(PathRevision node, String string) {
		String message = "Not valid node: " + node.toString(this.dataContainer) + ". ";
		throw new RuntimeException(message + string);		
	}
}
