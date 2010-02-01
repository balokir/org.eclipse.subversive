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
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNLogPath;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.revision.graph.operation.PathRevision.ReviosionNodeType;
import org.eclipse.team.svn.revision.graph.operation.PathRevision.RevisionNodeAction;

/**
 * TODO
 * Merge with CreateRevisionGraphModelOperation
 * 
 * @author Igor Burilo
 */
public class CreateRevisionGraphModelOperationHelper {

	protected SVNLogEntry[] rawEntries;
	protected SVNRevision svnRevision;
	protected String initialPath;
	
	/*
	 * Entries indexes correspond to revision number,
	 * if there's no entry with particular revision number then entry is null
	 * 
	 * Entries are sorted is ascending order  
	 */
	protected SVNLogEntry[] entries;
	
	protected PathRevision resultNode;
	
	protected IRepositoryResource resource;
	
	public CreateRevisionGraphModelOperationHelper(SVNLogEntry[] entries, String path, SVNRevision revision, IRepositoryResource resource) {
		this.rawEntries = entries;
		this.initialPath = path;
		this.svnRevision = revision;
		this.resource = resource;
		
		//sort
		long maxRevision = 0;
		for (SVNLogEntry entry : rawEntries) {
			if (entry.revision > maxRevision) {
				maxRevision = entry.revision; 
			}
		}								
		this.entries = new SVNLogEntry[(int)maxRevision + 1];
		for (SVNLogEntry entry : rawEntries) {
			this.entries[(int)entry.revision] = entry;
		}
	}
	
	public void runImpl(IProgressMonitor monitor) throws Exception {		
		long revision;		
		if (svnRevision.getKind() == SVNRevision.Kind.NUMBER) {
			revision = ((SVNRevision.Number) svnRevision).getNumber();
		} else if (svnRevision.getKind() == SVNRevision.Kind.HEAD) {			
			revision = this.entries[this.entries.length - 1].revision;
		} else {
			throw new Exception("Unexpected revision kind: " + svnRevision);
		}
				
		SVNLogEntry entry = this.findStartLogEntry(revision, this.initialPath);
		if (entry != null) {
			this.resultNode = this.createRevisionNode(entry, this.initialPath);	
			this.process(this.resultNode);						
		}		
	}
	
