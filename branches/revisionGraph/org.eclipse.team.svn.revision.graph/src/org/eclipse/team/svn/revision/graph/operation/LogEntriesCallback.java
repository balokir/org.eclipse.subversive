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

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNLogEntryCallbackWithMergeInfo;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.UnreportableException;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Provide progress for operation
 * Store each entry in cache
 * 
 * @author Igor Burilo
 */
public class LogEntriesCallback extends SVNLogEntryCallbackWithMergeInfo {
	
	protected IActionOperation op;
	protected int totalWork;
	protected IProgressMonitor monitor;
	
	protected int currentWork;	
	protected SVNLogEntry currentEntry;
	
	protected SVNLogSerializer logSerializer;
	protected CacheMetadata cacheMetadata;
			
	public LogEntriesCallback(IActionOperation op, IProgressMonitor monitor, int totalWork, File cacheFolder, CacheMetadata cacheMetadata) {
		this.op = op;
		this.monitor = monitor;
		this.totalWork = totalWork;
		
		this.logSerializer = new SVNLogSerializer(cacheFolder);
		this.cacheMetadata = cacheMetadata;
	}
	
	@Override
	protected void addEntry(SVNLogEntry entry) {
		//don't store entries
		//super.addEntry(entry);
		
		this.currentEntry = entry;
		ProgressMonitorUtility.setTaskInfo(this.monitor, this.op, "Revision: " + entry.revision);
		ProgressMonitorUtility.progress(this.monitor, ++ this.currentWork, this.totalWork);
				
		try {
			this.logSerializer.save(entry, this.cacheMetadata);
			
			long start = this.cacheMetadata.getStartSkippedRevision();
			long end = this.cacheMetadata.getEndSkippedRevision();		
			if (start > --end) {
				start = end = 0;
			} 		
			this.cacheMetadata.setSkippedRevisions(start, end);
			this.cacheMetadata.save();
		} catch (Exception e) {			
			//if we can't store data in cache, then interrupt the whole process
			//TODO check that it will interrupt
			throw new UnreportableException(e);
		}		
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
	
	public void dispose() {
		this.logSerializer.close();
	}	

}
