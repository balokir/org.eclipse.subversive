/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize.update.action;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.management.CleanupOperation;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeModelAction;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Cleanup action implementation for Synchronize view
 * 
 * @author Alexei Goncharov
 */
public class CleanUpAction extends AbstractSynchronizeModelAction {

	public CleanUpAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	public CleanUpAction(String text, ISynchronizePageConfiguration configuration, ISelectionProvider selectionProvider) {
		super(text, configuration, selectionProvider);
	}

	protected IActionOperation execute(FilteredSynchronizeModelOperation operation) {
		IResource []resources = FileUtility.getResourcesRecursive(operation.getSelectedResources(), IStateFilter.SF_VERSIONED_FOLDERS, IResource.DEPTH_INFINITE);
		CleanupOperation mainOp = new CleanupOperation(resources);
		CompositeOperation op = new CompositeOperation(mainOp.getId());
		op.add(mainOp);
		op.add(new RefreshResourcesOperation(resources));
		return op;
	}
	
	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		Object [] selectionArr = selection.toArray();
		if (selection.size() > 0) {
			for (int i = 0; i < selection.size(); i++) {
				if (!(selectionArr[i] instanceof SyncInfoModelElement)) {
					return false;
				}
				SyncInfoModelElement element = (SyncInfoModelElement)selectionArr[i];
				IResource[] resource = {element.getResource()};
				if (!FileUtility.checkForResourcesPresence(resource, IStateFilter.SF_VERSIONED_FOLDERS, IResource.DEPTH_ZERO) || !(element.getResource() instanceof IProject)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
