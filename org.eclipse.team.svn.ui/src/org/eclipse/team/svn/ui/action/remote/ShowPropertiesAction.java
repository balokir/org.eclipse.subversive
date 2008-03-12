/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.action.remote;

import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.operation.IResourcePropertyProvider;
import org.eclipse.team.svn.core.operation.remote.GetRemotePropertiesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.operation.ShowPropertiesOperation;
import org.eclipse.ui.PlatformUI;

/**
 * Show properties action imlementation
 * 
 * @author Sergiy Logvin
 */
public class ShowPropertiesAction extends AbstractRepositoryTeamAction {
	
	public ShowPropertiesAction() {
		super();
	}
		
	public void runImpl(IAction action) {
		IRepositoryResource resource = this.getSelectedRepositoryResources()[0];
		IResourcePropertyProvider provider = new GetRemotePropertiesOperation(resource);		
		ShowPropertiesOperation op = new ShowPropertiesOperation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), resource, provider);
		this.runScheduled(op);
	}
	
	public boolean isEnabled() {
		return this.getSelectedRepositoryResources().length == 1;
	}
	
}
