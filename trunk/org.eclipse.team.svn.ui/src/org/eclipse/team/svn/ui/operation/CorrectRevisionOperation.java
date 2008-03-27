/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.operation;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.remote.GetLogMessagesOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Corrects Log Message revision
 * 
 * @author Alexander Gurov
 */
public class CorrectRevisionOperation extends AbstractActionOperation {
	protected IRepositoryResource []repositoryResources;
	protected long []knownRevisions;
	protected GetLogMessagesOperation []msgsOps;
	protected IResource []resources;
	
	public CorrectRevisionOperation(GetLogMessagesOperation msgsOp, IRepositoryResource repositoryResource, long knownRevision, IResource resource) {
		this(msgsOp == null ? null : new GetLogMessagesOperation[] {msgsOp}, new IRepositoryResource[] {repositoryResource}, new long[] {knownRevision}, new IResource[] {resource});
	}

	public CorrectRevisionOperation(GetLogMessagesOperation []msgsOps, IRepositoryResource []repositoryResources, long []knownRevisions, IResource []resources) {
		super("Operation.CorrectRevision");
		this.repositoryResources = repositoryResources;
		this.knownRevisions = knownRevisions;
		this.msgsOps = msgsOps;
		this.resources = resources;
	}
	
	public int getOperationWeight() {
		if (this.msgsOps == null) {
			return 0;
		}
		return super.getOperationWeight();
	}
	
	protected void runImpl(final IProgressMonitor monitor) throws Exception {
		boolean hasWarning = false;
		for (int i = 0; i < this.repositoryResources.length; i++) {
			if (!this.repositoryResources[i].exists() && this.resources != null && this.resources[i] != null && this.resources[i].getType() != IResource.PROJECT) {
				// calculate peg revision for the repository resource
				ILocalResource parent = SVNRemoteStorage.instance().asLocalResource(this.resources[i].getParent());
				ILocalResource self = SVNRemoteStorage.instance().asLocalResource(this.resources[i]);
				if (parent != null && self != null) {
					boolean switchedStateEquals = (parent.getChangeMask() & ILocalResource.IS_SWITCHED) == (self.getChangeMask() & ILocalResource.IS_SWITCHED);
					if (switchedStateEquals) {
						long parentRevision = parent.getRevision();
						long selfRevision = self.getRevision();
						long revision = parentRevision > selfRevision ? parentRevision : selfRevision;
						if (revision != SVNRevision.INVALID_REVISION_NUMBER) {
							this.repositoryResources[i].setPegRevision(SVNRevision.fromNumber(revision));
						}
					}
					else {
						this.repositoryResources[i].setPegRevision(SVNRevision.fromNumber(self.getRevision()));
					}
				}
			}
			if (!this.repositoryResources[i].exists() && this.knownRevisions[i] != SVNRevision.INVALID_REVISION_NUMBER) {
				hasWarning = true;
				SVNRevision rev = SVNRevision.fromNumber(this.knownRevisions[i]);
				this.repositoryResources[i].setSelectedRevision(rev);
				this.repositoryResources[i].setPegRevision(rev);
				if (this.msgsOps != null) {
					this.msgsOps[i].setSelectedRevision(rev);
				}
			}
		}
		if (hasWarning) {
			UIMonitorUtility.getDisplay().syncExec(new Runnable() {
				public void run() {
					boolean one = CorrectRevisionOperation.this.repositoryResources.length == 1;
					MessageDialog dlg = new MessageDialog(
							UIMonitorUtility.getShell(), 
							CorrectRevisionOperation.this.getOperationResource(one ? "Title.Single" : "Title.Multi"), 
							null, 
							CorrectRevisionOperation.this.getOperationResource(one ? "Message.Single" : "Message.Multi"), 
							MessageDialog.WARNING, 
							new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}, 
							0);
					if (dlg.open() != 0) {
						monitor.setCanceled(true);
					}
				}
			});
		}
	}
	
}