	/*
	 * Note that 'Replacing' is used as 'copied' if 'Replacing' contains 'copied from path'
	 */
	protected void process(PathRevision startNode) {
		Queue<PathRevision> nodesQueue = new LinkedList<PathRevision>();
		nodesQueue.offer(startNode);		
		PathRevision node;
		while ((node = nodesQueue.poll()) != null) {
			this.createRevisionsChainForPath(node);									
			
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
				if (++ rev < this.entries.length) {
					SVNLogEntry entry = this.getEntry(rev);
					if (entry != null) {
						PathRevision nextNode = this.createRevisionNode(entry, node.getPath());
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
					SVNLogEntry entry = this.getEntry(rev);
					if (entry != null) {
						PathRevision prevNode = this.createRevisionNode(entry, node.getPath());
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
	
	protected Map<PathRevision, List<PathRevision>> findCopiedToNodesInRevisionChain(PathRevision node) {		
		//find path and revisions range for it [start - end]
		String path = node.getPath();
		long startRevision = node.getStartNodeInChain().getRevision();
		PathRevision endNodeInChain = node.getEndNodeInChain();
		long endRevision = this.isDeletedNode(endNodeInChain) ? endNodeInChain.getRevision() : Long.MAX_VALUE; 
		
		Map<PathRevision, List<PathRevision>> copyToMap = new HashMap<PathRevision, List<PathRevision>>();
		
		//look from 'start' to HEAD in entries, copied from path or parent
		for (long i = startRevision; i < this.entries.length; i ++) {
			SVNLogEntry entry = this.getEntry(i);
			if (entry != null && entry.changedPaths != null) {
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
				List<SVNLogPath> changedPaths = new ArrayList<SVNLogPath>();
				for (SVNLogPath changedPath : entry.changedPaths) {		
					if ((changedPath.action == SVNLogPath.ChangeType.ADDED || changedPath.action == SVNLogPath.ChangeType.REPLACED) && 
							changedPath.copiedFromPath != null &&
							changedPath.copiedFromRevision >= startRevision && changedPath.copiedFromRevision <= endRevision) {
						
						if (this.isParentPath(changedPath.copiedFromPath, path)) {
							boolean canAdd = true;
							if (!changedPaths.isEmpty()) {
								Iterator<SVNLogPath> iter = changedPaths.iterator();
								while (iter.hasNext()) {
									SVNLogPath existingChangedPath = iter.next();
									if (this.isParentPath(existingChangedPath.path, changedPath.path) && 
										this.isParentPath(existingChangedPath.copiedFromPath, changedPath.copiedFromPath)) {
										iter.remove();																				
									} else if (this.isParentPath(changedPath.path, existingChangedPath.path) &&
											this.isParentPath(changedPath.copiedFromPath, existingChangedPath.copiedFromPath)) {
										//ignore
										canAdd = false;
										break;
									}
								}
							}
							
							if (canAdd) {
								changedPaths.add(changedPath);	
							}							
						}											
					}																								
				}
								
				for (SVNLogPath changedPath : changedPaths) {		
					/*           
			         * Example:
					 * 	'trunk' copy to 'branch'
					 * 	1. path = trunk
					 * 	2. path = trunk/src/com
					 */																												
					String copyToPath = null;
					if (path.equals(changedPath.copiedFromPath)) {
						//check exact matching
						copyToPath = changedPath.path;							
					} else {
						//copy was from path's parent																											 
						copyToPath = changedPath.path + path.substring(changedPath.copiedFromPath.length()); 
					}
					
					if (copyToPath != null) {
						PathRevision copyToNode = this.createRevisionNode(entry, copyToPath);							
						PathRevision copyFromNode = node.findNodeInChain(changedPath.copiedFromRevision);
						if (copyFromNode == null) {
							///revision has no modifications and so it's not in the chain
							SVNLogEntry copyFromEntry = this.getEntry(changedPath.copiedFromRevision);								
							if (copyFromEntry != null) {									
								copyFromNode = this.createRevisionNode(copyFromEntry, path);
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
		}				
		return copyToMap;
	}
	
	protected PathRevision findCopiedFromNode(PathRevision node) {
		/*
		 * copied from: 	branches/br1 
		 * copied from:		branches/br1/src/com
		 */			
		SVNLogEntry entry = this.getEntry(node.getRevision());
		if (entry != null && entry.changedPaths != null) {			
			SVNLogPath parentPath = null;			
			for (SVNLogPath changedPath : entry.changedPaths) {
				if (changedPath.copiedFromPath != null && this.isParentPath(changedPath.path, node.getPath())) {					
					if (parentPath != null && this.isParentPath(parentPath.path, changedPath.path) || parentPath == null) {
						parentPath = changedPath;
					}												
				}			
			}
			
			if (parentPath != null) {
				SVNLogEntry copiedFromEntry = this.getEntry(parentPath.copiedFromRevision);
				if (copiedFromEntry != null) {
					String copiedFromPath;						
					if (parentPath.path.equals(node.getPath())) {
						//check exact matching
						copiedFromPath = parentPath.copiedFromPath;
					} else {
						//check if copy was from path's parent						
						copiedFromPath = parentPath.copiedFromPath + node.getPath().substring(parentPath.path.length());
					}
					return this.createRevisionNode(copiedFromEntry, copiedFromPath);	
				}
			}
		}
		return null;
	}
	
	protected SVNLogEntry findStartLogEntry(long revision, String path) {		
		for (long i = revision; i > 0; i --) {
			SVNLogEntry entry = this.getEntry(i);
			if (entry != null && entry.changedPaths != null) {			
				for (SVNLogPath changedPath : entry.changedPaths) {						
					if (this.isParentPath(changedPath.path, path)) {
						if (changedPath.action == SVNLogPath.ChangeType.ADDED || 
							changedPath.action == SVNLogPath.ChangeType.REPLACED && changedPath.copiedFromPath != null) {
							return entry;
						}
					}					
				}		
			}		
		}	
		return null;
	}
	
	/*
	 * As 'rename' is complex action (copy + delete) we handle it in specific way:
	 *  If path is created during 'rename', then returned node path corresponds to passed path
	 * 	If path is deleted during 'rename', then returned node path doesn't correspond to passed path 		 
	 */
	protected PathRevision createRevisionNode(SVNLogEntry entry, String path) {			
		//path can be changed during rename
		String nodePath = path;
		RevisionNodeAction action = PathRevision.RevisionNodeAction.NONE;	
				
		if (entry.changedPaths != null) {
			SVNLogPath parentPath = null;
			SVNLogPath childPath = null;
			IPath pCurrentPath = new Path(path);
			for (SVNLogPath changedPath : entry.changedPaths) {
				IPath pChangedPath = new Path(changedPath.path);
				if (this.isParentPath(pChangedPath, pCurrentPath)) {
					if (parentPath != null && this.isParentPath(new Path(parentPath.path), pChangedPath) || parentPath == null) {
						parentPath = changedPath;
					}					
				}				
				if (this.isParentPath(pCurrentPath, pChangedPath)) {
					if (childPath != null && this.isParentPath(pChangedPath, new Path(childPath.path)) || childPath == null) {
						childPath = changedPath;
					}
				}
			}
			
			if (parentPath != null) {				
				//as checkRenameAction is complex, it should be verified first
				SVNLogPath renamedLogPath = this.checkRenameAction(path, parentPath, entry);				
				if (renamedLogPath != null) {
					action = RevisionNodeAction.RENAME;
					
					if (parentPath.action == SVNLogPath.ChangeType.DELETED) {
						nodePath = renamedLogPath.path;
						if (path.startsWith(parentPath.path) && path.length() > parentPath.path.length()) {
							nodePath += path.substring(parentPath.path.length());
						}
					} else {
						nodePath = path;
					}
				} else if (this.isAddOnlyAction(path, parentPath)) {
					action = RevisionNodeAction.ADD;					
				} else if (this.isCopyAction(path, parentPath)) {
					action = RevisionNodeAction.COPY;					
				} else if (this.isDeleteAction(path, parentPath)) {
					action = RevisionNodeAction.DELETE;				
				}		
			} 
			if (action == PathRevision.RevisionNodeAction.NONE && childPath != null) {
				if (this.isModifyAction(path, childPath)) {
					action = RevisionNodeAction.MODIFY;
				}
			}			
		}
		
		ReviosionNodeType type = ReviosionNodeType.OTHER;
		if (/*TODO remove this.resource != null after merging with operation */ this.resource != null && 
			this.resource.getRepositoryLocation().isStructureEnabled() && (action == RevisionNodeAction.ADD || action == RevisionNodeAction.COPY)) {
			IPath pPath = new Path(nodePath);
			String[] segments = pPath.segments();
			for (int i = segments.length - 1; i >= 0; i --) {
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
		
		PathRevision node = new PathRevision(entry.revision, nodePath, entry.date, entry.author, entry.message, entry.changedPaths, action, type);
		return node;
	}
	
	/*
	 * It doesn't check whether this rename or delete,
	 * so if you need to differ them, call rename action at first
	 */
	protected boolean isDeleteAction(String path, SVNLogPath parentChangedPath) {
		return 
			parentChangedPath.action == SVNLogPath.ChangeType.REPLACED && parentChangedPath.copiedFromPath == null ||
			parentChangedPath.action == SVNLogPath.ChangeType.DELETED;
	}
	
	protected boolean isAddOnlyAction(String path, SVNLogPath parentChangedPath) {
		return parentChangedPath.action == SVNLogPath.ChangeType.ADDED && parentChangedPath.copiedFromPath == null;
	}		

	protected boolean isCopyAction(String path, SVNLogPath parentChangedPath) {
		return parentChangedPath.copiedFromPath != null &&
			(parentChangedPath.action == SVNLogPath.ChangeType.ADDED || parentChangedPath.action == SVNLogPath.ChangeType.REPLACED);					
	}
	
	/*
	 * If there's 'rename' return SVNLogPath which corresponds to 'Added' action,
	 * if there's no 'rename' return null 
	 */
	protected SVNLogPath checkRenameAction(String path, SVNLogPath parentChangedPath, SVNLogEntry parentEntry) {
		/*						Copied from:
		 * Deleted	path		
		 * Added	path-2		path
		 */
		if (parentChangedPath.action == SVNLogPath.ChangeType.DELETED) {
			for (SVNLogPath chPath : parentEntry.changedPaths) {
				if (chPath.action == SVNLogPath.ChangeType.ADDED && this.isParentPath(chPath.copiedFromPath, path)) {
					return chPath;
				}
			}
		}
		/*						Copied from:
		 * Added	path		path-2
		 * Deleted	path-2
		 */
		if (this.isCopyAction(path, parentChangedPath)) {
			for (SVNLogPath chPath : parentEntry.changedPaths) {
				if (chPath.action == SVNLogPath.ChangeType.DELETED && chPath.path.equals(parentChangedPath.copiedFromPath)) {
					return parentChangedPath;
				}
			}
		}
		return null;
	}
	
	protected boolean isModifyAction(String path, SVNLogPath childChangedPath) {
		return childChangedPath.path.equals(path) ? (childChangedPath.action == SVNLogPath.ChangeType.MODIFIED) : true; 
	}
	
	protected boolean isParentPath(String parentPath, String childPath) {
		IPath pParentPath = new Path(parentPath);
		IPath pChildPath = new Path(childPath);
		return this.isParentPath(pParentPath, pChildPath);		
	}
	
	protected boolean isParentPath(IPath parentPath, IPath childPath) {
		return parentPath.isPrefixOf(childPath);
	}
	
	/**
	 * Return start node of revision chain for passed resource
	 * 
	 * @return
	 */
	public PathRevision getResultNode() {
		return this.resultNode;
	}
	
	/*
	 * Note that entry can be null, e.g. because of cache repository, cache corrupted
	 */
	protected SVNLogEntry getEntry(long revision) {		
		return this.entries[(int) revision];
	}
}
