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
import org.eclipse.team.svn.core.synchronize.variant.RemoteResourceVariant;
import org.eclipse.team.svn.core.synchronize.variant.ResourceVariant;
import org.eclipse.team.svn.ui.operation.ShowHistoryViewOperation;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeLogicalModelAction;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Show resource history logical model action
 * 
 * @author Igor Burilo
 */
public class ShowHistoryModelAction extends AbstractSynchronizeLogicalModelAction {

	public ShowHistoryModelAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);		
	}
	
	protected boolean needsToSaveDirtyEditors() {	
		return false;
	}
	
	protected boolean updateSelection(IStructuredSelection selection) {
		if (super.updateSelection(selection)) {
			if (selection.size() == 1) {						
				AbstractSVNSyncInfo syncInfo = this.getSelectedSVNSyncInfo();
				if (syncInfo != null) {
					ILocalResource incoming = ((ResourceVariant)syncInfo.getRemote()).getResource();
					if (incoming instanceof IResourceChange) {
						return IStateFilter.ST_DELETED != incoming.getStatus();
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
		AbstractSVNSyncInfo info = this.getSelectedSVNSyncInfo();
		if (info != null ) {
			RemoteResourceVariant variant = (RemoteResourceVariant)info.getRemote();
			if (variant.getResource() instanceof IResourceChange) {
				return new ShowHistoryViewOperation(((IResourceChange)variant.getResource()).getOriginator(), 0, 0);
			}
		}
		return new ShowHistoryViewOperation(this.getSelectedResource(), 0, 0);
	}
	
}
