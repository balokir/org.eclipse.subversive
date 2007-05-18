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
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.operation.ShowHistoryViewOperation;

/**
 * Show history for remote resource action implementation
 * 
 * @author Alexander Gurov
 */
public class ShowHistoryAction extends AbstractRepositoryTeamAction {

	public ShowHistoryAction() {
		super();
	}
	
	public void run(IAction action) {
		IRepositoryResource resource = this.getSelectedRepositoryResources()[0];
		this.runBusy(new ShowHistoryViewOperation(resource, 0, 0));
	}

	protected boolean isEnabled() throws TeamException {
		return this.getSelectedRepositoryResources().length == 1;
	}

}
