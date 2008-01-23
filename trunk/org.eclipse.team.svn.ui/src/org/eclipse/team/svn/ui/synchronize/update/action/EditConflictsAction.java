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

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.operation.ShowConflictEditorOperation;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeModelAction;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Edit conflicts action implementation for Synchronize view
 * 
 * @author Alexei Goncharov
 */
public class EditConflictsAction extends AbstractSynchronizeModelAction {

	public EditConflictsAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	public EditConflictsAction(String text, ISynchronizePageConfiguration configuration, ISelectionProvider selectionProvider) {
		super(text, configuration, selectionProvider);
	}

	protected IActionOperation execute(final FilteredSynchronizeModelOperation operation) {
		final IActionOperation [] op = new IActionOperation[1];
		operation.getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				op[0] = new ShowConflictEditorOperation(FileUtility.getResourcesRecursive(operation.getSelectedResources(), IStateFilter.SF_CONFLICTING));
			}
		});
		return op[0];
	}
	
	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		if (selection.size() != 1 || !(selection.getFirstElement() instanceof SyncInfoModelElement)) {
		    return false;
		}
		return FileUtility.checkForResourcesPresenceRecursive(new IResource [] {((SyncInfoModelElement)selection.getFirstElement()).getResource()}, IStateFilter.SF_CONFLICTING);
	}

}
