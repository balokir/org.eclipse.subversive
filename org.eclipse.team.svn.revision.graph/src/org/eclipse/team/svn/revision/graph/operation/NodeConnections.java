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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/** 
 * Encapsulate logic of working with connections between nodes  
 * 
 * @author Igor Burilo
 */
public class NodeConnections {

	protected NodeConnections next;	
	protected NodeConnections previous;	
	protected List<NodeConnections> copiedTo = new ArrayList<NodeConnections>();
	protected NodeConnections copiedFrom;
	
	public NodeConnections[] getCopiedTo() {
		return this.copiedTo.toArray(new NodeConnections[0]);
	}
	
	public NodeConnections getCopiedFrom() {
		return this.copiedFrom;
	}
		
	public void setNext(NodeConnections nextNode) {
		if (nextNode == null) {
			throw new IllegalArgumentException("Node can't be null");
		}
		if (this.next != null && this.next.equals(nextNode)) {
			return;
		}
		
		NodeConnections tmp1 = this.next;
		NodeConnections tmp2 = nextNode.previous;
		
		this.next = nextNode;		
		nextNode.previous = this;					
		
		if (tmp1 != null) {
			tmp1.previous = null;	
		}		
		if (tmp2 != null) {
			tmp2.next = null;	
		}		
	}
	
	public void removeNext() {
		if (this.next != null) {
			this.next.previous = null;
			this.next = null;
		}
	}
	
	public void setPrevious(NodeConnections prevNode) {
		if (prevNode == null) {
			throw new IllegalArgumentException("Node can't be null");
		}
		prevNode.setNext(this);				
	}
	
	public void removePrevious() {
		if (this.previous != null) {
			this.previous.next = null;
			this.previous = null;
		}
	}
			
	public void addCopiedTo(NodeConnections node) {
		if (node == null) {
			throw new IllegalArgumentException("Node can't be null");
		}
		if (this.copiedTo.contains(node)) {
			return;
		}
		
		NodeConnections tmp = node.copiedFrom; 
			
		this.copiedTo.add(node);
		node.copiedFrom = this;
		
		if (tmp != null) {
			tmp.removeCopiedTo(node);	
		}		
	}
	
	public void removeCopiedTo(NodeConnections node) {
		if (node == null) {
			throw new IllegalArgumentException("Node can't be null");
		}
		if (!this.copiedTo.isEmpty()) {
			Iterator<NodeConnections> iter = this.copiedTo.iterator();
			while (iter.hasNext()) {
				NodeConnections copyTo = iter.next();
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
			Iterator<NodeConnections> iter = this.copiedTo.iterator();
			while (iter.hasNext()) {
				NodeConnections copyTo = iter.next();
				copyTo.copiedFrom = null;
				iter.remove();
			}	
		}
	}
	
	public void addCopiedTo(NodeConnections[] nodes) {
		if (nodes == null || nodes.length == 0) {
			throw new IllegalArgumentException("Nodes can't be null");
		}				
		for (NodeConnections node : nodes) {
			this.addCopiedTo(node);
		}			
	}	
	
	public void setCopiedFrom(NodeConnections node) {
		if (node == null) {
			throw new IllegalArgumentException("Node can't be null");
		}
		node.addCopiedTo(this);		
	}
	
	public void removeCopiedFrom() {
		if (this.copiedFrom != null) {
			this.copiedFrom.removeCopiedTo(this);
		}
	}
	
	public NodeConnections getNext() {
		return this.next;
	}
	
	public NodeConnections getPrevious() {
		return this.previous;
	}
	
	/*
	 * Return iterator which starts to iterate from start node in chain
	 */
	public Iterator<NodeConnections> iterateRevisionsChain() {		
		return new Iterator<NodeConnections>() {
			protected NodeConnections nextNode;			
			{
				this.nextNode = NodeConnections.this.getStartNodeInChain();
			}
			public boolean hasNext() {
				return this.nextNode != null;
			}
			public NodeConnections next() {
				if (!this.hasNext()) {
					throw new NoSuchElementException();
				}
				NodeConnections res = this.nextNode;
				this.nextNode = this.nextNode.next;
				return res;
			}

			public void remove() {
				throw new UnsupportedOperationException();				
			}			
		};
	}
	
	public NodeConnections getStartNodeInChain() {
		NodeConnections node = this;
		while (true) {
			if (node.getPrevious() == null) {
				return node;
			} else {
				node = node.getPrevious();
			}
		}
	}
	
	public NodeConnections getEndNodeInChain() {
		NodeConnections node = this;
		while (true) {
			if (node.getNext() == null) {
				return node;
			} else {
				node = node.getNext();
			}
		}
	}
		
	public NodeConnections getStartNodeInGraph() {		
		NodeConnections first = this.getStartNodeInChain();
		while (true) {
			NodeConnections copiedFrom = first.getCopiedFrom();
			if (copiedFrom != null) {
				first = copiedFrom.getStartNodeInChain();
			} else {
				break;
			}
		}
		return first;
	}			
	
	//---- for debug
	public static void showGraph(NodeConnections node) {
		System.out.println("\r\n------------------");
		
		//find start node
		NodeConnections first = node.getStartNodeInGraph();
		doShowGraph(first);
	}
	
	protected static void doShowGraph(NodeConnections node) {				
		List<NodeConnections> nextNodes = new ArrayList<NodeConnections>();
		
		System.out.println();
		
		Iterator<NodeConnections> iter = node.iterateRevisionsChain();
		while (iter.hasNext()) {
			NodeConnections start = iter.next();
			StringBuffer str = new StringBuffer();
			str.append(start);
			
			if (start.getCopiedFrom() != null) {
				//nextNodes.add(start.getCopiedFromNode());
				str.append("\r\n\tcopied from node: " + start.getCopiedFrom() + ", ");
			}
			
			NodeConnections[] copyToNodes = start.getCopiedTo();
			if (copyToNodes.length > 0) {
				str.append("\r\n\tcopy to nodes: ");
				for (NodeConnections copyToNode : copyToNodes) {
					nextNodes.add(copyToNode);
					str.append("\r\n\t" + copyToNode);
				}
			}
			System.out.println(str);
			
			//start = start.nextNode();
		}
		
		for (NodeConnections nextNode : nextNodes) {
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
