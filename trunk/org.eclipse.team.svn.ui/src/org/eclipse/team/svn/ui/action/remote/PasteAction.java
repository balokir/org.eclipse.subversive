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

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.team.svn.core.connector.SVNRevision.Kind;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.RemoteResourceTransfer;
import org.eclipse.team.svn.ui.RemoteResourceTransferrable;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.operation.PasteRemoteResourcesOperation;
import org.eclipse.team.svn.ui.operation.RefreshRemoteResourcesOperation;
import org.eclipse.team.svn.ui.panel.common.CommentPanel;

/**
 * Paste remote resource from clipboard action implementation
 * 
 * @author Alexander Gurov
 */
public class PasteAction extends AbstractRepositoryTeamAction {

	public PasteAction() {
		super();
	}
	
	public void runImpl(IAction action) {
	    CommentPanel commentPanel = new CommentPanel(SVNTeamUIPlugin.instance().getResource("PasteAction.Comment.Title"));
		DefaultDialog dialog = new DefaultDialog(this.getShell(), commentPanel);
		if (dialog.open() == 0) {
			final IRepositoryResource []resources = this.getSelectedRepositoryResources();
			resources[0] = resources[0] instanceof IRepositoryContainer ? resources[0] : resources[0].getParent();

			final PasteRemoteResourcesOperation pasteOp = new PasteRemoteResourcesOperation(resources[0], this.getShell().getDisplay(), commentPanel.getMessage());

			CompositeOperation op = new CompositeOperation(pasteOp.getId());
			
			op.add(pasteOp);
			op.add(new RefreshRemoteResourcesOperation(
				new IRepositoryResourceProvider() {
					public IRepositoryResource[] getRepositoryResources() {
						if (pasteOp.getOperationType() == RemoteResourceTransferrable.OP_COPY) {
							return resources;
						}
						HashSet fullSet = new HashSet(Arrays.asList(pasteOp.getRepositoryResources()));
						fullSet.addAll(Arrays.asList(resources));
						return SVNUtility.getCommonParents((IRepositoryResource [])fullSet.toArray(new IRepositoryResource[fullSet.size()]));
					}
				}));
			
			this.runScheduled(op);
		}
	}

	public boolean isEnabled() {
		IRepositoryResource []selected = this.getSelectedRepositoryResources();
		if (selected.length != 1 || selected[0].getSelectedRevision().getKind() != Kind.HEAD) {
			return false;
		}
        Clipboard clipboard = new Clipboard(getShell().getDisplay());
        try
		{
        	RemoteResourceTransferrable transferrable = (RemoteResourceTransferrable)clipboard.getContents(new RemoteResourceTransfer());
        	IRepositoryResource []pasted = null;
            if (transferrable == null ||
            	(pasted = transferrable.getResources()) == null ||
            	pasted.length == 0) {
            	return false;
            }
            IRepositoryResource target = selected[0] instanceof IRepositoryContainer ? selected[0] : selected[0].getParent();
            for (int i = 0; i < pasted.length; i++) {
            	if (this.isSource(target, pasted[i])) {
            		return false;
            	}
            }
            return selected[0].getRepositoryLocation() == pasted[0].getRepositoryLocation();
		}
        finally {
            clipboard.dispose();
        }
	}
	
	protected boolean isSource(IRepositoryResource selectedResource, IRepositoryResource resource) {
		Path selectedUrl = new Path(selectedResource.getUrl());
		return 
			new Path(resource.getUrl()).isPrefixOf(selectedUrl) || 
			resource.getParent() != null && new Path(resource.getParent().getUrl()).equals(selectedUrl);
	}
	
}
