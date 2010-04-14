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

import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionEditPart;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.operation.CompareRepositoryResourcesOperation;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Compare two revision nodes 
 * 
 * @author Igor Burilo
 */
public class CompareWithEachOtherAction extends BaseRevisionGraphAction {

	public final static String CompareWithEachOtherAction_ID = "CompareWithEachOther";	 //$NON-NLS-1$
	
	public CompareWithEachOtherAction(IWorkbenchPart part) {
		super(part);
		
		setText(SVNUIMessages.HistoryView_CompareEachOther);
		setId(CompareWithEachOtherAction_ID);
		setToolTipText(SVNUIMessages.HistoryView_CompareEachOther);
	}

	@Override
	protected boolean calculateEnabled() {
		RevisionEditPart[] editParts = null;
		if (this.getSelectedEditParts().length == 2 && 
			(editParts = this.getSelectedEditParts(BaseRevisionGraphAction.NOT_DELETED_ACTION_FILTER)).length == 2) {
			IRepositoryResource resource = BaseRevisionGraphAction.convertToResource(editParts[0]);
			boolean isCompareAllowed = 
				CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() >= ISVNConnectorFactory.APICompatibility.SVNAPI_1_5_x ||
				resource instanceof IRepositoryFile;
			if (isCompareAllowed) {
				return true;
			}	
		}
		return false;
	}
	
	@Override
	public void run() {
		IRepositoryResource[] resources = BaseRevisionGraphAction.convertToResources(this.getSelectedEditParts());
		IRepositoryResource prev = resources[0];
		IRepositoryResource next = null;
		if (resources.length == 2) {
			next = resources[1]; 
		}				
		if (next != null && ((SVNRevision.Number)next.getSelectedRevision()).getNumber() < ((SVNRevision.Number) prev.getSelectedRevision()).getNumber()) {				
			IRepositoryResource tmp = prev;
			prev = next;
			next = tmp;									
		}
				
		CompareRepositoryResourcesOperation op = new CompareRepositoryResourcesOperation(prev, next);
		op.setForceId(this.toString());				
		this.runOperation(op);
	}

}
