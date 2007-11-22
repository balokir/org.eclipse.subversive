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

package org.eclipse.team.svn.core.operation.local.management;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.local.AbstractWorkingCopyOperation;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Cleanup working copy after failure operation implementation
 * 
 * @author Alexander Gurov
 */
public class CleanupOperation extends AbstractWorkingCopyOperation {
	public CleanupOperation(IResource[] resources) {
		super("Operation.CleanupResources", resources);
	}

	public CleanupOperation(IResourceProvider provider) {
		super("Operation.CleanupResources", provider);
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []resources = this.operableData();

		IRemoteStorage storage = SVNRemoteStorage.instance();
		
		for (int i = 0; i < resources.length; i++) {
			IRepositoryLocation location = storage.getRepositoryLocation(resources[i]);
			final String wcPath = FileUtility.getWorkingCopyPath(resources[i]);
			final ISVNConnector proxy = location.acquireSVNProxy();
			
			ProgressMonitorUtility.setTaskInfo(monitor, this, resources[i].getName());

			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn cleanup \"" + FileUtility.normalizePath(wcPath) + "\"\n");
			
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					proxy.cleanup(
						wcPath, 
						new SVNProgressMonitor(CleanupOperation.this, monitor, null));
				}
			}, monitor, resources.length);
			location.releaseSVNProxy(proxy);
			ProgressMonitorUtility.progress(monitor, i, resources.length);
		}
	}

}
