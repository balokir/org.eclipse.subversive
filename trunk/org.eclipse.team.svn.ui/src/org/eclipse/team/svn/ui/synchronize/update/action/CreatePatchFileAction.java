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

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.CreatePatchOperation;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.extension.factory.ICommitDialog;
import org.eclipse.team.svn.ui.panel.local.CommitPanel;
import org.eclipse.team.svn.ui.synchronize.SVNChangeSetCapability;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeModelAction;
import org.eclipse.team.svn.ui.synchronize.update.UpdateSyncInfo;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Create patch file action for Synchronize View
 * 
 * @author Alexei Goncharov
 */
public class CreatePatchFileAction extends AbstractSynchronizeModelAction {

	public CreatePatchFileAction(String text,
			ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	public CreatePatchFileAction(String text,
			ISynchronizePageConfiguration configuration,
			ISelectionProvider selectionProvider) {
		super(text, configuration, selectionProvider);
	}
	
	protected FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter.SyncInfoDirectionFilter(new int[] {SyncInfo.CONFLICTING, SyncInfo.OUTGOING}) {
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
			    IResource resource = operation.getSelectedResource();
			    FileDialog dlg = new FileDialog(UIMonitorUtility.getShell(), SWT.PRIMARY_MODAL | SWT.SAVE);
				dlg.setText(SVNTeamUIPlugin.instance().getResource("SelectPatchFilePage.SavePatchAs"));
				dlg.setFileName(resource.getName() + ".patch");
				dlg.setFilterExtensions(new String[] {"patch", "*.*"});
				String file = dlg.open();
				if (file != null) {
					op[0] = new CreatePatchOperation(resource, file, true, true, true, true, true);
				}
			}
		});
		return op[0];
	}

	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		if (selection.size() != 1 || !(selection.getFirstElement() instanceof SyncInfoModelElement)) {
		    return false;
		}
		SyncInfoModelElement element = (SyncInfoModelElement)selection.getFirstElement();
		IResource[] resource = {element.getResource()};
		return !FileUtility.checkForResourcesPresenceRecursive(resource, IStateFilter.SF_UNVERSIONED);
	}
}
