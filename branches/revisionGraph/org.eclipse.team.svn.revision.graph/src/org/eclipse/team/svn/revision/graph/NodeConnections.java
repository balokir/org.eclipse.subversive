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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/** 
 * Encapsulate logic of working with connections between nodes  
 * 
 * Methods which add new data to model may throw RuntimeException's
 * if node connections are not valid after operation
 * 
 * @author Igor Burilo
 */
public class NodeConnections<T extends NodeConnections<T>>{

	private T next;	
	private T previous;	
	private Set<T> copiedTo = new HashSet<T>();
	private T copiedFrom;
	
	public T[] getCopiedTo(T[] a) {
		return this.copiedTo.toArray(a);
	}
	
	public Collection<T> getCopiedToAsCollection() {
		return new ArrayList<T>(this.copiedTo);
	}
	
	public T getCopiedFrom() {
		return this.copiedFrom;
	}
		
	public void setNext(T nextNode) {
		if (nextNode == null) {
			throw new IllegalArgumentException("Node can't be null"); //$NON-NLS-1$
		}
		if (this.next != null && this.next.equals(nextNode)) {
			return;
		}
		
		T tmp1 = this.next;
		T tmp2 = nextNode.previous;
		
		this.next = nextNode;		
		nextNode.previous = this.convertThisToGeneric();					
		
		if (tmp1 != null) {
			tmp1.previous = null;	
		}		
		if (tmp2 != null) {
			tmp2.next = null;	
		}		
		
		this.validate();
	}		
	
	public void removeNext() {
		if (this.next != null) {
			this.next.previous = null;
			this.next = null;
		}
	}
	
	public void setPrevious(T prevNode) {
		if (prevNode == null) {
			throw new IllegalArgumentException("Node can't be null"); //$NON-NLS-1$
		}
		prevNode.setNext(this.convertThisToGeneric());				
	}	
	
	public void removePrevious() {
		if (this.previous != null) {
			this.previous.next = null;
			this.previous = null;
		}
	}
			
	public void addCopiedTo(T node) {
		this.addCopiedTo(node, true);
	}
	
	protected void addCopiedTo(T node, boolean canValidate) {
		if (node == null) {
			throw new IllegalArgumentException("Node can't be null"); //$NON-NLS-1$
		}
		if (this.copiedTo.contains(node)) {
			return;
		}
		
		T tmp = node.copiedFrom; 
			
		this.copiedTo.add(node);
		node.copiedFrom = this.convertThisToGeneric();
		
		if (tmp != null) {
			tmp.removeCopiedTo(node);	
		}
		
		if (canValidate) {
			this.validate();	
		}		
	}	
	
	public void removeCopiedTo(T node) {
		if (node == null) {
			throw new IllegalArgumentException("Node can't be null"); //$NON-NLS-1$
		}
		if (!this.copiedTo.isEmpty()) {
			Iterator<T> iter = this.copiedTo.iterator();
			while (iter.hasNext()) {
				T copyTo = iter.next();
				if (copyTo.equals(node)) {
					copyTo.copiedFrom = null;
					iter.remove();
					break;
				}
			}	
		}
	}	
	
	public void removeAllCopiedTo() {
		if (!this.copiedTo.isEmpty()) {
			Iterator<T> iter = this.copiedTo.iterator();
			while (iter.hasNext()) {
				T copyTo = iter.next();
				copyTo.copiedFrom = null;
				iter.remove();
			}	
		}
	}
	
	public void addCopiedTo(T[] nodes) {
		if (nodes == null || nodes.length == 0) {
			throw new IllegalArgumentException("Nodes can't be null"); //$NON-NLS-1$
		}				
		for (T node : nodes) {
			this.addCopiedTo(node, false);
		}			
		
		this.validate();
	}	
	
	public void setCopiedFrom(T node) {
		if (node == null) {
			throw new IllegalArgumentException("Node can't be null"); //$NON-NLS-1$
		}
		node.addCopiedTo(this.convertThisToGeneric());		
	}
	
