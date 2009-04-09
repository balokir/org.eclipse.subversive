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

package org.eclipse.team.svn.ui.synchronize.action;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Edit tree conflicts action implementation for Synchronize view
 * 
 * @author Igor Burilo
 */
public class EditTreeConflictsAction extends AbstractSynchronizeModelAction {

	protected EditTreeConflictsActionHelper actionHelper;
	
	public EditTreeConflictsAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
		this.actionHelper = new EditTreeConflictsActionHelper(this, configuration);
	}
	
	protected boolean updateSelection(IStructuredSelection selection) {
		if (selection.getFirstElement() instanceof SyncInfoModelElement) {
			AbstractSVNSyncInfo syncInfo = (AbstractSVNSyncInfo)((SyncInfoModelElement)selection.getFirstElement()).getSyncInfo();
			return IStateFilter.SF_TREE_CONFLICTING.accept(syncInfo.getLocalResource());
		}			
		if (selection.getFirstElement() instanceof ISynchronizeModelElement) {
			ISynchronizeModelElement element = (ISynchronizeModelElement)selection.getFirstElement();
			return IStateFilter.SF_TREE_CONFLICTING.accept(SVNRemoteStorage.instance().asLocalResource(element.getResource()));
		}
		return false;
	}
	
	protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		return this.actionHelper.getOperation();
	}
}
