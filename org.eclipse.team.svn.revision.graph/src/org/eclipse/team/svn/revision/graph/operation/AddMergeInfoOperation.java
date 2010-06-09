/*******************************************************************************
 * Copyright (c) 2005-2010 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo (Polarion Software) - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.revision.graph.operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.ActivityCancelledException;
import org.eclipse.team.svn.revision.graph.PathRevision;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphMessages;
import org.eclipse.team.svn.revision.graph.TopRightTraverseVisitor;
import org.eclipse.team.svn.revision.graph.PathRevision.RevisionNodeAction;
import org.eclipse.team.svn.revision.graph.cache.CacheChangedPath;
import org.eclipse.team.svn.revision.graph.cache.CacheRevision;
import org.eclipse.team.svn.revision.graph.cache.MergeInfoStorage;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCache;
import org.eclipse.team.svn.revision.graph.operation.RepositoryConnectionInfo.IRepositoryConnectionInfoProvider;

/**
 * Add merge information to already created revision graph.
 * As a result of processing it adds merge info to revision nodes.
 * 
 * @author Igor Burilo
 */
public class AddMergeInfoOperation extends AbstractActionOperation {

	//flag to enable/disable debug
	protected final static boolean DEBUG = false;
		
	protected CreateRevisionGraphModelOperation createGraphOp;
	protected IRepositoryConnectionInfoProvider repositoryConnectionInfoProvider;
	protected RepositoryCache repositoryCache;
	
	
	//--- structures for quick search/access
	
	//revisions list which are merge targets
	protected Set<Long> mergeTargetRevisions = new HashSet<Long>();
	
	//key: source revision, value: merge target revisions
	protected Map<Long, Set<Long>> mergeSourceRevisions = new HashMap<Long, Set<Long>>();
	
	//contains copy data grouped by path
	protected Map<Integer, CopyDataContainer> copyStructure = new HashMap<Integer, CopyDataContainer>();
	
	//merges detected during calculating 'merge from'
	protected DetectedMerges mergedFromDetectedMerges;
	
	public AddMergeInfoOperation(CreateRevisionGraphModelOperation createGraphOp, IRepositoryConnectionInfoProvider repositoryConnectionInfoProvider) {		
		super("Operation_AddMergeInfo", SVNRevisionGraphMessages.class); //$NON-NLS-1$
		this.createGraphOp = createGraphOp;
		this.repositoryConnectionInfoProvider = repositoryConnectionInfoProvider;
	}
	
	protected void runImpl(final IProgressMonitor monitor) throws Exception {
		if (!this.repositoryConnectionInfoProvider.getRepositoryConnectionInfo().isSupportMergeInfo) {
			return;
		}
		PathRevision model = this.createGraphOp.getModel(); 
		if (model == null) {
			return;
		}
		
		this.repositoryCache = this.createGraphOp.getRepositoryCache();
		this.mergedFromDetectedMerges = new DetectedMerges(this.repositoryCache);
		
		this.preprocess();
						
		/*
		 * Important:
		 * 	Find merges: merge paths have common history
		 * 
		 * 	We don't try to look for merges in nodes which
		 * 	don't have changes for particular path (there can be cases
		 * 	where node has no changes but has merges into it) as
		 * 	there's no much sense in such merge info for user
		 * 
		 * At first process 'merged from' and then 'merge to'
		 * in order to be able to re-use 'merged from' results   
		 */
		
		PathRevision startNode = model.getStartNodeInGraph();
		
		//process merged from
		new TopRightTraverseVisitor<PathRevision>() {
			protected void visit(PathRevision node) {			
				if (monitor.isCanceled()) {
					throw new ActivityCancelledException();
				}
				if (node.action != RevisionNodeAction.NONE) {
					if (AddMergeInfoOperation.this.mergeTargetRevisions.contains(node.getRevision())) {
						AddMergeInfoOperation.this.processIncomingMerges(node);
					}
				}
			}			
		}.traverse(startNode);
		
		//process merge to
		new TopRightTraverseVisitor<PathRevision>() {
			protected void visit(PathRevision node) {
				if (monitor.isCanceled()) {
					throw new ActivityCancelledException();
				}
				if (node.action != RevisionNodeAction.NONE) {
					if (AddMergeInfoOperation.this.mergeSourceRevisions.containsKey(node.getRevision())) {
						AddMergeInfoOperation.this.processOutgoingMerges(node);	
					}
				}
			}			
		}.traverse(startNode);
	}
	
