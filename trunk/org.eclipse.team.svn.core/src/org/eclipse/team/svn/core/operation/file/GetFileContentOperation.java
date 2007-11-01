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

package org.eclipse.team.svn.core.operation.file;

import java.io.File;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.client.Revision.Kind;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * This operation retrieve a content of the file in selected revision
 * 
 * @author Alexander Gurov
 */
public class GetFileContentOperation extends AbstractFileOperation {
	public static final int DEFAULT_BUFFER_SIZE = 2048;
	
	protected Revision revision;
	protected Revision pegRevision;
	protected int bufferSize;
	protected OutputStream target;

	public GetFileContentOperation(File file, Revision revision, Revision pegRevision, OutputStream target) {
		this(file, revision, pegRevision, GetFileContentOperation.DEFAULT_BUFFER_SIZE, target);
	}
	
	public GetFileContentOperation(File file, Revision revision, Revision pegRevision, int bufferSize, OutputStream target) {
		super("Operation.GetFileContent", new File[] {file});
		this.revision = revision;
		this.pegRevision = pegRevision;
		this.bufferSize = bufferSize;
		this.target = target;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		File file = this.operableData()[0];
		
	    IRepositoryResource remote = SVNFileStorage.instance().asRepositoryResource(file, false);
		ISVNClientWrapper proxy = remote.getRepositoryLocation().acquireSVNProxy();
		try {
			int kind = this.revision.getKind();
			if (kind == Kind.BASE || kind == Kind.WORKING) {
				proxy.streamFileContent(file.getAbsolutePath(), this.revision, null, this.bufferSize, this.target, new SVNProgressMonitor(this, monitor, null));
			}
			else {
				proxy.streamFileContent(SVNUtility.encodeURL(remote.getUrl()), this.revision, this.pegRevision, this.bufferSize, this.target, new SVNProgressMonitor(this, monitor, null));
			}
		}
		finally {
		    remote.getRepositoryLocation().releaseSVNProxy(proxy);
		}
	}

}
