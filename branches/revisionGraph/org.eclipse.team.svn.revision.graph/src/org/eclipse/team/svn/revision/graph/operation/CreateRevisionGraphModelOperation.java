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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.svn.core.connector.SVNLogPath;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.ActivityCancelledException;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.revision.graph.PathRevision;
import org.eclipse.team.svn.revision.graph.TopRightTraverseVisitor;
import org.eclipse.team.svn.revision.graph.PathRevision.ReviosionNodeType;
import org.eclipse.team.svn.revision.graph.PathRevision.RevisionNodeAction;
import org.eclipse.team.svn.revision.graph.cache.ChangedPathStructure;
import org.eclipse.team.svn.revision.graph.cache.PathStorage;
import org.eclipse.team.svn.revision.graph.cache.RevisionDataContainer;
import org.eclipse.team.svn.revision.graph.cache.RevisionStructure;
import org.eclipse.team.svn.revision.graph.cache.TimeMeasure;

/**
 * Create revision graph model
 * 
 * @author Igor Burilo
 */
public class CreateRevisionGraphModelOperation extends AbstractActionOperation {	
	
	protected IRepositoryResource resource;	
	protected PrepareRevisionDataOperation prepareDataOp;
	protected RevisionDataContainer dataContainer;
	
	protected PathRevisionConnectionsValidator pathRevisionValidator;
	
	protected PathRevision resultNode; 
	
	public CreateRevisionGraphModelOperation(IRepositoryResource resource, PrepareRevisionDataOperation prepareDataOp) {
		super("Create RevisionGraph Model");
		this.resource = resource;		
		this.prepareDataOp = prepareDataOp;		
	}
	
	@Override
	protected void runImpl(IProgressMonitor monitor) throws Exception {		
		TimeMeasure totalMeasure = new TimeMeasure("Total");
				
		TimeMeasure readMeasure = new TimeMeasure("Read data");		
		this.dataContainer = this.prepareDataOp.getDataContainer();
		this.dataContainer.initForRead(monitor, this);		
		readMeasure.end();
		
		this.pathRevisionValidator = new PathRevisionConnectionsValidator(this.dataContainer);
		
		ProgressMonitorUtility.setTaskInfo(monitor, this, "Proccessing model");
		TimeMeasure processMeasure = new TimeMeasure("Create model");
		try {
			String url = this.resource.getUrl();
			String rootUrl = this.resource.getRepositoryLocation().getRepositoryRootUrl();	
		
			SVNRevision svnRevision = this.resource.getSelectedRevision();
			String path = url.substring(rootUrl.length());
						
			int pathIndex = this.dataContainer.getPathStorage().add(path);
					
			long revision;		
			if (svnRevision.getKind() == SVNRevision.Kind.NUMBER) {
				revision = ((SVNRevision.Number) svnRevision).getNumber();
			} else if (svnRevision.getKind() == SVNRevision.Kind.HEAD) {			
				//revision = this.entries[this.entries.length - 1].revision;				
				revision = this.dataContainer.getLastProcessedRevision();
			} else {
				throw new Exception("Unexpected revision kind: " + svnRevision);
			}								
			
			RevisionStructure entry = this.findStartLogEntry(revision, pathIndex);
			if (entry != null) {
				this.resultNode = this.createRevisionNode(entry, pathIndex, true);	
										
				this.process(this.resultNode, monitor);
				
				//fill result model with other data: author, message, date, children
				new TopRightTraverseVisitor<PathRevision>() {				
					protected void visit(PathRevision node) {
						PathRevision pathRevision = node;
						try {										
							dataContainer.loadRevisionData(pathRevision.getRevisionData());
						} catch (IOException ie) {
							CreateRevisionGraphModelOperation.this.reportWarning("Failed to load log entry data for revision: " + pathRevision.getRevision(), ie);
						}
					}
				}.traverse(this.resultNode.getStartNodeInGraph());
			}									
		} finally {
			this.dataContainer.closeForRead();
		}	
		
		processMeasure.end();
		
		totalMeasure.end();
	}
	
