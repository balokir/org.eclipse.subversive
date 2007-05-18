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
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.management.AddRevisionLinkOperation;
import org.eclipse.team.svn.core.operation.remote.management.SaveRepositoryLocationsOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.operation.GetRemoteResourceRevisionOperation;
import org.eclipse.team.svn.ui.operation.RefreshRepositoryLocationsOperation;
import org.eclipse.team.svn.ui.panel.common.InputRevisionPanel;
import org.eclipse.team.svn.ui.utility.ICancellableOperationWrapper;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Select revision for any repository resource
 * 
 * @author Alexander Gurov
 */
public class SelectResourceRevisionAction extends AbstractRepositoryTeamAction {
	public SelectResourceRevisionAction() {
		super();
	}

	public void run(IAction action) {
		this.runImpl(this.getSelectedRepositoryResources()[0]);
	}
	
	protected void runImpl(IRepositoryResource resource) {
		final IRepositoryLocation location = resource.getRepositoryLocation();
		
		InputRevisionPanel panel = new InputRevisionPanel(resource, SVNTeamUIPlugin.instance().getResource("SelectResourceRevisionAction.InputRevisionPanel.Title"));
		DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
		if (dialog.open() == Dialog.OK) {
			
			Revision revision = panel.getSelectedRevision();
			if (revision.equals(Revision.HEAD)) {
				GetRemoteResourceRevisionOperation revOp = new GetRemoteResourceRevisionOperation(resource);
				ICancellableOperationWrapper wrapper = UIMonitorUtility.doTaskBusyDefault(revOp);
				if (wrapper.getOperation().getExecutionState() != IActionOperation.OK) {
					return;
				}
				revision = Revision.getInstance(revOp.getRevision());
			}
			
			AbstractActionOperation mainOp = new AddRevisionLinkOperation(resource, revision);
			CompositeOperation op = new CompositeOperation(mainOp.getId());
			op.add(mainOp);
			op.add(new SaveRepositoryLocationsOperation());
			op.add(new RefreshRepositoryLocationsOperation(new IRepositoryLocation [] {location}, true));
			this.runBusy(op);
		}
	}
	
	protected boolean isEnabled() throws TeamException {
		return this.getSelectedRepositoryResources().length == 1;
	}

}
