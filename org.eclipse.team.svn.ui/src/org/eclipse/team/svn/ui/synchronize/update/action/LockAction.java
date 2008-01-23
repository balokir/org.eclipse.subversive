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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.LockOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.utility.FileUtility;
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

	public LockAction(String text, ISynchronizePageConfiguration configuration,
			ISelectionProvider selectionProvider) {
		super(text, configuration, selectionProvider);
	}
	
	protected FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter.SyncInfoDirectionFilter(new int[] {SyncInfo.INCOMING, SyncInfo.OUTGOING, SyncInfo.CONFLICTING}) {
            public boolean select(SyncInfo info) {
                if (super.select(info)) {
                    UpdateSyncInfo sync = (UpdateSyncInfo)info;
                    return !(IStateFilter.SF_OBSTRUCTED.accept(sync.getLocalResource()));
                }
                return false;
            }
        };
	}
	
	protected IActionOperation execute(final FilteredSynchronizeModelOperation operation) {
		final IActionOperation [] op = new IActionOperation[1];
		operation.getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				IResource [] selectedResources = operation.getSelectedResources();
			    boolean containsFolder = false;
			    for (int i = 0; i < selectedResources.length; i++) {
			    	if (selectedResources[i] instanceof IContainer) {
			    		containsFolder = true;
			    		break;
			    	}
			    }
				CommitPanel.CollectPropertiesOperation cop = new CommitPanel.CollectPropertiesOperation(selectedResources);
				ProgressMonitorUtility.doTaskExternal(cop, null);
				LockPanel commentPanel = new LockPanel(!containsFolder, cop.getMinLockSize());
				DefaultDialog dialog = new DefaultDialog(operation.getShell(), commentPanel);
				if (dialog.open() == 0) {
				    IResource []resources = FileUtility.getResourcesRecursive(selectedResources, org.eclipse.team.svn.ui.action.local.LockAction.SF_NONLOCKED, commentPanel.isRecursive() ? IResource.DEPTH_INFINITE : IResource.DEPTH_ONE);
				    LockOperation mainOp = new LockOperation(resources, commentPanel.getMessage(), commentPanel.getForce());
				    CompositeOperation lockOp = new CompositeOperation(mainOp.getId());
				    lockOp.add(mainOp);
				    lockOp.add(new RefreshResourcesOperation(resources));
				    op[0] = lockOp;
				}
			}
		});
		return op[0];
	}

	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		Object [] selectionArr = selection.toArray();
		if (selection.size() > 0) {
			for (int i = 0; i < selection.size(); i++) {
				if (!(selectionArr[i] instanceof SyncInfoModelElement)) {
					return false;
				}
				SyncInfoModelElement element = (SyncInfoModelElement)selectionArr[i];
				IResource[] resource = {element.getResource()};
				if (!FileUtility.checkForResourcesPresence(resource, org.eclipse.team.svn.ui.action.local.LockAction.SF_NONLOCKED, IResource.DEPTH_ZERO)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
