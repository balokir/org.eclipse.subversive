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
import org.eclipse.team.svn.core.client.ISVNClient;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Unlock resources
 * 
 * @author Alexander Gurov
 */
public class UnlockOperation extends AbstractFileOperation {
	public UnlockOperation(File []files) {
		super("Operation.UnlockFile", files);
	}

	public UnlockOperation(IFileProvider provider) {
		super("Operation.UnlockFile", provider);
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
					UnlockOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn unlock");
					for (int i = 0; i < paths.length && !monitor.isCanceled(); i++) {
						UnlockOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " \"" + paths[i] + "\"");
					}
					UnlockOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " --force" + FileUtility.getUsernameParam(location.getUsername()) + "\n");
				}
			});
			
			final ISVNClient proxy = location.acquireSVNProxy();
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					proxy.unlock(paths, true, new SVNProgressMonitor(UnlockOperation.this, monitor, null));
				}
			}, monitor, wc2Resources.size());
			
			location.releaseSVNProxy(proxy);
		}
	}

}
