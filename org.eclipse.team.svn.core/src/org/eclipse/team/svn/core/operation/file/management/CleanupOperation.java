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
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.file.AbstractFileOperation;
import org.eclipse.team.svn.core.operation.file.IFileProvider;
import org.eclipse.team.svn.core.operation.file.IFileStorage;
import org.eclipse.team.svn.core.operation.file.SVNFileStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Cleanup working copy after failure operation implementation
 * 
 * @author Alexander Gurov
 */
public class CleanupOperation extends AbstractFileOperation {
	public CleanupOperation(File []files) {
		super("Operation.CleanupFile", files);
	}

	public CleanupOperation(IFileProvider provider) {
		super("Operation.CleanupFile", provider);
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		File []files = this.operableData();

		IFileStorage storage = SVNFileStorage.instance();
		
		for (int i = 0; i < files.length && !monitor.isCanceled(); i++) {
			IRepositoryResource remote = storage.asRepositoryResource(files[i], false);
			
			IRepositoryLocation location = remote.getRepositoryLocation();
			final ISVNClientWrapper proxy = location.acquireSVNProxy();
			final String current = files[i].getAbsolutePath();
			
			ProgressMonitorUtility.setTaskInfo(monitor, this, current);

			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn cleanup \"" + FileUtility.normalizePath(current) + "\"\n");
			
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					proxy.cleanup(current, new SVNProgressMonitor(CleanupOperation.this, monitor, null));
				}
			}, monitor, files.length);
			location.releaseSVNProxy(proxy);
		}
	}

}
