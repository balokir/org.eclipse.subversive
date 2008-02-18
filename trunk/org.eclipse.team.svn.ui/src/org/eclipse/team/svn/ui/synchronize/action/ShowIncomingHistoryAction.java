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

package org.eclipse.team.svn.ui.synchronize.action;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.ui.operation.ShowHistoryViewOperation;
import org.eclipse.team.svn.ui.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.ui.synchronize.variant.RemoteResourceVariant;
import org.eclipse.team.svn.ui.synchronize.variant.ResourceVariant;
import org.eclipse.team.svn.ui.synchronize.variant.VirtualRemoteResourceVariant;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Show resource history action
 * 
 * @author Alexander Gurov
 */
public class ShowIncomingHistoryAction extends AbstractSynchronizeModelAction {

	public ShowIncomingHistoryAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		if (selection.size() == 1) {
			ISynchronizeModelElement element = (ISynchronizeModelElement)selection.getFirstElement();
			if (element instanceof SyncInfoModelElement) {
				AbstractSVNSyncInfo syncInfo = (AbstractSVNSyncInfo)((SyncInfoModelElement)element).getSyncInfo();
				ILocalResource incoming = ((ResourceVariant)syncInfo.getRemote()).getResource();
				if (!(syncInfo.getRemote() instanceof VirtualRemoteResourceVariant) && !IStateFilter.SF_NOTEXISTS.accept(incoming) && 
					(!IStateFilter.SF_DELETED.accept(incoming) || IStateFilter.SF_REPLACED.accept(incoming))) {
					return true;
				}
			}
		}
		return false;
	}

	protected IActionOperation execute(final FilteredSynchronizeModelOperation operation) {
	    IResourceChange change = (IResourceChange)((RemoteResourceVariant)operation.getSVNSyncInfo().getRemote()).getResource();
		return new ShowHistoryViewOperation(change.getOriginator(), 0, 0);
	}

}
