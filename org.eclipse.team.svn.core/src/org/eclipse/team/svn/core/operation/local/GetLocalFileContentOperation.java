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

package org.eclipse.team.svn.core.operation.local;

import java.io.File;
import java.io.FileOutputStream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.BaseMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevision.Kind;
import org.eclipse.team.svn.core.operation.AbstractGetFileContentOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * This operation retrieve a local file content of selected revision: BASE or WORKING
 * 
 * @author Alexander Gurov
 */
public class GetLocalFileContentOperation extends AbstractGetFileContentOperation {
	protected IResource resource;
	protected SVNRevision revision;

	public GetLocalFileContentOperation(IResource resource, int revisionKind) {
		super("Local"); //$NON-NLS-1$
		this.resource = resource;
		this.revision = revisionKind == Kind.BASE ? SVNRevision.BASE : SVNRevision.WORKING;
	}
	
	public int getOperationWeight() {
		return 0;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		if (this.revision.getKind() == Kind.BASE) {
		    IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(this.resource);
			ISVNConnector proxy = location.acquireSVNProxy();
			FileOutputStream stream = null;
			try {
				this.tmpFile = this.createTempFile();
				stream = new FileOutputStream(this.tmpFile);
				proxy.streamFileContent(
						new SVNEntryRevisionReference(FileUtility.getWorkingCopyPath(this.resource), null, this.revision), 
						2048, 
						stream, 
						new SVNProgressMonitor(GetLocalFileContentOperation.this, monitor, null));
			}
			finally {
			    location.releaseSVNProxy(proxy);
			    if (stream != null) {
			    	try {stream.close();} catch (Exception ex) {}
			    }
			}
		}
		else {
			this.tmpFile = new File(FileUtility.getWorkingCopyPath(this.resource));
		}
	}
	
	protected String getExtension() {
		return this.resource.getFileExtension();
	}
	
	protected String getShortErrorMessage(Throwable t) {
		return BaseMessages.format(super.getShortErrorMessage(t), new Object[] {this.resource.getName()});
	}
	
}
