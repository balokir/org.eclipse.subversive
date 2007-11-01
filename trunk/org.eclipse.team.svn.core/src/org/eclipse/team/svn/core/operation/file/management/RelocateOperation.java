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

package org.eclipse.team.svn.core.operation.file.management;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.client.Depth;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.file.AbstractFileOperation;
import org.eclipse.team.svn.core.operation.file.IFileProvider;
import org.eclipse.team.svn.core.operation.file.SVNFileStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Relocate specified folders to the new url.
 * 
 * @author Alexander Gurov
 */
public class RelocateOperation extends AbstractFileOperation {
	protected String toUrl;
	
	public RelocateOperation(File []folders, String toUrl) {
		super("Operation.RelocateFile", folders);
		this.toUrl = toUrl;
	}

	public RelocateOperation(IFileProvider provider, String toUrl) {
		super("Operation.RelocateFile", provider);
		this.toUrl = toUrl;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		File []files = FileUtility.shrinkChildNodes(this.operableData(), true);
		
		for (int i = 0; i < files.length && !monitor.isCanceled(); i++) {
			final File current = files[i];
			final IRepositoryResource remote = SVNFileStorage.instance().asRepositoryResource(current, false);
			final IRepositoryLocation location = remote.getRepositoryLocation();
			final ISVNClientWrapper proxy = location.acquireSVNProxy();
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					String path = current.getAbsolutePath();
					RelocateOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn switch --relocate \"" + remote.getUrl() + "\" \"" + RelocateOperation.this.toUrl + "\" \"" + FileUtility.normalizePath(path) + "\"" + FileUtility.getUsernameParam(location.getUsername()) + "\n");
					proxy.relocate(SVNUtility.encodeURL(remote.getUrl()), SVNUtility.encodeURL(RelocateOperation.this.toUrl), path, Depth.INFINITY, new SVNProgressMonitor(RelocateOperation.this, monitor, null));
				}
			}, monitor, files.length);
			location.releaseSVNProxy(proxy);
		}
	}

}
