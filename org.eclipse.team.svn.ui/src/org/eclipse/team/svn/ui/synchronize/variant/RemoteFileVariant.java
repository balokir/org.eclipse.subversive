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

package org.eclipse.team.svn.ui.synchronize.variant;

import java.io.ByteArrayInputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.client.Status;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.GetFileContentOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Remote file revision variant
 * 
 * @author Alexander Gurov
 */
public class RemoteFileVariant extends RemoteResourceVariant {

	public RemoteFileVariant(ILocalResource local) {
		super(local);
	}

	protected void fetchContents(IProgressMonitor monitor) throws TeamException {
		SVNRemoteStorage storage = SVNRemoteStorage.instance();
		if ((!this.local.isCopied() && this.local.getRevision() == Revision.SVN_INVALID_REVNUM) || 
		    IStateFilter.SF_DELETED.accept(this.local.getResource(), this.local.getStatus(), this.local.getChangeMask()) &&
		    !IStateFilter.SF_REPLACED.accept(this.local.getResource(), this.local.getStatus(), this.local.getChangeMask())) {
			this.setContents(new ByteArrayInputStream(new byte[0]), monitor);
			return;
		}
		IRepositoryResource remote = null;
		if (this.local.isCopied()) {
			IRepositoryLocation location = storage.getRepositoryLocation(local.getResource());
			Status st = SVNUtility.getSVNInfoForNotConnected(this.local.getResource());
			remote = location.asRepositoryFile(st.urlCopiedFrom, false);
			remote.setSelectedRevision(Revision.getInstance(st.revisionCopiedFrom));
			remote.setPegRevision(Revision.getInstance(st.revisionCopiedFrom));
		}
		else {
			remote = SVNRemoteStorage.instance().asRepositoryResource(this.local.getResource());
			if (this.local instanceof IResourceChange) {
				IRepositoryResource originator = ((IResourceChange)this.local).getOriginator();
				if (originator != null) {
					remote = originator;
				}
			}
			remote.setSelectedRevision(Revision.getInstance(this.local.getRevision()));
			remote.setPegRevision(((IResourceChange)this.local).getPegRevision());
		}
		
		GetFileContentOperation op = new GetFileContentOperation(remote);
		UIMonitorUtility.doTaskExternalDefault(op, monitor);
		if (op.getExecutionState() == IActionOperation.OK) {
			this.setContents(op.getContent(), monitor);
		}
	}

	public boolean isContainer() {
		return false;
	}

}
