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
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphMessages;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCache;

/**
 * Load cache data
 * 
 * @author Igor Burilo
 */
public class PrepareRevisionDataOperation extends AbstractActionOperation {
	
	protected RepositoryCache repositoryCache;
	
	public PrepareRevisionDataOperation(RepositoryCache repositoryCache) {
		super("Operation_PrepareRevisionData", SVNRevisionGraphMessages.class); //$NON-NLS-1$
		this.repositoryCache = repositoryCache;
	}
	
	@Override
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		this.repositoryCache.load(monitor);
	}
	
	public RepositoryCache getRepositoryCache() {
		return this.repositoryCache;
	}		
}
