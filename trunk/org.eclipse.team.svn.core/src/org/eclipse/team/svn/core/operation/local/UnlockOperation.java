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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.client.ISVNConnector;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Unlock resources operation implementation
 * 
 * @author Alexander Gurov
 */
public class UnlockOperation extends AbstractWorkingCopyOperation {
    public UnlockOperation(IResource []resources) {
        super("Operation.Unlock", resources);
    }

    public UnlockOperation(IResourceProvider provider) {
        super("Operation.Unlock", provider);
    }
    
    protected void runImpl(final IProgressMonitor monitor) throws Exception {
		IResource []resources = this.operableData();

		IRemoteStorage storage = SVNRemoteStorage.instance();
		Map wc2Resources = SVNUtility.splitWorkingCopies(resources);
		for (Iterator it = wc2Resources.entrySet().iterator(); it.hasNext() && !monitor.isCanceled(); ) {
			Map.Entry entry = (Map.Entry)it.next();
			final IRepositoryLocation location = storage.getRepositoryLocation((IProject)entry.getKey());
			final String []paths = FileUtility.asPathArray((IResource [])((List)entry.getValue()).toArray(new IResource[0]));

			this.complexWriteToConsole(new Runnable() {
				public void run() {
					UnlockOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn unlock");
					for (int i = 0; i < paths.length && !monitor.isCanceled(); i++) {
						UnlockOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " \"" + paths[i] + "\"");
					}
					UnlockOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " --force" + FileUtility.getUsernameParam(location.getUsername()) + "\n");
				}
			});
			
			final ISVNConnector proxy = location.acquireSVNProxy();
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					proxy.unlock(
						paths, 
						true, 
						new SVNProgressMonitor(UnlockOperation.this, monitor, null));
				}
			}, monitor, wc2Resources.size());
			location.releaseSVNProxy(proxy);
		}
    }

}
