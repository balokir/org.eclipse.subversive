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
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.operation.RefreshRemoteResourcesOperation;

/**
 * Refresh repository tree action
 * 
 * @author Alexander Gurov
 */
public class RefreshAction extends AbstractRepositoryTeamAction {

	public RefreshAction() {
		super();
	}
	
	public void run(IAction action) {
		this.runBusy(new RefreshRemoteResourcesOperation(this.getSelectedRepositoryResources()));
	}

	protected boolean isEnabled() throws TeamException {
		return true;
	}

}
