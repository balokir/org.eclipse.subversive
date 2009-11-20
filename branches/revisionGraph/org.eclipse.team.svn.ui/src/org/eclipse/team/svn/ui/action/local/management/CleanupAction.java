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

package org.eclipse.team.svn.ui.action.local.management;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.management.CleanupOperation;
import org.eclipse.team.svn.ui.action.AbstractWorkingCopyAction;

/**
 * Cleanup working copy after failure action implementation
 * 
 * @author Alexander Gurov
 */
public class CleanupAction extends AbstractWorkingCopyAction {

	public CleanupAction() {
		super();
	}
	
	public void runImpl(IAction action) {
		IResource []resources = this.getSelectedResources(IStateFilter.SF_VERSIONED_FOLDERS);

		CleanupOperation mainOp = new CleanupOperation(resources);
		CompositeOperation op = new CompositeOperation(mainOp.getId());

		op.add(mainOp);
		op.add(new RefreshResourcesOperation(resources));
		this.runScheduled(op);
	}

	public boolean isEnabled() {
		return this.checkForResourcesPresence(IStateFilter.SF_VERSIONED_FOLDERS);
	}
	
	protected boolean needsToSaveDirtyEditors() {
		return true;
	}
	
}
