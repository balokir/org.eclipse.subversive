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

package org.eclipse.team.svn.core.operation.file.refactor;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.file.AbstractFileOperation;
import org.eclipse.team.svn.core.operation.file.IFileProvider;
import org.eclipse.team.svn.core.operation.file.SVNFileStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Delete resources from WC
 * 
 * @author Alexander Gurov
 */
public class DeleteOperation extends AbstractFileOperation {
	public DeleteOperation(File []files) {
		super("Operation_DeleteFile", SVNMessages.class, files); //$NON-NLS-1$
	}

	public DeleteOperation(IFileProvider provider) {
		super("Operation_DeleteFile", SVNMessages.class, provider); //$NON-NLS-1$
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		File []files = FileUtility.shrinkChildNodes(this.operableData(), false);
		for (int i = 0; i < files.length && !monitor.isCanceled(); i++) {
			final File current = files[i];
			IRepositoryResource remote = SVNFileStorage.instance().asRepositoryResource(current, true);
			if (remote == null) {
				ProgressMonitorUtility.setTaskInfo(monitor, this, current.getAbsolutePath());
				FileUtility.deleteRecursive(current, monitor);
				ProgressMonitorUtility.progress(monitor, i, files.length);
			}
			else {
				IRepositoryLocation location = remote.getRepositoryLocation();
				final ISVNConnector proxy = location.acquireSVNProxy();
				this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws Exception {
						DeleteOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn delete \"" + FileUtility.normalizePath(current.getAbsolutePath()) + "\" --force\n"); //$NON-NLS-1$ //$NON-NLS-2$
						proxy.remove(new String[] {current.getAbsolutePath()}, "", ISVNConnector.Options.FORCE, null, new SVNProgressMonitor(DeleteOperation.this, monitor, null)); //$NON-NLS-1$
					}
				}, monitor, files.length);
				location.releaseSVNProxy(proxy);
			}
		}
	}

}
