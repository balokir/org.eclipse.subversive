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
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeLogicalModelAction;
import org.eclipse.team.svn.ui.synchronize.action.ShowOutgoingPropertiesActionHelper;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Show properties logical model action implementation for Synchronize view.
 * 
 * @author Igor Burilo
 */
public class ShowOutgoingPropertiesModelAction extends AbstractSynchronizeLogicalModelAction {

	protected ShowOutgoingPropertiesActionHelper actionHelper;
	
	public ShowOutgoingPropertiesModelAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
		this.actionHelper = new ShowOutgoingPropertiesActionHelper(this, configuration);
	}

	protected boolean needsToSaveDirtyEditors() {
		return false;
	}
	
	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		if (selection.size() == 1) {	
			IResource resource = this.getSelectedResource();
			return IStateFilter.SF_VERSIONED.accept(SVNRemoteStorage.instance().asLocalResource(resource));
		}
	    return false;
	}
	
	protected IActionOperation getOperation() {
		return this.actionHelper.getOperation();
	}

}
