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
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.operation.RemoteShowAnnotationOperation;

/**
 * Show annotation for repository file
 * 
 * @author Alexander Gurov
 */
public class ShowAnnotationAction extends AbstractRepositoryTeamAction {

	public ShowAnnotationAction() {
		super();
	}
	
	public void runImpl(IAction action) {
		this.runScheduled(new RemoteShowAnnotationOperation(this.getSelectedRepositoryResources()[0]));
	}

	public boolean isEnabled() {
		return this.getSelectedRepositoryResources().length == 1;
	}

}