	protected void processOutgoingMerges(PathRevision node) {
		/*
		 * Example of merges:
		 *					br1@10 				
		 * trunk@28 ->      br2@20
		 * 					br3@20
		 */
		
		if (AddMergeInfoOperation.DEBUG) {		
			System.out.println("\nprocess merged to: " + this.getPathAsString(node.getPathIndex(), node.getRevision()) +  //$NON-NLS-1$
				" (Note that source and target are reversed)"); //$NON-NLS-1$
		}
		
		if (this.hasMergeSource(node)) {
			List<CacheRevision> mergeTargetRevisions = this.getMergeTargetRevisions(node.getRevision());
			if (!mergeTargetRevisions.isEmpty()) {
				
				MergeDataContainer detectedMerges = new MergeDataContainer();				
				
				CopyFromHistoryContainer sourceHistory = new CopyFromHistoryContainer(node.getPathIndex(), node.getRevision());								
				for (CacheRevision mergeTargetRevision : mergeTargetRevisions) {
					/*
					 * We call this method with reversed source and target 
					 * (i.e. instead of mergeSourceRevision we pass mergeTargetRevision and
					 * instead of targetHistory we pass sourceHistory),
					 * as a result we'll get detected merges also in reversed order, 
					 * so when applying merge results we'll need to reverse them back. 
					 */					
					this.processRevision(mergeTargetRevision, detectedMerges, sourceHistory, false);					
				}
				
				this.applyOutgoingMergeResults(node, detectedMerges);
			}
		} 
	}
	
	/*
	 * During processing we can find merges from nodes without changes
	 */
	protected void processIncomingMerges(PathRevision node) {
		/*
		 * Example of merges:
		 * br1/src/com@7,8,9,10,20... -> br2@28
		 * br4/src/com@7,12,18... -> br2@28
		 */
				
		if (AddMergeInfoOperation.DEBUG) {
			System.out.println("\nprocess merged from: " + this.getPathAsString(node.getPathIndex(), node.getRevision())); //$NON-NLS-1$
		}
		
		if (this.hasMergeTarget(node)) {
			List<CacheRevision> mergeSourceRevisions = this.getMergeSourceRevisions(node.getRevision());
			if (!mergeSourceRevisions.isEmpty()) {								
				
				MergeDataContainer detectedMerges = new MergeDataContainer();
				
				CopyFromHistoryContainer targetHistory = new CopyFromHistoryContainer(node.getPathIndex(), node.getRevision());
				for (CacheRevision mergeSourceRevision : mergeSourceRevisions) {
					this.processRevision(mergeSourceRevision, detectedMerges, targetHistory, true);					
				}
				
				this.applyIncomingMergeResults(node, detectedMerges);
			}
		}		
	}
	
