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

package org.eclipse.team.svn.core.history;

import java.net.URI;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.history.provider.FileRevision;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractGetFileContentOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.GetLocalFileContentOperation;
import org.eclipse.team.svn.core.operation.remote.GetLogMessagesOperation;
import org.eclipse.team.svn.core.resource.ILocalFolder;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Resource revision based on the local info
 * 
 * @author Alexander Gurov
 */
public class SVNLocalResourceRevision extends FileRevision {
	protected ILocalResource local;
	protected boolean onServer;
	protected SVNRevision rev;

	public SVNLocalResourceRevision(ILocalResource local, SVNRevision rev) {
		this.local = local;
		this.onServer = IStateFilter.SF_ONREPOSITORY.accept(this.local.getResource(), this.local.getStatus(), this.local.getChangeMask());
	}

	public URI getURI() {
		return this.local.getResource().getLocationURI();
	}

	public long getTimestamp() {
		return !this.onServer ? -1 : this.local.getLastCommitDate();
	}

	public boolean exists() {
		return true;
	}

	public String getContentIdentifier() {
		return !this.onServer ? null : String.valueOf(this.local.getRevision());
	}

	public String getAuthor() {
		return this.local.getAuthor();
	}

	public String getComment() {
		return null;
	}
	
	public String getName() {
		return this.local.getName();
	}

	public IStorage getStorage(IProgressMonitor monitor) throws CoreException {
		if (this.local instanceof ILocalFolder) {
			return null;
		}
		if (IStateFilter.SF_UNVERSIONED.accept(this.local.getResource(), this.local.getStatus(), this.local.getChangeMask()) &&
			!IStateFilter.SF_PREREPLACED.accept(this.local.getResource(), this.local.getStatus(), this.local.getChangeMask())) {
			return (IStorage)this.local.getResource();
		}
		return new LocalStorage();
	}

	public boolean isPropertyMissing() {
		return this.onServer;
	}

	public IFileRevision withAllProperties(IProgressMonitor monitor) throws CoreException {
		if (!this.onServer) {
			return this;
		}
		IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(this.local.getResource());
		remote.setSelectedRevision(SVNRevision.fromNumber(this.local.getRevision()));
		GetLogMessagesOperation log = new GetLogMessagesOperation(remote);
		log.setLimit(1);
		ProgressMonitorUtility.doTaskExternal(log, monitor);
		if (log.getExecutionState() == IActionOperation.OK && log.getMessages().length > 0) {
			return new SVNRemoteResourceRevision(remote, log.getMessages()[0]);
		}
		return null;
	}

	protected class LocalStorage extends ResourceContentStorage {
		public LocalStorage() {
			super(SVNRemoteStorage.instance().asRepositoryResource(SVNLocalResourceRevision.this.local.getResource()));
		}
		
		public IPath getFullPath() {
			return SVNLocalResourceRevision.this.local.getResource().getLocation();
		}

		protected AbstractGetFileContentOperation getLoadContentOperation() {
		    return new GetLocalFileContentOperation(SVNLocalResourceRevision.this.local.getResource(), SVNLocalResourceRevision.this.rev.getKind());
		}
		
	}
}
