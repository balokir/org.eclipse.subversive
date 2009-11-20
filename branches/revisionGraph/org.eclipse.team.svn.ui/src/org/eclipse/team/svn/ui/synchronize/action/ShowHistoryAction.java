/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize.action;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.core.synchronize.variant.ResourceVariant;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Show resource history action
 * 
 * @author Alexander Gurov
 */
public class ShowHistoryAction extends AbstractSynchronizeModelAction {
	
	protected ShowHistoryActionHelper actionHelper;
	
	public ShowHistoryAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
		this.actionHelper = new ShowHistoryActionHelper(this, configuration);
	}

	protected boolean needsToSaveDirtyEditors() {
		return false;
	}
	
	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		if (selection.size() == 1) {
			if (selection.getFirstElement() instanceof SyncInfoModelElement) {
				AbstractSVNSyncInfo syncInfo = (AbstractSVNSyncInfo)((SyncInfoModelElement)selection.getFirstElement()).getSyncInfo();
				ILocalResource incoming = ((ResourceVariant)syncInfo.getRemote()).getResource();
				if (incoming instanceof IResourceChange) {
					return IStateFilter.SF_TREE_CONFLICTING.accept(incoming) ? IStateFilter.SF_TREE_CONFLICTING_REPOSITORY_EXIST.accept(incoming) : IStateFilter.ST_DELETED != incoming.getStatus(); 
				}
			}
			if (selection.getFirstElement() instanceof ISynchronizeModelElement) {
				ISynchronizeModelElement element = (ISynchronizeModelElement)selection.getFirstElement();
				return IStateFilter.SF_ONREPOSITORY.accept(SVNRemoteStorage.instance().asLocalResource(element.getResource()));
			}
		}
		return false;
	}

	protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		return this.actionHelper.getOperation();
	}

}
