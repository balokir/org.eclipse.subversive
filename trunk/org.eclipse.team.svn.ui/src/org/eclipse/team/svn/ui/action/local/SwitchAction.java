/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
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
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.local.ClearLocalStatusesOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.SaveProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.SwitchOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractNonRecursiveTeamAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.dialog.OperationErrorDialog;
import org.eclipse.team.svn.ui.panel.local.SwitchPanel;

/**
 * Switch working copy to the new URL action implementation
 * 
 * @author Alexander Gurov
 */
public class SwitchAction extends AbstractNonRecursiveTeamAction {

	public SwitchAction() {
		super();
	}
	
	public void run(IAction action) {
		IResource []resources = this.getSelectedResources(IStateFilter.SF_EXCLUDE_DELETED);
		
		if (!OperationErrorDialog.isAcceptableAtOnce(resources, SVNTeamUIPlugin.instance().getResource("SwitchAction.Error"), this.getShell())) {
			return;
		}
		
		// in case of multiple switch selected repository resource should be recognized as branch/tag/trunk (using copied from). If "copied from" inaccessible multi switch will fails, single switch for project should be performed like for folders 
		
	//FIXME peg revision, revision limitation ???
		SwitchPanel panel;
		if (resources.length > 1) {
			IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(resources[0]);
			remote = SVNUtility.getTrunkLocation(remote);
			panel = new SwitchPanel(remote, Revision.SVN_INVALID_REVNUM);
		}
		else {
			IResource resource = resources[0];
			IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(resource);
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
			if (local == null) {
				return;
			}
			panel = new SwitchPanel(remote, local.getRevision());
		}
			
		DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
		if (dialog.open() == 0) {
			IRepositoryResource []destinations = panel.getSelection(resources);

			SwitchOperation mainOp = new SwitchOperation(resources, destinations);
			
			CompositeOperation op = new CompositeOperation(mainOp.getId());

			SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(resources);
			op.add(saveOp);
			op.add(mainOp);
			op.add(new RestoreProjectMetaOperation(saveOp));
			op.add(new ClearLocalStatusesOperation(resources));
			op.add(new RefreshResourcesOperation(resources));

			this.runScheduled(op);
		}
	}

	protected boolean isEnabled() {
		return 
//			this.getSelectedResources().length == 1 && 
			this.checkForResourcesPresence(IStateFilter.SF_EXCLUDE_DELETED);
	}
	
}
