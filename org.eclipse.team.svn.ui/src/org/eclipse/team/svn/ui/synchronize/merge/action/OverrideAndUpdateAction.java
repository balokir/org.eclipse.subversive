/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Tobias Bosch - [patch] bug fix: Quick fix for JavaSVN problem with "force" option
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize.merge.action;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.ClearLocalStatusesOperation;
import org.eclipse.team.svn.core.operation.local.MergeOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RemoveNonVersionedResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.RevertOperation;
import org.eclipse.team.svn.core.operation.local.SaveProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.UpdateOperation;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.operation.ClearMergeStatusesOperation;
import org.eclipse.team.svn.ui.panel.local.OverrideResourcesPanel;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeModelAction;
import org.eclipse.team.svn.ui.synchronize.action.ISyncStateFilter;
import org.eclipse.team.svn.ui.synchronize.merge.MergeSubscriber;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Override and update action
 * 
 * @author Alexander Gurov
 */
public class OverrideAndUpdateAction extends AbstractSynchronizeModelAction {

	public OverrideAndUpdateAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter.SyncInfoDirectionFilter(new int[] {SyncInfo.CONFLICTING, SyncInfo.INCOMING});
	}

	protected IActionOperation execute(final FilteredSynchronizeModelOperation operation) {
		final IResource [][]resources = new IResource[1][];
		operation.getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				IResource []obstructedResources = operation.getSelectedResourcesRecursive(IStateFilter.SF_OBSTRUCTED);
				obstructedResources = FileUtility.addOperableParents(obstructedResources, IStateFilter.SF_OBSTRUCTED);
				HashSet allResources = new HashSet(Arrays.asList(obstructedResources));
				IResource []changedResources = operation.getSelectedResourcesRecursive(ISyncStateFilter.SF_OVERRIDE);
				changedResources = FileUtility.addOperableParents(changedResources, IStateFilter.SF_NOTONREPOSITORY);
				allResources.addAll(Arrays.asList(changedResources));
				IResource []fullSet = (IResource [])allResources.toArray(new IResource[allResources.size()]);
				OverrideResourcesPanel panel = new OverrideResourcesPanel(fullSet, fullSet, OverrideResourcesPanel.MSG_UPDATE);
				DefaultDialog dialog = new DefaultDialog(operation.getShell(), panel);
				if (dialog.open() == 0) {
					resources[0] = panel.getSelectedResources();
				}
			}
		});
		
		if (resources[0] == null) {
			return null;
		}
		
		CompositeOperation op = new CompositeOperation("Operation.MOverrideAndUpdate");

		SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(resources[0]);
		op.add(saveOp);
		RevertOperation revertOp = new RevertOperation(FileUtility.getResourcesRecursive(resources[0], IStateFilter.SF_REVERTABLE, IResource.DEPTH_ZERO), true);
		op.add(revertOp);
		op.add(new ClearLocalStatusesOperation(resources[0]));
		RemoveNonVersionedResourcesOperation removeNonVersionedResourcesOp = new RemoveNonVersionedResourcesOperation(resources[0], true);
		op.add(removeNonVersionedResourcesOp);
		// Obstructed resources are deleted now. So, try to revert all corresponding entries
		RevertOperation revertOp1 = new RevertOperation(FileUtility.getResourcesRecursive(resources[0], IStateFilter.SF_OBSTRUCTED, IResource.DEPTH_ZERO), true);
		op.add(revertOp1);
		op.add(new ClearLocalStatusesOperation(resources[0]));
		op.add(new UpdateOperation(FileUtility.getResourcesRecursive(resources[0], IStateFilter.SF_OBSTRUCTED, IResource.DEPTH_ZERO), true));
		
		op.add(new MergeOperation(resources[0], MergeSubscriber.instance().getMergeScope().getMergeSet(), true));
        
		op.add(new RestoreProjectMetaOperation(saveOp));
		op.add(new ClearMergeStatusesOperation(resources[0]));
		op.add(new RefreshResourcesOperation(resources[0]));
		
		return op;
	}

}
