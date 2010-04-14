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
import org.eclipse.team.svn.revision.graph.cache.RepositoryCache;

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

	protected RepositoryCache repositoryCache;
	
	public PathRevisionConnectionsValidator(RepositoryCache repositoryCache) {
		this.repositoryCache = repositoryCache;
	}
	
	public void validate(PathRevision node) {		
		//nodes in the same chain should have the same path
		if (node.getNext() != null && node.getPathIndex() != node.getNext().getPathIndex()) {
			this.reportProblem(node, 
				"Its path and next node path are not equal. " + //$NON-NLS-1$
				"Next node: " + node.getNext().toString(this.repositoryCache)); //$NON-NLS-1$
		}
		if (node.getPrevious() != null && node.getPathIndex() != node.getPrevious().getPathIndex()) {
			this.reportProblem(node, 
				"Its path and previous node path are not equal. " + //$NON-NLS-1$
				"Previous node: " + node.getPrevious().toString(this.repositoryCache)); //$NON-NLS-1$
		}
		
		//check copy from and not previous
		if (node.getCopiedFrom() != null && node.getPrevious() != null) {
			this.reportProblem(node,							 
				"It contains previous and copied from nodes. " + //$NON-NLS-1$
				"Previous node: " + node.getPrevious().toString(this.repositoryCache) + ", " + //$NON-NLS-1$ //$NON-NLS-2$
				"copied from node: " + node.getCopiedFrom().toString(this.repositoryCache)); //$NON-NLS-1$
		}				
		
		//check rename					
		PathRevision[] copiedToNodes = node.getCopiedTo();
		PathRevision renameNode = null;
		for (PathRevision copiedToNode : copiedToNodes) {
			if (copiedToNode.action == RevisionNodeAction.RENAME) {
				
				//check rename and not next
				if (node.getNext() != null) {
					this.reportProblem(node,
						"It contains next and rename nodes. " + //$NON-NLS-1$
						"Next node: " + node.getNext().toString(this.repositoryCache) + ", " + //$NON-NLS-1$ //$NON-NLS-2$
						"rename node: " + copiedToNode.toString(this.repositoryCache)); //$NON-NLS-1$
				}
				
				//check that there's only one rename
				if (renameNode != null) {
					this.reportProblem(node,										 
						"It contains several rename nodes. " + //$NON-NLS-1$
						"Rename node1: " + renameNode.toString(this.repositoryCache) + ", " + //$NON-NLS-1$ //$NON-NLS-2$
						"rename node2: " + copiedToNode.toString(this.repositoryCache)); //$NON-NLS-1$
				}
				
				renameNode = copiedToNode;
			}
		}
	}

	protected void reportProblem(PathRevision node, String string) {
		String message = "Not valid node: " + node.toString(this.repositoryCache) + ". "; //$NON-NLS-1$ //$NON-NLS-2$
		throw new RuntimeException(message + string);		
	}
}
