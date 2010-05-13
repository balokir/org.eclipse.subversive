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
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.ISVNConnector.Options;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.UnreportableException;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphMessages;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCache;
import org.eclipse.team.svn.revision.graph.cache.TimeMeasure;

/**
 * 
 * @author Igor Burilo
 */
public abstract class BaseFetchOperation extends AbstractActionOperation {

	protected IRepositoryResource resource;
	protected CheckRepositoryConnectionOperation checkConnectionOp;
	protected RepositoryCache repositoryCache;
	
	//should be filled by derived classes
	protected boolean canRun;
	protected long startRevision;
	protected long endRevision;	
	
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
				LogEntriesCallback callback = new LogEntriesCallback(this, monitor, (int) (this.endRevision - this.startRevision + 1), repositoryCache);
				
				ISVNConnector proxy = this.resource.getRepositoryLocation().acquireSVNProxy();
				try {
					proxy.logEntries(
						SVNUtility.getEntryReference(this.resource.getRepositoryLocation().getRepositoryRoot()),								
						SVNRevision.fromNumber(this.endRevision),
						SVNRevision.fromNumber(this.startRevision),
						ISVNConnector.DEFAULT_LOG_ENTRY_PROPS,
						0,
						Options.DISCOVER_PATHS/*TODO | Options.INCLUDE_MERGED_REVISIONS*/,
						callback,
						new SVNProgressMonitor(this, monitor, null));	
				} finally {
					this.resource.getRepositoryLocation().releaseSVNProxy(proxy);
					
					if (callback.getError() != null){
						Throwable t = callback.getError();
						if (!(t instanceof RuntimeException)) {
							t = new UnreportableException(t);
						}
						this.reportError(t);
					}
					
					//save not yet saved revisions
					if (repositoryCache.isDirty()) {
						repositoryCache.save(monitor);
					}
				} 		
			}						
		}		
		
		measure.end();
	}
	
	protected abstract void prepareData(IProgressMonitor monitor) throws Exception;

}
