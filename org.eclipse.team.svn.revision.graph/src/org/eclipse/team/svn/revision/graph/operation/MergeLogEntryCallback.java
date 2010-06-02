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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphMessages;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCache;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCacheInfo;
import org.eclipse.team.svn.revision.graph.operation.BaseFetchOperation.ISVNLogEntryCallbackWithError;

/**
 * Provide progress for fetching merge info
 * Store merge info in cache
 * 
 * @author Igor Burilo
 */
public class MergeLogEntryCallback implements ISVNLogEntryCallbackWithError {

	protected final static int REVISIONS_COUNT_FOR_SAVE = 50000;
	
	protected IActionOperation op;
	protected int totalWork;
	protected IProgressMonitor monitor;
	protected RepositoryCache repositoryCache;
	
	protected int currentWork;
	
	protected int processedRevisionsCount;
	
	/*
	 * As merge info can be nested, we need to know at which merge level we are.
	 * -1 means that there's no merge info now
	 */
	protected int mergeDepth = -1;
	protected SVNLogEntry mergeParentEntry;
	
	protected Throwable error;
	
	public MergeLogEntryCallback(IActionOperation op, IProgressMonitor monitor, int totalWork, RepositoryCache repositoryCache) {
		this.op = op;
		this.monitor = monitor;
		this.totalWork = totalWork;				
		this.repositoryCache = repositoryCache;
	}
	
	/*
	 * Process merges only on zero level, merges on deeper levels we ignore
	 */
	public void next(SVNLogEntry entry) {		
		if (this.error != null) {
			return;
		}
		
		if (entry.revision == SVNRevision.INVALID_REVISION_NUMBER) {
			if (-- this.mergeDepth == -1) {
				//add merge info to cache
				this.repositoryCache.addMergeInfo(this.mergeParentEntry);
				this.mergeParentEntry = null;
								
				this.updateSkippedRevisions();				
				
				//save cache
				try {					
					if (++ this.processedRevisionsCount  % LogEntriesCallback.REVISIONS_COUNT_FOR_SAVE == 0) {
						if (this.repositoryCache.isDirty()) {
							this.repositoryCache.save(this.monitor);	
						}						
						if (this.repositoryCache.getCacheInfo().isDirty()) {
							this.repositoryCache.getCacheInfo().save();
						}
					}
				} catch (Throwable e) {
					this.error = e;				
					this.monitor.setCanceled(true);	
				}
			}
		} else {
			if (this.mergeDepth == -1 && !entry.hasChildren()) {
				//record skipped revisions for each revision which doesn't contain merge info 
				this.updateSkippedRevisions();				
			}
			
			//if entry isn't merged then show progress
			if (this.mergeDepth == -1) {
				ProgressMonitorUtility.setTaskInfo(this.monitor, this.op, SVNRevisionGraphMessages.format(SVNRevisionGraphMessages.LogEntriesCallback_Message, entry.revision));
				ProgressMonitorUtility.progress(this.monitor, ++ this.currentWork, this.totalWork);
			} else if (this.mergeDepth == 0) {
				//show info for merge
				ProgressMonitorUtility.setTaskInfo(this.monitor, this.op, "Revision: " + this.mergeParentEntry.revision + 
						". Add merge revision: " + entry.revision);						
				
				this.mergeParentEntry.add(entry);									
			}
			
			if (entry.hasChildren()) {
				if (this.mergeDepth ++ == -1) {
					this.mergeParentEntry = entry;	
				}								
			}
		}		
	}
	
	//update skipped revisions counters
	protected void updateSkippedRevisions() {
		RepositoryCacheInfo cacheInfo = this.repositoryCache.getCacheInfo();
		long start = cacheInfo.getMergeStartSkippedRevision();
		long end = cacheInfo.getMergeEndSkippedRevision();		
		if (start > --end) {
			start = end = 0;
		} 		
		cacheInfo.setMergeSkippedRevisions(start, end);
	}
	
	public Throwable getError() {
		return this.error;
	}
}
