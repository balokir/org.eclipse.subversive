/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.operation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.remote.AbstractRepositoryOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.ui.repository.RepositoriesView;
import org.eclipse.team.svn.ui.repository.RepositoryTreeViewer;
import org.eclipse.team.svn.ui.repository.model.IDataTreeNode;

/**
 * Refresh repository tree operation
 * 
 * @author Alexander Gurov
 */
public class RefreshRemoteResourcesOperation extends AbstractRepositoryOperation {

	public RefreshRemoteResourcesOperation(IRepositoryResource []resources) {
		super("Operation.RefreshRemote", resources);
	}

	public RefreshRemoteResourcesOperation(IRepositoryResourceProvider provider) {
		super("Operation.RefreshRemote", provider);
	}
	
	public int getOperationWeight() {
		return 0;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRepositoryResource []resources = this.operableData();
		
		for (int i = 0; i < resources.length; i++) {
			final IRepositoryResource current = resources[i];
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					if (current instanceof IRepositoryRoot && ((IRepositoryRoot)current).getKind() == IRepositoryRoot.KIND_LOCATION_ROOT) {
						RepositoriesView.refresh(current.getRepositoryLocation(), new RefreshVisitor());
					}
					else {
						RepositoriesView.refresh(current, new RefreshVisitor());
					}
				}
			}, monitor, resources.length);
		}
	}
	
    protected class RefreshVisitor implements RepositoryTreeViewer.IRefreshVisitor {
		public void visit(Object element) {
			if (element instanceof IDataTreeNode) {
				((IDataTreeNode)element).refresh();
			}
		}
	}
    
}
