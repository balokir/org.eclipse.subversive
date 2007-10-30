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

package org.eclipse.team.svn.ui.action.remote.management;

import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.operation.RefreshRepositoryLocationsOperation;

/**
 * Refresh repository location in the repository tree action
 * 
 * @author Alexander Gurov
 */
public class RefreshRepositoryLocationAction extends AbstractRepositoryTeamAction {

	public RefreshRepositoryLocationAction() {
		super();
	}
	
	public void runImpl(IAction action) {
		this.runBusy(this.getSelection().isEmpty() ? new RefreshRepositoryLocationsOperation(true) : new RefreshRepositoryLocationsOperation(this.getSelectedRepositoryLocations(), true));
	}

	public boolean isEnabled() {
		return true;
	}

}
