/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elena Matokhina - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.action.remote;

import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.operation.remote.AbstractCopyMoveResourcesOperation;
import org.eclipse.team.svn.core.operation.remote.MoveResourcesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.operation.RefreshRemoteResourcesOperation;

/**
 * Move repository resource action implementation
 *
 * @author Elena Matokhina
 */
public class MoveToAction extends AbstractCopyMoveAction {
	public MoveToAction() {
		super("MoveToAction");
	}
	
	protected AbstractCopyMoveResourcesOperation makeCopyOperation(IRepositoryResource destination, IRepositoryResource[] selected, String message) {
		return new MoveResourcesOperation(destination, selected, message);
	}

	protected RefreshRemoteResourcesOperation makeRefreshOperation(IRepositoryResource destination, IRepositoryResource[] selected) {
		IRepositoryResource []toRefresh = new IRepositoryResource[selected.length + 1];
		System.arraycopy(selected, 0, toRefresh, 0, selected.length);
		toRefresh[selected.length] = destination;
		return new RefreshRemoteResourcesOperation(SVNUtility.getCommonParents(toRefresh));
	}

	protected boolean isEnabled() throws TeamException {
		IRepositoryResource []resources = this.getSelectedRepositoryResources();
		if (resources.length == 0) {
			return false;
		}
		//disable transfer between different repositories
		IRepositoryLocation first = resources[0].getRepositoryLocation();
		for (int i = 0; i < resources.length; i++) {
			IRepositoryLocation location = resources[i].getRepositoryLocation();
			if (first != location || 
				resources[i].getSelectedRevision().getKind() != Revision.Kind.head || 
				resources[i] instanceof IRepositoryRoot && 
				(((IRepositoryRoot)resources[i]).getKind() == IRepositoryRoot.KIND_ROOT || ((IRepositoryRoot)resources[i]).getKind() == IRepositoryRoot.KIND_LOCATION_ROOT)) {
				return false;
			}
		}
		return true;
	}

}
