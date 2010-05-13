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
import org.eclipse.team.svn.core.connector.SVNConnectorCancelException;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.ActivityCancelledException;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphMessages;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Check connection to repository and returns last revision in repository.
 * If there's no connection it asks user whether to use data from cache
 * 
 * @author Igor Burilo
 */
public class CheckRepositoryConnectionOperation extends AbstractActionOperation {

	protected IRepositoryResource resource;
	
	protected boolean hasConnection;
	protected long lastRepositoryRevision;
	
	public CheckRepositoryConnectionOperation(IRepositoryResource resource) {
		super("Operation_CheckRepositoryConnection", SVNRevisionGraphMessages.class); //$NON-NLS-1$
		this.resource = resource;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {											
		IRepositoryRoot root = this.resource.getRepositoryLocation().getRepositoryRoot();
		try {
			this.lastRepositoryRevision = root.getRevision();
			this.hasConnection = this.lastRepositoryRevision != SVNRevision.INVALID_REVISION_NUMBER;
		} catch (SVNConnectorException e) {
			if (e instanceof SVNConnectorCancelException) {
				throw e;
			} else {
				this.hasConnection = false;
			}
		}
				
		if (!this.hasConnection) {
			
			final boolean[] isProceedWithoutConnection = new boolean[]{ false };
			//ask if there's no connection
			UIMonitorUtility.getDisplay().syncExec(new Runnable() {
				public void run() {									
					MessageDialog dlg = new MessageDialog(
						UIMonitorUtility.getShell(), 
						SVNRevisionGraphMessages.Dialog_GraphTitle,
						null, 
						SVNRevisionGraphMessages.CheckRepositoryConnectionOperation_DialogMessage,
						MessageDialog.QUESTION, 
						new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}, 
						0);

					isProceedWithoutConnection[0] = dlg.open() == 0;
				}
			});	
			
			if (!isProceedWithoutConnection[0]) {
				throw new ActivityCancelledException();
			}	
		}
	}
	
	public boolean hasConnection() {
		return this.hasConnection;
	}
	
	public long getLastRepositoryRevision() {
		return this.lastRepositoryRevision;
	}

}
