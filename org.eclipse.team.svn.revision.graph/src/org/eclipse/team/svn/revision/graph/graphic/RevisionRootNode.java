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
package org.eclipse.team.svn.revision.graph.graphic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.eclipse.team.svn.revision.graph.NodeConnections;
import org.eclipse.team.svn.revision.graph.PathRevision;
import org.eclipse.team.svn.revision.graph.TopRightTraverseVisitor;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode.RevisionNodeItem;

/**
 * Root of revision nodes 
 *
 * TODO add expand/collapse
 * 
 * @author Igor Burilo
 */
public class RevisionRootNode extends ChangesNotifier {
	
	protected final PathRevision pathRevision;	
	
	protected RevisionNode initialStartNode;
	protected RevisionNode currentStartNode;
	
	protected List<RevisionConnectionNode> connectionsStore = new ArrayList<RevisionConnectionNode>(); 
	
	protected boolean isSimpleMode;
		
	protected NodesFilterManager filterManager;
	
	protected List<RevisionNode> currentNodesList = new ArrayList<RevisionNode>();
	protected List<RevisionConnectionNode> currentConnections = new ArrayList<RevisionConnectionNode>();
	
	public RevisionRootNode(PathRevision node) {
		this.pathRevision = node;
		this.filterManager = new NodesFilterManager();										
	}
	
	public void init(boolean isSimpleMode) {		
		this.createInitialConnections();
		
		this.internalSetMode(isSimpleMode);
		
		this.processCurrentModel();
	}
	
	public List<RevisionNode> getChildren() {
		return this.currentNodesList;
	}
	
	public List<RevisionConnectionNode> getConnections(RevisionNode node, boolean isSource) {
		List<RevisionConnectionNode> res = new ArrayList<RevisionConnectionNode>();
		for (RevisionConnectionNode con : this.currentConnections) {
			if (isSource && con.source.equals(node) || !isSource && con.target.equals(node)) {
				res.add(con);
			}
		}
		return res;		
	} 				
	
	protected void processCurrentModel() {
		/*
		 * Remember previous nodes in order to update them, 
		 * i.e. update their connections, as during filtering, collapsing
		 * some nodes can be deleted
		 */
		final List<RevisionNode> previousNodes = new ArrayList<RevisionNode>();
		if (this.currentStartNode != null) {			
			new TopRightTraverseVisitor() {
				public void visit(NodeConnections node) {
					previousNodes.add(((RevisionNodeItem) node).getRevisionNode());
				}				
			}.traverse(this.currentStartNode.getCurrentConnectionItem());			
		}
		
		//restore current connections to initial state 
		this.createCurrentConnectionsFromInitial();					
		
		//TODO apply collapse
		
		//apply filters
		RevisionNodeItem startItem = this.filterManager.applyFilters(this.currentStartNode.getCurrentConnectionItem());
		if (startItem != null) {				
			this.currentStartNode = startItem.getRevisionNode();
		}				
		//TODO handle that after filtering and collapsing there are no nodes 
						
		//prepare children and connections
		this.currentNodesList.clear();
		this.currentConnections.clear();
		
		new TopRightTraverseVisitor() {			
			public void visit(NodeConnections node) {				
				RevisionNodeItem item = (RevisionNodeItem) node;
				currentNodesList.add(item.getRevisionNode());
								
				if (item.getNext() != null) {
					RevisionConnectionNode con = RevisionRootNode.this.createConnection(item.getRevisionNode(), item.getNext().getRevisionNode(), false);
					RevisionRootNode.this.currentConnections.add(con);
				}			
				if (item.getCopiedTo().length > 0) {
					for (RevisionNodeItem copyToItem : item.getCopiedTo()) {
						RevisionConnectionNode con = RevisionRootNode.this.createConnection(item.getRevisionNode(), copyToItem.getRevisionNode(), false);
						RevisionRootNode.this.currentConnections.add(con);
					}
				}
			}
		}.traverse(this.currentStartNode.getCurrentConnectionItem());
		
		
		//update previous nodes
		if (!previousNodes.isEmpty()) {
			for (RevisionNode prevNode : previousNodes) {
				prevNode.refreshConnections();
			}			
		}
	}
	
