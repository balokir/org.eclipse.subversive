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

/**
 * Root of revision nodes 
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
		this.createRevisionNodesModel();
		this.currentStartNode = this.initialStartNode;
		
		this.internalSetMode(isSimpleMode);
		
		this.filter(false);
	}
	
	public List<RevisionNode> getChildren() {
		return this.currentNodesList;
	}
	
	public List<RevisionConnectionNode> getConnections(RevisionNode node, boolean isSource) {
		List<RevisionConnectionNode> res = isSource ? this.currentSourceConnections.get(node) : this.currentTargetConnections.get(node);
		return res != null ? res : Collections.<RevisionConnectionNode>emptyList();
	} 				
	
	/*
	 * Change revision graph model
	 * 
	 * This method doesn't actually changes the model, it only
	 * performs needed pre and post actions. Model is changed by passed operation. 
	 */
	protected void changeModel(RevisionModelOperation op) {
		TimeMeasure processMeasure = new TimeMeasure("Re-structure nodes in model");
			
		boolean hasPreviousData = !this.currentNodesList.isEmpty();
		
		/*
		 * Remember previous nodes in order to update them, 
		 * i.e. update their connections, as during filtering, collapsing
		 * some nodes can be deleted
		 */										
		final Set<RevisionNode> previousNodes = new HashSet<RevisionNode>();
		if (this.currentStartNode != null) {			
			new TopRightTraverseVisitor<RevisionNode>() {
				public void visit(RevisionNode node) {
					previousNodes.add(node);
				}				
			}.traverse(this.currentStartNode);			
		}
										
		Set<RevisionConnectionNode> previousConnections = new HashSet<RevisionConnectionNode>();		
		for (List<RevisionConnectionNode> connections : this.currentSourceConnections.values()) {
			previousConnections.addAll(connections);
		}		
		
		//change model
		op.run();				
				
		//prepare children and connections
		this.currentNodesList.clear();
		this.currentSourceConnections.clear();
		this.currentTargetConnections.clear();
		
		new TopRightTraverseVisitor<RevisionNode>() {			
			public void visit(RevisionNode node) {				
				RevisionNode item = node;
				currentNodesList.add(item);
												
				if (item.getNext() != null) {
					addCurrentConnection(item, item.getNext());									
				}			
				if (item.getCopiedTo().length > 0) {
					for (RevisionNode copyToItem : item.getCopiedTo()) {
						addCurrentConnection(item, copyToItem);
					}
				}
			}
		}.traverse(this.currentStartNode);
				
		/*
		 * update previous nodes
		 * 
		 * This operation can take long time. It has the same problem as with setContents#setContents
		 */				
		if (hasPreviousData) {								
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
	
	/*
	 * Convert PathRevision model to RevisionNode model
	 */
	protected final void createRevisionNodesModel() {
		Queue<RevisionNode> queue = new LinkedList<RevisionNode>();
		
		PathRevision pathFirst = (PathRevision) this.pathRevision.getStartNodeInGraph();
		RevisionNode first = this.createRevisionNode(pathFirst);
		this.initialStartNode = first;		
		queue.offer(first);
		
		RevisionNode node = null;
		while ((node = queue.poll()) != null) {							
			
			PathRevision pathNext = node.pathRevision.getNext();
			if (pathNext != null) {
				RevisionNode next = this.createRevisionNode(pathNext);				
				node.setNext(next);
				queue.offer(next);
			}
			
			PathRevision[] pathCopiedToNodes = node.pathRevision.getCopiedTo();
			for (PathRevision pathCopiedToNode : pathCopiedToNodes) {
				RevisionNode copiedTo = this.createRevisionNode(pathCopiedToNode);
				node.addCopiedTo(copiedTo);
				queue.offer(copiedTo);
			}
		}			
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
			
		this.filter(true);
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
	
	public RevisionNode getCurrentStartNode() {
		return this.currentStartNode;
	}
	
	/*
	 * Operation which changes revision nodes model
	 * 
	 * TODO handle that after filtering and collapsing there are no nodes
	 */
	protected abstract class RevisionModelOperation {
		public abstract void run();
				
		protected RevisionNode findStartNode(RevisionNode topNode) {
			//go bottom starting from 'topNode' to find start node
			RevisionNode startNode = topNode;
			while (true) {
				RevisionNode tmp = startNode.getPrevious();
				if (tmp != null) {
					startNode = tmp;
				} else {
					tmp = startNode.getCopiedFrom();
					if (tmp != null) {
						startNode = tmp;
					} else {
						break;
					}
				}
			}
			return startNode;
		}
	}

	protected void filter(boolean isMakeNotification) {
		this.changeModel(new RevisionModelOperation() {
			public void run() {
				//apply filter to the whole model
				filterManager.applyFilters(initialStartNode);
						
				RevisionNode candidateNode = this.findStartNode(currentStartNode);			
				currentStartNode = currentStartNode != candidateNode ? candidateNode :
					(currentStartNode.isFiltered() ? currentStartNode.getNext() : currentStartNode);				
			}			
		});
		
		if (isMakeNotification) {
			this.firePropertyChange(RevisionRootNode.LAYOUT_PROPERTY, null, new Boolean(this.isSimpleMode));
		}
	}
	
	//--- Expand/Collapse
	
	public void collapseNext(final RevisionNode node) {						
		this.changeModel(new RevisionModelOperation() {					
			public void run() {
				node.setNextCollapsed(true);
				
				//current start node isn't changed here
			}
		});
		
		this.firePropertyChange(RevisionRootNode.EXPAND_COLLAPSE_PROPERTY, null, node);
	}

	public void collapsePrevious(final RevisionNode node) {
		this.changeModel(new RevisionModelOperation() {					
			public void run() {
				node.setPreviousCollapsed(true);
				
				RevisionRootNode.this.currentStartNode = node;
			}
		});					
		
		this.firePropertyChange(RevisionRootNode.EXPAND_COLLAPSE_PROPERTY, null, node);			
	}

	public void collapseCopiedTo(final RevisionNode node) {		
		this.changeModel(new RevisionModelOperation() {					
			public void run() {
				node.setCopiedToCollapsed(true);
				
				//current start node isn't changed here
			}
		});					 
		
		this.firePropertyChange(RevisionRootNode.EXPAND_COLLAPSE_PROPERTY, null, node);		
	}

	public void collapseCopiedFrom(final RevisionNode node) {
		this.changeModel(new RevisionModelOperation() {					
			public void run() {
				node.setCopiedFromCollapsed(true);
				
				RevisionRootNode.this.currentStartNode = node;
			}
		});					
		
		this.firePropertyChange(RevisionRootNode.EXPAND_COLLAPSE_PROPERTY, null, node);					
	}

	public void expandNext(final RevisionNode node) {
		this.changeModel(new RevisionModelOperation() {					
			public void run() {
				node.setNextCollapsed(false);
				
				//current start node isn't changed here
			}
		});					 
		
		this.firePropertyChange(RevisionRootNode.EXPAND_COLLAPSE_PROPERTY, null, node);				
	}

	public void expandPrevious(final RevisionNode node) {
		this.changeModel(new RevisionModelOperation() {					
			public void run() {
				node.setPreviousCollapsed(false);
				
				RevisionRootNode.this.currentStartNode = findStartNode(node);
			}
		});					
		
		this.firePropertyChange(RevisionRootNode.EXPAND_COLLAPSE_PROPERTY, null, node);
	}

	public void expandCopiedTo(final RevisionNode node) {
		this.changeModel(new RevisionModelOperation() {					
			public void run() {
				node.setCopiedToCollapsed(false);
				
				//current start node isn't changed here 
			}
		});					
		
		this.firePropertyChange(RevisionRootNode.EXPAND_COLLAPSE_PROPERTY, null, node);			
	}

	public void expandCopiedFrom(final RevisionNode node) {
		this.changeModel(new RevisionModelOperation() {					
			public void run() {
				node.setCopiedFromCollapsed(false);
				
				RevisionRootNode.this.currentStartNode = this.findStartNode(node);
			}
		});					
		
		this.firePropertyChange(RevisionRootNode.EXPAND_COLLAPSE_PROPERTY, null, node);		
	}
}
