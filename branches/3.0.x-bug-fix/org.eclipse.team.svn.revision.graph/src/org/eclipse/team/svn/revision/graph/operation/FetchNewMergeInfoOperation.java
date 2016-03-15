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
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCache;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCacheInfo;

/**
 * Fetch merge info after last processed revision
 * 
 * @author Igor Burilo
 */
public class FetchNewMergeInfoOperation extends BaseFetchOperation {

	protected long lastRepositoryRevision;
	
	public FetchNewMergeInfoOperation(IRepositoryResource resource, RepositoryCache repositoryCache, long lastRepositoryRevision, boolean isSkipFetchErrors) {	
		super("Operation_FetchNewMergeInfo", resource, repositoryCache, isSkipFetchErrors); //$NON-NLS-1$
		this.lastRepositoryRevision = lastRepositoryRevision;
	}
	
	protected void prepareData(IProgressMonitor monitor) throws Exception {
		RepositoryCacheInfo cacheInfo = this.repositoryCache.getCacheInfo();
		
		long startRevision = cacheInfo.getMergeLastProcessedRevision() + 1;
		long endRevision = this.lastRepositoryRevision;
		
		this.canRun = this.lastRepositoryRevision > cacheInfo.getMergeLastProcessedRevision();
		if (this.canRun) {
			this.logOptions = ISVNConnector.Options.INCLUDE_MERGED_REVISIONS;
			//don't retrieve any revision properties, e.g. author, date, comment
			this.revProps = new String[] {""}; //$NON-NLS-1$
			this.logEntryCallback = new MergeLogEntryCallback(this, monitor, (int) (endRevision - startRevision + 1), this.repositoryCache);
				
			cacheInfo.setMergeSkippedRevisions(startRevision, endRevision);
			cacheInfo.setMergeLastProcessedRevision(endRevision);
			cacheInfo.save();							
		}
	}
	
	protected long getStartSkippedRevision() {
		return this.repositoryCache.getCacheInfo().getMergeStartSkippedRevision();
	}
	
	protected long getEndSkippedRevision() {
		return this.repositoryCache.getCacheInfo().getMergeEndSkippedRevision();
	}
}
