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

import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;

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
			
			T next = this.getNext(node);
			if (next != null) {
				queue.offer(next);
			}
			
			Collection<T> copiedToNodes = this.getCopiedToAsCollection(node);
			for (T copiedToNode : copiedToNodes) {
				queue.offer(copiedToNode);
			}
		}			
	}

	/*
	 * Can be overridden in sub-classes
	 */
	protected Collection<T> getCopiedToAsCollection(T node) {
		return node.getCopiedToAsCollection();
	}
	
	/*
	 * Can be overridden in sub-classes
	 */
	protected T getNext(T node) {
		return node.getNext();
	}
	
	protected abstract void visit(T node);	

	/**
	 * Traverse all revision nodes even if some nodes filtered or collapsed
	 */
	public static abstract class AllNodesVisitor extends TopRightTraverseVisitor<RevisionNode> {
		@Override
		protected RevisionNode getNext(RevisionNode node) {
			return node.internalGetNext();
		};		
		@Override
		protected Collection<RevisionNode> getCopiedToAsCollection(RevisionNode node) {
			return node.internalGetCopiedToAsCollection();
		}
	}
}
