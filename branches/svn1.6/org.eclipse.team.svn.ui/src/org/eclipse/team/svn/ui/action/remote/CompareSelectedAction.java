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

package org.eclipse.team.svn.ui.action.remote;

import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.operation.CompareRepositoryResourcesOperation;
import org.eclipse.team.svn.ui.operation.UILoggedOperation;

/**
 * Compare two selected resources
 * 
 * @author Alexander Gurov
 */
public class CompareSelectedAction extends AbstractRepositoryTeamAction {

	public CompareSelectedAction() {
		super();
	}

    public void runImpl(IAction action) {
    	IRepositoryResource []resources = this.getSelectedRepositoryResources();
    	try {
    		if (resources[1].getRevision() > resources[0].getRevision()) {
    			IRepositoryResource tmp = resources[1];
    			resources[1] = resources[0];
    			resources[0] = tmp;
    		}
    	}
    	catch (SVNConnectorException ex) {
    		UILoggedOperation.reportError("Compare", ex);
    	}
        this.runScheduled(new CompareRepositoryResourcesOperation(resources[1], resources[0]));
    }

	public boolean isEnabled() {
		IRepositoryResource []resources = this.getSelectedRepositoryResources();
		if (resources.length != 2) {
			return false;
		}
		boolean isCompareFoldersAllowed = CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() == ISVNConnectorFactory.APICompatibility.SVNAPI_1_5_x;
		return (isCompareFoldersAllowed || resources[0] instanceof IRepositoryFile) && !(resources[0] instanceof IRepositoryFile ^ resources[1] instanceof IRepositoryFile);
	}

}
