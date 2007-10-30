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

package org.eclipse.team.svn.ui.action.remote;

import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.ui.RemoteResourceTransferrable;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.operation.PrepareRemoteResourcesTransferrableOperation;

/**
 * Cut remote resource into clipboard action implementation
 * 
 * @author Alexander Gurov
 */
public class CutAction extends AbstractRepositoryTeamAction {

	public CutAction() {
		super();
	}

	public void runImpl(IAction action) {
		this.runBusy(new PrepareRemoteResourcesTransferrableOperation(
			this.getSelectedRepositoryResources(),
			RemoteResourceTransferrable.OP_CUT, 
			this.getShell().getDisplay()
		));
	}
	
	public boolean isEnabled() {
		IRepositoryResource []resources = this.getSelectedRepositoryResources();
		if (resources.length == 0) {
			return false;
		}
		// disable transfer between different repositories
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
