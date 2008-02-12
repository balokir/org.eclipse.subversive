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

package org.eclipse.team.svn.ui.action.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.GetRemoteContentsOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.SaveProjectMetaOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.action.AbstractNonRecursiveTeamAction;
import org.eclipse.team.svn.ui.dialog.ReplaceWarningDialog;

/**
 * Team services menu "replace with latest revision" action implementation
 * 
 * @author Alexander Gurov
 */
public class ReplaceWithLatestRevisionAction extends AbstractNonRecursiveTeamAction {
	public ReplaceWithLatestRevisionAction() {
		super();
	}
	
	public void runImpl(IAction action) {
		IResource []resources = this.getSelectedResources(IStateFilter.SF_ONREPOSITORY);
		IActionOperation op = ReplaceWithLatestRevisionAction.getReplaceOperation(resources, this.getShell());
		if (op != null) {
			this.runScheduled(op);
		}
	}

	public boolean isEnabled() {
		return this.checkForResourcesPresence(IStateFilter.SF_ONREPOSITORY);
	}

	public static IActionOperation getReplaceOperation(IResource []resources, Shell shell) {
		ReplaceWarningDialog dialog = new ReplaceWarningDialog(shell);
		if (dialog.open() == 0) {
			CompositeOperation op = new CompositeOperation("Operation.ReplaceWithLatest");
			
			SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(resources);
			op.add(saveOp);
			IRepositoryResource []remote = new IRepositoryResource[resources.length];
			for (int i = 0; i < resources.length; i++) {
				remote[i] = SVNRemoteStorage.instance().asRepositoryResource(resources[i]);
			}
			op.add(new GetRemoteContentsOperation(resources, remote));
			op.add(new RestoreProjectMetaOperation(saveOp));
			op.add(new RefreshResourcesOperation(resources));

			return op;
		}
		return null;
	}
	
}
