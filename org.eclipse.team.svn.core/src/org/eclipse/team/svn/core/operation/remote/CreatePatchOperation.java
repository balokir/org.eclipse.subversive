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

package org.eclipse.team.svn.core.operation.remote;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.client.Depth;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
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
		ISVNClientWrapper proxy = location.acquireSVNProxy();
		try {
			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn diff \"" + first.getUrl() + "@" + first.getSelectedRevision() + "\" \"" + second.getUrl() + "@" + second.getSelectedRevision() + "\"" + (this.recurse ? "" : " -N") + (this.ignoreDeleted ? " --no-diff-deleted" : "") + FileUtility.getUsernameParam(location.getUsername()) + "\n");
			proxy.diff(
				SVNUtility.encodeURL(first.getUrl()), first.getSelectedRevision(), first.getPegRevision(), 
				SVNUtility.encodeURL(second.getUrl()), second.getSelectedRevision(), second.getPegRevision(), 
				this.fileName, this.recurse ? Depth.infinity : Depth.immediates, this.ignoreAncestry, this.ignoreDeleted, this.processBinary, 
				false, false, new SVNProgressMonitor(this, monitor, null));
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}

}
