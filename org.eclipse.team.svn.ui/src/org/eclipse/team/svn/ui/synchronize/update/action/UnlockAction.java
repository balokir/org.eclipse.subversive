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
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeModelAction;
import org.eclipse.team.svn.ui.synchronize.update.UpdateSyncInfo;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Unlock action implementation for Synchronize View
 * 
 * @author Alexei Goncharov
 */
public class UnlockAction extends AbstractSynchronizeModelAction {

	public UnlockAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	public UnlockAction(String text,
			ISynchronizePageConfiguration configuration,
			ISelectionProvider selectionProvider) {
		super(text, configuration, selectionProvider);
	}
	
	protected FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter.SyncInfoDirectionFilter(new int[] {SyncInfo.IN_SYNC}) {
            public boolean select(SyncInfo info) {
                if (super.select(info)) {
                    UpdateSyncInfo sync = (UpdateSyncInfo)info;
                    return !(IStateFilter.SF_OBSTRUCTED.accept(sync.getLocalResource()));
                }
                return false;
            }
        };
	}

	protected IActionOperation execute(FilteredSynchronizeModelOperation operation) {
		// TODO Auto-generated method stub
		return null;
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
				if (!FileUtility.checkForResourcesPresence(resource, org.eclipse.team.svn.ui.action.local.UnlockAction.SF_LOCKED, IResource.DEPTH_ZERO)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
