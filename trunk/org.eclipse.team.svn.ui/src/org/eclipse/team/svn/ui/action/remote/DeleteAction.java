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

package org.eclipse.team.svn.ui.action.remote;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.client.SVNRevision.Kind;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.remote.DeleteResourcesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.operation.RefreshRemoteResourcesOperation;
import org.eclipse.team.svn.ui.panel.common.CommentPanel;
import org.eclipse.team.svn.ui.repository.RepositoriesView;

/**
 * Delete remote resource action
 * 
 * @author Alexander Gurov
 */
public class DeleteAction extends AbstractRepositoryTeamAction {

	public DeleteAction() {
		super();
	}
	
	public void runImpl(IAction action) {
	    CommentPanel commentPanel = new CommentPanel(SVNTeamUIPlugin.instance().getResource("DeleteAction.Comment.Title"));
		DefaultDialog dialog = new DefaultDialog(this.getShell(), commentPanel);
		if (dialog.open() == 0) {
			IRepositoryResource []resources = this.getSelectedRepositoryResources();
			IRepositoryResource []commonParents = SVNUtility.getCommonParents(resources);
			
			DeleteResourcesOperation mainOp = new DeleteResourcesOperation(resources, commentPanel.getMessage());
			CompositeOperation op = new CompositeOperation(mainOp.getId());
			
			op.add(mainOp);
			op.add(new RefreshRemoteResourcesOperation(commonParents));
			op.add(new AbstractActionOperation("") {
				protected void runImpl(IProgressMonitor monitor) throws Exception {
					DeleteAction.this.getShell().getDisplay().syncExec(new Runnable() {
						public void run() {
							RepositoriesView view = RepositoriesView.instance();
							if (view == null) {
								return;
							}
							
							view.getRepositoryTree().fireEmptySelectionEvent();
						}
					});
				}

				public ISchedulingRule getSchedulingRule() {
					return null;
				}
			});
			
			this.runScheduled(op);
		}		
	}

	public boolean isEnabled() {
		IRepositoryResource []resources = this.getSelectedRepositoryResources();
		for (int i = 0; i < resources.length; i++) {
			IRepositoryLocation location = resources[i].getRepositoryLocation();
			if (resources[i].getUrl().equals(location.getRoot().getUrl()) ||
				resources[i].getSelectedRevision().getKind() != Kind.HEAD ||
				resources[i] instanceof IRepositoryRoot && 
				(((IRepositoryRoot)resources[i]).getKind() == IRepositoryRoot.KIND_ROOT || ((IRepositoryRoot)resources[i]).getKind() == IRepositoryRoot.KIND_LOCATION_ROOT)) {
				return false;
			}
		}
		return resources.length > 0;
	}

}
