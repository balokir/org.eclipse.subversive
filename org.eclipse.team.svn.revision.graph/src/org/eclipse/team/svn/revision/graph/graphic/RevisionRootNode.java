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
import java.util.regex.Pattern;

import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.revision.graph.PathRevision;
import org.eclipse.team.svn.revision.graph.TopRightTraverseVisitor;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCache;
import org.eclipse.team.svn.revision.graph.cache.TimeMeasure;

/**
 * Root of revision nodes 
 * 
 * @author Igor Burilo
 */
public class RevisionRootNode extends ChangesNotifier {
	
	protected final IRepositoryResource resource;
	protected final PathRevision pathRevision;	
	protected final RepositoryCache repositoryCache;
	
	protected RevisionNode initialStartNode;
	//set its value only through setter
	protected RevisionNode currentStartNode;	
	/*
	 * used during filtering to find start node if currentStartNode is null,
	 * currentStartNode can be null if during previous filtering all nodes are filtered out
	 */
	protected RevisionNode lastNotNullCurrentStartNode;
	
	protected boolean isSimpleMode;
	protected boolean isIncludeMergeInfo;
	protected boolean isTruncatePaths;
	protected SVNRevision fromRevision;
	protected SVNRevision toRevision; 
	
	protected NodesFilterManager filterManager;
		
	protected List<RevisionNode> currentNodesList = new ArrayList<RevisionNode>();
		
	protected Map<RevisionNode, List<RevisionConnectionNode>> currentSourceConnections = new HashMap<RevisionNode, List<RevisionConnectionNode>>();
	protected Map<RevisionNode, List<RevisionConnectionNode>> currentTargetConnections = new HashMap<RevisionNode, List<RevisionConnectionNode>>();
	 
	//nodes which have incoming or outgoing merges
	protected Set<RevisionNode> nodesWithMerges = new HashSet<RevisionNode>();
	
	public RevisionRootNode(IRepositoryResource resource, PathRevision node, RepositoryCache repositoryCache) {
		this.resource = resource;
		this.pathRevision = node;
		this.repositoryCache = repositoryCache;
		this.filterManager = new NodesFilterManager();										
	}
	
