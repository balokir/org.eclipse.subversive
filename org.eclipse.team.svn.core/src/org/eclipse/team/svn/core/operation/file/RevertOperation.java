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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.client.ISVNConnector;
import org.eclipse.team.svn.core.client.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Revert local modifications
 * 
 * @author Alexander Gurov
 */
public class RevertOperation extends AbstractFileOperation {
	protected boolean recursive;

	public RevertOperation(File []files, boolean recursive) {
		super("Operation.RevertFile", files);
		this.recursive = recursive;
	}

	public RevertOperation(IFileProvider provider, boolean recursive) {
		super("Operation.RevertFile", provider);
		this.recursive = recursive;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		File []files = this.operableData();
		
		if (this.recursive) {
			files = FileUtility.shrinkChildNodes(files, false);
		}
		else {
			FileUtility.reorder(files, false);
		}
		
		for (int i = 0; i < files.length && !monitor.isCanceled(); i++) {
			final File current = files[i];
			IRepositoryResource remote = SVNFileStorage.instance().asRepositoryResource(current, false);
			IRepositoryLocation location = remote.getRepositoryLocation();
			final ISVNConnector proxy = location.acquireSVNProxy();
			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn revert \"" + FileUtility.normalizePath(current.getAbsolutePath()) + "\"" + (this.recursive ? " -R" : "") + "\n");
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					proxy.revert(current.getAbsolutePath(), Depth.infinityOrEmpty(RevertOperation.this.recursive), new SVNProgressMonitor(RevertOperation.this, monitor, null));
				}
			}, monitor, files.length);
			location.releaseSVNProxy(proxy);
		}
	}

}
