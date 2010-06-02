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
	
	public FetchSkippedRevisionsOperation(IRepositoryResource resource, CheckRepositoryConnectionOperation checkConnectionOp, RepositoryCache repositoryCache) {
		super("Operation_FetchSkippedRevisions", resource, checkConnectionOp, repositoryCache);	 //$NON-NLS-1$
	}
	
	protected void prepareData(IProgressMonitor monitor) throws Exception {		
		this.startRevision = this.repositoryCache.getCacheInfo().getStartSkippedRevision();
		this.endRevision = this.repositoryCache.getCacheInfo().getEndSkippedRevision();			
		this.canRun = this.startRevision != 0;
		if (this.canRun) {
			this.logOptions = Options.DISCOVER_PATHS;
			this.revProps = ISVNConnector.DEFAULT_LOG_ENTRY_PROPS;
			this.logEntryCallback = new LogEntriesCallback(this, monitor, (int) (this.endRevision - this.startRevision + 1), this.repositoryCache);	
		}
	}

}
