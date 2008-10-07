/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
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
import org.eclipse.team.svn.core.connector.SVNChangeStatus;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.GetFileContentOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

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
		if ((!this.local.isCopied() && this.local.getRevision() == SVNRevision.INVALID_REVISION_NUMBER) || 
		    IStateFilter.SF_DELETED.accept(this.local) && !IStateFilter.SF_REPLACED.accept(this.local)) {
			this.setContents(new ByteArrayInputStream(new byte[0]), monitor);
			return;
		}
		IRepositoryResource remote = null;
		if (this.local.isCopied()) {
			IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(this.local.getResource());
			SVNChangeStatus st = SVNUtility.getSVNInfoForNotConnected(this.local.getResource());
			remote = location.asRepositoryFile(st.urlCopiedFrom, false);
			remote.setSelectedRevision(SVNRevision.fromNumber(st.revisionCopiedFrom));
			remote.setPegRevision(SVNRevision.fromNumber(st.revisionCopiedFrom));
		}
		else {
			remote = SVNRemoteStorage.instance().asRepositoryResource(this.local.getResource());
			remote.setSelectedRevision(SVNRevision.fromNumber(this.local.getRevision()));
			remote.setPegRevision(((IResourceChange)this.local).getPegRevision());
			if (this.local instanceof IResourceChange) {
				IRepositoryResource originator = ((IResourceChange)this.local).getOriginator();
				if (originator != null) {
					remote = originator;
				}
			}
		}
		
		GetFileContentOperation op = new GetFileContentOperation(remote);
		ProgressMonitorUtility.doTaskExternal(op, monitor);
		if (op.getExecutionState() == IActionOperation.OK) {
			this.setContents(op.getContent(), monitor);
		}
	}

	public boolean isContainer() {
		return false;
	}

}
