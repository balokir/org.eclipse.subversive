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

package org.eclipse.team.svn.ui.operation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.RunExternalRepositoryCompareOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.ui.preferences.SVNTeamDiffViewerPage;

/**
 * Two way compare for repository resources operation implementation
 * It runs either external or eclipse's compare editor
 * 
 * @author Igor Burilo
 */
public class CompareRepositoryResourcesOperation extends CompositeOperation {

	protected CompareRepositoryResourcesInernalOperation internalCompare;
	
	public CompareRepositoryResourcesOperation(IRepositoryResourceProvider provider, boolean forceReuse) {
		super("Operation_CompareRepository"); //$NON-NLS-1$		
		
		final RunExternalRepositoryCompareOperation externalCompare = new RunExternalRepositoryCompareOperation(provider, SVNTeamDiffViewerPage.loadDiffViewerSettings());
		this.add(externalCompare);
		
		this.internalCompare = new CompareRepositoryResourcesInernalOperation(provider, forceReuse) {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				if (!externalCompare.isExecuted()) {
					super.runImpl(monitor);	
				}				
			}
		};
		this.add(this.internalCompare, new IActionOperation[]{externalCompare});
	}
	
	
	public CompareRepositoryResourcesOperation(IRepositoryResource prev, IRepositoryResource next, boolean forceReuse) {
		this(new IRepositoryResourceProvider.DefaultRepositoryResourceProvider(new IRepositoryResource[] {prev, next}), forceReuse);
	}
	
	public CompareRepositoryResourcesOperation(IRepositoryResource prev, IRepositoryResource next) {
		this(prev, next, false);
	}
	
	public CompareRepositoryResourcesOperation(IRepositoryResourceProvider provider) {
		this(provider, false);
	}

	public void setForceId(String forceId) {
		this.internalCompare.setForceId(forceId);
	}
}
