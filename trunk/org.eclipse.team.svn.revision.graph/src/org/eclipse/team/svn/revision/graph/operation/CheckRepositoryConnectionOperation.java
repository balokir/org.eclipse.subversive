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
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNConnectorCancelException;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.connector.SVNLogEntryCallbackWithMergeInfo;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.ActivityCancelledException;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphMessages;
import org.eclipse.team.svn.revision.graph.operation.RepositoryConnectionInfo.IRepositoryConnectionInfoProvider;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Check connection to repository and returns last revision in repository.
 * If there's connection it also verifies if server supports merge info.
 * If there's no connection it asks user whether to use data from cache,
 * if user doesn't want to continue then cancel execution, i.e. throw cancel exception 
 * 
 * @author Igor Burilo
 */
public class CheckRepositoryConnectionOperation extends AbstractActionOperation implements IRepositoryConnectionInfoProvider {

	protected IRepositoryResource resource;
	//indicates that it's decided to include merge info
	protected boolean canIncludeMergeInfo;
	/* there can be cases where we don't want to validate if server
	 * supports merge info, e.g. when we call refresh we already know
	 * if we support it as we checked it in previous step
	 */
	protected boolean isValidateMergeInfo;
	
	//output
	protected boolean hasConnection;
	protected long lastRepositoryRevision;
	protected boolean isServerSupportsMergeInfo;
	
	public CheckRepositoryConnectionOperation(IRepositoryResource resource, boolean canIncludeMergeInfo, boolean isValidateMergeInfo) {
		super("Operation_CheckRepositoryConnection", SVNRevisionGraphMessages.class); //$NON-NLS-1$
		this.resource = resource;
		this.canIncludeMergeInfo = canIncludeMergeInfo;
		this.isValidateMergeInfo = isValidateMergeInfo;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		this.isServerSupportsMergeInfo = this.canIncludeMergeInfo;
		
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
		} else if (this.isValidateMergeInfo && this.canIncludeMergeInfo) {
			this.checkMergeInfo(monitor);						
		}				
	}
	
	protected void checkMergeInfo(IProgressMonitor monitor) {	
		IRepositoryLocation location = this.resource.getRepositoryLocation();
		ISVNConnector proxy = location.acquireSVNProxy();
		try {				
			proxy.logEntries(
					SVNUtility.getEntryReference(location.getRepositoryRoot()), 
					SVNRevision.fromNumber(this.lastRepositoryRevision),
					SVNRevision.fromNumber(this.lastRepositoryRevision),
					new String[0], 
					1,
					ISVNConnector.Options.INCLUDE_MERGED_REVISIONS,
					new SVNLogEntryCallbackWithMergeInfo(),
					new SVNProgressMonitor(this, monitor, null));
			
			this.isServerSupportsMergeInfo = true;				
		} catch (SVNConnectorException e) {
			this.isServerSupportsMergeInfo = false;
		} finally {
			location.releaseSVNProxy(proxy);
		}
		
		if (!this.isServerSupportsMergeInfo) {
			UIMonitorUtility.getDisplay().syncExec(new Runnable() {					
				public void run() {
					MessageDialog dlg = new MessageDialog(
						UIMonitorUtility.getShell(), 
						SVNRevisionGraphMessages.Dialog_GraphTitle,
						null, 
						SVNRevisionGraphMessages.CheckRepositoryConnectionOperation_MergeNotSupported,
						MessageDialog.INFORMATION, 
						new String[] {IDialogConstants.OK_LABEL}, 
						0);
					dlg.open();
				}
			});
		}	
	}
	
	public RepositoryConnectionInfo getRepositoryConnectionInfo() {
		return new RepositoryConnectionInfo(this.hasConnection, this.lastRepositoryRevision, this.isServerSupportsMergeInfo);
	}
}
