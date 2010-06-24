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
import org.eclipse.team.svn.core.connector.ISVNConnector.Options;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCacheInfo;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCache;

/**
 * Fetch revisions after last processed revision
 * 
 * @author Igor Burilo
 */
public class FetchNewRevisionsOperation extends BaseFetchOperation {
	
	protected long lastRepositoryRevision;
	
	public FetchNewRevisionsOperation(IRepositoryResource resource, RepositoryCache repositoryCache, long lastRepositoryRevision, boolean isSkipFetchErrors) {
		super("Operation_FetchNewRevisions", resource, repositoryCache, isSkipFetchErrors); //$NON-NLS-1$
		this.lastRepositoryRevision = lastRepositoryRevision;
	}
	
	protected void prepareData(IProgressMonitor monitor) throws Exception {
		RepositoryCacheInfo cacheInfo = this.repositoryCache.getCacheInfo();
		
		long startRevision = cacheInfo.getLastProcessedRevision() + 1;
		long endRevision = this.lastRepositoryRevision;
		
		this.canRun = this.lastRepositoryRevision > cacheInfo.getLastProcessedRevision();
		if (this.canRun) {
			this.logOptions = Options.DISCOVER_PATHS;
			this.revProps = ISVNConnector.DEFAULT_LOG_ENTRY_PROPS;
			this.logEntryCallback = new LogEntriesCallback(this, monitor, (int) (endRevision - startRevision + 1), this.repositoryCache);
				
			cacheInfo.setSkippedRevisions(startRevision, endRevision);
			cacheInfo.setLastProcessedRevision(endRevision);
			cacheInfo.save();	
			
			this.repositoryCache.expandRevisionsCount(endRevision);
		}
	}
	
	protected long getStartSkippedRevision() {
		return this.repositoryCache.getCacheInfo().getStartSkippedRevision();
	}
	
	protected long getEndSkippedRevision() {
		return this.repositoryCache.getCacheInfo().getEndSkippedRevision();
	}
		
}
