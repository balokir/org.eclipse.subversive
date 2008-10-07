/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.action.remote;

import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.operation.CompareRepositoryResourcesOperation;
import org.eclipse.team.svn.ui.operation.UILoggedOperation;
import org.eclipse.team.svn.ui.panel.remote.ComparePanel;

/**
 * Compare two repository resources action (available from Repositories View)
 * 
 * @author Sergiy Logvin
 */
public class CompareAction extends AbstractRepositoryTeamAction {
    public CompareAction() {
        super();
    }

    public void runImpl(IAction action) {
        IRepositoryResource first = this.getSelectedRepositoryResources()[0];
        ComparePanel panel = new ComparePanel(first);
        panel.setFilterCurrent(true);
        DefaultDialog dlg = new DefaultDialog(this.getShell(), panel);
        if (dlg.open() == 0) {
        	this.doCompare(first, panel.getSelectedResource());
        }
    }

    public boolean isEnabled() {
		IRepositoryResource []resources = this.getSelectedRepositoryResources();
		if (resources.length != 1) {
			return false;
		}
		boolean isCompareFoldersAllowed = CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() == ISVNConnectorFactory.APICompatibility.SVNAPI_1_5_x;
        return isCompareFoldersAllowed || resources[0] instanceof IRepositoryFile;
    }
    
    protected void doCompare(IRepositoryResource first, IRepositoryResource second) {
    	try {
    		if (second.getRevision() > first.getRevision()) {
    			IRepositoryResource tmp = second;
    			second = first;
    			first = tmp;
    		}
    	}
    	catch (SVNConnectorException ex) {
    		UILoggedOperation.reportError("Compare", ex);
    	}
        this.runScheduled(new CompareRepositoryResourcesOperation(second, first));
    }

}
