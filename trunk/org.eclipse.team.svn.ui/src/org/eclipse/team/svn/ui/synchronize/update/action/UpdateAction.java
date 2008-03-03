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

package org.eclipse.team.svn.ui.synchronize.update.action;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeModelAction;
import org.eclipse.team.svn.ui.synchronize.action.ISyncStateFilter;
import org.eclipse.team.svn.ui.synchronize.update.UpdateSyncInfo;
import org.eclipse.team.svn.ui.utility.UnacceptableOperationNotificator;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Synchronize view update action implementation
 * 
 * @author Alexander Gurov
 */
public class UpdateAction extends AbstractSynchronizeModelAction {
	protected boolean advancedMode;

	public UpdateAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
		this.advancedMode = false;
	}

	public UpdateAction(String text, ISynchronizePageConfiguration configuration, ISelectionProvider selectionProvider) {
		super(text, configuration, selectionProvider);
		this.advancedMode = true;
	}

	protected FastSyncInfoFilter getSyncInfoFilter() {
		//FastSyncInfoFilter.AndSyncInfoFilter()
		//FastSyncInfoFilter.OrSyncInfoFilter()
		//FastSyncInfoFilter.SyncInfoDirectionFilter()
		//FastSyncInfoFilter.SyncInfoChangeTypeFilter()
		return new FastSyncInfoFilter.SyncInfoDirectionFilter(new int[] {SyncInfo.INCOMING, SyncInfo.CONFLICTING}) {
            public boolean select(SyncInfo info) {
                return super.select(info) && !IStateFilter.SF_OBSTRUCTED.accept(((UpdateSyncInfo)info).getLocalResource());
            }
        };
	}

	protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		// IStateFilter.SF_NONVERSIONED not versioned locally
		IResource []resources = UnacceptableOperationNotificator.shrinkResourcesWithNotOnRespositoryParents(configuration.getSite().getShell(), this.syncInfoSelector.getSelectedResourcesRecursive(ISyncStateFilter.SF_ONREPOSITORY));
		if (resources == null || resources.length == 0) {
			return null;
		}
		
		resources = FileUtility.addOperableParents(resources, IStateFilter.SF_UNVERSIONED);
		
		final IResource []missing = FileUtility.getResourcesRecursive(resources, IStateFilter.SF_MISSING);//, IResource.DEPTH_ZERO
		if (missing.length > 0) {
			if (!org.eclipse.team.svn.ui.action.local.UpdateAction.updateMissing(configuration.getSite().getShell(), missing)) {
				return null;
			}
		}
		if (this.advancedMode) {
			String message;
			if (resources.length == 1) {
				message = SVNTeamUIPlugin.instance().getResource("UpdateAll.Message.Single");
			}
			else {
				message = SVNTeamUIPlugin.instance().getResource("UpdateAll.Message.Multi", new String[] {String.valueOf(resources.length)});
			}
			MessageDialog dlg = new MessageDialog(configuration.getSite().getShell(), SVNTeamUIPlugin.instance().getResource("UpdateAll.Title"), null, message, MessageDialog.QUESTION, new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}, 0);
			if (dlg.open() != 0) {
				return null;
			}
		}

		return org.eclipse.team.svn.ui.action.local.UpdateAction.getUpdateOperation(configuration.getSite().getShell(), resources);
	}

}
