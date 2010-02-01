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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNConnectorCancelException;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNLogEntryCallbackWithMergeInfo;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.ISVNConnector.Options;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphPlugin;
import org.eclipse.team.svn.revision.graph.operation.ShowRevisionGraphUtility.ISVNLogEntryProvider;

/**
 * Fetch revisions from repository and manage cache
 * 
 * TODO
 * There can be about million revisions, e.g. http://svn.apache.org/repos/asf/subversion/trunk/,
 * so probably we should write revisions immediately in cache in order not to loose data. 
 * 
 * TODO
 * 	1. OutOfMemoryError 18:35-18:59 with merge info on http://svn.collab.net/repos/svn
 * 	2. Include merge info
 * 	3. What if Cancel is called during getting Merge info, i.e. will merge info be retrieved for next revision
 *
 *
 *
 * @author Igor Burilo
 */
public class FetchRevisionsOperation extends AbstractActionOperation implements ISVNLogEntryProvider {
	
	protected IRepositoryResource resource;
	
	protected SVNLogEntry[] entries;
	protected boolean hasConnectionToRepository;
		
	/**
	 * Provide progress of operation
	 * 
	 * @author Igor Burilo
	 */
	protected static class SVNLogEntryCallbackWithMergeInfoExt extends SVNLogEntryCallbackWithMergeInfo {
		protected IActionOperation op;
		protected int totalWork;
		protected IProgressMonitor monitor;
		
		protected int currentWork;
		
		protected SVNLogEntry currentEntry;
		
		public SVNLogEntryCallbackWithMergeInfoExt(IActionOperation op, IProgressMonitor monitor, int totalWork) {
			this.op = op;
			this.monitor = monitor;
			this.totalWork = totalWork;
		}
		
		@Override
		protected void addEntry(SVNLogEntry entry) {
			this.currentEntry = entry;
			this.monitor.subTask("Revision: " + entry.revision);
			ProgressMonitorUtility.setTaskInfo(this.monitor, this.op, "Revision: " + entry.revision);
			ProgressMonitorUtility.progress(this.monitor, ++ this.currentWork, this.totalWork);
			
			super.addEntry(entry);
		}					
		@Override
		protected void addChildEntry(SVNLogEntry parent, SVNLogEntry child) {			
			if (this.currentEntry != null) {
				ProgressMonitorUtility.setTaskInfo(this.monitor, this.op, "Revision: " + this.currentEntry.revision + 
						". Add merge revision: " + child.revision + " to revision: " + parent.revision);						
			}			 					
			super.addChildEntry(parent, child);
		}
	}; 
	
	protected static class SVNLogEntriesComparator implements Comparator<SVNLogEntry> {
		public int compare(SVNLogEntry o1, SVNLogEntry o2) {
			return (int) (o1.revision - o2.revision);
		}	
	}		
	
	public FetchRevisionsOperation(IRepositoryResource resource) {	
		super("Fetch Revisions");
		this.resource = resource;
	}	
	
	//TODO delete it
//	protected void runImpl2(final IProgressMonitor monitor) throws Exception {
//		IRepositoryRoot root = FetchRevisionsOperation.this.resource.getRepositoryLocation().getRepositoryRoot();
//		
//		ISVNConnector proxy = this.resource.getRepositoryLocation().acquireSVNProxy();
//		SVNLogEntryCallbackWithMergeInfoExt callback = new SVNLogEntryCallbackWithMergeInfoExt(this, monitor, 100);
//		try {
//			proxy.logEntries(
//					SVNUtility.getEntryReference(root),								
//					SVNRevision.HEAD,
//					SVNRevision.fromNumber(1),																							
//					ISVNConnector.DEFAULT_LOG_ENTRY_PROPS,
//					100,
//					Options.DISCOVER_PATHS/*TODO | Options.INCLUDE_MERGED_REVISIONS*/,
//					callback,
//					new SVNProgressMonitor(FetchRevisionsOperation.this, monitor, null));				
//		} finally {
//			this.resource.getRepositoryLocation().releaseSVNProxy(proxy);
//		}			
//		
//		this.entries = callback.getEntries();
//	}
		
