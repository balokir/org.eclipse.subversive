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

package org.eclipse.team.svn.ui.action.local;

import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.ui.action.AbstractRecursiveTeamAction;
import org.eclipse.team.svn.ui.operation.ShowConflictEditorOperation;

/**
 * Edit conflicting files action implementation
 * 
 * @author Alexander Gurov
 */
public class EditConflictsAction extends AbstractRecursiveTeamAction {
	public EditConflictsAction() {
		super();
	}

	public void runImpl(IAction action) {
		this.runScheduled(new ShowConflictEditorOperation(this.getSelectedResourcesRecursive(IStateFilter.SF_CONTENT_CONFLICTING), false));
	}
	
	public boolean isEnabled() {
		return this.checkForResourcesPresenceRecursive(IStateFilter.SF_CONTENT_CONFLICTING);
	}

	protected boolean needsToSaveDirtyEditors() {
		return true;
	}
	
}