	/*
	 * Note that 'Replacing' is used as 'copied' if 'Replacing' contains 'copied from path'
	 */
	protected void process(PathRevision startNode, IProgressMonitor monitor) {
		Queue<PathRevision> nodesQueue = new LinkedList<PathRevision>();
		nodesQueue.offer(startNode);		
		PathRevision node;
		while ((node = nodesQueue.poll()) != null) {
			
			if (monitor.isCanceled()) {
				throw new ActivityCancelledException();
			}
						
			this.createRevisionsChainForPath(node);									
			
			/*
			 * For current revision chain build map of nodes, where key is a node from chain and
			 * value is a list of nodes to which key is copied to
			 */
			Map<PathRevision, List<PathRevision>> copiedToEntries = this.findCopiedToNodesInRevisionChain(node);
						
			if (!copiedToEntries.isEmpty()) {
				for (Map.Entry<PathRevision, List<PathRevision>> mapEntries : copiedToEntries.entrySet()) {
					PathRevision revisionNode = mapEntries.getKey();		
					List<PathRevision> copyToNodes = mapEntries.getValue();					
											
					if (!copyToNodes.isEmpty()) {
						PathRevision[] existingCopyToNodes = node.getCopiedTo();
						PathRevision existingCopyToNode = existingCopyToNodes.length == 1 ? existingCopyToNodes[0] : null;
						for (PathRevision copyToNode : copyToNodes) {																												
							if (existingCopyToNode != null && copyToNode.equals(existingCopyToNode)) {
								continue;
							}
							
							//if node isn't in chain, insert it
							if (revisionNode.action == RevisionNodeAction.NONE &&
								revisionNode.getNext() == null && 
								revisionNode.getPrevious() == null) {
								node.insertNodeInRevisionsChain(revisionNode);
							}
							
							revisionNode.addCopiedTo(copyToNode);
							nodesQueue.add(copyToNode);
						}				
					}																														
				}				
			}				
					
			PathRevision startNodeInChain = node.getStartNodeInChain();
			if (startNodeInChain.getCopiedFrom() == null) {				
				PathRevision copyFromNode = this.findCopiedFromNode(startNodeInChain);
				if (copyFromNode != null) {
					startNodeInChain.setCopiedFrom(copyFromNode);	
					nodesQueue.add(copyFromNode);					
				} 	
			}		
		}				
	}

	/*
	 * Create chain of revision, starting from start revision (action is 'added' or 'copied') to
	 * end revision (action is 'deleted', 'replaced', 'renamed' or last revision) 
	 */
	protected void createRevisionsChainForPath(PathRevision node) {		
		if (!this.isDeletedNode(node)) {
			//go forward
			long rev = node.getRevision();
			PathRevision processNode = node;
			while (true) {								
				if (++ rev < this.dataContainer.getLastProcessedRevision()) {
					RevisionStructure entry = this.getEntry(rev);
					if (entry != null) {
						PathRevision nextNode = this.createRevisionNode(entry, node.getPathIndex(), true);
						//not modified nodes are not included in chain
						if (nextNode.action != RevisionNodeAction.NONE) {
							//'rename' stops processing 
							if (nextNode.action == RevisionNodeAction.RENAME) {
								break;
							}							
							processNode.setNext(nextNode);							
							if (this.isDeletedNode(nextNode)) {
								break;
							}
							processNode = nextNode;
						}						
					}
				} else {
					break;
				}								
			}						
		}
		
		if (!this.isCreatedNode(node)) {
			//go back
			long rev = node.getRevision();
			PathRevision processNode = node;
			while (true) {
				if (-- rev > 0) {
					RevisionStructure entry = this.getEntry(rev);
					if (entry != null) {
						PathRevision prevNode = this.createRevisionNode(entry, node.getPathIndex(), true);
						//not modified nodes are not included in chain
						if (prevNode.action != RevisionNodeAction.NONE) {
							processNode.setPrevious(prevNode);
							if (this.isCreatedNode(prevNode)) {
								break;
							}
							processNode = prevNode;
						}
					}					
				} else {
					break;
				}
			}
		}
	}
		
	protected boolean isCreatedNode(PathRevision node) {
		return 
			node.action == RevisionNodeAction.ADD || 
			node.action == RevisionNodeAction.COPY || 
			node.action == RevisionNodeAction.RENAME; 
	}
	
	protected boolean isDeletedNode(PathRevision node) {
		return node.action == RevisionNodeAction.DELETE;			
	}
	
