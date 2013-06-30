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

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IResourcePropertyProvider;
import org.eclipse.team.svn.core.operation.local.property.GetPropertiesOperation;
import org.eclipse.team.svn.ui.operation.ShowPropertiesOperation;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.PlatformUI;

/**
 * Show properties action helper implementation for Synchronize view.
 * 
 * @author Igor Burilo
 */
public class ShowOutgoingPropertiesActionHelper extends AbstractActionHelper {

	public ShowOutgoingPropertiesActionHelper(IAction action, ISynchronizePageConfiguration configuration) {
		super(action, configuration);
	}

	public IActionOperation getOperation() {
		IResource selectedResource = this.getSelectedResource();
		IResourcePropertyProvider provider = new GetPropertiesOperation(selectedResource);
		ShowPropertiesOperation op = new ShowPropertiesOperation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), selectedResource, provider);
		return op;
	}

}
