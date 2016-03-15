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

package org.eclipse.team.svn.ui.action.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.action.AbstractNonRecursiveTeamAction;
import org.eclipse.team.svn.ui.action.remote.management.SelectResourceRevisionAction;

/**
 * Team services add revision link action implementation
 * 
 * @author Igor Burilo
 */
public class AddRevisionLinkAction extends AbstractNonRecursiveTeamAction {

	public AddRevisionLinkAction() {
		super();
	}
	
	public void runImpl(IAction action) {
		IResource[] selectedResources = this.getSelectedResources(IStateFilter.SF_ONREPOSITORY);
		IRepositoryResource[] resources = new IRepositoryResource[selectedResources.length];
		for (int i = 0; i < selectedResources.length; i ++) {
			resources[i] = SVNRemoteStorage.instance().asRepositoryResource(selectedResources[i]);
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(selectedResources[i]);			
			resources[i].setSelectedRevision(SVNRevision.fromNumber(local.getRevision()));
		}				
		IActionOperation op = SelectResourceRevisionAction.getAddRevisionLinkOperation(resources, this.getShell());
		if (op != null) {
			this.runScheduled(op);
		}
	}
	
	public boolean isEnabled() {
		return this.checkForResourcesPresence(IStateFilter.SF_ONREPOSITORY);
	}

}
