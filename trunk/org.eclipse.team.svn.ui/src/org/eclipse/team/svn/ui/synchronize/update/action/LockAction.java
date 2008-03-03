/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize.update.action;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.LockOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.local.CommitPanel;
import org.eclipse.team.svn.ui.panel.local.LockPanel;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeModelAction;
import org.eclipse.team.svn.ui.synchronize.update.UpdateSyncInfo;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Lock action implementation for Synchronize View
 * 
 * @author Alexei Goncharov
 */
public class LockAction extends AbstractSynchronizeModelAction {
	public LockAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}
	
	protected boolean needsToSaveDirtyEditors() {
		return false;
	}
	
	protected FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter.SyncInfoDirectionFilter(new int[] {SyncInfo.OUTGOING, SyncInfo.CONFLICTING}) {
            public boolean select(SyncInfo info) {
                return super.select(info) && IStateFilter.SF_READY_TO_LOCK.accept(((UpdateSyncInfo)info).getLocalResource());
            }
        };
	}

	protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		IResource [] selectedResources = LockAction.this.syncInfoSelector.getSelectedResources();
		CommitPanel.CollectPropertiesOperation cop = new CommitPanel.CollectPropertiesOperation(selectedResources);
		ProgressMonitorUtility.doTaskExternal(cop, null);
		LockPanel commentPanel = new LockPanel(true, cop.getMinLockSize());
		DefaultDialog dialog = new DefaultDialog(configuration.getSite().getShell(), commentPanel);
		if (dialog.open() == 0) {
		    LockOperation mainOp = new LockOperation(selectedResources, commentPanel.getMessage(), commentPanel.getForce());
		    CompositeOperation lockOp = new CompositeOperation(mainOp.getId());
		    lockOp.add(mainOp);
		    lockOp.add(new RefreshResourcesOperation(selectedResources));
		    return lockOp;
		}
		return null;
	}

}
