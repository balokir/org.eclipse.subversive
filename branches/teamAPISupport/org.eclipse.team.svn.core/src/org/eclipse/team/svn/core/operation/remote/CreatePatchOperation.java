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

package org.eclipse.team.svn.core.operation.remote;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Create diff between two repository resources. Will be usefull, for example, in order to collect differences between trunk and branch.
 * 
 * @author Alexander Gurov
 */
public class CreatePatchOperation extends AbstractRepositoryOperation {
	protected String fileName;
	protected boolean recurse;
	protected boolean ignoreDeleted;
	protected boolean processBinary;
	protected boolean ignoreAncestry;

	public CreatePatchOperation(IRepositoryResource first, IRepositoryResource second, String fileName, boolean recurse, boolean ignoreDeleted, boolean processBinary) {
		this(first, second, fileName, recurse, ignoreDeleted, processBinary, true);
	}
	
	public CreatePatchOperation(IRepositoryResource first, IRepositoryResource second, String fileName, boolean recurse, boolean ignoreDeleted, boolean processBinary, boolean ignoreAncestry) {
		super("Operation.CreatePatchRemote", new IRepositoryResource[] {first, second});
		this.fileName = fileName;
		this.recurse = recurse;
		this.ignoreDeleted = ignoreDeleted;
		this.processBinary = processBinary;
		this.ignoreAncestry = ignoreAncestry;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRepositoryResource first = this.operableData()[0];
		IRepositoryResource second = this.operableData()[1];
		IRepositoryLocation location = first.getRepositoryLocation();
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
			SVNEntryRevisionReference ref1 = SVNUtility.getEntryRevisionReference(first);
			SVNEntryRevisionReference ref2 = SVNUtility.getEntryRevisionReference(second);
			long options = this.ignoreAncestry ? ISVNConnector.Options.IGNORE_ANCESTRY : ISVNConnector.Options.NONE;
			options |= this.ignoreDeleted ? ISVNConnector.Options.SKIP_DELETED : ISVNConnector.Options.NONE;
			options |= this.processBinary ? ISVNConnector.Options.FORCE : ISVNConnector.Options.NONE;
			if (SVNUtility.useSingleReferenceSignature(ref1, ref2)) {
				this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn diff -r " + ref1.revision + ":" + ref2.revision + " \"" + first.getUrl() + "@" + ref1.pegRevision + "\"" + (this.recurse ? "" : " -N") + (this.ignoreDeleted ? " --no-diff-deleted" : "") + FileUtility.getUsernameParam(location.getUsername()) + "\n");
				proxy.diff(ref1, ref1.revision, ref2.revision, null, this.fileName, 
						this.recurse ? Depth.INFINITY : Depth.IMMEDIATES, options, null, new SVNProgressMonitor(this, monitor, null));
			}
			else {
				this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn diff \"" + first.getUrl() + "@" + first.getSelectedRevision() + "\" \"" + second.getUrl() + "@" + second.getSelectedRevision() + "\"" + (this.recurse ? "" : " -N") + (this.ignoreDeleted ? " --no-diff-deleted" : "") + FileUtility.getUsernameParam(location.getUsername()) + "\n");
				proxy.diff(ref1, ref2, null, this.fileName, 
						this.recurse ? Depth.INFINITY : Depth.IMMEDIATES, options, null, new SVNProgressMonitor(this, monitor, null));
			}
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}

}