	public void init() {		
		this.createRevisionNodesModel();
		this.setCurrentStartNode(this.initialStartNode);
		
		this.initNodesWithMerges();
		
		this.simpleSetMode(isSimpleMode);
		
		this.filter(false);
		
		this.truncatePaths();
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
		TimeMeasure processMeasure = new TimeMeasure("Re-structure nodes in model"); //$NON-NLS-1$
			
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
				
				RevisionNode[] copiedTo = item.getCopiedTo();
				for (RevisionNode copyToItem : copiedTo) {
					addCurrentConnection(item, copyToItem);
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
	
	protected static class RevisionNodeItem {
		final RevisionNode revisionNode;
		final PathRevision pathRevision;
		public RevisionNodeItem(RevisionNode revisionNode, PathRevision pathRevision) {
			this.revisionNode = revisionNode;
			this.pathRevision = pathRevision;
		}
	}
	
	/*
	 * Convert PathRevision model to RevisionNode model
	 */
	protected final void createRevisionNodesModel() {
		Queue<RevisionNodeItem> queue = new LinkedList<RevisionNodeItem>();
		
		PathRevision pathFirst = (PathRevision) this.pathRevision.getStartNodeInGraph();
		RevisionNode first = this.createRevisionNode(pathFirst);
		this.initialStartNode = first;		
		queue.offer(new RevisionNodeItem(first, pathFirst));
		
		RevisionNodeItem node = null;
		while ((node = queue.poll()) != null) {							
			
			PathRevision pathNext = node.pathRevision.getNext();
			if (pathNext != null) {
				RevisionNode next = this.createRevisionNode(pathNext);				
				node.revisionNode.setNext(next);
				queue.offer(new RevisionNodeItem(next, pathNext));
			}
			
			PathRevision[] pathCopiedToNodes = node.pathRevision.getCopiedTo();
			for (PathRevision pathCopiedToNode : pathCopiedToNodes) {
				RevisionNode copiedTo = this.createRevisionNode(pathCopiedToNode);
				node.revisionNode.addCopiedTo(copiedTo);
				queue.offer(new RevisionNodeItem(copiedTo, pathCopiedToNode));
			}
		}			
	}
	
	protected RevisionNode createRevisionNode(PathRevision pathRevision) {		
		RevisionNode node = new RevisionNode(pathRevision, this);
		return node;		
	}

	public boolean isSimpleMode() {
		return this.isSimpleMode;
	}
	
	//just changes the flag
	public void simpleSetMode(boolean isSimpleMode) {
		this.isSimpleMode = isSimpleMode;					
		if (this.isSimpleMode) {
			this.filterManager.addFilter(AbstractRevisionNodeFilter.SIMPLE_MODE_FILTER);
		} else {
			this.filterManager.removeFilter(AbstractRevisionNodeFilter.SIMPLE_MODE_FILTER);
		}		
	}
	
	//re-build model
	public void setMode(boolean isSimpleMode) {		
		this.simpleSetMode(isSimpleMode);
			
		this.filter(true);
	}
	
	public boolean simpleSetTruncatePaths(boolean isTruncatePaths) {
		if (this.isTruncatePaths == isTruncatePaths) {
			return false;
		}
		this.isTruncatePaths = isTruncatePaths;
		return true;
	}
	
	public void setTruncatePaths(final boolean isTruncatePaths) {
		if (this.simpleSetTruncatePaths(isTruncatePaths)) {
			this.truncatePaths();
		}		
	}
	
	public void setRevisionsRange(SVNRevision fromRevision, SVNRevision toRevision) {
		this.fromRevision = fromRevision;
		this.toRevision = toRevision;
	}
	
	public SVNRevision getFromRevision() {
		return this.fromRevision;
	}
	
	public SVNRevision getToRevision() {
		return this.toRevision;
	}
	
	protected void truncatePaths() {
		new TopRightTraverseVisitor.AllNodesVisitor() {
			protected void visit(RevisionNode node) {			
				node.setTruncatePath(RevisionRootNode.this.isTruncatePaths);
			}
		}.traverse(this.currentStartNode);
	}
	
	public boolean isTruncatePaths() {
		return this.isTruncatePaths;
	}
	
	public void setIncludeMergeInfo(boolean isIncludeMergeInfo) {
		this.isIncludeMergeInfo = isIncludeMergeInfo;
	}
	
	public boolean isIncludeMergeInfo() {
		return this.isIncludeMergeInfo;
	}
	
	public String getRevisionPath(int pathIndex) {
		return this.repositoryCache.getPathStorage().getPath(pathIndex);	
	}

	/** 
	 * @return resource for which revision graph is launched
	 */
	public IRepositoryResource getRepositoryResource() {
		return this.resource;
	}
	
	/** 
	 * @param pathIndex
	 * @return	full path which contains repository root 
	 */
	public String getRevisionFullPath(RevisionNode revisionNode) {
		String url = this.resource.getRepositoryLocation().getRepositoryRootUrl();
		url += revisionNode.getPath();
		return url;
	}
	
	public RepositoryCache getRepositoryCache() {
		return this.repositoryCache;
	}
	
	/**
	 * It can be null if all nodes filtered and(or) collapsed
	 */
	public RevisionNode getCurrentStartNode() {
		return this.currentStartNode;
	}
	
	protected void setCurrentStartNode(RevisionNode node) {
		if (node != null) {
			this.lastNotNullCurrentStartNode = node;			
		}
		this.currentStartNode = node;
	}
	
	/*
	 * Operation which changes revision nodes model
	 */
	protected abstract class RevisionModelOperation {
		public abstract void run();
				
		/*
		 * Go bottom starting from 'topNode' to find start node. 
		 * Result can't be null
		 */
		protected RevisionNode findStartNode(RevisionNode topNode) {
			if (topNode == null) {
				throw new IllegalArgumentException("Node can't be null"); //$NON-NLS-1$
			}
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
				filterManager.applyFilters(initialStartNode, currentStartNode);
						
				/*
				 * if there are no nodes after filtering, just show nothing in graph.
				 * You can return back by disabling filter
				 */
				RevisionNode candidateNode = this.findStartNode(currentStartNode != null ? currentStartNode : lastNotNullCurrentStartNode);
				setCurrentStartNode(candidateNode.isFiltered() ? candidateNode.getNext() : candidateNode);
			}			
		});
		
		if (isMakeNotification) {
			this.firePropertyChange(RevisionRootNode.FILTER_NODES_PROPERTY, null, new Boolean(this.isSimpleMode));
		}
	}
	
	protected void initNodesWithMerges() {
		new TopRightTraverseVisitor<RevisionNode>() {
			protected void visit(RevisionNode node) {
				if (node.hasIncomingMerges() || node.hasOutgoingMerges()) {
					RevisionRootNode.this.nodesWithMerges.add(node);
				}
				
			}			
		}.traverse(this.currentStartNode);		
	}
	
	public boolean hasNodesWithMerges() {
		return !this.nodesWithMerges.isEmpty();
	}
	
	/**
	 * Clear all merge connections
	 */
	public void clearAllMerges() {
		//at first try to remove target connections as they send less notifications
		for (RevisionNode node : this.nodesWithMerges) {			
			node.removeAllIncomingMergeConnections();
		}
		for (RevisionNode node : this.nodesWithMerges) {			
			node.removeAllOutgoingMergeConnections();
		}		
	}	
	
	//--- Expand/Collapse
	
	public void collapseNext(final RevisionNode node) {						
		this.changeModel(new RevisionModelOperation() {					
			public void run() {
				node.internalSetNextCollapsed(true);
				
				//current start node isn't changed here
			}
		});
		
		this.firePropertyChange(RevisionRootNode.EXPAND_COLLAPSE_NODES_PROPERTY, null, null);
	}

