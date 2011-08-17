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
import org.eclipse.team.svn.core.connector.ISVNConnector.Options;
import org.eclipse.team.svn.core.connector.SVNRevision.Kind;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.RenameResourceOperation;
import org.eclipse.team.svn.core.operation.remote.SetRevisionAuthorNameOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.operation.RefreshRemoteResourcesOperation;
import org.eclipse.team.svn.ui.panel.remote.RenameResourcePanel;

/**
 * Rename remote resource action implementation
 * 
 * @author Alexander Gurov
 */
public class RenameAction extends AbstractRepositoryTeamAction {

	public RenameAction() {
		super();
	}

	public void runImpl(IAction action) {
		IRepositoryResource []resources = this.getSelectedRepositoryResources();
		RenameResourcePanel panel = new RenameResourcePanel(resources[0].getName());
		DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
		
		if (dialog.open() == 0) {
			RenameResourceOperation mainOp = new RenameResourceOperation(resources[0], panel.getResourceName(), panel.getMessage());
			
			CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
			
			op.add(mainOp);
			op.add(new RefreshRemoteResourcesOperation(SVNUtility.getCommonParents(resources)));
			op.add(new SetRevisionAuthorNameOperation(mainOp, Options.FORCE), new IActionOperation[] {mainOp});
			
			this.runScheduled(op);
		}
	}
	
	public boolean isEnabled() {
		IRepositoryResource []resources = this.getSelectedRepositoryResources();
		for (int i = 0; i < resources.length; i++) {
			IRepositoryLocation location = resources[i].getRepositoryLocation();
			if (resources[i].getUrl().equals(location.getRoot().getUrl()) ||
				resources[i].getSelectedRevision().getKind() != Kind.HEAD ||
				resources[i] instanceof IRepositoryRoot && 
				(((IRepositoryRoot)resources[i]).getKind() == IRepositoryRoot.KIND_ROOT || ((IRepositoryRoot)resources[i]).getKind() == IRepositoryRoot.KIND_LOCATION_ROOT)) {
				return false;
			}
		}
		return resources.length == 1;
	}

}
