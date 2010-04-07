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
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.ActivityCancelledException;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphPlugin;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCacheInfo;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCache;

/** 
 * @author Igor Burilo
 */
public class CreateCacheDataOperation extends AbstractActionOperation {

	protected IRepositoryResource resource;
	
	protected RepositoryCacheInfo cacheInfo;
	protected RepositoryCache repositoryCache;
	
	public CreateCacheDataOperation(IRepositoryResource resource) {
		super("Create Cache Data");
		this.resource = resource;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		this.cacheInfo = SVNRevisionGraphPlugin.instance().getRepositoryCachesManager().getCache(this.resource);
		this.repositoryCache = this.cacheInfo.createCacheData(this.resource, monitor);
		
		if (this.repositoryCache == null) {
			throw new ActivityCancelledException();
		}			
	}

	public RepositoryCacheInfo getCacheInfo() {
		return this.cacheInfo;
	}
	public RepositoryCache getRepositoryCache() {
		return this.repositoryCache;
	}						
}
