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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Lock resources operation implementation
 * 
 * @author Alexander Gurov
 */
public class LockOperation extends AbstractWorkingCopyOperation {
	protected String message;
	protected boolean force;

	public LockOperation(IResource []resources, String message, boolean force) {
		super("Operation.Lock", resources);
		this.message = message;
		this.force = force;
	}

	public LockOperation(IResourceProvider provider, String message, boolean force) {
		super("Operation.Lock", provider);
		this.message = message;
		this.force = force;
	}

    protected void runImpl(final IProgressMonitor monitor) throws Exception {
		IResource []resources = this.operableData();

		Map wc2Resources = SVNUtility.splitWorkingCopies(resources);
		for (Iterator it = wc2Resources.entrySet().iterator(); it.hasNext() && !monitor.isCanceled(); ) {
			Map.Entry entry = (Map.Entry)it.next();
			final IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation((IProject)entry.getKey());
			final String []paths = FileUtility.asPathArray((IResource [])((List)entry.getValue()).toArray(new IResource[0]));
			
			this.complexWriteToConsole(new Runnable() {
				public void run() {
					LockOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn lock");
					for (int i = 0; i < paths.length && !monitor.isCanceled(); i++) {
						LockOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " \"" + paths[i] + "\"");
					}
					LockOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, (LockOperation.this.force ? " --force" : "") + " -m \"" + LockOperation.this.message + "\"" + FileUtility.getUsernameParam(location.getUsername()) + "\n");
				}
			});
			
			final ISVNConnector proxy = location.acquireSVNProxy();
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					proxy.lock(
						paths, 
						LockOperation.this.message, 
						LockOperation.this.force ? ISVNConnector.Options.FORCE : ISVNConnector.Options.NONE, 
						new SVNProgressMonitor(LockOperation.this, monitor, null));
				}
			}, monitor, wc2Resources.size());
			location.releaseSVNProxy(proxy);
		}
    }

}