	public void collapseRename(final RevisionNode node) {						
		this.changeModel(new RevisionModelOperation() {					
			public void run() {
				node.internalSetRenameCollapsed(true);
				
				//current start node isn't changed here
			}
		});
		
		this.firePropertyChange(RevisionRootNode.EXPAND_COLLAPSE_NODES_PROPERTY, null, null);
	}
	
	public void collapsePrevious(final RevisionNode node) {
		this.changeModel(new RevisionModelOperation() {					
			public void run() {
				node.internalSetPreviousCollapsed(true);
				
				setCurrentStartNode(node);
			}
		});					
		
		this.firePropertyChange(RevisionRootNode.EXPAND_COLLAPSE_NODES_PROPERTY, null, null);			
	}

	public void collapseCopiedTo(final RevisionNode node) {		
		this.changeModel(new RevisionModelOperation() {					
			public void run() {
				node.internalSetCopiedToCollapsed(true);
				
				//current start node isn't changed here
			}
		});					 
		
		this.firePropertyChange(RevisionRootNode.EXPAND_COLLAPSE_NODES_PROPERTY, null, null);		
	}

	public void collapseCopiedFrom(final RevisionNode node) {
		this.changeModel(new RevisionModelOperation() {					
			public void run() {
				node.internalSetCopiedFromCollapsed(true);
				
				setCurrentStartNode(node);
			}
		});					
		
		this.firePropertyChange(RevisionRootNode.EXPAND_COLLAPSE_NODES_PROPERTY, null, null);					
	}

	public void expandNext(final RevisionNode node) {
		this.changeModel(new RevisionModelOperation() {					
			public void run() {
				node.internalSetNextCollapsed(false);
				
				//current start node isn't changed here
			}
		});					 
		
		this.firePropertyChange(RevisionRootNode.EXPAND_COLLAPSE_NODES_PROPERTY, null, null);				
	}
	
	public void expandRename(final RevisionNode node) {
		this.changeModel(new RevisionModelOperation() {					
			public void run() {
				node.internalSetRenameCollapsed(false);
				
				//current start node isn't changed here
			}
		});					 
		
		this.firePropertyChange(RevisionRootNode.EXPAND_COLLAPSE_NODES_PROPERTY, null, null);				
	}

	public void expandPrevious(final RevisionNode node) {
		this.changeModel(new RevisionModelOperation() {					
			public void run() {
				node.internalSetPreviousCollapsed(false);
				
				setCurrentStartNode(findStartNode(node));
			}
		});					
		
		this.firePropertyChange(RevisionRootNode.EXPAND_COLLAPSE_NODES_PROPERTY, null, null);
	}

	public void expandCopiedTo(final RevisionNode node) {
		this.changeModel(new RevisionModelOperation() {					
			public void run() {
				node.internalSetCopiedToCollapsed(false);
				
				//current start node isn't changed here 
			}
		});					
		
		this.firePropertyChange(RevisionRootNode.EXPAND_COLLAPSE_NODES_PROPERTY, null, null);			
	}

	public void expandCopiedFrom(final RevisionNode node) {
		this.changeModel(new RevisionModelOperation() {					
			public void run() {
				node.internalSetCopiedFromCollapsed(false);
				
				setCurrentStartNode(this.findStartNode(node));
			}
		});					
		
		this.firePropertyChange(RevisionRootNode.EXPAND_COLLAPSE_NODES_PROPERTY, null, null);		
	}
		
	public void refresh(RevisionRootNode newRootNode) {
		newRootNode.simpleSetMode(this.isSimpleMode());
		newRootNode.simpleSetTruncatePaths(this.isTruncatePaths());
		newRootNode.setIncludeMergeInfo(this.isIncludeMergeInfo());
		newRootNode.setRevisionsRange(this.getFromRevision(), this.getToRevision());
		
		this.firePropertyChange(ChangesNotifier.REFRESH_NODES_PROPERTY, this, newRootNode);
	}
	
	/**
	 * Search currently active, i.e. not filtered and collapsed, revision nodes.
	 * Either revision or path must be specified. If path is specified then node
	 * matches if its path contains any part of the search path. 
	 */
	public RevisionNode[] search(final SearchOptions options) {
		if (options == null) {
			throw new NullPointerException("Search options"); //$NON-NLS-1$
		}
		if (options.revision == -1 && options.path == null) {
			throw new IllegalArgumentException("Either revision or path should be specified"); //$NON-NLS-1$
		}
		
		final List<RevisionNode> result = new ArrayList<RevisionNode>();
		//case insensitive
		final Pattern pattern = options.path != null ? Pattern.compile(Pattern.quote(options.path), Pattern.CASE_INSENSITIVE) : null;
		new TopRightTraverseVisitor<RevisionNode>() {
			protected void visit(RevisionNode node) {
				if (options.revision != -1 && node.getRevision() != options.revision) {
					return;
				}
				//TODO improvement: don't check paths for nodes in the same chain								
				if (options.path != null && !pattern.matcher(node.getPath()).find()) {
					return;
				}
				result.add(node);
			}			
		}.traverse(this.currentStartNode);
		return result.toArray(new RevisionNode[0]);
	}
}
