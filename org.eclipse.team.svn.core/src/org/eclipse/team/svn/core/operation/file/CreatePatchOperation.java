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
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNRevision;
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
	protected boolean useRelativePath;
	protected int depth;
	protected long options;
	protected long diffOptions;

	public CreatePatchOperation(File file, String fileName, boolean recurse, boolean ignoreDeleted, boolean processBinary, boolean useRelativePath) {
		this(file, fileName, SVNDepth.infinityOrFiles(recurse), 
			(ignoreDeleted ? ISVNConnector.Options.SKIP_DELETED : ISVNConnector.Options.NONE) |
			(processBinary ? ISVNConnector.Options.FORCE : ISVNConnector.Options.NONE), useRelativePath, ISVNConnector.DiffOptions.NONE);
	}

	public CreatePatchOperation(File file, String fileName, int depth, long options, boolean useRelativePath, long diffOptions) {
		super("Operation_CreatePatchFile", SVNMessages.class, new File[] {file}); //$NON-NLS-1$
		this.fileName = fileName;
		this.depth = depth;
		this.options = options | ISVNConnector.Options.IGNORE_ANCESTRY;
		this.useRelativePath = useRelativePath;
		this.diffOptions = diffOptions;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		File file = this.operableData()[0];
		IRepositoryResource remote = SVNFileStorage.instance().asRepositoryResource(file, false);
		ISVNConnector proxy = remote.getRepositoryLocation().acquireSVNProxy();
		try {
			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn diff " + (this.depth == SVNDepth.INFINITY ? "" : " -N") + ISVNConnector.Options.asCommandLine(this.options) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			String path = file.getAbsolutePath();
			proxy.diffTwo(
				new SVNEntryRevisionReference(path, null, SVNRevision.BASE), new SVNEntryRevisionReference(path, null, SVNRevision.WORKING), 
				this.useRelativePath ? path : null, this.fileName, this.depth, this.options, null, this.diffOptions, new SVNProgressMonitor(this, monitor, null));
		}
		finally {
			remote.getRepositoryLocation().releaseSVNProxy(proxy);
		}
	}

}
