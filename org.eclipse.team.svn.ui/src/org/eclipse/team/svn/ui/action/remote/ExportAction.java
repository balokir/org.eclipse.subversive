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
import org.eclipse.team.svn.core.operation.remote.ExportOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.remote.ExportPanel;
/**
 * Export Action implementation
 * 
 * @author Sergiy Logvin
 */
public class ExportAction extends AbstractRepositoryTeamAction {
	
	public ExportAction() {
		super();
	}
	
	public void runImpl(IAction action) {
		IRepositoryResource resource = this.getSelectedRepositoryResources()[0];
		ExportPanel panel = new ExportPanel(resource);
		DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
		if (dialog.open() == 0) {
			resource = SVNUtility.copyOf(resource);
			resource.setSelectedRevision(panel.getSelectedRevision());
	    	this.runScheduled(new ExportOperation(resource, panel.getLocation()));
	    }
	}
	
	public boolean isEnabled() {
		return this.getSelectedRepositoryResources().length == 1;
	}
	
}
