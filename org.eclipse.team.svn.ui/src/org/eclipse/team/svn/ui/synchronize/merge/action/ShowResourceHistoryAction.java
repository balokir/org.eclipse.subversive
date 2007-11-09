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

package org.eclipse.team.svn.ui.synchronize.merge.action;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.ui.operation.ShowHistoryViewOperation;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeModelAction;
import org.eclipse.team.svn.ui.synchronize.merge.MergeSyncInfo;
import org.eclipse.team.svn.ui.synchronize.variant.RemoteResourceVariant;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Show resource history action
 * 
 * @author Alexander Gurov
 */
public class ShowResourceHistoryAction extends AbstractSynchronizeModelAction {

	public ShowResourceHistoryAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		if (selection.size() != 1 || !(selection.getFirstElement() instanceof SyncInfoModelElement)) {
		    return false;
		}
		return true;
	}
	
	protected IActionOperation execute(final FilteredSynchronizeModelOperation operation) {
	    MergeSyncInfo info = (MergeSyncInfo)operation.getSVNSyncInfo();
	    ILocalResource local = ((RemoteResourceVariant)info.getRemote()).getResource();
		if (local instanceof IResourceChange) {
			// merge info always contains originator
			return new ShowHistoryViewOperation(((IResourceChange)local).getOriginator(), 0, 0);
		}
		else {
			return new ShowHistoryViewOperation(local.getResource(), 0, 0);
		}
	}

}
