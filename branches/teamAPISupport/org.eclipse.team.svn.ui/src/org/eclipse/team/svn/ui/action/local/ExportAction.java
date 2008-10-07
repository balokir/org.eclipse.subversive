/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.action.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.local.ExportOperation;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractWorkingCopyAction;

/**
 * Export working copy action implementation
 * 
 * @author Sergiy Logvin
 */

public class ExportAction extends AbstractWorkingCopyAction {

	public ExportAction() {
		super();
	}
	
	public void runImpl(IAction action) {
		IResource []resources = this.getSelectedResources(IStateFilter.SF_EXCLUDE_DELETED);
		
		DirectoryDialog fileDialog = new DirectoryDialog(this.getShell());
		fileDialog.setText(SVNTeamUIPlugin.instance().getResource("ExportAction.Select.Title"));
		fileDialog.setMessage(SVNTeamUIPlugin.instance().getResource("ExportAction.Select.Description"));
		String path = fileDialog.open();
		if (path != null) {
			this.runScheduled(new ExportOperation(resources, path, SVNRevision.WORKING));
		}
	}
	
	public boolean isEnabled() {
		return this.checkForResourcesPresence(IStateFilter.SF_EXCLUDE_DELETED);
	}
	
	protected boolean needsToSaveDirtyEditors() {
		return true;
	}
	
}
