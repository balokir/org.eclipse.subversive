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

package org.eclipse.team.svn.ui.action;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Abstract team action implementation with the recursive resources selection
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractRecursiveTeamAction extends AbstractNonRecursiveTeamAction implements IResourceSelector {

	public AbstractRecursiveTeamAction() {
		super();
	}

	public IResource []getSelectedResourcesRecursive(IStateFilter filter) {
		return this.getSelectedResourcesRecursive(filter, IResource.DEPTH_INFINITE);
	}
	
	public IResource []getSelectedResourcesRecursive(final IStateFilter filter, final int depth) {
		final IResource [][]retVal = new IResource[][] {new IResource[0]};
		IActionOperation op = new AbstractActionOperation("Operation.CollectingResources") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				retVal[0] = FileUtility.getResourcesRecursive(AbstractRecursiveTeamAction.this.getSelectedResources(), filter, depth, this, monitor);
			}
		};
		if (CoreExtensionsManager.instance().getOptionProvider().isSVNCacheEnabled()) {
			UIMonitorUtility.doTaskBusyDefault(op);
		}
		else {
			UIMonitorUtility.doTaskNowDefault(this.getShell(), op, true);
		}
		return retVal[0];
	}
	
}
