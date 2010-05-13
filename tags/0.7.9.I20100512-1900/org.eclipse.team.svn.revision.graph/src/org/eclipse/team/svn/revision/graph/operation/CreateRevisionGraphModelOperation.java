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
import org.eclipse.team.svn.core.connector.SVNLogPath.ChangeType;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.ActivityCancelledException;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.revision.graph.PathRevision;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphMessages;
import org.eclipse.team.svn.revision.graph.PathRevision.ReviosionNodeType;
import org.eclipse.team.svn.revision.graph.PathRevision.RevisionNodeAction;
import org.eclipse.team.svn.revision.graph.cache.CacheChangedPath;
import org.eclipse.team.svn.revision.graph.cache.CacheChangedPathWithRevision;
import org.eclipse.team.svn.revision.graph.cache.CacheRevision;
import org.eclipse.team.svn.revision.graph.cache.PathStorage;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCache;
import org.eclipse.team.svn.revision.graph.cache.TimeMeasure;

/**
 * Create revision graph model
 * 
 * @author Igor Burilo
 */
public class CreateRevisionGraphModelOperation extends AbstractActionOperation {	
	
	protected IRepositoryResource resource;	
	protected IRepositoryCacheProvider cacheProvider;
	protected RepositoryCache repositoryCache;
	
	protected PathRevisionConnectionsValidator pathRevisionValidator;
	
	protected PathRevision resultNode; 
	
	public CreateRevisionGraphModelOperation(IRepositoryResource resource, IRepositoryCacheProvider cacheProvider) {
		super("Operation_CreateRevisionGraphModel", SVNRevisionGraphMessages.class); //$NON-NLS-1$
		this.resource = resource;		
		this.cacheProvider = cacheProvider;
	}
	
	@Override
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		this.repositoryCache = this.cacheProvider.getRepositoryCache();	
		
		this.pathRevisionValidator = new PathRevisionConnectionsValidator(this.repositoryCache);
		
		TimeMeasure processMeasure = new TimeMeasure("Create model"); //$NON-NLS-1$
		
		String url = this.resource.getUrl();
		String rootUrl = this.resource.getRepositoryLocation().getRepositoryRootUrl();	
	
		SVNRevision svnRevision = this.resource.getSelectedRevision();
		String path = url.substring(rootUrl.length());
					
		int pathIndex = this.repositoryCache.getPathStorage().add(path);
				
		long revision;		
		if (svnRevision.getKind() == SVNRevision.Kind.NUMBER) {
			revision = ((SVNRevision.Number) svnRevision).getNumber();
		} else if (svnRevision.getKind() == SVNRevision.Kind.HEAD) {			
			//revision = this.entries[this.entries.length - 1].revision;				
			revision = this.repositoryCache.getLastProcessedRevision();
		} else {
			throw new Exception("Unexpected revision kind: " + svnRevision); //$NON-NLS-1$
		}								
		
		CacheRevision entry = this.findStartLogEntry(revision, pathIndex);
		if (entry != null) {
			this.resultNode = this.createRevisionNode(entry, pathIndex, false);	
									
			this.process(this.resultNode, monitor);						
		}									
				
