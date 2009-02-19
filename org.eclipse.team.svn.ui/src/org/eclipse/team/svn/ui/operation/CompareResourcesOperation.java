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
import org.eclipse.team.svn.core.operation.local.RunExternalCompareOperation;
import org.eclipse.team.svn.core.operation.local.UDiffGenerateOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.preferences.SVNTeamDiffViewerPage;

/**
 * Compare WORKING and BASE revisions of a local resources operation
 * 
 * It runs either external or eclipse's compare editor
 * 
 * @author Igor Burilo
 */
public class CompareResourcesOperation extends CompositeOperation {
	
	protected ILocalResource local;
	protected IRepositoryResource remote;
	
	protected CompareResourcesInternalOperation internalCompareOp;
	
	public CompareResourcesOperation(ILocalResource local, IRepositoryResource remote) {
		this(local, remote, false, false);
	}
	
	public CompareResourcesOperation(ILocalResource local, IRepositoryResource remote, boolean forceReuse) {
		this(local, remote, forceReuse, false);
	}
	
	public CompareResourcesOperation(ILocalResource local, IRepositoryResource remote, boolean forceReuse, boolean showInDialog) {
		super("Operation_CompareLocal"); //$NON-NLS-1$
		this.local = local;
		this.remote = remote;
		
		final RunExternalCompareOperation externalCompareOp = new RunExternalCompareOperation(local, remote, SVNTeamDiffViewerPage.loadDiffViewerSettings());
		this.add(externalCompareOp);
		
		this.internalCompareOp = new CompareResourcesInternalOperation(local, remote, forceReuse, showInDialog) {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				if (!externalCompareOp.isExecuted()) {
					super.runImpl(monitor);
				}				
			}
		};
		this.add(this.internalCompareOp, new IActionOperation[]{externalCompareOp});				
	}
	
	public void setDiffFile(String diffFile) {
		if (diffFile != null) {
			this.add(new UDiffGenerateOperation(this.local, this.remote, diffFile), new IActionOperation[]{this.internalCompareOp});
		}		
	}
	
	public void setForceId(String forceId) {
		this.internalCompareOp.setForceId(forceId);
	}
}
