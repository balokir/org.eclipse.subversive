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

package org.eclipse.team.svn.ui.synchronize.update.action;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.SaveProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.UpdateOperation;
import org.eclipse.team.svn.core.svnstorage.ResourcesParentsProvider;
import org.eclipse.team.svn.core.synchronize.UpdateSyncInfo;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.operation.ClearUpdateStatusesOperation;
import org.eclipse.team.svn.ui.operation.NotifyUnresolvedConflictOperation;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeModelAction;
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
		IResource []resources = UnacceptableOperationNotificator.shrinkResourcesWithNotOnRespositoryParents(configuration.getSite().getShell(), this.syncInfoSelector.getSelectedResources());
		if (resources == null || resources.length == 0) {
			return null;
		}
		
		resources = FileUtility.addOperableParents(resources, IStateFilter.SF_UNVERSIONED);
		
		final IResource []missing = FileUtility.getResourcesRecursive(resources, org.eclipse.team.svn.ui.action.local.UpdateAction.SF_MISSING_RESOURCES);//, IResource.DEPTH_ZERO
		if (missing.length > 0) {
			if (!org.eclipse.team.svn.ui.action.local.UpdateAction.updateMissing(configuration.getSite().getShell(), missing)) {
				return null;
			}
		}
		if (this.advancedMode) {
			String message;
			if (resources.length == 1) {
				message = SVNUIMessages.UpdateAll_Message_Single;
			}
			else {
				message = SVNUIMessages.format(SVNUIMessages.UpdateAll_Message_Multi, new String[] {String.valueOf(resources.length)});
			}
			MessageDialog dlg = new MessageDialog(configuration.getSite().getShell(), SVNUIMessages.UpdateAll_Title, null, message, MessageDialog.QUESTION, new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}, 0);
			if (dlg.open() != 0) {
				return null;
			}
		}

		CompositeOperation op = new CompositeOperation("Operation_Update"); //$NON-NLS-1$
		
		SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(resources);
		op.add(saveOp);
		
		Map<SVNRevision, Set<IResource>> splitted = UpdateAction.splitByPegRevision(this, resources);
		
		for (Map.Entry<SVNRevision, Set<IResource>> entry : splitted.entrySet()) {
			boolean ignoreExternals = SVNTeamPreferences.getBehaviourBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BEHAVIOUR_IGNORE_EXTERNALS_NAME);
			UpdateOperation mainOp = new UpdateOperation(entry.getValue().toArray(new IResource[0]), entry.getKey(), ignoreExternals);
			op.add(mainOp);
			op.add(new ClearUpdateStatusesOperation(mainOp));
			op.add(new NotifyUnresolvedConflictOperation(mainOp));
		}
		
		op.add(new RestoreProjectMetaOperation(saveOp));
		op.add(new RefreshResourcesOperation(new ResourcesParentsProvider(resources)/*, IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL*/));
		
		return op;
	}
	
	public static Map<SVNRevision, Set<IResource>> splitByPegRevision(AbstractSynchronizeModelAction action, IResource []resources) {
		Map<SVNRevision, Set<IResource>> splitted = new HashMap<SVNRevision, Set<IResource>>();
		//NOTE See bug 237204: SVN replaces files with second last revision after commit: https://bugs.eclipse.org/bugs/show_bug.cgi?id=237204
		// Due to described problem we should roll back code which prepares update to revisions saved in synchronize view, because potential damage
		// from updating of unapproved changes many times smaller than reverting all committed files to its previous states
		splitted.put(SVNRevision.HEAD, new HashSet<IResource>(Arrays.asList(resources)));
//		for (IResource resource : resources) {
//			try {
//				AbstractSVNSyncInfo info = (AbstractSVNSyncInfo)UpdateSubscriber.instance().getSyncInfo(resource);
//				ILocalResource local = ((RemoteResourceVariant)info.getRemote()).getResource();
//				// can be ILocalResource in context of OverrideAndUpdateAction
//				if (local instanceof IResourceChange) {
//					SVNRevision pegRev = ((IResourceChange)local).getPegRevision();
//					Set<IResource> list = splitted.get(pegRev);
//					if (list == null) {
//						splitted.put(pegRev, list = new HashSet<IResource>());
//					}
//					list.add(resource);
//				}
//			}
//			catch (TeamException ex) {
//				UILoggedOperation.reportError(action.getText(), ex);
//				return null;
//			}
//		}
		return splitted;
	}

}