	protected void processRevision(CacheRevision mergeSourceRevision, MergeDataContainer detectedMerges, 
		CopyFromHistoryContainer targetHistory, boolean isIncomingMerge) {
		
		long sourceRevision = mergeSourceRevision.getRevision();
		
		if (AddMergeInfoOperation.DEBUG) {
			System.out.println("\n" + getIndent(1) + "source revision: " + sourceRevision); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		//traverse paths
		CacheChangedPath[] changedPaths = mergeSourceRevision.getChangedPaths();
		pathsLabel:
		for (CacheChangedPath changedPath : changedPaths) {
	
			if (AddMergeInfoOperation.DEBUG) {
				System.out.println("\n" + getIndent(2) + "Changed path: " + this.getPath(changedPath.getPathIndex())); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			//check if path is covered by detected merges
			for (MergeData mergeData : detectedMerges.mergeDataCol) {
				if (this.isParentPath(mergeData.mergeSourcePath, changedPath.getPathIndex())) {
					//if there's still no current revision, add it
					if (!mergeData.mergeSourceRevisions.contains(sourceRevision)) {																		
						mergeData.mergeSourceRevisions.add(sourceRevision);
					}
					
					if (AddMergeInfoOperation.DEBUG) {
						System.out.println(getIndent(3) + "Covered by already detected merge: " + this.getPath(mergeData.mergeSourcePath)); //$NON-NLS-1$
					}
					
					continue pathsLabel;
				}
			}
						
			if (!isIncomingMerge) {
				//Note: source and target are reversed here
				
				//check if path is covered by merges detected during calculating 'merge from'	
				MergeData legacyMerge = this.mergedFromDetectedMerges.findMergeForTarget(
					targetHistory.getBasePath(), new PathWithRevision(changedPath.getPathIndex(), sourceRevision));
				if (legacyMerge != null) {
					//save merge data in reverse order
					MergeData md = new MergeData(
						legacyMerge.mergeTargetPath, legacyMerge.mergeTargetRevision,
						legacyMerge.mergeSourcePath, sourceRevision);
					
					if (AddMergeInfoOperation.DEBUG) {
						System.out.println(getIndent(3) + "Covered by 'copy from' detected merge: " + this.getPath(md.mergeSourcePath)); //$NON-NLS-1$
					}
					
					detectedMerges.addMergeData(md);					
					continue;
				}						
			}
									
			CopyFromHistoryContainer sourceHistory = new CopyFromHistoryContainer(changedPath.getPathIndex(), sourceRevision);									
			MergeData mergeData = this.findMerge(sourceHistory, targetHistory);
			if (mergeData != null) {
				detectedMerges.addMergeData(mergeData);				
				continue;
			} else {
				//TODO record as not-detected-merge ?
			}
		}					
	}
	
	protected void applyIncomingMergeResults(PathRevision node, MergeDataContainer detectedMerges) {				
		int nodePath = node.getPathIndex();
		for (MergeData mergeData : detectedMerges.mergeDataCol) {
						
			//prepare 'merge from' path
			int targetPath = mergeData.mergeTargetPath;
			int path = mergeData.mergeSourcePath;
			if (targetPath != nodePath) {
				/*
				 * Example:
				 * trunk/src merge to branch/src
				 * graph called for branch/src/foo/bar
				 * then resulted merge from path will be: trunk/src + foo/bar 
				 */
				int[] relativeParts = this.repositoryCache.getPathStorage().makeRelative(targetPath, nodePath);
				path = this.repositoryCache.getPathStorage().add(path, relativeParts);
			}
			
			node.addIncomingMerges(path, mergeData.mergeSourceRevisions);				
		}
		
		this.mergedFromDetectedMerges.add(node.getRevision(), detectedMerges);
	}
	
	protected void applyOutgoingMergeResults(PathRevision node, MergeDataContainer detectedMerges) {				
		int nodePath = node.getPathIndex();
		for (MergeData mergeData : detectedMerges.mergeDataCol) {
						
			//reverse source and target
			int sourcePath = mergeData.mergeTargetPath;
			int targetPath = mergeData.mergeSourcePath;
			Set<Long> targetRevisions = mergeData.mergeSourceRevisions;
			
			//prepare 'merge to' path
			if (targetPath != nodePath) {
				/*
				 * Example:
				 * trunk/src merge to branch/src
				 * graph called for branch/src/foo/bar
				 * then resulted merge from path will be: trunk/src + foo/bar 
				 */
				int[] relativeParts = this.repositoryCache.getPathStorage().makeRelative(sourcePath, nodePath);
				targetPath = this.repositoryCache.getPathStorage().add(targetPath, relativeParts);
			}
						
			for (long rev : targetRevisions) {
				node.addOutgoingMerge(targetPath, rev);
			}				
		}
	}
		
	/*
	 * If it finds that source and target paths have common history, then return merge
	 */
	protected MergeData findMerge(CopyFromHistoryContainer sourceHistory, CopyFromHistoryContainer targetHistory) {
		//don't compare 0 and 0 levels
		int level = 1;
		while (true) {			
			boolean hasSource = sourceHistory.increaseHistory();
			//targetHistory can be cached
			boolean hasTarget = false;			
			if (targetHistory.getHistoryLevel() >= level) {
				hasTarget = true;
			} else {
				hasTarget = targetHistory.increaseHistory();
			}
			if (!hasSource && !hasTarget) {
				break;
			}
			
			if (hasSource) {
				//compare new history level of paths from source with each history level of paths from target
				List<PathHistory> sourcePaths = sourceHistory.getHistoryPaths(level);
				for (int i = 0, n = Math.min(level, targetHistory.getHistoryLevel()); i <= n; i ++) {
					List<PathHistory> targetPaths = targetHistory.getHistoryPaths(i);
					MergeData mergeData = this.findMerge(sourcePaths, level, targetPaths, i);
					if (mergeData != null) {
						return mergeData;
					}
				}								
			}
			
			if (hasTarget) {
				//compare new history level of paths from target with each history level of paths from source
				List<PathHistory> targetPaths = targetHistory.getHistoryPaths(level);
				//don't take last level as it was already used
				for (int i = 0, n = Math.min(level - 1, sourceHistory.getHistoryLevel()); i <= n; i ++) {
					List<PathHistory> sourcePaths = sourceHistory.getHistoryPaths(i);
					MergeData mergeData = this.findMerge(sourcePaths, i, targetPaths, level);
					if (mergeData != null) {
						return mergeData;
					}
				}
			}
			
			level ++;
		}
		return null;
	}
	
	protected MergeData findMerge(
		List<PathHistory> sourceHistory, int sourceHistoryLevel,
		List<PathHistory> targetHistory, int targetHistoryLevel) {
		
		if (sourceHistory.isEmpty() || targetHistory.isEmpty()) {
			return null;
		}		
		
		if (AddMergeInfoOperation.DEBUG) {
			System.out.println("\n" + getIndent(3) + "Sources: " + sourceHistoryLevel);		 //$NON-NLS-1$ //$NON-NLS-2$
			for (PathHistory sourcePathHistory : sourceHistory) {
				System.out.println(getIndent(4) + sourcePathHistory.toString(this.repositoryCache));
			}
			System.out.println(getIndent(3) + "Targets: " + targetHistoryLevel);		 //$NON-NLS-1$
			for (PathHistory targetPathHistory : targetHistory) {
				System.out.println(getIndent(4) + targetPathHistory.toString(this.repositoryCache));
			}
		}
		
		for (PathHistory sourcePathHistory : sourceHistory) {						
			PathWithRevision sourcePath = sourcePathHistory.get(sourceHistoryLevel);
			for (PathHistory targetPathHistory : targetHistory) {												
				PathWithRevision targetPath = targetPathHistory.get(targetHistoryLevel);
				if (sourcePath.path == targetPath.path) {															
					PathWithRevision sPath = sourcePathHistory.getFirst();
					PathWithRevision tPath = targetPathHistory.getFirst();					
					/*
					 * check that start elements are not equal,
					 * this can happen if paths from 0 level have common parents, e.g.
					 * /Subversive/trunk/foo and /Subversive/br2/foo
					 * have '/Subversive' as common parent 
					 */
					if (sPath.path != tPath.path) {
						MergeData res = new MergeData(sPath.path, sPath.revision, tPath.path, tPath.revision);
						
						if (AddMergeInfoOperation.DEBUG) {
							System.out.println(getIndent(3) + "Found merge: " + //$NON-NLS-1$
								this.getPathAsString(tPath) + " --> " + //$NON-NLS-1$
								this.getPathAsString(sPath));								
						}
						
						return res;	
					}					
				}
			}
		}
		return null;
	}	
	
	//if there's path or parent for node's path then return true
	protected boolean hasMergeTarget(PathRevision node) {		
		int nodePath = node.getPathIndex();
		for (CacheChangedPath cacheChangedPath : node.getChangedPaths()) {
			if (this.isParentPath(cacheChangedPath.getPathIndex(), nodePath)) {
				return true;
			}
		}		
		return false;
	}
	
	/*
	 * if there's path or children for node's path then return true
	 * 
	 * Important:
	 * as we take children into account to determine
	 * whether we have merge then there's a possibility that
	 * during calculation of merge targets we'll detect merges that
	 * didn't not really happen from current path, indeed merge happened
	 * at one of its children, but there's no way to exclude it.
	 * 
	 * Note:
	 * If there was merge but node's path or children are not modified we
	 * don't consider this as merge. But it's acceptable as this node will be
	 * marked as node without changes.
	 * From the other side if we also try to take into account such merges we may
	 * return not existing merges (this can happen if nodes have common
	 * ancestor) which is not applicable.
	 */
	protected boolean hasMergeSource(PathRevision node) {
		int nodePath = node.getPathIndex();
		for (CacheChangedPath cacheChangedPath : node.getChangedPaths()) {
			if (this.isParentPath(nodePath, cacheChangedPath.getPathIndex())) {
				return true;
			}
		}		
		return false;
	}
	
	protected List<CacheRevision> getMergeSourceRevisions(long revision) {
		long[] revisions = this.repositoryCache.getMergeInfoStorage().getMergeSourceRevisions(revision);
		List<CacheRevision> result = new ArrayList<CacheRevision>();
		for (int i = 0; i < revisions.length; i ++) {
			CacheRevision cacheRevision = this.repositoryCache.getRevision(revisions[i]);
			if (cacheRevision != null) {
				result.add(cacheRevision);
			}			
		}
		return result;
	}
	
	protected List<CacheRevision> getMergeTargetRevisions(long revision) {
		List<CacheRevision> result = new ArrayList<CacheRevision>();
		Set<Long> revisions = this.mergeSourceRevisions.get(revision);
		if (revisions != null && !revisions.isEmpty()) {
			Iterator<Long> iter = revisions.iterator();
			while (iter.hasNext()) {
				CacheRevision cacheRevision = this.repositoryCache.getRevision(iter.next());
				if (cacheRevision != null) {
					result.add(cacheRevision);
				}
			}
		}				
		return result;
	}
	
	/*
	 * Prepare merge revisions, copied from structure for further operations
	 */
	protected void preprocess() {				
		//merge revisions
		MergeInfoStorage mergeStorage = this.repositoryCache.getMergeInfoStorage();
		
		long[] targetRevisions = mergeStorage.getMergeTargetRevisions();
		for (long targetRevision : targetRevisions) {
			this.mergeTargetRevisions.add(targetRevision);									
			
			long[] sourceRevisions = mergeStorage.getMergeSourceRevisions(targetRevision);
			for (long sourceRevision : sourceRevisions) {
				Set<Long> revs = this.mergeSourceRevisions.get(sourceRevision);
				if (revs == null) {
					revs = new HashSet<Long>();
					this.mergeSourceRevisions.put(sourceRevision, revs);
				}
				revs.add(targetRevision);
			}
		}	
		
		//copied from
		for (long i = 0, n = this.repositoryCache.getLastProcessedRevision(); i < n; i ++) {
			CacheRevision cacheRevision = this.repositoryCache.getRevision(i);
			if (cacheRevision != null) {
				CacheChangedPath[] changedPaths = cacheRevision.getChangedPaths();
				for (int j = 0; j < changedPaths.length; j ++) {
					CacheChangedPath changedPath = changedPaths[j];
					//TODO check this, whether we allow renames, replaces etc.
					if (changedPath.getCopiedFromPathIndex() != RepositoryCache.UNKNOWN_INDEX) {
						CopyDataContainer dataContainer = this.copyStructure.get(changedPath.getPathIndex());
						if (dataContainer == null) {
							dataContainer = new CopyDataContainer();
							this.copyStructure.put(changedPath.getPathIndex(), dataContainer);
						}
						dataContainer.addCopiedFrom(cacheRevision.getRevision(), changedPath.getCopiedFromRevision(), changedPath.getCopiedFromPathIndex());
					}
				}
			}
		}		
	}
	
	protected boolean isParentPath(int parentPathIndex, int childPathIndex) {
		return this.repositoryCache.getPathStorage().isParentIndex(parentPathIndex, childPathIndex);		
	}
	
	
	//--- for debug
	
	protected String getPath(int pathIndex) {
		return this.repositoryCache.getPathStorage().getPath(pathIndex);
	}
	
	protected static String getIndent(int count) {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < count; i ++) {
			str.append("\t"); //$NON-NLS-1$
		}
		return str.toString();
	} 
	
	protected String getPathAsString(PathWithRevision path) {
		return getPathAsString(path.path, path.revision);
	}
	
	protected String getPathAsString(int path, long revision) {
		return this.getPath(path) + "@" + revision; //$NON-NLS-1$
	}
	
	
	//---- Helper classes
	
	protected static class PathWithRevision {
		final int path;
		final long revision;
		
		public PathWithRevision(int path, long revision) {
			this.path = path;
			this.revision = revision;
		}
	}
	
	/*
	 * Contain 'copy from' history for paths
	 */
	protected class CopyFromHistoryContainer {
				
		//base path for which 'copy from' history is calculated
		protected final PathWithRevision basePath;
		
		protected List<PathHistory> pathsHistory = new ArrayList<PathHistory>();
		//latest history level; starts from zero
		protected int historyLevel = -1;
		protected boolean isFilledAllHistory;
				
		public CopyFromHistoryContainer(int path, long revision) {
			this.basePath = new PathWithRevision(path, revision);			
			this.addPathWithParents(path, revision);
		}
		
		private void addPathWithParents(int path, long revision) {
			List<Integer> parents = AddMergeInfoOperation.this.repositoryCache.getPathStorage().getPathParents(path);
			//longer path has lower index in pathsHistory
			ListIterator<Integer> iter = parents.listIterator(parents.size());			
			while (iter.hasPrevious()) {
				this.pathsHistory.add(new PathHistory(new PathWithRevision(iter.previous(), revision)));
			}
			
			this.historyLevel = 0;
		}
		
		public int getHistoryLevel() {		
			return this.historyLevel;
		}
		
		public PathWithRevision getBasePath() {
			return this.basePath;
		}
		
		public boolean increaseHistory() {
			if (this.historyLevel == -1) {
				throw new IllegalStateException("Paths were not set"); //$NON-NLS-1$
			}
			boolean isIncreased = false;
			if (!this.isFilledAllHistory) {
				for (PathHistory pathHistory : this.pathsHistory) {
					if (pathHistory.size() - 1 == this.historyLevel) {
						PathWithRevision path = pathHistory.get(this.historyLevel);						
						PathWithRevision copyFromPath = this.getCopyFromPath(path);
						if (copyFromPath != null) {
							pathHistory.add(copyFromPath);							
							isIncreased = true;
						}
					}
				}
				
				if (!isIncreased) {
					this.isFilledAllHistory = true;
				} else {
					this.historyLevel ++;
				}
			}
			return isIncreased;			
		}
		
		protected PathWithRevision getCopyFromPath(PathWithRevision path) {
			CopyData copyFrom = null;
			CopyDataContainer copyDataContainer = AddMergeInfoOperation.this.copyStructure.get(path.path);
			if (copyDataContainer != null) {			
				for (CopyData copyData : copyDataContainer.copiedFrom) {
					/*
					 * As for path we can have several copy from paths then we need to select
					 * copy from path which has revision less then path revision and which is more
					 * than other copy from path revisions, in other words, we select nearest less copy from path
					 */
					if (copyData.pathRevision <= path.revision) {
						if (copyFrom != null) {
							if (copyData.pathRevision > copyFrom.pathRevision) {
								copyFrom = copyData;
							}
						} else {
							copyFrom = copyData;
						}
					}
				}
			}
			return copyFrom != null ? new PathWithRevision(copyFrom.copyPath, copyFrom.copyRevision) : null;
		}
		
		public List<PathHistory> getHistoryPaths(int level) {
			List<PathHistory> res = new ArrayList<PathHistory>();
			for (PathHistory pathHistory : this.pathsHistory) {
				if (pathHistory.hasPath(level)) {
					res.add(pathHistory);
				}
			}			
			return res;
		}
	}
	
	//'copy from' history for path
	protected static class PathHistory {
		
		protected LinkedList<PathWithRevision> list = new LinkedList<PathWithRevision>();
		
		public PathHistory(PathWithRevision path) {
			this.list.add(path);
		}
		
		public void add(PathWithRevision path) { this.list.add(path); }
		
		public PathWithRevision getFirst() { return this.list.getFirst(); }
		
		public boolean hasPath(int index) { return index < this.list.size(); }
		
		public PathWithRevision get(int index) { return this.list.get(index); }
		
		public int size() { return this.list.size(); }
		
		//for debug
		public String toString(RepositoryCache cache) {
			StringBuilder str = new StringBuilder();
			Iterator<PathWithRevision> iter = this.list.iterator();
			while (iter.hasNext()) {
				PathWithRevision path = iter.next();
				str.append(cache.getPathStorage().getPath(path.path));
				if (iter.hasNext()) {
					str.append(" <-- "); //$NON-NLS-1$
				}
			}
			return str.toString();
		}
	}
	
	//Container for copy data
	protected static class CopyDataContainer {		
		List<CopyData> copiedFrom = new ArrayList<CopyData>();
		
		public void addCopiedFrom(long pathRevision, long copyRevision, int copyPath) {
			this.copiedFrom.add(new CopyData(pathRevision, copyRevision, copyPath));
		}
	}
	
	protected static class CopyData {
		final long pathRevision;
		final long copyRevision;
		final int copyPath;
		
		public CopyData(long pathRevision, long copyRevision, int copyPath) {
			this.pathRevision = pathRevision;
			this.copyRevision = copyRevision;
			this.copyPath = copyPath;
		}
	}
	
	//structure presents merge from several revisions of source path to target path
	protected static class MergeData {
		final int mergeSourcePath;
		Set<Long> mergeSourceRevisions = new HashSet<Long>();
		final int mergeTargetPath;
		final long mergeTargetRevision;				
		
		public MergeData(int mergeSourcePath, long mergeSourceRevision, int mergeTargetPath, long mergeTargetRevision) {
			this.mergeSourcePath = mergeSourcePath;
			this.mergeTargetPath = mergeTargetPath;
			this.mergeSourceRevisions.add(mergeSourceRevision);
			this.mergeTargetRevision = mergeTargetRevision;
		}		
	}
	
	//container for merge data
	protected static class MergeDataContainer {
		
		List<MergeData> mergeDataCol = new ArrayList<MergeData>();
		
		public void addMergeData(MergeData md) {
			MergeData foundMd = this.findMergeData(md.mergeSourcePath, md.mergeTargetPath);
			if (foundMd == null) {
				this.mergeDataCol.add(md);
			} else {
				foundMd.mergeSourceRevisions.addAll(md.mergeSourceRevisions);
			}						
		}
		
		public void addMergeData(List<MergeData> mdList) {
			for (MergeData md : mdList) {
				this.addMergeData(md);
			}
		}
		
		protected MergeData findMergeData(int sourcePath, int targetPath) {
			for (MergeData md : this.mergeDataCol) {
				if (md.mergeSourcePath == sourcePath && md.mergeTargetPath == targetPath) {
					return md;
				}
			}
			return null;
		}
	}
	
	/**
	 * Contain merges detected during calculating 'merge from' merges.
	 * These merges are re-used while calculating 'merge to' merges.
	 * Merges are grouped by merge target revision for quick access
	 */	
	protected static class DetectedMerges {
		
		protected RepositoryCache cache;
		
		protected Map<Long, List<MergeDataContainer>> merges = new HashMap<Long, List<MergeDataContainer>>();
		
		public DetectedMerges(RepositoryCache cache) {
			this.cache = cache;
		}
		
		public void add(long revision, MergeDataContainer mergeDataContainer) {
			List<MergeDataContainer> mergeList = this.merges.get(revision);
			if (mergeList == null) {
				mergeList = new ArrayList<MergeDataContainer>();
				this.merges.put(revision, mergeList);
			}
			mergeList.add(mergeDataContainer);
		}
		
		public MergeData findMergeForTarget(PathWithRevision source, PathWithRevision target) {				
			/*
			 * Example:
			 * find target merge, where source: trunk/src/com@10 and target: br2/src/foo@12
			 * 
			 * detected merges:
			 * 	trunk@10,81,12 -> br2@12
			 *  br1/src@6,15,16 -> br2/src@12
			 *  yy@4,5 -> br2@12 
			 * 
			 * Result:
			 * 	trunk@10,81,12 -> br2@12, because br2 is parent of br2/src/foo and
			 *  trunk is parent of trunk/src/com  
			 */			
			List<MergeDataContainer> mergeList = this.merges.get(target.revision);
			if (mergeList != null) {
				for (MergeDataContainer mdContainer : mergeList) {
					for (MergeData md : mdContainer.mergeDataCol) {
						if (md.mergeSourceRevisions.contains(source.revision) &&							
							this.cache.getPathStorage().isParentIndex(md.mergeTargetPath, target.path) && 
							this.cache.getPathStorage().isParentIndex(md.mergeSourcePath, source.path)) {
							return md;
						}
					}
				}
			}
			return null;
		}
	}

}
