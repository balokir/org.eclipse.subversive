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
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * @author Igor Burilo
 */
public class CreateCacheDataOperation extends AbstractActionOperation implements IRepositoryCacheProvider {

	protected IRepositoryResource resource;
	protected boolean isRefresh;
	
	protected RepositoryCache repositoryCache;
	
	public CreateCacheDataOperation(IRepositoryResource resource, boolean isRefresh) {
		super("Operation_CreateCacheData"); //$NON-NLS-1$
		this.resource = resource;
		this.isRefresh = isRefresh;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {				
		RepositoryCacheInfo cacheInfo = SVNRevisionGraphPlugin.instance().getRepositoryCachesManager().getCache(this.resource);
		CacheResult cacheResult = this.isRefresh ? 
				cacheInfo.refreshCacheData(this.resource, monitor) :
				cacheInfo.createCacheData(this.resource, monitor);
		
		if (cacheResult.status == CacheResultEnum.BROKEN) {
			throw new ActivityCancelledException();
		} else if (cacheResult.status == CacheResultEnum.CALCULATING) {
									
			//say that cache is calculating now by another task
			UIMonitorUtility.getDisplay().syncExec(new Runnable() {
				public void run() {									
					MessageDialog dlg = new MessageDialog(
						UIMonitorUtility.getShell(), 
						SVNRevisionGraphMessages.Dialog_GraphTitle,
						null, 
						SVNRevisionGraphMessages.CreateCacheDataOperation_DialogMessage,
						MessageDialog.INFORMATION, 
						new String[] {IDialogConstants.OK_LABEL}, 
						0);
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
