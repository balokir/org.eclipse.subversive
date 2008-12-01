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

package org.eclipse.team.svn.ui.action.remote.management;

import java.util.HashSet;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.LocateResourceURLInHistoryOperation;
import org.eclipse.team.svn.core.operation.remote.management.AddRevisionLinkOperation;
import org.eclipse.team.svn.core.operation.remote.management.SaveRepositoryLocationsOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNUIMessages;
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
		IRepositoryResource []resources = this.getSelectedRepositoryResources();
		this.runImpl(resources);
	}
	
	protected void runImpl(IRepositoryResource []resources) {
		SVNRevision selectedRevision = null;
		
		if (resources.length == 1) {
			InputRevisionPanel panel = new InputRevisionPanel(resources[0], SVNUIMessages.SelectResourceRevisionAction_InputRevisionPanel_Title);
			DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
			if (dialog.open() == Dialog.OK) {
				selectedRevision = panel.getSelectedRevision();
				resources[0] = SVNUtility.copyOf(resources[0]);
				resources[0].setSelectedRevision(selectedRevision);
			}
			else {
				return;
			}
		}
		
		LocateResourceURLInHistoryOperation locateOp = new LocateResourceURLInHistoryOperation(resources);
		AbstractActionOperation mainOp = new AddRevisionLinkOperation(locateOp, selectedRevision);
		CompositeOperation op = new CompositeOperation(mainOp.getId());
		op.add(locateOp);
		op.add(mainOp, new IActionOperation[] {locateOp});
		op.add(new SaveRepositoryLocationsOperation());
		HashSet<IRepositoryLocation> locations = new HashSet<IRepositoryLocation>();
		for (IRepositoryResource resource : resources) {
			locations.add(resource.getRepositoryLocation());
		}
		op.add(new RefreshRepositoryLocationsOperation(locations.toArray(new IRepositoryLocation[locations.size()]), true));
		this.runScheduled(op);
	}
	
	public boolean isEnabled() {
		return this.getSelectedRepositoryResources().length > 0;
	}

}
