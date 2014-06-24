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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNLogEntryCallback;
import org.eclipse.team.svn.core.connector.SVNConnectorCancelException;
import org.eclipse.team.svn.core.connector.SVNRevisionRange;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.ActivityCancelledException;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.UnreportableException;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphMessages;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCache;
import org.eclipse.team.svn.revision.graph.cache.TimeMeasure;

/**
 * Base class for fetching history.
 * 
 * @author Igor Burilo
 */
public abstract class BaseFetchOperation extends AbstractActionOperation {

	protected final IRepositoryResource resource;
	protected final RepositoryCache repositoryCache;
	protected final boolean isSkipFetchErrors;
	
	//should be filled by derived classes in 'prepareData' method
	protected boolean canRun;	
	protected long logOptions;
	protected String[] revProps;
	protected ISVNLogEntryCallbackWithError logEntryCallback;
	
	public interface ISVNLogEntryCallbackWithError extends ISVNLogEntryCallback {
		Throwable getError();
		//return has revisions to process
		boolean skipRevision();
		void retryRevision();
	}
	
	public BaseFetchOperation(String operationName, IRepositoryResource resource, RepositoryCache repositoryCache, boolean isSkipFetchErrors) {
		super(operationName, SVNRevisionGraphMessages.class);
		this.resource = resource;
		this.repositoryCache = repositoryCache;
		this.isSkipFetchErrors = isSkipFetchErrors;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		TimeMeasure measure = new TimeMeasure("Fetch revisions " + this.getClass().getName()); //$NON-NLS-1$
									
		this.prepareData(monitor);
		
		if (this.canRun) {
			ISVNConnector proxy = this.resource.getRepositoryLocation().acquireSVNProxy();
			try {
				this.fetch(proxy, monitor);
			} finally {
				this.finalizeFetch(proxy, monitor);				
			}
		}
		
		measure.end();
	}	
	
	protected void fetch(ISVNConnector proxy, IProgressMonitor monitor) throws Exception {
		//variable to track permanent errors: if error happens twice on same revision it's considered as permanent
		long lastErrorRevision = -1;
		
		while (true) {																										
			try {
				proxy.logEntries(
						SVNUtility.getEntryReference(this.resource.getRepositoryLocation().getRepositoryRoot()),
						new SVNRevisionRange[] {new SVNRevisionRange(this.getEndSkippedRevision(), this.getStartSkippedRevision())},
						this.revProps,
						0,
						this.logOptions,
						this.logEntryCallback,
						new SVNProgressMonitor(this, monitor, null));
				//processed all revisions
				return;					
			} catch (Exception e) {		
				if (e instanceof SVNConnectorCancelException || 
					e instanceof ActivityCancelledException ||
					e instanceof OperationCanceledException) {
					return;
				}
				
				if (!this.isSkipFetchErrors) {
					throw e;
				}
										
				if (lastErrorRevision == this.getEndSkippedRevision()) {
					//permanent error: skip revision
					String msg = SVNRevisionGraphMessages.format(SVNRevisionGraphMessages.BaseFetchOperation_SkipMessage, new Object[]{this.getEndSkippedRevision()});										
					this.reportStatus(IStatus.ERROR, null, new UnreportableException(msg, e));
					ProgressMonitorUtility.setTaskInfo(monitor, this, msg);	
					
					if (!this.logEntryCallback.skipRevision()) {
						return;
					}
					
					//as we skip revision then we clear error info
					lastErrorRevision = -1;
				} else {
					//random error: retry					
					String msg = SVNRevisionGraphMessages.format(SVNRevisionGraphMessages.BaseFetchOperation_RetryMessage, new Object[]{this.getEndSkippedRevision()});
					this.reportStatus(IStatus.WARNING, msg, e);
									
					this.logEntryCallback.retryRevision();
					
					lastErrorRevision = this.getEndSkippedRevision();
				}						
			}
		}				
	}
	
	protected void finalizeFetch(ISVNConnector proxy, IProgressMonitor monitor) throws Exception {
		this.resource.getRepositoryLocation().releaseSVNProxy(proxy);
		
		if (this.logEntryCallback.getError() != null){
			Throwable t = this.logEntryCallback.getError();
			if (!(t instanceof RuntimeException)) {
				t = new UnreportableException(t);
			}
			this.reportStatus(IStatus.ERROR, null, t);
		}
		
		//save not yet saved revisions
		if (this.repositoryCache.isDirty()) {
			this.repositoryCache.save(monitor);
		}
		/*
		 * There can be cases where cache data isn't modified but cache info is modified,
		 * e.g. when we process merge info we record skipped information for
		 * every revision even if it doesn't contain merge info
		 */
		if (this.repositoryCache.getCacheInfo().isDirty()) {
			this.repositoryCache.getCacheInfo().save();
		}
	}
	
	protected abstract void prepareData(IProgressMonitor monitor) throws Exception;
	protected abstract long getStartSkippedRevision();
	protected abstract long getEndSkippedRevision();
}
