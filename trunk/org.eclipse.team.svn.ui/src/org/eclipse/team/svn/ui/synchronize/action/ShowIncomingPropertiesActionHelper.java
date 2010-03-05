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

import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IResourcePropertyProvider;
import org.eclipse.team.svn.core.operation.remote.GetRemotePropertiesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.ui.operation.ShowPropertiesOperation;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.PlatformUI;

/**
 * Show properties action helper implementation for Synchronize view.
 * 
 * @author Igor Burilo
 */
public class ShowIncomingPropertiesActionHelper extends AbstractActionHelper {

	public ShowIncomingPropertiesActionHelper(IAction action, ISynchronizePageConfiguration configuration) {
		super(action, configuration);		
	}

	public IActionOperation getOperation() {
		IResourceChange change = (IResourceChange)this.getSelectedSVNSyncInfo().getRemoteChangeResource();
	    IRepositoryResource remote = change.getOriginator();
		IResourcePropertyProvider provider = new GetRemotePropertiesOperation(remote);
		ShowPropertiesOperation op = new ShowPropertiesOperation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), remote, provider);
		return op;
	}

}
