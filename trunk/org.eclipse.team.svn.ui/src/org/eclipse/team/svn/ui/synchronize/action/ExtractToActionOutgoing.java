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

package org.eclipse.team.svn.ui.synchronize.action;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.ExtractToOperationLocal;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Outgoing Extract To action for Synchronize View
 * 
 * @author Alexei Goncharov
 */
public class ExtractToActionOutgoing extends AbstractSynchronizeModelAction {

	public ExtractToActionOutgoing(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}
	
	protected FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter.SyncInfoDirectionFilter(new int[] {SyncInfo.OUTGOING, SyncInfo.CONFLICTING});
	}
	protected IActionOperation execute(final FilteredSynchronizeModelOperation operation) {
		IResource [] outgoingResources = operation.getSelectedResourcesRecursive(new IStateFilter.AbstractStateFilter() {

			protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
				return IStateFilter.SF_COMMITABLE.accept(resource, state, mask)
				|| IStateFilter.SF_CONFLICTING.accept(resource, state, mask)
				|| IStateFilter.SF_NEW.accept(resource, state, mask);
			}

			protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
				return IStateFilter.SF_COMMITABLE.accept(resource, state, mask)
				|| IStateFilter.SF_CONFLICTING.accept(resource, state, mask)
				|| IStateFilter.SF_NEW.accept(resource, state, mask);
			}
			
		});
		final String path[] = {null};
		operation.getShell().getDisplay().syncExec(new Runnable () {
			public void run() {
				DirectoryDialog fileDialog = new DirectoryDialog(operation.getShell());
				fileDialog.setText(SVNTeamUIPlugin.instance().getResource("ExtractToAction.Select.Title"));
				fileDialog.setMessage(SVNTeamUIPlugin.instance().getResource("ExtractToAction.Select.Description"));
				path[0] = fileDialog.open();
			}
		});
		if (path[0] != null) {
			return new ExtractToOperationLocal(outgoingResources, operation.getSelectedResourcesRecursive(), path[0], true);
		}
		return null;
	}
	
}
