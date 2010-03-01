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
package org.eclipse.team.svn.revision.graph.operation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.operation.ActivityCancelledException;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionEditPart;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * 
 * @author Igor Burilo
 */
public class GetRepositoryResourcesForCompareOperation extends GetRepositoryResourcesOperation {

	public GetRepositoryResourcesForCompareOperation(RevisionEditPart[] editParts) {
		super(editParts);
	}
	
	@Override
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		super.runImpl(monitor);
		
		IRepositoryResource[] resources = this.getRepositoryResources();
		IRepositoryResource prev = resources[0];
		IRepositoryResource next = null;
		if (resources.length == 2) {
			next = resources[1]; 
		}		
		
		boolean isCompareAllowed = 
			CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() >= ISVNConnectorFactory.APICompatibility.SVNAPI_1_5_x ||
			prev instanceof IRepositoryFile;
		if (isCompareAllowed) {
			if (next != null && ((SVNRevision.Number)next.getSelectedRevision()).getNumber() < ((SVNRevision.Number) prev.getSelectedRevision()).getNumber()) {				
				IRepositoryResource tmp = prev;
				prev = next;
				next = tmp;
				this.resources = new IRepositoryResource[]{prev, next};					
			}				
		} else {				
			UIMonitorUtility.getDisplay().syncExec(new Runnable() {						
				public void run() {
					MessageDialog dlg = new MessageDialog(
						UIMonitorUtility.getShell(), 
						"Compare", 
						null, 
						"Can't compare graph nodes because current connector doesn't support comparing of directories.", 
						MessageDialog.INFORMATION, 
						new String[] {IDialogConstants.OK_LABEL}, 
						0);
		        	dlg.open();							
				}
			});
        	//abort operation execution
        	throw new ActivityCancelledException();
		}								
	}

}
