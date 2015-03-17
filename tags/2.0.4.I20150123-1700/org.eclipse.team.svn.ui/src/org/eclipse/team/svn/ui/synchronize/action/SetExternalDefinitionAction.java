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
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Set externals properties operation implementation for Synchronize view
 * 
 * @author Igor Burilo
 */
public class SetExternalDefinitionAction extends AbstractSynchronizeModelAction {

	public SetExternalDefinitionAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {		
		return org.eclipse.team.svn.ui.action.local.SetExternalDefinitionAction.getAction(this.getSelectedResource(), configuration.getSite().getShell());
	}
	
	protected boolean updateSelection(IStructuredSelection selection) {
		if (super.updateSelection(selection) && selection.size() == 1) {
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(this.getSelectedResource());
			return IStateFilter.SF_VERSIONED_FOLDERS.accept(local); 						
		}
		return false;
	}
	
	protected boolean needsToSaveDirtyEditors() {
		return false;
	}
}
