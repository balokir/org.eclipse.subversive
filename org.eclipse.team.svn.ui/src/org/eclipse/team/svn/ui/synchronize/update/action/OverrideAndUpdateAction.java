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

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.ClearLocalStatusesOperation;
import org.eclipse.team.svn.core.operation.local.DetectDeletedProjectsOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RemoveNonVersionedResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.RevertOperation;
import org.eclipse.team.svn.core.operation.local.SaveProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.UpdateOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.operation.ClearUpdateStatusesOperation;
import org.eclipse.team.svn.ui.operation.ProcessDeletedProjectsOperation;
import org.eclipse.team.svn.ui.panel.local.OverrideResourcesPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeModelAction;
import org.eclipse.team.svn.ui.synchronize.action.ISyncStateFilter;
import org.eclipse.team.svn.ui.utility.UnacceptableOperationNotificator;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Override and update conflicting files action
 * 
 * @author Alexander Gurov
 */
public class OverrideAndUpdateAction extends AbstractSynchronizeModelAction {

	public OverrideAndUpdateAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter.SyncInfoDirectionFilter(new int[] {SyncInfo.OUTGOING, SyncInfo.INCOMING, SyncInfo.CONFLICTING});
	}

	protected IActionOperation execute(final FilteredSynchronizeModelOperation operation) {
		final IResource [][]resources = new IResource[1][];
		operation.getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				IResource []obstructedResources = operation.getSelectedResourcesRecursive(IStateFilter.SF_OBSTRUCTED);
				obstructedResources = FileUtility.addOperableParents(obstructedResources, IStateFilter.SF_OBSTRUCTED);
				HashSet allResources = new HashSet(Arrays.asList(obstructedResources));
				IResource []changedResources = operation.getSelectedResourcesRecursive(ISyncStateFilter.SF_OVERRIDE);
				changedResources = UnacceptableOperationNotificator.shrinkResourcesWithNotOnRespositoryParents(operation.getShell(), changedResources);
				if (changedResources != null) {
					changedResources = FileUtility.addOperableParents(changedResources, IStateFilter.SF_NOTONREPOSITORY);
					allResources.addAll(Arrays.asList(changedResources));
				}
				
				if (allResources.size() > 0) {
					IResource []fullSet = (IResource [])allResources.toArray(new IResource[allResources.size()]);
					OverrideResourcesPanel panel = new OverrideResourcesPanel(fullSet, fullSet, OverrideResourcesPanel.MSG_UPDATE);
					DefaultDialog dialog = new DefaultDialog(operation.getShell(), panel);
					if (dialog.open() == 0) {
						resources[0] = panel.getSelectedResources();
					}
				}
			}
		});
		
		if (resources[0] == null) {
			return null;
		}
		
		CompositeOperation op = new CompositeOperation("Operation.UOverrideAndUpdate");

		boolean detectDeleted = SVNTeamPreferences.getResourceSelectionBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.DETECT_DELETED_PROJECTS_NAME);
		final IResourceProvider detectOp = detectDeleted ? new DetectDeletedProjectsOperation(resources[0]) : new IResourceProvider() {
			public IResource[] getResources() {
				return resources[0];
			}
		};
		
		if (detectDeleted) {
			op.add((IActionOperation)detectOp);
		}
		
		SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(detectOp);
		op.add(saveOp);
		AbstractActionOperation revertOp = new RevertOperation(new IResourceProvider() {
			public IResource[] getResources() {
				return FileUtility.getResourcesRecursive(detectOp.getResources(), IStateFilter.SF_REVERTABLE, IResource.DEPTH_ZERO);
			}
		}, true);
		op.add(revertOp);
		op.add(new ClearLocalStatusesOperation(resources[0]));
		AbstractActionOperation removeNonVersionedResourcesOp = new RemoveNonVersionedResourcesOperation(detectOp, true);
		op.add(removeNonVersionedResourcesOp);
		// Obstructed resources are deleted now. So, try to revert all corresponding entries
		RevertOperation revertOp1 = new RevertOperation(FileUtility.getResourcesRecursive(resources[0], IStateFilter.SF_OBSTRUCTED, IResource.DEPTH_ZERO), true);
		op.add(revertOp1);
		op.add(new ClearLocalStatusesOperation(resources[0]));
		op.add(new UpdateOperation(new IResourceProvider() {
			public IResource[] getResources() {
				return 
					FileUtility.getResourcesRecursive(detectOp.getResources(), new IStateFilter.AbstractStateFilter() {
						protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
							return IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask) || IStateFilter.SF_NOTEXISTS.accept(resource, state, mask);
						}
						protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
							return true;
						}
					}, IResource.DEPTH_ZERO);
			}
		}, true), new IActionOperation[] {revertOp, revertOp1, removeNonVersionedResourcesOp});
		op.add(new RestoreProjectMetaOperation(saveOp));
		if (detectDeleted) {
			op.add(new ProcessDeletedProjectsOperation((DetectDeletedProjectsOperation)detectOp));
		}
		op.add(new ClearUpdateStatusesOperation(resources[0]));
		op.add(new RefreshResourcesOperation(resources[0]/*, IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL*/));

		return op;
	}

}
