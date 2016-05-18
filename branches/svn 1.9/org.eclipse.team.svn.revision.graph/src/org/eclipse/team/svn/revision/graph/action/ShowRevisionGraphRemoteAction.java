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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.operation.IActionOperation;
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
		IActionOperation op = RevisionGraphUtility.getRevisionGraphOperation(this.getResources().toArray(new IRepositoryResource[0]));
		if (op != null) {
			this.runScheduled(op);
		}
	}
	
	public boolean isEnabled() {
		return !this.getResources().isEmpty();
	}

	protected List<IRepositoryResource> getResources() {
		List<IRepositoryResource> result = new ArrayList<IRepositoryResource>();
		IRepositoryResource[] resources = this.getSelectedRepositoryResources();
		for (IRepositoryResource resource : resources) {
			//don't enable for repository root
			if (resource instanceof IRepositoryRoot && resource.getUrl().equals(resource.getRepositoryLocation().getRepositoryRootUrl())) {
				continue;
			}
			result.add(resource);	
		}		
		return result;
	}
}
