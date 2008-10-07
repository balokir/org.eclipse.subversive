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

import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.management.ShareProjectOperation;
import org.eclipse.team.svn.core.operation.remote.CreateFolderOperation;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.operation.RefreshRemoteResourcesOperation;
import org.eclipse.team.svn.ui.panel.remote.CreateProjectStructurePanel;

/**
 * Base implementation for CreateXXXLocationAction's
 * 
 * @author Alexander Gurov
 */
public class CreateProjectStructureAction extends AbstractRepositoryTeamAction {

	public CreateProjectStructureAction() {
		super();
	}

	public void runImpl(IAction action) {
		CreateProjectStructurePanel panel = new CreateProjectStructurePanel();
		DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
		if (dialog.open() == 0) {
			String name = panel.getResourceName();
			IRepositoryResource []parent = this.getSelectedRepositoryResources();
			IRepositoryLocation location = parent[0].getRepositoryLocation();
			String trunk = ShareProjectOperation.getTrunkName(location);
			String branches = ShareProjectOperation.getBranchesName(location);
			String tags = ShareProjectOperation.getTagsName(location);
			String []folders = name.length() == 0 ? new String[] {trunk, branches, tags} : new String[] {name + "/" + trunk, name + "/" + branches, name + "/" + tags};

			CreateFolderOperation mainOp = new CreateFolderOperation(parent[0], folders, panel.getMessage());
			CompositeOperation op = new CompositeOperation(mainOp.getId());
			op.add(mainOp);
			op.add(new RefreshRemoteResourcesOperation(parent), new IActionOperation[] {mainOp});
			
			this.runScheduled(op);
		}
	}

	public boolean isEnabled() {
		IRepositoryResource []resources = this.getSelectedRepositoryResources();
		return resources.length == 1 && resources[0] instanceof IRepositoryContainer;
	}
	
}
