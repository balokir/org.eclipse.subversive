/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.revision.graph.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.revision.graph.operation.RevisionGraphUtility;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;

/**
 * Show revision graph for remote resource action implementation
 * 
 * @author Igor Burilo
 */
public class ShowRevisionGraphRemoteAction extends AbstractRepositoryTeamAction {

	public ShowRevisionGraphRemoteAction() {
		super();
	}
	
	public void runImpl(IAction action) {
		IRepositoryResource resource = this.getSelectedRepositoryResources()[0];
		this.runScheduled(RevisionGraphUtility.getRevisionGraphOperation(resource));
	}
	
	public boolean isEnabled() {
		IRepositoryResource[] resources = this.getSelectedRepositoryResources();
		if (resources.length == 1) {
			IRepositoryResource resource = resources[0];
			//don't enable for repository root
			if (resource instanceof IRepositoryRoot) {
				return !resource.getUrl().equals(resource.getRepositoryLocation().getRepositoryRootUrl());
			} else {
				return true;
			}
		}
		return false;
	}

}
