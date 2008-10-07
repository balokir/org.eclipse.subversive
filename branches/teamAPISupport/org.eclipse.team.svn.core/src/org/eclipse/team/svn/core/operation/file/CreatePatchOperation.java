/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryResource;

/**
 * Create patch operation implementation
 * 
 * @author Alexander Gurov
 */
public class CreatePatchOperation extends AbstractFileOperation {
	protected String fileName;
	protected boolean recurse;
	protected boolean ignoreDeleted;
	protected boolean processBinary;
	protected boolean useRelativePath;

	public CreatePatchOperation(File file, String fileName, boolean recurse, boolean ignoreDeleted, boolean processBinary, boolean useRelativePath) {
		super("Operation.CreatePatchFile", new File[] {file});
		this.fileName = fileName;
		this.recurse = recurse;
		this.ignoreDeleted = ignoreDeleted;
		this.processBinary = processBinary;
		this.useRelativePath = useRelativePath;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		File file = this.operableData()[0];
		IRepositoryResource remote = SVNFileStorage.instance().asRepositoryResource(file, false);
		ISVNConnector proxy = remote.getRepositoryLocation().acquireSVNProxy();
		try {
			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn diff " + (this.recurse ? "" : " -N") + (this.ignoreDeleted ? " --no-diff-deleted" : "") + "\n");
			String path = file.getAbsolutePath();
			long options = ISVNConnector.Options.IGNORE_ANCESTRY;
			options |= this.ignoreDeleted ? ISVNConnector.Options.SKIP_DELETED : ISVNConnector.Options.NONE;
			options |= this.processBinary ? ISVNConnector.Options.FORCE : ISVNConnector.Options.NONE;
			proxy.diff(
				new SVNEntryRevisionReference(path, null, SVNRevision.BASE), new SVNEntryRevisionReference(path, null, SVNRevision.WORKING), this.useRelativePath ? path : null, this.fileName, 
				Depth.infinityOrFiles(this.recurse), options, null, new SVNProgressMonitor(this, monitor, null));
		}
		finally {
			remote.getRepositoryLocation().releaseSVNProxy(proxy);
		}
	}

}