	protected void runImpl(final IProgressMonitor monitor) throws Exception {			
		final List<SVNLogEntry> entriesList = new ArrayList<SVNLogEntry>();
		
		final File cacheDir = this.getCacheFolder();
		
		final IRepositoryRoot root = FetchRevisionsOperation.this.resource.getRepositoryLocation().getRepositoryRoot();
		
		final int totalWork = 31;
		
		//load entries from cache
		final ArrayList<SVNLogEntry> entriesFromCache = new ArrayList<SVNLogEntry>();
		ProgressMonitorUtility.doSubTask(this, new IUnprotectedOperation() {			
			public void run(IProgressMonitor monitor) throws Exception {
				ProgressMonitorUtility.setTaskInfo(monitor, FetchRevisionsOperation.this, "Load entries from cache");
				
				if (cacheDir.exists()) {
					SVNLogSerializer serializer = new SVNLogSerializer(cacheDir);			
					SVNLogEntry[] logEntries = serializer.load(monitor);
					if (logEntries != null && logEntries.length > 0) {
						entriesFromCache.addAll(Arrays.asList(logEntries));
						entriesList.addAll(entriesFromCache);
					}
				}				
			}
		}, monitor, totalWork, 3);															
		
		//find latest revision in repository
		final long[] lastRepositoryRevision = new long[]{SVNRevision.INVALID_REVISION_NUMBER};
		ProgressMonitorUtility.doSubTask(this, new IUnprotectedOperation() {		
			public void run(IProgressMonitor monitor) throws Exception {
				ProgressMonitorUtility.setTaskInfo(monitor, FetchRevisionsOperation.this, "Find latest revision in repository");
				
				try {
					lastRepositoryRevision[0] = root.getRevision();
					FetchRevisionsOperation.this.hasConnectionToRepository = lastRepositoryRevision[0] != SVNRevision.INVALID_REVISION_NUMBER;
				} catch (SVNConnectorException e) {
					if (e instanceof SVNConnectorCancelException) {
						throw e;
					} else {
						FetchRevisionsOperation.this.hasConnectionToRepository = false;
					}
				}			
			}
		}, monitor, totalWork, 5);				
							
		if (this.hasConnectionToRepository) {
			final List<SVNLogEntry> previouslySkippedEntries = new ArrayList<SVNLogEntry>();
			final List<SVNLogEntry> newEntries = new ArrayList<SVNLogEntry>();
			
			final ISVNConnector proxy = this.resource.getRepositoryLocation().acquireSVNProxy();
			try {																							
				//fetch previously skipped entries				
				ProgressMonitorUtility.doSubTask(this, new IUnprotectedOperation() {					
					public void run(IProgressMonitor monitor) throws Exception {
						long startRevisionToRetrieve = 0;
						long endRevisionToRetrieve = 0;
						if (!entriesList.isEmpty()) {							
							if (entriesFromCache.get(0).revision > 1) {
								//Example: 70, 69, 68
								startRevisionToRetrieve = 1;
								endRevisionToRetrieve = entriesFromCache.get(0).revision - 1;
							} else if (entriesFromCache.size() >= 2) {
								//Example: 70, 69, 68,    3, 2, 1
								long previousRevision = entriesFromCache.get(entriesFromCache.size() - 1).revision;				
								for (int i = entriesFromCache.size() - 2; i >= 0; i --) {
									if (previousRevision - entriesFromCache.get(i).revision > 1) {
										startRevisionToRetrieve = entriesFromCache.get(i).revision + 1;
										endRevisionToRetrieve = previousRevision - 1;
										break;
									} else {
										previousRevision = entriesFromCache.get(i).revision;
									}
								}
							}						
						}
						
						if (startRevisionToRetrieve != 0) {
							SVNLogEntryCallbackWithMergeInfoExt callback = new SVNLogEntryCallbackWithMergeInfoExt(FetchRevisionsOperation.this, monitor, (int) (endRevisionToRetrieve - startRevisionToRetrieve + 1));															
							try {
								proxy.logEntries(
										SVNUtility.getEntryReference(root),								
										SVNRevision.fromNumber(endRevisionToRetrieve),
										SVNRevision.fromNumber(startRevisionToRetrieve),																							
										ISVNConnector.DEFAULT_LOG_ENTRY_PROPS,
										0,
										Options.DISCOVER_PATHS/*TODO | Options.INCLUDE_MERGED_REVISIONS*/,
										callback,
										new SVNProgressMonitor(FetchRevisionsOperation.this, monitor, null));	
							} catch (SVNConnectorCancelException e) {						
								SVNLogEntry[] entrs = callback.getEntries();
								if (entrs.length > 0) {
									previouslySkippedEntries.addAll(Arrays.asList(entrs));
									entriesList.addAll(previouslySkippedEntries);							
								}						
								throw e;
							}
							
							SVNLogEntry[] entrs = callback.getEntries();
							if (entrs.length > 0) {
								previouslySkippedEntries.addAll(Arrays.asList(entrs));
								entriesList.addAll(previouslySkippedEntries);
							}
						}							
					}
				}, monitor, totalWork, 10);
														
				//fetch new entries							
				ProgressMonitorUtility.doSubTask(this, new IUnprotectedOperation() {						
					public void run(IProgressMonitor monitor) throws Exception {						
						long lastProcessedRevision = !entriesFromCache.isEmpty() ? entriesFromCache.get(entriesFromCache.size() - 1).revision : 0;												
						if (lastRepositoryRevision[0] > lastProcessedRevision) {
							SVNLogEntryCallbackWithMergeInfoExt callback = new SVNLogEntryCallbackWithMergeInfoExt(FetchRevisionsOperation.this, monitor, (int) (lastRepositoryRevision[0] - lastProcessedRevision));
							try {											
								proxy.logEntries(
										SVNUtility.getEntryReference(root),
										SVNRevision.fromNumber(lastRepositoryRevision[0]),
										SVNRevision.fromNumber(lastProcessedRevision + 1),																							
										ISVNConnector.DEFAULT_LOG_ENTRY_PROPS,
										0,
										Options.DISCOVER_PATHS/*TODO | Options.INCLUDE_MERGED_REVISIONS*/,
										callback,
										new SVNProgressMonitor(FetchRevisionsOperation.this, monitor, null));	
							} catch (SVNConnectorException e) {
								SVNLogEntry[] entrs = callback.getEntries();
								if (entrs.length > 0) {			
									newEntries.addAll(Arrays.asList(entrs));
									Collections.sort(newEntries, new SVNLogEntriesComparator());										
								}						
								throw e;
							}
							SVNLogEntry[] entrs = callback.getEntries();
							if (entrs.length > 0) {
								newEntries.addAll(Arrays.asList(entrs));
								entriesList.addAll(newEntries);								
							}	
						}
						
					}
				}, monitor, totalWork, 10);										
										
			} finally {
				this.resource.getRepositoryLocation().releaseSVNProxy(proxy);
				 
				this.entries = entriesList.toArray(new SVNLogEntry[0]);
				Arrays.sort(this.entries, new SVNLogEntriesComparator());
								
				//save
				this.protectStep(new IUnprotectedOperation() {					
					public void run(IProgressMonitor monitor) throws Exception {
						ProgressMonitorUtility.setTaskInfo(monitor, FetchRevisionsOperation.this, "Save entries to cache");
						
						//save
						if (!newEntries.isEmpty() && previouslySkippedEntries.isEmpty()) {
							SVNLogEntry[] entriesToSave = newEntries.toArray(new SVNLogEntry[0]);							
							Arrays.sort(entriesToSave, new SVNLogEntriesComparator());
							SVNLogSerializer serializer = new SVNLogSerializer(cacheDir);
							serializer.save(entriesToSave, true);																					
						} else if (!newEntries.isEmpty() || !previouslySkippedEntries.isEmpty()) {					
							final SVNLogEntry[] entriesToSave = FetchRevisionsOperation.this.entries; 						
							SVNLogSerializer serializer = new SVNLogSerializer(cacheDir);
							serializer.save(entriesToSave, false);													
						}		
					}
				}, monitor, totalWork, 3);							
			}
		} else {
			this.entries = entriesFromCache.toArray(new SVNLogEntry[0]);
		}			
	}	

	protected File getCacheFolder() {
		IPath stateLocation = SVNRevisionGraphPlugin.instance().getStateLocation();
		String folderName = SVNUtility.base64Encode(this.resource.getRepositoryLocation().getRepositoryRootUrl());		
		//folder name may contain '/', so replace it to '_'
		folderName = folderName.replaceAll("/", "_");		
		File cacheDir = stateLocation.append(folderName).toFile();
		return cacheDir;
	}

	//TODO take into account in caller
	public boolean hasConnectionToRepository() {
		return this.hasConnectionToRepository;
	}
	
	/**
	 * Return entries sorted in ascending order by revision
	 */
	public SVNLogEntry[] getLogEntries() {
		return this.entries != null ? this.entries : new SVNLogEntry[0];
	}

}