	protected RevisionConnectionNode createConnection(RevisionNode source, RevisionNode target, boolean isCreate) {
		RevisionConnectionNode con = null;
		if (!isCreate && !this.connectionsStore.isEmpty()) {
			for (RevisionConnectionNode existingCon : this.connectionsStore) {
				if (existingCon.source.equals(source) && existingCon.target.equals(target)) {
					con = existingCon;
					break;
				}
			}
		} 
		
		if (con == null) {
			con = new RevisionConnectionNode(source, target);
			this.connectionsStore.add(con);
		}		
		return con;		
	}
	
	protected final void createInitialConnections() {
		Queue<RevisionNode> queue = new LinkedList<RevisionNode>();
		
		PathRevision pathFirst = (PathRevision) this.pathRevision.getStartNodeInGraph();
		RevisionNode first = this.createRevisionNode(pathFirst);
		this.initialStartNode = first;		
		queue.offer(first);
		
		RevisionNode node = null;
		while ((node = queue.poll()) != null) {
			RevisionNodeItem initialItem = node.getInitialConnectionItem();							
			
			PathRevision pathNext = node.pathRevision.getNext();
			if (pathNext != null) {
				RevisionNode next = this.createRevisionNode(pathNext);				
				initialItem.setNext(next.getInitialConnectionItem());				
				this.createConnection(node, next, true);				
				queue.offer(next);
			}
			
			PathRevision[] pathCopiedToNodes = node.pathRevision.getCopiedTo();
			for (PathRevision pathCopiedToNode : pathCopiedToNodes) {
				RevisionNode copiedTo = this.createRevisionNode(pathCopiedToNode);
				initialItem.addCopiedTo(copiedTo.getInitialConnectionItem());
				this.createConnection(node, copiedTo, true);				
				queue.offer(copiedTo);
			}
		}			
	}
	
	protected void createCurrentConnectionsFromInitial() {
		//copy connections from initial to current
		new TopRightTraverseVisitor() {			
			public void visit(NodeConnections node) {
				RevisionNodeItem initialItem = (RevisionNodeItem) node;
				RevisionNodeItem currentItem = initialItem.getRevisionNode().getCurrentConnectionItem();
				
				if (initialItem.getNext() != null) {
					RevisionNode nextRNode = initialItem.getNext().getRevisionNode();
					currentItem.setNext(nextRNode.getCurrentConnectionItem());	
				} else {
					currentItem.removeNext();
				}
				
				//reset previous value
				currentItem.removeAllCopiedTo();
				//add new values
				if (initialItem.getCopiedTo().length != 0) {
					for (RevisionNodeItem copiedTo : initialItem.getCopiedTo()) {
						RevisionNode rCopiedTo = copiedTo.getRevisionNode();
						currentItem.addCopiedTo(rCopiedTo.getCurrentConnectionItem());
					}
				}			
			}
		}.traverse(this.initialStartNode.getInitialConnectionItem());
			
		this.currentStartNode = this.initialStartNode;
	}
	
	protected RevisionNode createRevisionNode(PathRevision pathRevision) {		
		RevisionNode node = new RevisionNode(pathRevision);		
		return node;		
	}

	public boolean isSimpleMode() {
		return this.isSimpleMode;
	}

	protected void internalSetMode(boolean isSimpleMode) {
		this.isSimpleMode = isSimpleMode;					
		if (this.isSimpleMode) {
			this.filterManager.addFilter(AbstractRevisionNodeFilter.SIMPLE_MODE_FILTER);
		} else {
			this.filterManager.removeFilter(AbstractRevisionNodeFilter.SIMPLE_MODE_FILTER);
		}		
	}
	
	public void setMode(boolean isSimpleMode) {		
		this.internalSetMode(isSimpleMode);
			
		this.processCurrentModel();		
		this.firePropertyChange(RevisionRootNode.LAYOUT_PROPERTY, null, new Boolean(this.isSimpleMode));
	}	
}
