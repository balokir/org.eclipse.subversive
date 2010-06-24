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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.ActivityCancelledException;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphMessages;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphPlugin;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCache;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCacheInfo;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCacheInfo.CacheResult;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCacheInfo.CacheResultEnum;
import org.eclipse.team.svn.revision.graph.operation.RepositoryConnectionInfo.IRepositoryConnectionInfoProvider;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Create or refresh repository cache operation
 * 
 * @author Igor Burilo
 */
public class CreateCacheDataOperation extends AbstractActionOperation implements IRepositoryCacheProvider {

	protected IRepositoryResource resource;
	protected boolean isRefresh;
	protected IRepositoryConnectionInfoProvider repositoryConnectionInfoProvider;	
	protected boolean isSkipFetchErrors;
	
	protected RepositoryCache repositoryCache;
	
	public CreateCacheDataOperation(IRepositoryResource resource, boolean isRefresh, 
		IRepositoryConnectionInfoProvider repositoryConnectionInfoProvider, boolean isSkipFetchErrors) {
		
		super("Operation_CreateCacheData", SVNRevisionGraphMessages.class); //$NON-NLS-1$
		this.resource = resource;
		this.isRefresh = isRefresh;
		this.repositoryConnectionInfoProvider = repositoryConnectionInfoProvider;
		this.isSkipFetchErrors = isSkipFetchErrors;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		RepositoryCacheInfo cacheInfo = SVNRevisionGraphPlugin.instance().getRepositoryCachesManager().getCache(this.resource);
		RepositoryConnectionInfo connectionData = this.repositoryConnectionInfoProvider.getRepositoryConnectionInfo();
		CacheResult cacheResult = this.isRefresh ? 
				cacheInfo.refreshCacheData(this.resource, connectionData, this.isSkipFetchErrors, monitor) :
				cacheInfo.createCacheData(this.resource, connectionData, this.isSkipFetchErrors, monitor);
		
		if (cacheResult.status == CacheResultEnum.BROKEN) {
			throw new ActivityCancelledException();
		} else if (cacheResult.status == CacheResultEnum.CALCULATING) {
			//say that cache is calculating now by another task
			UIMonitorUtility.getDisplay().syncExec(new Runnable() {
				public void run() {									
					MessageDialog dlg = RevisionGraphUtility.getCacheCalculatingDialog();
					dlg.open();
				}
			});	
			
			throw new ActivityCancelledException();
		} else if (cacheResult.status == CacheResultEnum.OK) {
			this.repositoryCache = cacheResult.repositoryCache;
		} else {
			//unknown
			throw new ActivityCancelledException();
		}
	}
	
	public RepositoryCache getRepositoryCache() {
		return this.repositoryCache;
	}						
}
