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

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RevertOperation;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.local.RevertPanel;
import org.eclipse.team.svn.ui.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeModelAction;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Synchronize view revert action implementation
 * 
 * @author Alexander Gurov
 */
public class RevertAction extends AbstractSynchronizeModelAction {

	public RevertAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter() {
			public boolean select(SyncInfo info) {
				AbstractSVNSyncInfo sync = (AbstractSVNSyncInfo)info;
				return IStateFilter.SF_REVERTABLE.accept(sync.getLocalResource());
			}
		};
	}

	protected IActionOperation execute(final FilteredSynchronizeModelOperation operation) {
		final IResource [][]resources = new IResource[1][];
		operation.getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				IResource []changedResources = operation.getSelectedResourcesRecursive(IStateFilter.SF_REVERTABLE);
				IResource []userSelectedResources = operation.getSelectedResources();
				RevertPanel panel = new RevertPanel(changedResources, userSelectedResources);
				DefaultDialog rDlg = new DefaultDialog(operation.getShell(), panel);
				if (rDlg.open() == 0) {
					resources[0] = panel.getSelectedResources();
				}
			}
		});
		
		if (resources[0] == null) {
			return null;
		}
		
		RevertOperation mainOp =  new RevertOperation(resources[0], false);
		
		CompositeOperation op = new CompositeOperation(mainOp.getId());

		op.add(mainOp);
		op.add(new RefreshResourcesOperation(resources[0]/*, IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL*/));

		return op;
	}

}
