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

package org.eclipse.team.svn.ui.action.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.SaveProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.UpdateOperation;
import org.eclipse.team.svn.core.svnstorage.ResourcesParentsProvider;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractRecursiveTeamAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.operation.ClearUpdateStatusesOperation;
import org.eclipse.team.svn.ui.operation.NotifyUnresolvedConflictOperation;
import org.eclipse.team.svn.ui.panel.local.ResourceListPanel;
import org.eclipse.team.svn.ui.utility.UnacceptableOperationNotificator;

/**
 * Team services menu update action implementation
 * 
 * @author Alexander Gurov
 */
public class UpdateAction extends AbstractRecursiveTeamAction {

	public UpdateAction() {
		super();
	}

	public void runImpl(IAction action) {
		IResource []resources = UnacceptableOperationNotificator.shrinkResourcesWithNotOnRespositoryParents(this.getShell(), this.getSelectedResources(IStateFilter.SF_ONREPOSITORY));
		if (resources == null || resources.length == 0) {
			return;
		}
		
		if (this.checkForResourcesPresenceRecursive(IStateFilter.SF_REVERTABLE)) {
			IResource []missing = this.getSelectedResourcesRecursive(IStateFilter.SF_MISSING);
			if (missing.length > 0 && !UpdateAction.updateMissing(this.getShell(), missing)) {
				return;
			}
		}
		
		this.runScheduled(UpdateAction.getUpdateOperation(resources));
	}
	
	public boolean isEnabled() {
		return this.checkForResourcesPresence(IStateFilter.SF_ONREPOSITORY);
	}
	
	public static boolean updateMissing(Shell shell, IResource []missing) {
		ResourceListPanel panel = new ResourceListPanel(missing, SVNTeamUIPlugin.instance().getResource("UpdateAction.List.Title"), SVNTeamUIPlugin.instance().getResource("UpdateAction.List.Description"), SVNTeamUIPlugin.instance().getResource("UpdateAction.List.Message"), new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL});
		panel.setShowLocalNames(true);
		return new DefaultDialog(shell, panel).open() == 0;
	}

	public static CompositeOperation getUpdateOperation(IResource []updateSet) {
		UpdateOperation mainOp = new UpdateOperation(updateSet, true);
		
		CompositeOperation op = new CompositeOperation(mainOp.getId());
		
		SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(updateSet);
		op.add(saveOp);
		op.add(mainOp);
		op.add(new RestoreProjectMetaOperation(saveOp));
		op.add(new ClearUpdateStatusesOperation(mainOp));
		op.add(new RefreshResourcesOperation(new ResourcesParentsProvider(mainOp)/*, IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL*/));
		op.add(new NotifyUnresolvedConflictOperation(mainOp));
		
		return op;
	}
	
	protected boolean needsToSaveDirtyEditors() {
		return true;
	}
	
}
