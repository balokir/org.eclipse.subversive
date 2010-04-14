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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNLogEntryCallbackWithMergeInfo;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphMessages;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCache;

/**
 * Provide progress for operation
 * Store each entry in cache
 * 
 * @author Igor Burilo
 */
public class LogEntriesCallback extends SVNLogEntryCallbackWithMergeInfo {
	
	protected final static int REVISIONS_COUNT_FOR_SAVE = 50000;
	
	protected IActionOperation op;
	protected int totalWork;
	protected IProgressMonitor monitor;
	
	protected int currentWork;	
	protected SVNLogEntry currentEntry;
	
	protected RepositoryCache repositoryCache;
	
	protected int processedRevisionsCount;
	
	protected Throwable error;
	
	public LogEntriesCallback(IActionOperation op, IProgressMonitor monitor, int totalWork, RepositoryCache repositoryCache) throws IOException {
		this.op = op;
		this.monitor = monitor;
		this.totalWork = totalWork;				
		this.repositoryCache = repositoryCache;
	}
	
	@Override
	protected void addEntry(SVNLogEntry entry) {
		if (this.error == null) {
			//don't store entries
			//super.addEntry(entry);
			
			this.currentEntry = entry;
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
	
//	@Override
//	protected void addChildEntry(SVNLogEntry parent, SVNLogEntry child) {
//		super.addChildEntry(parent, child);
//		
//		if (this.currentEntry != null) {
//			ProgressMonitorUtility.setTaskInfo(this.monitor, this.op, "Revision: " + this.currentEntry.revision + 
//					". Add merge revision: " + child.revision + " to revision: " + parent.revision);						
//		}			 							
//	}
}
