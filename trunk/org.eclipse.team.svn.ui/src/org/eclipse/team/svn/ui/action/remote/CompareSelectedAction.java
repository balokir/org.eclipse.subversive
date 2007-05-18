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
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.operation.CompareRepositoryResourcesOperation;

/**
 * Compare two selected resources
 * 
 * @author Alexander Gurov
 */
public class CompareSelectedAction extends AbstractRepositoryTeamAction {

	public CompareSelectedAction() {
		super();
	}

    public void run(IAction action) {
    	IRepositoryResource []resources = this.getSelectedRepositoryResources();
        this.runScheduled(new CompareRepositoryResourcesOperation(resources[0], resources[1]));
    }

	protected boolean isEnabled() throws TeamException {
		IRepositoryResource []resources = this.getSelectedRepositoryResources();
		if (resources.length != 2) {
			return false;
		}
		if (CoreExtensionsManager.instance().getSVNClientWrapperFactory().isCompareFoldersAllowed()) {
	        return true;
		}
		return resources[0] instanceof IRepositoryFile && resources[1] instanceof IRepositoryFile;
	}

}
