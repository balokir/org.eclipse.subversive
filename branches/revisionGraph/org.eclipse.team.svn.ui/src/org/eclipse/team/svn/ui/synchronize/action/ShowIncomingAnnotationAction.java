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
import org.eclipse.team.svn.core.resource.IFileChange;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.core.synchronize.variant.RemoteResourceVariant;
import org.eclipse.team.svn.core.synchronize.variant.ResourceVariant;
import org.eclipse.team.svn.ui.operation.RemoteShowAnnotationOperation;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Show annotation action
 * 
 * @author Alexander Gurov
 */
public class ShowIncomingAnnotationAction extends AbstractSynchronizeModelAction {

	public ShowIncomingAnnotationAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected boolean needsToSaveDirtyEditors() {
		return false;
	}
	
	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		if (selection.size() == 1 && selection.getFirstElement() instanceof SyncInfoModelElement) {
			AbstractSVNSyncInfo syncInfo = (AbstractSVNSyncInfo)((SyncInfoModelElement)selection.getFirstElement()).getSyncInfo();
			ILocalResource incoming = ((ResourceVariant)syncInfo.getRemote()).getResource();
			if (incoming instanceof IFileChange) {
				return IStateFilter.SF_TREE_CONFLICTING.accept(incoming) ? IStateFilter.SF_TREE_CONFLICTING_REPOSITORY_EXIST.accept(incoming) : IStateFilter.ST_DELETED != incoming.getStatus();
			} 			
		}
		return false;
	}

	protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
	    IResourceChange change = (IResourceChange)((RemoteResourceVariant)this.getSelectedSVNSyncInfo().getRemote()).getResource();
		return new RemoteShowAnnotationOperation(change.getOriginator());
	}

}