	protected List<ChangedPathStructure> filterOutCopyToData(List<ChangedPathStructure> copyToList, long startRevision, long endRevision, int path) {
		/*						
		 * Filter out unrelated changed paths
		 *  		 
		 * There can be following situation:
		 * Action:		Path:									Copy from path:								Revision
		 * Added		/RevisionGraph/tags/t1					/RevisionGraph/branches/br1					7349
		 * Replacing	/RevisionGraph/tags/t1/src/Foo.java		/RevisionGraph/branches/br1/src/Foo.java	7351
		 * 
		 * In this case we need to choose more specific case and remove other case, 
		 * this is important because they have different copied from revision
		 */		
		List<ChangedPathStructure> filteredCopyToList = new ArrayList<ChangedPathStructure>();
		
		for (ChangedPathStructure copy : copyToList) {		
			long rev = copy.getCopiedFromRevision();
			if (rev >= startRevision && rev <= endRevision) {							
				
				if (this.isParentPath(copy.getCopiedFromPathIndex(), path)) {
					boolean canAdd = true;
					//if in particular revision there are several copies related to path then we select more specific copy
					if (!filteredCopyToList.isEmpty()) {
						Iterator<ChangedPathStructure> iter = filteredCopyToList.iterator();
						while (iter.hasNext()) {
							ChangedPathStructure existingChangedPath = iter.next();
							if (existingChangedPath.getRevision() == copy.getRevision()) {
								if (this.isParentPath(existingChangedPath.getPathIndex(), copy.getPathIndex()) && 
										this.isParentPath(existingChangedPath.getCopiedFromPathIndex(), copy.getCopiedFromPathIndex())) {
										iter.remove();																				
									} else if (this.isParentPath(copy.getPathIndex(), existingChangedPath.getPathIndex()) &&
											this.isParentPath(copy.getCopiedFromPathIndex(), existingChangedPath.getCopiedFromPathIndex())) {
										//ignore
										canAdd = false;
										break;
									}	
							}
						}
					}
					
					if (canAdd) {
						filteredCopyToList.add(copy);	
					}							
				}											
			}					
		}
		
		return filteredCopyToList;
	}
	
	protected void postProcessCopyToMap(Map<PathRevision, List<PathRevision>> copyToMap) {
		/*
		 * Post process result map:
		 * If there was rename in revision chain we couldn't know about it from chain.
		 * 'Rename' means that revision chain stopped to exist
		 * in 'renamed' revision, so we need to ignore all copies after rename.
		 */
		if (!copyToMap.isEmpty()) {
			long renameRevision = -1;
			for (Map.Entry<PathRevision, List<PathRevision>> entry : copyToMap.entrySet()) {
				PathRevision copyFrom = entry.getKey();				
				for (PathRevision copyTo : entry.getValue()) {
					if (copyTo.action == RevisionNodeAction.RENAME) {
						renameRevision = copyFrom.getRevision();
						break;
					}
				}				
				if (renameRevision != -1) {
					break;
				} 
			}
			if (renameRevision != -1) {
				Iterator<PathRevision> iter = copyToMap.keySet().iterator();
				while (iter.hasNext()) {
					PathRevision copyFrom = iter.next();
					if (copyFrom.getRevision() > renameRevision) {
						iter.remove();
					}
				}												
			}	
		}
	}
	
