/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.action.remote;

import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.remote.AbstractCopyMoveResourcesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.operation.RefreshRemoteResourcesOperation;
import org.eclipse.team.svn.ui.panel.common.CommentPanel;
import org.eclipse.team.svn.ui.panel.common.RepositoryTreePanel;

/**
 * Abstract class for the copy and move remote resources actions
 *
 * @author Sergiy Logvin
 */
public abstract class AbstractCopyMoveAction extends AbstractRepositoryTeamAction {
	protected String operationId;
	
	public AbstractCopyMoveAction(String operationId) {
		super();
		this.operationId = operationId;
	}
	
	public void runImpl(IAction action) {
		RepositoryTreePanel panel = new RepositoryTreePanel(SVNTeamUIPlugin.instance().getResource(this.operationId + ".Select.Title"), this.getSelectedRepositoryResources(), this.operationId.toLowerCase().equals("copy"));
		DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
		if (dialog.open() == 0) {
			IRepositoryResource destination = panel.getSelectedResource();
			if (destination != null) {
				CommentPanel commentPanel = new CommentPanel(SVNTeamUIPlugin.instance().getResource(this.operationId + ".Comment.Title"));
				dialog = new DefaultDialog(this.getShell(), commentPanel);
				if (dialog.open() == 0) {
					String message = commentPanel.getMessage();
					IRepositoryResource []selected = this.getSelectedRepositoryResources();
					
					AbstractCopyMoveResourcesOperation moveOp = this.makeCopyOperation(destination, selected, message);
					CompositeOperation op = new CompositeOperation(moveOp.getId());
					op.add(moveOp);
					op.add(this.makeRefreshOperation(destination, selected));
					
					this.runScheduled(op);
				}
			}
		}
	}
	
	protected abstract AbstractCopyMoveResourcesOperation makeCopyOperation(IRepositoryResource destination, IRepositoryResource []selected, String message);
	protected abstract RefreshRemoteResourcesOperation makeRefreshOperation(IRepositoryResource destination, IRepositoryResource []selected);
	
}
