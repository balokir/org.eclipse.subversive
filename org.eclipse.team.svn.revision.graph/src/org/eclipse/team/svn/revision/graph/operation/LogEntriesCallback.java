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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphMessages;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCache;
import org.eclipse.team.svn.revision.graph.operation.BaseFetchOperation.ISVNLogEntryCallbackWithError;

/**
 * Provide progress for operation
 * Store each entry in cache
 * 
 * @author Igor Burilo
 */
public class LogEntriesCallback implements ISVNLogEntryCallbackWithError {
	
	protected final static int REVISIONS_COUNT_FOR_SAVE = 50000;
	
	protected IActionOperation op;
	protected int totalWork;
	protected IProgressMonitor monitor;
	
	protected int currentWork;
	
	protected RepositoryCache repositoryCache;
	
	protected int processedRevisionsCount;
	
	protected Throwable error;
	
	public LogEntriesCallback(IActionOperation op, IProgressMonitor monitor, int totalWork, RepositoryCache repositoryCache) {
		this.op = op;
		this.monitor = monitor;
		this.totalWork = totalWork;				
		this.repositoryCache = repositoryCache;
	}
	
	public void next(SVNLogEntry log) {
		//if (log.revision != SVNRevision.INVALID_REVISION_NUMBER) {
			this.addEntry(log);
		//}
	}
		
	protected void addEntry(SVNLogEntry entry) {
		if (this.error == null) {
			ProgressMonitorUtility.setTaskInfo(this.monitor, this.op, SVNRevisionGraphMessages.format(SVNRevisionGraphMessages.LogEntriesCallback_Message, entry.revision));
			ProgressMonitorUtility.progress(this.monitor, ++ this.currentWork, this.totalWork);
					
			try {
				this.repositoryCache.addEntry(entry);
				
				//save
				if (++ this.processedRevisionsCount  % LogEntriesCallback.REVISIONS_COUNT_FOR_SAVE == 0) {
					this.repositoryCache.save(this.monitor);
				}
				
				long start = this.repositoryCache.getCacheInfo().getStartSkippedRevision();
				long end = this.repositoryCache.getCacheInfo().getEndSkippedRevision();		
				if (start > --end) {
					start = end = 0;
				} 		
				this.repositoryCache.getCacheInfo().setSkippedRevisions(start, end);				
			} catch (Throwable e) {
				this.error = e;				
				this.monitor.setCanceled(true);				
			}			
		}				
	}		
	
	public Throwable getError() {
		return this.error;
	}
}
