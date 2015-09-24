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
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphMessages;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionEditPart;
import org.eclipse.team.svn.ui.operation.CompareRepositoryResourcesOperation;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Compare revision node with HEAD
 * 
 * @author Igor Burilo
 */
public class CompareWithHeadAction extends BaseRevisionGraphAction {

	public final static String CompareWithHeadAction_ID = "CompareWithHead"; //$NON-NLS-1$
	
	public CompareWithHeadAction(IWorkbenchPart part) {
		super(part);
		
		setText(SVNRevisionGraphMessages.CompareWithHeadAction);
		setId(CompareWithHeadAction_ID);
		setToolTipText(SVNRevisionGraphMessages.CompareWithHeadAction);
	}

	@Override
	protected boolean calculateEnabled() {
		RevisionEditPart[] editParts = null;
		if (this.getSelectedEditParts().length == 1 && 
			(editParts = this.getSelectedEditParts(BaseRevisionGraphAction.NOT_DELETED_ACTION_FILTER)).length == 1) {
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
		IRepositoryResource resource = BaseRevisionGraphAction.convertToResource(this.getSelectedEditPart());
		
		IRepositoryResource headResource = resource instanceof IRepositoryFile ?
			(IRepositoryResource)((IRepositoryRoot)resource.getRoot()).asRepositoryFile(resource.getUrl(), false) : 
			((IRepositoryRoot)resource.getRoot()).asRepositoryContainer(resource.getUrl(), false);
		headResource.setSelectedRevision(SVNRevision.HEAD);
		headResource.setPegRevision(SVNRevision.HEAD);				
		
		CompareRepositoryResourcesOperation op = new CompareRepositoryResourcesOperation(headResource, resource);
		op.setForceId(this.toString());				
		this.runOperation(op);			
	}

}
