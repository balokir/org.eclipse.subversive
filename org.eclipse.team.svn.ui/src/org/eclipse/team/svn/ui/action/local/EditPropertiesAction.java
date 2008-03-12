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

package org.eclipse.team.svn.ui.action.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IResourcePropertyProvider;
import org.eclipse.team.svn.core.operation.local.property.GetPropertiesOperation;
import org.eclipse.team.svn.ui.action.AbstractWorkingCopyAction;
import org.eclipse.team.svn.ui.operation.ShowPropertiesOperation;

/**
 * Team services menu "edit resource properties" action implementation
 * 
 * @author Alexander Gurov
 */
public class EditPropertiesAction extends AbstractWorkingCopyAction {
    
	public EditPropertiesAction() {
		super();
	}
	
	public void runImpl(IAction action) {
		IResource []resources = this.getSelectedResources(IStateFilter.SF_EXCLUDE_PREREPLACED_AND_DELETED);
		IResourcePropertyProvider provider = new GetPropertiesOperation(resources[0]);
		ShowPropertiesOperation op = new ShowPropertiesOperation(this.getTargetPage(), resources[0], provider);
		this.runScheduled(op);
	}

	public boolean isEnabled() {
		return 
			this.getSelectedResources().length == 1 &&
			this.checkForResourcesPresence(IStateFilter.SF_EXCLUDE_PREREPLACED_AND_DELETED);
	}

}
