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
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeLogicalModelAction;
import org.eclipse.team.svn.ui.synchronize.action.SetPropertyActionHelper;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Set property logical model action implementation for Synchronize view
 * 
 * @author Igor Burilo
 */
public class SetPropertyModelAction extends AbstractSynchronizeLogicalModelAction {

	protected SetPropertyActionHelper actionHelper;
	
	public SetPropertyModelAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
		this.actionHelper = new SetPropertyActionHelper(this, configuration);
	}

	protected boolean needsToSaveDirtyEditors() {
		return false;
	}
	
	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		
		IResource[] selectedResources = this.getAllSelectedResources();
		if (FileUtility.checkForResourcesPresence(selectedResources, IStateFilter.SF_VERSIONED, IResource.DEPTH_ZERO)) {
			return true;
		}
	    return false;
	}
	
	protected IActionOperation getOperation() {
		return this.actionHelper.getOperation();
	}

}
