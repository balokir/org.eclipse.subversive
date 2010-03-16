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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.revision.graph.PathRevision;
import org.eclipse.team.svn.revision.graph.TopRightTraverseVisitor;
import org.eclipse.team.svn.revision.graph.cache.RevisionDataContainer;
import org.eclipse.team.svn.revision.graph.cache.TimeMeasure;
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
	protected final RevisionDataContainer dataContainer;
	
	protected RevisionNode initialStartNode;
	protected RevisionNode currentStartNode;	
	
	protected boolean isSimpleMode;
		
	protected NodesFilterManager filterManager;
		
	protected List<RevisionNode> currentNodesList = new ArrayList<RevisionNode>();
		
	protected Map<RevisionNode, List<RevisionConnectionNode>> currentSourceConnections = new HashMap<RevisionNode, List<RevisionConnectionNode>>();
	protected Map<RevisionNode, List<RevisionConnectionNode>> currentTargetConnections = new HashMap<RevisionNode, List<RevisionConnectionNode>>();
	
	public RevisionRootNode(PathRevision node, RevisionDataContainer dataContainer) {
		this.pathRevision = node;
		this.dataContainer = dataContainer;
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
		List<RevisionConnectionNode> res = isSource ? this.currentSourceConnections.get(node) : this.currentTargetConnections.get(node);
		return res != null ? res : Collections.<RevisionConnectionNode>emptyList();
	} 				
	
	protected void processCurrentModel() {
		TimeMeasure processMeasure = new TimeMeasure("Re-structure nodes in model");
				
		/*
		 * Remember previous nodes in order to update them, 
		 * i.e. update their connections, as during filtering, collapsing
		 * some nodes can be deleted
		 */										
		final Set<RevisionNode> previousNodes = new HashSet<RevisionNode>();
		if (this.currentStartNode != null) {			
			new TopRightTraverseVisitor<RevisionNodeItem>() {
				public void visit(RevisionNodeItem node) {
					previousNodes.add(node.getRevisionNode());
				}				
			}.traverse(this.currentStartNode.getCurrentConnectionItem());			
		}
										
		Set<RevisionConnectionNode> previousConnections = new HashSet<RevisionConnectionNode>();		
		for (List<RevisionConnectionNode> connections : this.currentSourceConnections.values()) {
			previousConnections.addAll(connections);
		}		

		//process model
		this.doProcessCurrentModel();
						
		
		/*
		 * update previous nodes
		 * 
		 * This operation can take long time. It has the same problem as with setContents#setContents
		 */				
		if (!previousConnections.isEmpty()) {								
			Set<RevisionConnectionNode> newConnections = new HashSet<RevisionConnectionNode>();
			for (List<RevisionConnectionNode> connections : this.currentSourceConnections.values()) {
				newConnections.addAll(connections);
			}
						
			Set<RevisionNode> changedNodes = new HashSet<RevisionNode>();
			
			for (RevisionConnectionNode previousConnection : previousConnections) {
				if (!newConnections.contains(previousConnection)) {
					changedNodes.add(previousConnection.source);
					changedNodes.add(previousConnection.target);
				}
			}
			
			//check new connections
			for (RevisionConnectionNode newConnection : newConnections) {
				if (!previousConnections.contains(newConnection)) {															
					if (previousNodes.contains(newConnection.source)) {
						changedNodes.add(newConnection.source);
					}					
					if (previousNodes.contains(newConnection.target)) {
						changedNodes.add(newConnection.target);	
					}					
				}
			}						
			
			for (RevisionNode changedNode : changedNodes) {			
				changedNode.refreshConnections();
			}
		}						
		processMeasure.end();
	}
	
	protected void doProcessCurrentModel() {
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
		this.currentSourceConnections.clear();
		this.currentTargetConnections.clear();
		
		new TopRightTraverseVisitor<RevisionNodeItem>() {			
			public void visit(RevisionNodeItem node) {				
				RevisionNodeItem item = node;
				currentNodesList.add(item.getRevisionNode());
												
				if (item.getNext() != null) {
					addCurrentConnection(item.getRevisionNode(), item.getNext().getRevisionNode());									
				}			
				if (item.getCopiedTo().length > 0) {
					for (RevisionNodeItem copyToItem : item.getCopiedTo()) {
						addCurrentConnection(item.getRevisionNode(), copyToItem.getRevisionNode());
					}
				}
			}
		}.traverse(this.currentStartNode.getCurrentConnectionItem());
	}
	
	protected void addCurrentConnection(RevisionNode source, RevisionNode target) {
		RevisionConnectionNode con = new RevisionConnectionNode(source, target);
		
		//source
		List<RevisionConnectionNode> sourceConnections = this.currentSourceConnections.get(source);
		if (sourceConnections == null) {
			sourceConnections = new ArrayList<RevisionConnectionNode>();
			this.currentSourceConnections.put(source, sourceConnections);
		}
		sourceConnections.add(con);
		
		//target
		List<RevisionConnectionNode> targetConnections = this.currentTargetConnections.get(target);
		if (targetConnections == null) {
			targetConnections = new ArrayList<RevisionConnectionNode>();
			this.currentTargetConnections.put(target, targetConnections);
		}
		targetConnections.add(con);		
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
				queue.offer(next);
			}
			
			PathRevision[] pathCopiedToNodes = node.pathRevision.getCopiedTo();
			for (PathRevision pathCopiedToNode : pathCopiedToNodes) {
				RevisionNode copiedTo = this.createRevisionNode(pathCopiedToNode);
				initialItem.addCopiedTo(copiedTo.getInitialConnectionItem());
				queue.offer(copiedTo);
			}
		}			
	}
	
	protected void createCurrentConnectionsFromInitial() {
		//copy connections from initial to current
		new TopRightTraverseVisitor<RevisionNodeItem>() {			
			public void visit(RevisionNodeItem node) {
				RevisionNodeItem initialItem = node;
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
	
	public String getRevisionPath(int pathIndex) {
		return this.dataContainer.getPathStorage().getPath(pathIndex);	
	}

	public IRepositoryResource getRepositoryResource() {
		return this.dataContainer.getRepositoryResource();
	}
	
	public String getRevisionFullPath(RevisionNode revisionNode) {
		return this.dataContainer.getRevisionFullPath(revisionNode.pathRevision.getPathIndex());
	}	
}