	protected Map<PathRevision, List<PathRevision>> findCopiedToNodesInRevisionChain(PathRevision node) {
		Map<PathRevision, List<PathRevision>> copyToMap = new HashMap<PathRevision, List<PathRevision>>();					
		
		//find path and revisions range for it [start - end]			
		long startRevision = node.getStartNodeInChain().getRevision();
		PathRevision endNodeInChain = node.getEndNodeInChain();
		long endRevision = this.isDeletedNode(endNodeInChain) ? endNodeInChain.getRevision() : Long.MAX_VALUE;
		
		int path = node.getPathIndex();	
		do {
						
			List<ChangedPathStructure> copyToList = this.dataContainer.getCopiedToData(path);
			List<ChangedPathStructure> filteredCopyToList = this.filterOutCopyToData(copyToList, startRevision, endRevision, node.getPathIndex());
									
			Iterator<ChangedPathStructure> iter = filteredCopyToList.iterator();
			while (iter.hasNext()) {							
				ChangedPathStructure changedPath = iter.next();
									
				/*           
		         * Example:
				 * 	'trunk' copy to 'branch'
				 * 	1. path = trunk
				 * 	2. path = trunk/src/com
				 */																												
				int copyToPath = PathStorage.UNKNOWN_INDEX;
				if (node.getPathIndex() == changedPath.getCopiedFromPathIndex()) {
					//check exact matching
					copyToPath = changedPath.getPathIndex();							
				} else {
					//copy was from path's parent																											 
					//copyToPath = changedPath.pathIndex + node.getPathIndex().substring(changedPath.copiedFromPathIndex.length());					
					int[] relativeParts = this.dataContainer.getPathStorage().makeRelative(changedPath.getCopiedFromPathIndex(), node.getPathIndex());
					copyToPath = this.dataContainer.getPathStorage().add(changedPath.getPathIndex(), relativeParts);										
				}	
				
				if (copyToPath != PathStorage.UNKNOWN_INDEX) {
					RevisionStructure rsCopyTo = this.getEntry(changedPath.getRevision());
					PathRevision copyToNode = null;
					if (rsCopyTo != null) {
						copyToNode = this.createRevisionNode(rsCopyTo, copyToPath, false);
					}
					
					if (copyToNode != null) {
						PathRevision copyFromNode = node.findNodeInChain(changedPath.getCopiedFromRevision());
						if (copyFromNode == null) {
							///revision has no modifications and so it's not in the chain
							RevisionStructure copyFromEntry = this.getEntry(changedPath.getCopiedFromRevision());								
							if (copyFromEntry != null) {									
								copyFromNode = this.createRevisionNode(copyFromEntry, node.getPathIndex(), false);
							}																
						}
						
						if (copyFromNode != null) {
							List<PathRevision> copyToNodes;
							if (copyToMap.containsKey(copyFromNode)) {
								copyToNodes = copyToMap.get(copyFromNode);
							} else {
								copyToNodes = new ArrayList<PathRevision>();
								copyToMap.put(copyFromNode, copyToNodes);
							}
							copyToNodes.add(copyToNode);
						}
					} 			
				}									
			}									
		} while ((path = this.dataContainer.getPathStorage().getParentPathIndex(path)) != PathStorage.ROOT_INDEX);
		
		this.postProcessCopyToMap(copyToMap);
		
		return copyToMap;
	}
	
	protected PathRevision findCopiedFromNode(PathRevision node) {
		/*
		 * copied from: 	branches/br1 
		 * copied from:		branches/br1/src/com
		 */			
		RevisionStructure entry = this.getEntry(node.getRevision());
		if (entry != null && entry.hasChangedPaths()) {			
			ChangedPathStructure parentPath = null;			
			for (ChangedPathStructure changedPath : entry.getChangedPaths()) {
				if (changedPath.getCopiedFromPathIndex() != PathStorage.UNKNOWN_INDEX && this.isParentPath(changedPath.getPathIndex(), node.getPathIndex())) {					
					if (parentPath != null && this.isParentPath(parentPath.getPathIndex(), changedPath.getPathIndex()) || parentPath == null) {
						parentPath = changedPath;
					}												
				}			
			}
			
			if (parentPath != null) {
				RevisionStructure copiedFromEntry = this.getEntry(parentPath.getCopiedFromRevision());
				if (copiedFromEntry != null) {
					int copiedFromPath;						
					if (parentPath.getPathIndex() == node.getPathIndex()) {
						//check exact matching
						copiedFromPath = parentPath.getCopiedFromPathIndex();
					} else {
						//check if copy was from path's parent									
						//copiedFromPath = parentPath.copiedFromPathIndex + node.getPathIndex().substring(parentPath.pathIndex.length());
						int[] relativeParts = this.dataContainer.getPathStorage().makeRelative(parentPath.getPathIndex(), node.getPathIndex());
						copiedFromPath = this.dataContainer.getPathStorage().add(parentPath.getCopiedFromPathIndex(), relativeParts);
					}
					return this.createRevisionNode(copiedFromEntry, copiedFromPath, false);	
				}
			}
		}
		return null;
	}
	