	public void removeCopiedFrom() {
		if (this.copiedFrom != null) {
			this.copiedFrom.removeCopiedTo(this.convertThisToGeneric());
		}
	}
	
	public T getNext() {
		return this.next;
	}
	
	public T getPrevious() {
		return this.previous;
	}
	
	/*
	 * Return iterator which starts to iterate from start node in chain
	 */
	public Iterator<T> iterateRevisionsChain() {		
		return new Iterator<T>() {
			protected T nextNode;			
			{
				this.nextNode = NodeConnections.this.getStartNodeInChain();
			}
			public boolean hasNext() {
				return this.nextNode != null;
			}
			public T next() {
				if (!this.hasNext()) {
					throw new NoSuchElementException();
				}
				T res = this.nextNode;
				this.nextNode = this.nextNode.next;
				return res;
			}

			public void remove() {
				throw new UnsupportedOperationException();				
			}			
		};
	}
	
	public T getStartNodeInChain() {
		T node = this.convertThisToGeneric();
		while (true) {
			if (node.getPrevious() == null) {
				return node;
			} else {
				node = node.getPrevious();
			}
		}
	}
	
	public T getEndNodeInChain() {
		T node = this.convertThisToGeneric();
		while (true) {
			if (node.getNext() == null) {
				return node;
			} else {
				node = node.getNext();
			}
		}
	}
		
	public T getStartNodeInGraph() {		
		T first = this.getStartNodeInChain();
		while (true) {
			T copiedFrom = first.getCopiedFrom();
			if (copiedFrom != null) {
				first = copiedFrom.getStartNodeInChain();
			} else {
				break;
			}
		}
		return first;
	}			
	
	/**
	 * Validate node connections after the changes has been applied.
	 * Base implementation does nothing
	 */
	protected void validate() {
		//do nothing
	}
	
	@SuppressWarnings("unchecked")
	protected T convertThisToGeneric() {
		return (T) this;
	}
	
	//---- for debug
	public static <T extends NodeConnections<T>> void showGraph(T node) {
		System.out.println("\r\n------------------"); //$NON-NLS-1$
		
		//find start node
		T first = node.getStartNodeInGraph();
		doShowGraph(first);
	}
	
	protected static <T extends NodeConnections<T>> void doShowGraph(T node) {				
		List<T> nextNodes = new ArrayList<T>();
		
		System.out.println();
		
		Iterator<T> iter = node.iterateRevisionsChain();
		while (iter.hasNext()) {
			T start = iter.next();
			StringBuffer str = new StringBuffer();
			str.append(start);
			
			if (start.getCopiedFrom() != null) {
				//nextNodes.add(start.getCopiedFromNode());
				str.append("\r\n\tcopied from node: " + start.getCopiedFrom() + ", "); //$NON-NLS-1$ //$NON-NLS-2$
			}
						
			if (!start.copiedTo.isEmpty()) {
				str.append("\r\n\tcopy to nodes: "); //$NON-NLS-1$
				for (T copyToNode : start.copiedTo) {
					nextNodes.add(copyToNode);
					str.append("\r\n\t" + copyToNode); //$NON-NLS-1$
				}
			}
			System.out.println(str);
			
			//start = start.nextNode();
		}
		
		for (T nextNode : nextNodes) {
			doShowGraph(nextNode);
		}
	}
	
//	protected void showChain(NodeWithConnections node) {
//		StringBuffer str = new StringBuffer();
//		NodeWithConnections start = node;
//		while (true) {
//			NodeWithConnections prev = start.getPrevious();
//			if (prev == null) {
//				break;
//			} else {
//				start = prev;
//			}
//		}		
//		while (start != null) {
//			str.append(start).append("\r\n");
//			start = start.getNext();
//		}
//		System.out.println(str);
//	}
}
