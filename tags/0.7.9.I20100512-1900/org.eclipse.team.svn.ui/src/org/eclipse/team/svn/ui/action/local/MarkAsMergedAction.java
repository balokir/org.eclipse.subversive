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

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.local.MarkAsMergedOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractNonRecursiveTeamAction;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;

/**
 * Mark selected resource as merged action implementation
 * 
 * @author Alexander Gurov
 */
public class MarkAsMergedAction extends AbstractNonRecursiveTeamAction {
	public MarkAsMergedAction() {
		super();
	}

	public void runImpl(IAction action) {
		IResource []resources = this.getSelectedResources(IStateFilter.SF_CONFLICTING);
		boolean ignoreExternals = SVNTeamPreferences.getBehaviourBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BEHAVIOUR_IGNORE_EXTERNALS_NAME);
		MarkAsMergedOperation mainOp = new MarkAsMergedOperation(resources, false, null, ignoreExternals);
		CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
		op.add(mainOp);
		op.add(new RefreshResourcesOperation(FileUtility.getParents(resources, false)));
		
		this.runScheduled(op);
	}
	
	public boolean isEnabled() {
		return 
			this.getSelectedResources().length == 1 &&
			this.checkForResourcesPresence(IStateFilter.SF_CONFLICTING);
	}

	protected boolean needsToSaveDirtyEditors() {
		return true;
	}
	
}
