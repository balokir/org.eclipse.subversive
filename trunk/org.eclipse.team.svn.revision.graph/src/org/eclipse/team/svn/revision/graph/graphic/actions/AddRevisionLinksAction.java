/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.revision.graph.graphic.actions;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.remote.management.AddRevisionLinkOperation;
import org.eclipse.team.svn.core.operation.remote.management.SaveRepositoryLocationsOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRevisionLink;
import org.eclipse.team.svn.core.resource.IRevisionLinkProvider;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionEditPart;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.operation.RefreshRepositoryLocationsOperation;
import org.eclipse.team.svn.ui.panel.common.InputRevisionPanel;
import org.eclipse.ui.IWorkbenchPart;

/**
 * 
 * @author Igor Burilo
 */
public class AddRevisionLinksAction extends BaseRevisionGraphAction {

	public final static String AddRevisionLinksAction_ID = "AddRevisionLinks";	 //$NON-NLS-1$
	
	public AddRevisionLinksAction(IWorkbenchPart part) {
		super(part);
		
		setText(SVNUIMessages.AddRevisionLinkAction_label);
		setId(AddRevisionLinksAction_ID);
		setToolTipText(SVNUIMessages.AddRevisionLinkAction_label);		
	}

	@Override
	protected boolean calculateEnabled() {		
		return this.getSelectedEditParts(BaseRevisionGraphAction.NOT_DELETED_ACTION_FILTER).length > 0;
	}
	
	@Override
	public void run() {
		InputRevisionPanel panel = new InputRevisionPanel(null, false, null);		
		DefaultDialog dlg = new DefaultDialog(this.getWorkbenchPart().getSite().getShell(), panel);
		if (dlg.open() == Dialog.OK) {
			RevisionEditPart[] editParts = this.getSelectedEditParts(BaseRevisionGraphAction.NOT_DELETED_ACTION_FILTER);
			final IRepositoryResource[] resources = BaseRevisionGraphAction.convertToResources(editParts);
			
			CompositeOperation op = new CompositeOperation("Operation_HAddSelectedRevision", SVNUIMessages.class); //$NON-NLS-1$
			
			final String comment = panel.getRevisionComment();
			AddRevisionLinkOperation addLinksOp = new AddRevisionLinkOperation(new IRevisionLinkProvider() {
				public IRevisionLink[] getRevisionLinks() {
					IRevisionLink[] links = new IRevisionLink[resources.length];
					for (int i = 0; i < resources.length; i ++) {
						links[i] = SVNUtility.createRevisionLink(resources[i]);
						links[i].setComment(comment);
					} 							
					return links;
				}				
			}, null);
			op.add(addLinksOp);						
			
			op.add(new SaveRepositoryLocationsOperation());
			IRepositoryLocation location = getRepositoryLocation(editParts[0]);
			op.add(new RefreshRepositoryLocationsOperation(new IRepositoryLocation [] {location}, true));
			
			this.runOperation(op);
		}	
	}

}
