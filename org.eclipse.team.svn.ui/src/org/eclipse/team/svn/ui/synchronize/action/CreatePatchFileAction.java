/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize.action;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.CreatePatchOperation;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Create patch file action for Synchronize View
 * 
 * @author Alexei Goncharov
 */
public class CreatePatchFileAction extends AbstractSynchronizeModelAction {
	public CreatePatchFileAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter.SyncInfoDirectionFilter(new int[] {SyncInfo.OUTGOING, SyncInfo.CONFLICTING}) {
            public boolean select(SyncInfo info) {
                return super.select(info) && IStateFilter.SF_ANY_CHANGE.accept(((AbstractSVNSyncInfo)info).getLocalResource());
            }
        };
	}
	
	protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		IResource []resources = FileUtility.shrinkChildNodes(this.syncInfoSelector.getSelectedResources(new ISyncStateFilter.StateFilterWrapper(IStateFilter.SF_ANY_CHANGE, true)));
	    FileDialog dlg = new FileDialog(configuration.getSite().getShell(), SWT.PRIMARY_MODAL | SWT.SAVE);
		dlg.setText(SVNTeamUIPlugin.instance().getResource("SelectPatchFilePage.SavePatchAs"));
		dlg.setFileName(resources[0].getName() + ".patch");
		dlg.setFilterExtensions(new String[] {"patch", "*.*"});
		String file = dlg.open();
		return file == null ? null : new CreatePatchOperation(resources, file, true, true, true, true);
	}

}