	protected RevisionStructure findStartLogEntry(long revision, int path) {		
		for (long i = revision; i > 0; i --) {
			RevisionStructure entry = this.getEntry(i);
			if (entry != null && entry.hasChangedPaths()) {			
				for (ChangedPathStructure changedPath : entry.getChangedPaths()) {						
					if (this.isParentPath(changedPath.getPathIndex(), path)) {
						if (changedPath.getAction() == SVNLogPath.ChangeType.ADDED || 
							changedPath.getCopiedFromPathIndex() != PathStorage.UNKNOWN_INDEX) {
							return entry;
						}
					}					
				}		
			}		
		}	
		return null;
	}
	
	/** 
	 * Create PathRevision node with filled action and type 
	 * 
	 * As 'rename' is complex action (copy + delete) we handle it in specific way:
	 *  If path is created during 'rename', then returned node path corresponds to passed path
	 * 	If path is deleted during 'rename', then returned node path doesn't correspond to passed path
	 * 
	 * @param isCalledFromChain This flag is used if there's Replace action, depending on how from where
	 * 							this method is called resulted action varies	 
	 */
	protected PathRevision createRevisionNode(RevisionStructure entry, int pathIndex, boolean isCalledFromChain) {		
		//path can be changed during rename
		int nodePath = pathIndex;
		RevisionNodeAction action = PathRevision.RevisionNodeAction.NONE;	
				
		if (entry.hasChangedPaths()) {
			ChangedPathStructure parentPath = null;
			ChangedPathStructure childPath = null;			
			for (ChangedPathStructure changedPath : entry.getChangedPaths()) {				
				if (this.isParentPath(changedPath.getPathIndex(), pathIndex)) {
					if (parentPath != null && this.isParentPath(parentPath.getPathIndex(), changedPath.getPathIndex()) || parentPath == null) {
						parentPath = changedPath;
					}					
				}				
				if (this.isParentPath(pathIndex, changedPath.getPathIndex())) {
					if (childPath != null && this.isParentPath(changedPath.getPathIndex(), childPath.getPathIndex()) || childPath == null) {
						childPath = changedPath;
					}
				}
			}
			
			if (parentPath != null) {
				/*
				 * At first, check Replace as this is a special case. We can have following cases with Replace:
				 * 												  Copy From Path:
				 * Replacing /subversion/branches/tree-conflicts  /subversion/branches/tree-conflicts 		872329
				 * Replacing /RevisionGraph/tags/t1/src/Foo.java  /RevisionGraph/branches/br1/src/Foo.java	7351
				 * 
				 * In this case if depending on where we call this method, node's action will be 
				 * either Delete or Copy
				 */
				if (parentPath.getAction() == SVNLogPath.ChangeType.REPLACED) {					
					//has copied from
					if (parentPath.getCopiedFromPathIndex() != PathStorage.UNKNOWN_INDEX) {						
						action = isCalledFromChain ? RevisionNodeAction.DELETE : RevisionNodeAction.COPY;
					} else {
						action = RevisionNodeAction.DELETE;
					}															
				} else {
					//as checkRenameAction is complex, it should be verified first
					ChangedPathStructure renamedLogPath = this.checkRenameAction(pathIndex, parentPath, entry);				
					if (renamedLogPath != null) {
						action = RevisionNodeAction.RENAME;
						
						if (parentPath.getAction() == SVNLogPath.ChangeType.DELETED) {
							nodePath = renamedLogPath.getPathIndex();
							if (/*pathIndex.startsWith(parentPath.pathIndex) && pathIndex.length() > parentPath.pathIndex.length()*/
									this.dataContainer.getPathStorage().isParentIndex(parentPath.getPathIndex(), pathIndex)) {
								//nodePath += pathIndex.substring(parentPath.pathIndex.length());
								int[] relativeParts = this.dataContainer.getPathStorage().makeRelative(parentPath.getPathIndex(), pathIndex);
								nodePath = this.dataContainer.getPathStorage().add(nodePath, relativeParts);	
							}
						} else {
							nodePath = pathIndex;
						}
					} else if (this.isAddOnlyAction(pathIndex, parentPath)) {
						action = RevisionNodeAction.ADD;					
					} else if (this.isCopyAction(pathIndex, parentPath)) {
						action = RevisionNodeAction.COPY;					
					} else if (this.isDeleteAction(pathIndex, parentPath)) {
						action = RevisionNodeAction.DELETE;				
					}			
				}
			} 
			if (action == PathRevision.RevisionNodeAction.NONE && childPath != null) {
				if (this.isModifyAction(pathIndex, childPath)) {
					action = RevisionNodeAction.MODIFY;
				}
			}			
		}
		
		ReviosionNodeType type = ReviosionNodeType.OTHER;
		if (this.resource.getRepositoryLocation().isStructureEnabled() && (action == RevisionNodeAction.ADD || action == RevisionNodeAction.COPY)) {					
			IPath pPath = new Path(this.dataContainer.getPathStorage().getPath(nodePath));
			String[] segments = pPath.segments();
			for (int i = 0; i < segments.length; i ++) {
				if (this.resource.getRepositoryLocation().getTrunkLocation().equals(segments[i])) {
					type = ReviosionNodeType.TRUNK;
					break;
				} else if (this.resource.getRepositoryLocation().getBranchesLocation().equals(segments[i])) {
					type = ReviosionNodeType.BRANCH;
					break;
				} else if (this.resource.getRepositoryLocation().getTagsLocation().equals(segments[i])) {
					type = ReviosionNodeType.TAG;
					break;
				} 
			}
		}
		
		PathRevision node = new PathRevision(entry, nodePath, action, type);
		node.setValidator(this.pathRevisionValidator);
		return node;
	}
	
