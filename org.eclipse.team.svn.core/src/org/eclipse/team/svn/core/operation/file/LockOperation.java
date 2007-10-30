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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Lock resources operation implementation
 * 
 * @author Alexander Gurov
 */
public class LockOperation extends AbstractFileOperation {
	protected String message;
	protected boolean force;

	public LockOperation(File []files, String message, boolean force) {
		super("Operation.LockFile", files);
		this.message = message;
		this.force = force;
	}

	public LockOperation(IFileProvider provider, String message, boolean force) {
		super("Operation.LockFile", provider);
		this.message = message;
		this.force = force;
	}

	protected void runImpl(final IProgressMonitor monitor) throws Exception {
		File []files = this.operableData();

		Map wc2Resources = SVNUtility.splitWorkingCopies(files);
		for (Iterator it = wc2Resources.entrySet().iterator(); it.hasNext() && !monitor.isCanceled(); ) {
			Map.Entry entry = (Map.Entry)it.next();
			IRepositoryResource wcRoot = SVNFileStorage.instance().asRepositoryResource((File)entry.getKey(), false);
			final IRepositoryLocation location = wcRoot.getRepositoryLocation();
			
			final String []paths = FileUtility.asPathArray((File [])((List)entry.getValue()).toArray(new File[0]));
			
			this.complexWriteToConsole(new Runnable() {
				public void run() {
					LockOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn lock");
					for (int i = 0; i < paths.length && !monitor.isCanceled(); i++) {
						LockOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " \"" + paths[i] + "\"");
					}
					LockOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, (LockOperation.this.force ? " --force" : "") + " -m \"" + LockOperation.this.message + "\"" + FileUtility.getUsernameParam(location.getUsername()) + "\n");
				}
			});
			
			final ISVNClientWrapper proxy = location.acquireSVNProxy();
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					proxy.lock(
						paths, 
						LockOperation.this.message, 
						LockOperation.this.force, 
						new SVNProgressMonitor(LockOperation.this, monitor, null));
				}
			}, monitor, wc2Resources.size());
			location.releaseSVNProxy(proxy);
		}
	}

}
