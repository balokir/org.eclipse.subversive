/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize.action.logicalmodel;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeLogicalModelAction;
import org.eclipse.team.svn.ui.synchronize.action.ShowHistoryActionHelper;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Show resource history logical model action
 * 
 * @author Igor Burilo
 */
public class ShowHistoryModelAction extends AbstractSynchronizeLogicalModelAction {

	protected ShowHistoryActionHelper actionHelper;
	
	public ShowHistoryModelAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);	
		this.actionHelper = new ShowHistoryActionHelper(this, configuration);
	}
	
	protected boolean needsToSaveDirtyEditors() {	
		return false;
	}
	
	protected boolean updateSelection(IStructuredSelection selection) {
		if (super.updateSelection(selection)) {
			if (selection.size() == 1) {						
				AbstractSVNSyncInfo syncInfo = this.getSelectedSVNSyncInfo();
				if (syncInfo != null) {
					ILocalResource incoming = syncInfo.getRemoteChangeResource();
					if (incoming instanceof IResourceChange) {
						return IStateFilter.SF_TREE_CONFLICTING.accept(incoming) ? IStateFilter.SF_TREE_CONFLICTING_REPOSITORY_EXIST.accept(incoming) : IStateFilter.ST_DELETED != incoming.getStatus();
					}
				}
				IResource selectedResource = this.getSelectedResource();
				if (selectedResource != null) {
					return IStateFilter.SF_ONREPOSITORY.accept(SVNRemoteStorage.instance().asLocalResource(selectedResource));
				}
			}	
		}
		return false;
	}
	
	protected IActionOperation getOperation() {
		return this.actionHelper.getOperation();
	}
	
}
