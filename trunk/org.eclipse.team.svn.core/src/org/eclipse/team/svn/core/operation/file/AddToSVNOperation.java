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
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Add to version control operation
 * 
 * @author Alexander Gurov
 */
public class AddToSVNOperation extends AbstractFileOperation {
	protected boolean isRecursive;
	
	public AddToSVNOperation(File []files, boolean isRecursive) {
		super("Operation.AddToSVNFile", files);
		this.isRecursive = isRecursive;
	}

	public AddToSVNOperation(IFileProvider provider, boolean isRecursive) {
		super("Operation.AddToSVNFile", provider);
		this.isRecursive = isRecursive;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		File []files = this.operableData();
		if (this.isRecursive) {
			files = FileUtility.shrinkChildNodes(files, false);
		}
		else {
			FileUtility.reorder(files, true);
		}
		
		for (int i = 0; i < files.length && !monitor.isCanceled(); i++) {
		    final File current = files[i];
			IRepositoryLocation location = SVNFileStorage.instance().asRepositoryResource(current, false).getRepositoryLocation();
			final ISVNConnector proxy = location.acquireSVNProxy();
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					AddToSVNOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn add \"" + FileUtility.normalizePath(current.getAbsolutePath()) + "\"" + (AddToSVNOperation.this.isRecursive ? "" : " -N") + "\n");
					
					File parent = current.getParentFile();
					if (parent != null) {
						org.eclipse.team.svn.core.operation.local.AddToSVNOperation.removeFromParentIgnore(proxy, parent.getAbsolutePath(), current.getName());
					}
					
					proxy.add(current.getAbsolutePath(), Depth.infinityOrEmpty(AddToSVNOperation.this.isRecursive), ISVNConnector.Options.FORCE | ISVNConnector.Options.INCLUDE_PARENTS, new SVNProgressMonitor(AddToSVNOperation.this, monitor, null));
				}
			}, monitor, files.length);
			location.releaseSVNProxy(proxy);
		}
	}

}
