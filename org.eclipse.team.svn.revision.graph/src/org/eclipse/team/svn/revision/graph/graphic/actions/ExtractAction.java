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

import java.util.HashMap;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.FiniExtractLogOperation;
import org.eclipse.team.svn.core.operation.local.InitExtractLogOperation;
import org.eclipse.team.svn.core.operation.remote.ExtractToOperationRemote;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionEditPart;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.history.FromDifferenceRepositoryResourceProviderOperation;
import org.eclipse.ui.IWorkbenchPart;

/**
 * 
 * @author Igor Burilo
 */
public class ExtractAction extends BaseRevisionGraphAction {

	public final static String ExtractAction_ID = "Extract"; //$NON-NLS-1$
	
	public ExtractAction(IWorkbenchPart part) {
		super(part);
		
		setText(SVNUIMessages.ExtractToAction_Label);
		setId(ExtractAction_ID);
		setToolTipText(SVNUIMessages.ExtractToAction_Label);		
	}

	@Override
	protected boolean calculateEnabled() {		
		if (this.isEnable(BaseRevisionGraphAction.EXIST_IN_PREVIOUS_FILTER, 1)) {
			return true;
		} else {
			RevisionEditPart[] editParts = this.getSelectedEditParts(BaseRevisionGraphAction.NOT_DELETED_ACTION_FILTER);
			//check that resources have the same path
			if (editParts.length == 2 && editParts[0].getCastedModel().getPath().equals(editParts[1].getCastedModel().getPath())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void run() {
		DirectoryDialog fileDialog = new DirectoryDialog(this.getWorkbenchPart().getSite().getShell());
		fileDialog.setText(SVNUIMessages.ExtractToAction_Select_Title);
		fileDialog.setMessage(SVNUIMessages.ExtractToAction_Select_Description);
		String path = fileDialog.open();
		if (path != null) {
			IRepositoryResource[] resources = BaseRevisionGraphAction.convertToResources(this.getSelectedEditParts());
			
			HashMap<String, String> resource2project = new HashMap<String, String>();				
			IRepositoryResource remote = resources[0];
			resource2project.put(remote.getUrl(), remote.getName());															
																		
			IRepositoryResource next = resources[0];
			IRepositoryResource prev = null;
			if (resources.length == 1) {					
				prev = SVNUtility.copyOf(next);
				prev.setSelectedRevision(SVNRevision.fromNumber(((SVNRevision.Number)next.getSelectedRevision()).getNumber() - 1));
				prev.setPegRevision(next.getPegRevision());
			}
			else {						
				prev = resources[1];									
				if (((SVNRevision.Number)next.getSelectedRevision()).getNumber() < ((SVNRevision.Number) prev.getSelectedRevision()).getNumber()) {
					IRepositoryResource tmp = next;
					next = prev;
					prev = tmp;
				}	
			}					
			
			InitExtractLogOperation logger = new InitExtractLogOperation(path);
			FromDifferenceRepositoryResourceProviderOperation provider = new FromDifferenceRepositoryResourceProviderOperation(next, prev);
			
			CompositeOperation op = new CompositeOperation(SVNMessages.Operation_ExtractTo);
			op.add(provider);
			op.add(logger);
			op.add(new ExtractToOperationRemote(provider, provider.getDeletionsProvider(), path, resource2project, logger, true), new IActionOperation [] {provider});
			op.add(new FiniExtractLogOperation(logger));
			
			this.runOperation(op);
		}
	}

}
