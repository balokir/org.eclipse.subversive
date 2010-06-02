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
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNLogEntryCallback;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.UnreportableException;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
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
	protected final CheckRepositoryConnectionOperation checkConnectionOp;
	protected final RepositoryCache repositoryCache;
	
	//should be filled by derived classes in 'prepareData' method
	protected boolean canRun;
	protected long startRevision;
	protected long endRevision;	
	protected long logOptions;
	protected String[] revProps;
	protected ISVNLogEntryCallbackWithError logEntryCallback;
	
	public interface ISVNLogEntryCallbackWithError extends ISVNLogEntryCallback {
		Throwable getError();		
	}
	
	public BaseFetchOperation(String operationName, IRepositoryResource resource, CheckRepositoryConnectionOperation checkConnectionOp, RepositoryCache repositoryCache) {
		super(operationName, SVNRevisionGraphMessages.class);
		this.resource = resource;
		this.checkConnectionOp = checkConnectionOp;
		this.repositoryCache = repositoryCache;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		TimeMeasure measure = new TimeMeasure("Fetch revisions " + this.getClass().getName()); //$NON-NLS-1$
		
		if (this.checkConnectionOp.hasConnection()) {												
			this.prepareData(monitor);
			if (this.canRun) {
				ISVNConnector proxy = this.resource.getRepositoryLocation().acquireSVNProxy();
				try {
					proxy.logEntries(
						SVNUtility.getEntryReference(this.resource.getRepositoryLocation().getRepositoryRoot()),								
						SVNRevision.fromNumber(this.endRevision),
						SVNRevision.fromNumber(this.startRevision),
						this.revProps,
						0,
						this.logOptions,
						this.logEntryCallback,
						new SVNProgressMonitor(this, monitor, null));	
				} finally {
					this.resource.getRepositoryLocation().releaseSVNProxy(proxy);
					
					if (this.logEntryCallback.getError() != null){
						Throwable t = this.logEntryCallback.getError();
						if (!(t instanceof RuntimeException)) {
							t = new UnreportableException(t);
						}
						this.reportError(t);
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
			}						
		}		
		
		measure.end();
	}
	
	protected abstract void prepareData(IProgressMonitor monitor) throws Exception;

}
