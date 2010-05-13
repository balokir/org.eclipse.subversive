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
import org.eclipse.team.svn.core.connector.ISVNConnector.Options;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.remote.ImportOperation;
import org.eclipse.team.svn.core.operation.remote.SetRevisionAuthorNameOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.operation.RefreshRemoteResourcesOperation;
import org.eclipse.team.svn.ui.panel.remote.ImportPanel;

/**
 * Import Action implementation
 * 
 * @author Sergiy Logvin
 */
public class ImportAction extends AbstractRepositoryTeamAction {
	
	public ImportAction() {
		super();
	}
	
	public void runImpl(IAction action) {
		IRepositoryResource resource = this.getSelectedRepositoryResources()[0];
		ImportPanel panel = new ImportPanel(resource.getUrl());
		DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
	    if (dialog.open() == 0) {
	    	ImportOperation mainOp = new ImportOperation(resource, panel.getLocation(), panel.getMessage(), panel.getDepth());
			CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
			op.add(mainOp);
			op.add(new RefreshRemoteResourcesOperation(this.getSelectedRepositoryResources()));
			op.add(new SetRevisionAuthorNameOperation(mainOp, Options.FORCE));
			this.runScheduled(op);
	    }
	}
	
	public boolean isEnabled() {
		return this.getSelectedRepositoryResources().length == 1;
	}
	
}