		processMeasure.end();
	}
	
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
				if (++ rev <= this.repositoryCache.getLastProcessedRevision()) {
					CacheRevision entry = this.getEntry(rev);
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
					CacheRevision entry = this.getEntry(rev);
					if (entry != null) {
						PathRevision prevNode = this.createRevisionNode(entry, node.getPathIndex(), false);
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
	
	protected void filterOutCopyToData(long startRevision, long endRevision, int path, 
		List<CacheChangedPathWithRevision> candidateCopyToList, List<CacheChangedPathWithRevision> filteredCopyToList) {
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
		
		for (CacheChangedPathWithRevision candidateCopy : candidateCopyToList) {		
			long rev = candidateCopy.getCopiedFromRevision();
			if (rev >= startRevision && rev <= endRevision) {							
				
				if (this.isParentPath(candidateCopy.getCopiedFromPathIndex(), path)) {
					boolean canAdd = true;
					//if in particular revision there are several copies related to path then we select more specific copy
					if (!filteredCopyToList.isEmpty()) {
						Iterator<CacheChangedPathWithRevision> iter = filteredCopyToList.iterator();
						while (iter.hasNext()) {
							CacheChangedPathWithRevision existingChangedPath = iter.next();
							if (existingChangedPath.getRevision() == candidateCopy.getRevision()) {
								if (this.isParentPath(existingChangedPath.getPathIndex(), candidateCopy.getPathIndex()) && 
										this.isParentPath(existingChangedPath.getCopiedFromPathIndex(), candidateCopy.getCopiedFromPathIndex())) {
									iter.remove();
								} else if (this.isParentPath(candidateCopy.getPathIndex(), existingChangedPath.getPathIndex()) &&
										this.isParentPath(candidateCopy.getCopiedFromPathIndex(), existingChangedPath.getCopiedFromPathIndex())) {
									//ignore
									canAdd = false;
									break;
								}	
							}
						}
					}
					
					if (canAdd) {
						filteredCopyToList.add(candidateCopy);	
					}							
				}											
			}					
		}
	}
	
	protected void postProcessCopyToMap(Map<PathRevision, List<PathRevision>> copyToMap) {
		/*
		 * Post process result map:
		 * If there was rename in revision chain we couldn't know about it from chain.
		 * 'Rename' means that revision chain stopped to exist
		 * in 'renamed' revision, so we need to ignore all copies after rename.
		 */
		if (!copyToMap.isEmpty()) {
			long renameRevision = Long.MAX_VALUE;
			for (Map.Entry<PathRevision, List<PathRevision>> entry : copyToMap.entrySet()) {
				PathRevision copyFrom = entry.getKey();				
				for (PathRevision copyTo : entry.getValue()) {
					if (copyTo.action == RevisionNodeAction.RENAME && renameRevision > copyFrom.getRevision()) {
						renameRevision = copyFrom.getRevision();
					}
				}
			}
			if (renameRevision != Long.MAX_VALUE) {
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
				
		//traverse path and its parents in order to get all copied to data		
		List<CacheChangedPathWithRevision> filteredCopyToList = new ArrayList<CacheChangedPathWithRevision>();		
		for (int path = node.getPathIndex(); path != PathStorage.ROOT_INDEX; path = this.repositoryCache.getPathStorage().getParentPathIndex(path)) {
			this.filterOutCopyToData(startRevision, endRevision, node.getPathIndex(), this.repositoryCache.getCopiedToData(path), filteredCopyToList);
		}
		
		Iterator<CacheChangedPathWithRevision> iter = filteredCopyToList.iterator();
		while (iter.hasNext()) {
			CacheChangedPathWithRevision changedPath = iter.next();

			/*
	         * Example:
			 * 	'trunk' copy to 'branch'
			 * 	1. path = trunk
			 * 	2. path = trunk/src/com
			 */
			int copyToPath = RepositoryCache.UNKNOWN_INDEX;
			if (node.getPathIndex() == changedPath.getCopiedFromPathIndex()) {
				//check exact matching
				copyToPath = changedPath.getPathIndex();
			} else {
				//copy was from path's parent																											 
				//copyToPath = changedPath.pathIndex + node.getPathIndex().substring(changedPath.copiedFromPathIndex.length());					
				int[] relativeParts = this.repositoryCache.getPathStorage().makeRelative(changedPath.getCopiedFromPathIndex(), node.getPathIndex());
				copyToPath = this.repositoryCache.getPathStorage().add(changedPath.getPathIndex(), relativeParts);
			}	
			
			if (copyToPath != RepositoryCache.UNKNOWN_INDEX) {
				CacheRevision rsCopyTo = this.getEntry(changedPath.getRevision());
				PathRevision copyToNode = null;
				if (rsCopyTo != null) {
					copyToNode = this.createRevisionNode(rsCopyTo, copyToPath, false);
				}
				
				if (copyToNode != null) {
					PathRevision copyFromNode = node.findNodeInChain(changedPath.getCopiedFromRevision());
					if (copyFromNode == null) {
						///revision has no modifications and so it's not in the chain
						CacheRevision copyFromEntry = this.getEntry(changedPath.getCopiedFromRevision());
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
		
		
		this.postProcessCopyToMap(copyToMap);
		
		return copyToMap;
	}
	
	protected PathRevision findCopiedFromNode(PathRevision node) {
		/*
		 * copied from: 	branches/br1 
		 * copied from:		branches/br1/src/com
		 */			
		CacheRevision entry = this.getEntry(node.getRevision());
		if (entry != null && entry.hasChangedPaths()) {			
			CacheChangedPath parentPath = null;			
			for (CacheChangedPath changedPath : entry.getChangedPaths()) {
				if (changedPath.getCopiedFromPathIndex() != RepositoryCache.UNKNOWN_INDEX && this.isParentPath(changedPath.getPathIndex(), node.getPathIndex())) {					
					if (parentPath != null && this.isParentPath(parentPath.getPathIndex(), changedPath.getPathIndex()) || parentPath == null) {
						parentPath = changedPath;
					}												
				}			
			}
			
			if (parentPath != null) {
				CacheRevision copiedFromEntry = this.getEntry(parentPath.getCopiedFromRevision());
				if (copiedFromEntry != null) {
					int copiedFromPath;						
					if (parentPath.getPathIndex() == node.getPathIndex()) {
						//check exact matching
						copiedFromPath = parentPath.getCopiedFromPathIndex();
					} else {
						//check if copy was from path's parent									
						//copiedFromPath = parentPath.copiedFromPathIndex + node.getPathIndex().substring(parentPath.pathIndex.length());
						int[] relativeParts = this.repositoryCache.getPathStorage().makeRelative(parentPath.getPathIndex(), node.getPathIndex());
						copiedFromPath = this.repositoryCache.getPathStorage().add(parentPath.getCopiedFromPathIndex(), relativeParts);
					}
					return this.createRevisionNode(copiedFromEntry, copiedFromPath, false);	
				}
			}
		}
		return null;
	}
	
	protected CacheRevision findStartLogEntry(long revision, int path) {
		//go bottom
		for (long i = revision; i > 0; i --) {
			CacheRevision entry = this.getEntry(i);
			if (entry != null && entry.hasChangedPaths()) {			
				for (CacheChangedPath changedPath : entry.getChangedPaths()) {						
					if (this.isParentPath(changedPath.getPathIndex(), path)) {
						if (changedPath.getAction() == SVNLogPath.ChangeType.ADDED || 
							changedPath.getCopiedFromPathIndex() != RepositoryCache.UNKNOWN_INDEX) {
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
	 * @param isChooseDeleteActionInReplace
	 * 			if Replace action contains copied from fields, we can't exactly detect resulted node action:
	 * 			it can be either Delete or Copy/Rename. This flag indicates which action to use.
	 * 			How to detect which value to pass to this flag:
	 * 			- if we traverse resource history starting from its first revision, 
	 * 			i.e. traverse history from bottom to top, then we consider that resource as deleted. 		
	 *          - otherwise, i.e. traverse history from top to bottom, then we consider that resource as copied. 	
	 *          
	 * 			Examples for Replace action with copies:      
	 * 			- Replacing /subversion/branches/tree-conflicts  /subversion/branches/tree-conflicts 		872329
	 * 
	 *          - Replacing /RevisionGraph/tags/t1/src/Foo.java  /RevisionGraph/branches/br1/src/Foo.java	7351   
	 *          
	 *          - Revision: 7520    
     *			  Deleted     /ProjectsData/Subversive/WorkingProject/src/com/e.txt
     *  		  Replaced    /ProjectsData/Subversive/WorkingProject/src/com/s.txt   /ProjectsData/Subversive/WorkingProject/src/com/e.txt@7519
	 */
	protected PathRevision createRevisionNode(CacheRevision entry, int pathIndex, boolean isChooseDeleteActionInReplace) {		
		//path can be changed during rename
		int nodePath = pathIndex;
		RevisionNodeAction action = PathRevision.RevisionNodeAction.NONE;	
				
		if (entry.hasChangedPaths()) {
			CacheChangedPath parentPath = null;
			CacheChangedPath childPath = null;			
			for (CacheChangedPath changedPath : entry.getChangedPaths()) {
								
				/*
				 * If we have several changed paths corresponding to passed path
				 * then select more appropriate one.
				 * 
				 * Example:
				 * Added       /tags/tg1/C++           ...(copied from)
				 * Modified    /tags/tg1/C++/hgh.h
				 * 
				 * Passed path: /tags/tg1/C++/hgh.h
				 * 
				 * Then as a parent we need to select '/tags/tg1/C++' but not '/tags/tg1/C++/hgh.h'  
				 */
				if (this.isParentPath(changedPath.getPathIndex(), pathIndex)) {
					if (parentPath != null) {
						CacheChangedPath parent;
						CacheChangedPath child;
						if (this.isParentPath(parentPath.getPathIndex(), changedPath.getPathIndex())) {
							parent = parentPath;
							child = changedPath;
						} else {
							parent = changedPath;
							child = parentPath;
						}						
						
						if (child.getAction() != ChangeType.MODIFIED) {
							parentPath = child;
						} else {
							parentPath = parent.getAction() != ChangeType.MODIFIED ? parent : child;
						}
					} else {
						parentPath = changedPath;
					}
				}
				
				/*
				 * it seems it doesn't matter which changed path to select if we have several
				 * child paths
				 */				
				if (this.isParentPath(pathIndex, changedPath.getPathIndex())) {
					if (childPath != null && this.isParentPath(changedPath.getPathIndex(), childPath.getPathIndex()) || childPath == null) {
						childPath = changedPath;
					}
				}
			}
			
			if (parentPath != null) {
				//as checkRenameAction is complex, it should be verified first
				CacheChangedPath renamedLogPath = this.checkRenameAction(parentPath, entry);
				if (renamedLogPath != null) {
					action = RevisionNodeAction.RENAME;
					
					if (parentPath.getAction() == SVNLogPath.ChangeType.DELETED) {
						nodePath = renamedLogPath.getPathIndex();
						if (/*pathIndex.startsWith(parentPath.pathIndex) && pathIndex.length() > parentPath.pathIndex.length()*/
								this.repositoryCache.getPathStorage().isParentIndex(parentPath.getPathIndex(), pathIndex)) {
							//nodePath += pathIndex.substring(parentPath.pathIndex.length());
							int[] relativeParts = this.repositoryCache.getPathStorage().makeRelative(parentPath.getPathIndex(), pathIndex);
							nodePath = this.repositoryCache.getPathStorage().add(nodePath, relativeParts);
						}
					} else {
						nodePath = pathIndex;
					}
				} else if (this.isAddOnlyAction(parentPath)) {
					action = RevisionNodeAction.ADD;
				} else if (this.isCopyAction(parentPath)) {
					action = RevisionNodeAction.COPY;
				} else if (this.isDeleteAction(parentPath)) {
					action = RevisionNodeAction.DELETE;	
				}	
				
				//check if there was Replace
				if (this.isCopyAction(parentPath) && parentPath.getAction() == SVNLogPath.ChangeType.REPLACED && isChooseDeleteActionInReplace) {
					action = RevisionNodeAction.DELETE;
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
			IPath pPath = new Path(this.repositoryCache.getPathStorage().getPath(nodePath));
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
	protected boolean isDeleteAction(CacheChangedPath parentChangedPath) {
		return parentChangedPath.getAction() == SVNLogPath.ChangeType.DELETED || parentChangedPath.getAction() == SVNLogPath.ChangeType.REPLACED;
	}
	
	protected boolean isAddOnlyAction(CacheChangedPath parentChangedPath) {
		return parentChangedPath.getAction() == SVNLogPath.ChangeType.ADDED && parentChangedPath.getCopiedFromPathIndex() == RepositoryCache.UNKNOWN_INDEX;
	}		

	protected boolean isCopyAction(CacheChangedPath parentChangedPath) {
		return parentChangedPath.getCopiedFromPathIndex() != RepositoryCache.UNKNOWN_INDEX;					
	}
	
	/*
	 * If there's 'rename' return SVNLogPath which corresponds to 'Added' action,
	 * if there's no 'rename' return null 
	 */
	protected CacheChangedPath checkRenameAction(CacheChangedPath parentChangedPath, CacheRevision parentEntry) {
		/*						Copied from:
		 * Deleted	path		
		 * Added	path-2		path
		 */
		if (parentChangedPath.getAction() == SVNLogPath.ChangeType.DELETED) {
			for (CacheChangedPath chPath : parentEntry.getChangedPaths()) {
				if (this.isCopyAction(chPath) && chPath.getCopiedFromPathIndex() == parentChangedPath.getPathIndex()) {
					return chPath;
				}
			}
		}
		
		/*						Copied from:
		 * Added	path		path-2
		 * Deleted	path-2
		 */
		if (this.isCopyAction(parentChangedPath)) {
			for (CacheChangedPath chPath : parentEntry.getChangedPaths()) {
				if (chPath.getAction() == SVNLogPath.ChangeType.DELETED && chPath.getPathIndex() == parentChangedPath.getCopiedFromPathIndex()) {
					return parentChangedPath;
				}
			}
		}
		return null;
	}
	
	protected boolean isModifyAction(int path, CacheChangedPath childChangedPath) {
		return childChangedPath.getPathIndex() == path ? (childChangedPath.getAction() == SVNLogPath.ChangeType.MODIFIED) : true; 
	}
	
	protected boolean isParentPath(int parentPathIndex, int childPathIndex) {
		return this.repositoryCache.getPathStorage().isParentIndex(parentPathIndex, childPathIndex);		
	}
	
	/*
	 * Note that entry can be null, e.g. because of cache repository, cache corrupted
	 */
	protected CacheRevision getEntry(long revision) {
		return this.repositoryCache.getRevision(revision);
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
	
	public RepositoryCache getRepositoryCache() {
		return this.repositoryCache;
	}
	
	/*
	 * For DEBUG
	 */
	protected String getPath(int pathIndex) {
		return this.repositoryCache.getPathStorage().getPath(pathIndex);
	}
}
