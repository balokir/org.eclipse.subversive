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
import org.eclipse.team.svn.core.operation.remote.AbstractCopyMoveResourcesOperation;
import org.eclipse.team.svn.core.operation.remote.CopyResourcesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.operation.RefreshRemoteResourcesOperation;

/**
 * Copy remote resource to the destination, selected on the dialog
 *
 * @author Elena Matokhina
 */
public class CopyToAction extends AbstractCopyMoveAction {
	public CopyToAction() {
		super("CopyToAction");
	}

	protected AbstractCopyMoveResourcesOperation makeCopyOperation(IRepositoryResource destination, IRepositoryResource[] selected, String message) {
		return new CopyResourcesOperation(destination, selected, message);
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
			if (first != location) {
				return false;
			}
		}
		return true;
	}

	protected RefreshRemoteResourcesOperation makeRefreshOperation(IRepositoryResource destination, IRepositoryResource[] selected) {
		return new RefreshRemoteResourcesOperation(new IRepositoryResource[] {destination});
	}

}
