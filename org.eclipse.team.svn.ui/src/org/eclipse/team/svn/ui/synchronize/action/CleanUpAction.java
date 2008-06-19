/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize.action;

import java.util.Iterator;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.management.CleanupOperation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
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

	protected boolean needsToSaveDirtyEditors() {
		return false;
	}
	
	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		for (Iterator<?> it = selection.iterator(); it.hasNext(); ) {
			ISynchronizeModelElement element = (ISynchronizeModelElement)it.next();
			if (IStateFilter.SF_VERSIONED_FOLDERS.accept(SVNRemoteStorage.instance().asLocalResource(element.getResource()))) {
				return true;
			}
		}
	    return false;
	}
	
	protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		IResource []resources = this.treeNodeSelector.getSelectedResources(IStateFilter.SF_VERSIONED_FOLDERS);
		CleanupOperation mainOp = new CleanupOperation(resources);
		CompositeOperation op = new CompositeOperation(mainOp.getId());
		op.add(mainOp);
		op.add(new RefreshResourcesOperation(resources));
		return op;
	}
	
}
