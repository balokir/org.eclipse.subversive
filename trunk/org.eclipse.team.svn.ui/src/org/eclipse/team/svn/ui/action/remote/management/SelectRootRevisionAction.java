/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
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
import org.eclipse.team.svn.ui.repository.model.RepositoryRevisions;

/**
 * Select revision for repository location root
 * 
 * @author Alexander Gurov
 */
public class SelectRootRevisionAction extends SelectResourceRevisionAction {
	public SelectRootRevisionAction() {
		super();
	}

	public void runImpl(IAction action) {
		RepositoryRevisions root = ((RepositoryRevisions [])this.getAdaptedSelection(RepositoryRevisions.class))[0];
		this.runImpl(root.getRepositoryLocation().getRoot());
	}
	
	public boolean isEnabled() {
		return true;
	}
	
}
