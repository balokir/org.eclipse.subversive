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

package org.eclipse.team.svn.ui.action.remote.management;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.team.svn.core.client.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.LocateResourceURLInHistoryOperation;
import org.eclipse.team.svn.core.operation.remote.management.AddRevisionLinkOperation;
import org.eclipse.team.svn.core.operation.remote.management.SaveRepositoryLocationsOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.operation.RefreshRepositoryLocationsOperation;
import org.eclipse.team.svn.ui.panel.common.InputRevisionPanel;

/**
 * Select revision for any repository resource
 * 
 * @author Alexander Gurov
 */
public class SelectResourceRevisionAction extends AbstractRepositoryTeamAction {
	public SelectResourceRevisionAction() {
		super();
	}

	public void runImpl(IAction action) {
		this.runImpl(this.getSelectedRepositoryResources()[0]);
	}
	
	protected void runImpl(IRepositoryResource resource) {
		final IRepositoryLocation location = resource.getRepositoryLocation();
		
		InputRevisionPanel panel = new InputRevisionPanel(resource, SVNTeamUIPlugin.instance().getResource("SelectResourceRevisionAction.InputRevisionPanel.Title"));
		DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
		if (dialog.open() == Dialog.OK) {
			resource = SVNUtility.copyOf(resource);
			SVNRevision selectedRevision = panel.getSelectedRevision();
			resource.setSelectedRevision(selectedRevision);
			
			LocateResourceURLInHistoryOperation locateOp = new LocateResourceURLInHistoryOperation(new IRepositoryResource[] {resource}, true);
			AbstractActionOperation mainOp = new AddRevisionLinkOperation(locateOp, selectedRevision);
			CompositeOperation op = new CompositeOperation(mainOp.getId());
			op.add(locateOp);
			op.add(mainOp, new IActionOperation[] {locateOp});
			op.add(new SaveRepositoryLocationsOperation());
			op.add(new RefreshRepositoryLocationsOperation(new IRepositoryLocation [] {location}, true));
			this.runScheduled(op);
		}
	}
	
	public boolean isEnabled() {
		return this.getSelectedRepositoryResources().length == 1;
	}

}
