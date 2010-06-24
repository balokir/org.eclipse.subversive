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
import org.eclipse.team.svn.revision.graph.cache.RepositoryCache;

/**
 * Fetch previously skipped revisions
 * 
 * @author Igor Burilo
 */
public class FetchSkippedRevisionsOperation extends BaseFetchOperation {
	
	public FetchSkippedRevisionsOperation(IRepositoryResource resource, RepositoryCache repositoryCache, boolean isSkipFetchErrors) {
		super("Operation_FetchSkippedRevisions", resource, repositoryCache, isSkipFetchErrors);	 //$NON-NLS-1$
	}
	
	protected void prepareData(IProgressMonitor monitor) throws Exception {		
		long startRevision = this.repositoryCache.getCacheInfo().getStartSkippedRevision();
		long endRevision = this.repositoryCache.getCacheInfo().getEndSkippedRevision();
		this.canRun = startRevision != 0;
		if (this.canRun) {
			this.logOptions = Options.DISCOVER_PATHS;
			this.revProps = ISVNConnector.DEFAULT_LOG_ENTRY_PROPS;
			this.logEntryCallback = new LogEntriesCallback(this, monitor, (int) (endRevision - startRevision + 1), this.repositoryCache);	
		}
	}
	
	protected long getStartSkippedRevision() {
		return this.repositoryCache.getCacheInfo().getStartSkippedRevision();
	}
	
	protected long getEndSkippedRevision() {
		return this.repositoryCache.getCacheInfo().getEndSkippedRevision();
	}

}
