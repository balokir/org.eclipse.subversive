/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
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
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
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
		super("Operation_Revert", resources); //$NON-NLS-1$
		this.doRecursiveRevert = doRecursiveRevert;
	}

	public RevertOperation(IResourceProvider provider, boolean doRecursiveRevert) {
		super("Operation_Revert", provider); //$NON-NLS-1$
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
			final ISVNConnector proxy = location.acquireSVNProxy();
			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn revert \"" + FileUtility.normalizePath(wcPath) + "\"" + (this.doRecursiveRevert ? " -R" : "") + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					proxy.revert(wcPath, 
								 Depth.infinityOrEmpty(RevertOperation.this.doRecursiveRevert), 
								 null, new SVNProgressMonitor(RevertOperation.this, monitor, null));
				}
			}, monitor, resources.length);
			location.releaseSVNProxy(proxy);
		}
	}

}