	/*
	 * It doesn't check whether this rename or delete,
	 * so if you need to differ them, call rename action at first
	 */
	protected boolean isDeleteAction(int path, ChangedPathStructure parentChangedPath) {
		return parentChangedPath.getAction() == SVNLogPath.ChangeType.DELETED;
	}
	
	protected boolean isAddOnlyAction(int path, ChangedPathStructure parentChangedPath) {
		return parentChangedPath.getAction() == SVNLogPath.ChangeType.ADDED && parentChangedPath.getCopiedFromPathIndex() == PathStorage.UNKNOWN_INDEX;
	}		

	protected boolean isCopyAction(int path, ChangedPathStructure parentChangedPath) {
		return parentChangedPath.getCopiedFromPathIndex() != PathStorage.UNKNOWN_INDEX && parentChangedPath.getAction() == SVNLogPath.ChangeType.ADDED;					
	}
	
	/*
	 * If there's 'rename' return SVNLogPath which corresponds to 'Added' action,
	 * if there's no 'rename' return null 
	 */
	protected ChangedPathStructure checkRenameAction(int path, ChangedPathStructure parentChangedPath, RevisionStructure parentEntry) {
		/*						Copied from:
		 * Deleted	path		
		 * Added	path-2		path
		 */
		if (parentChangedPath.getAction() == SVNLogPath.ChangeType.DELETED) {
			for (ChangedPathStructure chPath : parentEntry.getChangedPaths()) {
				if (chPath.getAction() == SVNLogPath.ChangeType.ADDED && this.isParentPath(chPath.getCopiedFromPathIndex(), path)) {
					return chPath;
				}
			}
		}
		/*						Copied from:
		 * Added	path		path-2
		 * Deleted	path-2
		 */
		if (this.isCopyAction(path, parentChangedPath)) {
			for (ChangedPathStructure chPath : parentEntry.getChangedPaths()) {
				if (chPath.getAction() == SVNLogPath.ChangeType.DELETED && chPath.getPathIndex() == parentChangedPath.getCopiedFromPathIndex()) {
					return parentChangedPath;
				}
			}
		}
		return null;
	}
	
	protected boolean isModifyAction(int path, ChangedPathStructure childChangedPath) {
		return childChangedPath.getPathIndex() == path ? (childChangedPath.getAction() == SVNLogPath.ChangeType.MODIFIED) : true; 
	}
	
	protected boolean isParentPath(int parentPathIndex, int childPathIndex) {
		return this.dataContainer.getPathStorage().isParentIndex(parentPathIndex, childPathIndex);		
	}
	
	/*
	 * Note that entry can be null, e.g. because of cache repository, cache corrupted
	 */
	protected RevisionStructure getEntry(long revision) {
		return this.dataContainer.getRevision(revision);
	}
	
	/**
	 * Return start node of revision chain for passed resource
	 * 
	 * @return
	 */
	public PathRevision getModel() {
		return this.resultNode;
	}
	
	public IRepositoryResource getResource() {
		return this.resource;
	}
	
	/*
	 * For DEBUG
	 */
	protected String getPath(int pathIndex) {
		return this.dataContainer.getPathStorage().getPath(pathIndex);
	}
}
