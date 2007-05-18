/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Vladimir Bykov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.action.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.remote.ExportOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractWorkingCopyAction;
import org.eclipse.team.svn.ui.operation.CorrectRevisionOperation;

/**
 * Export working copy action implementation
 * 
 * @author Vladimir Bykov
 */

public class ExportAction extends AbstractWorkingCopyAction {

	public ExportAction() {
		super();
	}
	
	public void run(IAction action) {
		IResource localTo = this.getSelectedResources(IStateFilter.SF_EXCLUDE_DELETED)[0];
		IRemoteStorage storage = SVNRemoteStorage.instance();
		IRepositoryResource remote = storage.asRepositoryResource(localTo);
		
		DirectoryDialog fileDialog = new DirectoryDialog(this.getShell());
		fileDialog.setText(SVNTeamUIPlugin.instance().getResource("ExportAction.Select.Title"));
		fileDialog.setMessage(SVNTeamUIPlugin.instance().getResource("ExportAction.Select.Description"));
		String path = fileDialog.open();
		if (path != null) {
			ExportOperation mainOp = new ExportOperation(remote, path);
			CompositeOperation op = new CompositeOperation(mainOp.getId());
			ILocalResource local = storage.asLocalResource(localTo);
			if (local != null) {
				op.add(new CorrectRevisionOperation(null, remote, local.getRevision(), localTo));
			}
			op.add(mainOp);
			this.runScheduled(op);
		}
	}
	
	protected boolean isEnabled() {
		return 
			this.getSelectedResources().length == 1 && 
			this.checkForResourcesPresence(IStateFilter.SF_EXCLUDE_DELETED);
	}
	
}
