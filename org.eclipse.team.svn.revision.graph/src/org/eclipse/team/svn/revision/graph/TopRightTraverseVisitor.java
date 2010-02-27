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
package org.eclipse.team.svn.revision.graph;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Allow to traverse nodes and visit
 * 
 * @author Igor Burilo
 */
public abstract class TopRightTraverseVisitor<T extends NodeConnections<T>> {	
	
	public void traverse(T startNode) {
		Queue<T> queue = new LinkedList<T>();						
		queue.offer(startNode);			
		
		T node = null;
		while ((node = queue.poll()) != null) {
			this.visit(node);
			
			T next = node.getNext();
			if (next != null) {
				queue.offer(next);
			}
			
			Collection<T> copiedToNodes = node.getCopiedToAsCollection();
			for (T copiedToNode : copiedToNodes) {
				queue.offer(copiedToNode);
			}
		}			
	}

	protected abstract void visit(T node);	

}
