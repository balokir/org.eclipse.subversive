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

package org.eclipse.team.svn.core.operation.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.client.ISVNClient;
import org.eclipse.team.svn.core.client.ISVNClient.Depth;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Revert operation implementation
 * 
 * @author Alexander Gurov
 */
public class RevertOperation extends AbstractWorkingCopyOperation {
	protected boolean doRecursiveRevert;

	public RevertOperation(IResource []resources, boolean doRecursiveRevert) {
		super("Operation.Revert", resources);
		this.doRecursiveRevert = doRecursiveRevert;
	}

	public RevertOperation(IResourceProvider provider, boolean doRecursiveRevert) {
		super("Operation.Revert", provider);
		this.doRecursiveRevert = doRecursiveRevert;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []resources = this.operableData();

		if (this.doRecursiveRevert) {
		    resources = FileUtility.shrinkChildNodes(resources);
		}
		else {
			FileUtility.reorder(resources, false);
		}
		
		IRemoteStorage storage = SVNRemoteStorage.instance();
		for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
			IRepositoryLocation location = storage.getRepositoryLocation(resources[i]);
			final String wcPath = FileUtility.getWorkingCopyPath(resources[i]);
			final ISVNClient proxy = location.acquireSVNProxy();
			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn revert \"" + FileUtility.normalizePath(wcPath) + "\"" + (this.doRecursiveRevert ? " -R" : "") + "\n");
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					proxy.revert(wcPath, 
								 Depth.infinityOrEmpty(RevertOperation.this.doRecursiveRevert), 
								 new SVNProgressMonitor(RevertOperation.this, monitor, null));
				}
			}, monitor, resources.length);
			location.releaseSVNProxy(proxy);
		}
	}

}
