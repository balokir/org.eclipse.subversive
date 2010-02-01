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

import java.util.LinkedList;
import java.util.Queue;

/**
 * 
 * @author Igor Burilo
 */
public abstract class TopRightTraverseVisitor implements INodeVisitor {	
	
	public void traverse(NodeConnections startNode) {
		Queue<NodeConnections> queue = new LinkedList<NodeConnections>();						
		queue.offer(startNode);			
		
		NodeConnections node = null;
		while ((node = queue.poll()) != null) {				
			this.visit(node);
			
			NodeConnections next = node.getNext();
			if (next != null) {
				queue.offer(next);
			}
			
			NodeConnections[] copiedToNodes = node.getCopiedTo();
			for (NodeConnections copiedToNode : copiedToNodes) {
				queue.offer(copiedToNode);
			}
		}			
	}

}
