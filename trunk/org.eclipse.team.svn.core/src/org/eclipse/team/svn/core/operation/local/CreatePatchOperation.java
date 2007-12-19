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

package org.eclipse.team.svn.core.operation.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Create patch operation implementation
 * 
 * @author Alexander Gurov
 */
public class CreatePatchOperation extends AbstractActionOperation {
	protected IResource resource;
	protected String fileName;
	protected boolean recurse;
	protected boolean ignoreDeleted;
	protected boolean processBinary;
	protected boolean processUnversioned;
	protected boolean useRelativePath;
	
	public CreatePatchOperation(IResource resource, String fileName, boolean recurse, boolean ignoreDeleted, boolean processBinary, boolean processUnversioned, boolean useRelativePath) {
		super("Operation.CreatePatchLocal");
		this.resource = resource;
		this.fileName = fileName;
		this.recurse = recurse;
		this.ignoreDeleted = ignoreDeleted;
		this.processBinary = processBinary;
		this.processUnversioned = processUnversioned;
		this.useRelativePath = useRelativePath;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		SVNRemoteStorage storage = SVNRemoteStorage.instance();
		IRepositoryLocation location = storage.getRepositoryLocation(this.resource);
		String wcPath = FileUtility.getWorkingCopyPath(this.resource);
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn diff " + (this.recurse ? "" : " -N") + (this.ignoreDeleted ? " --no-diff-deleted" : "") + "\n");
			long options = ISVNConnector.Options.IGNORE_ANCESTRY;
			options |= this.ignoreDeleted ? ISVNConnector.Options.SKIP_DELETED : ISVNConnector.Options.NONE;
			options |= this.processBinary ? ISVNConnector.Options.FORCE : ISVNConnector.Options.NONE;
			options |= this.processUnversioned ? ISVNConnector.Options.INCLUDE_UNVERSIONED : ISVNConnector.Options.NONE;
			proxy.diff(
				new SVNEntryRevisionReference(wcPath, null, SVNRevision.BASE), new SVNEntryRevisionReference(wcPath, null, SVNRevision.WORKING), this.useRelativePath ? wcPath : null, this.fileName, 
				Depth.infinityOrFiles(this.recurse), options, new SVNProgressMonitor(this, monitor, null));
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}

}
