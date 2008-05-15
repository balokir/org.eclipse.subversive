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
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.action.local.BranchTagAction;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Create branch action for synchronize view
 * 
 * @author Alexei Goncharov
 */
public class CreateBranchAction extends AbstractSynchronizeModelAction {
	public CreateBranchAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		for (Iterator it = selection.iterator(); it.hasNext(); ) {
			ISynchronizeModelElement element = (ISynchronizeModelElement)it.next();
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(element.getResource());
			// null for change set nodes
			if (local != null && IStateFilter.SF_EXCLUDE_DELETED.accept(local)) {
				return true;
			}
		}
	    return false;
	}
	
	protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		IResource []resources = FileUtility.getResourcesRecursive(this.treeNodeSelector.getSelectedResources(), IStateFilter.SF_EXCLUDE_DELETED, IResource.DEPTH_ZERO);
		
		return BranchTagAction.getBranchTagOperation(configuration.getSite().getShell(), BranchTagAction.BRANCH_ACTION, resources);
	}

}
