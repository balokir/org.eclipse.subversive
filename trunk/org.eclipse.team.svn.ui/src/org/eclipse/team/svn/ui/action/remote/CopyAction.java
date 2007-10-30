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
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.RemoteResourceTransferrable;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.operation.PrepareRemoteResourcesTransferrableOperation;

/**
 * Copy remote resource into clipboard action implementation
 * 
 * @author Alexander Gurov
 */
public class CopyAction extends AbstractRepositoryTeamAction {

	public CopyAction() {
		super();
	}

	public void runImpl(IAction action) {
		this.runBusy(new PrepareRemoteResourcesTransferrableOperation(
			this.getSelectedRepositoryResources(),
			RemoteResourceTransferrable.OP_COPY, 
			this.getShell().getDisplay()
		));
	}
	
	public boolean isEnabled() {
		IRepositoryResource []resources = this.getSelectedRepositoryResources();
		if (resources.length == 0) {
			return false;
		}
		// disable copying between different repositories
		IRepositoryLocation first = resources[0].getRepositoryLocation();
		for (int i = 1; i < resources.length; i++) {
			if (first != resources[i].getRepositoryLocation()) {
				return false;
			}
		}
		return true;
	}

}
