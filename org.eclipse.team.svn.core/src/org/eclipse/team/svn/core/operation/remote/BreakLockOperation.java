/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.remote;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Break repository resources lock operation
 *
 * @author Sergiy Logvin
 */
public class BreakLockOperation extends AbstractRepositoryOperation {
	public BreakLockOperation(IRepositoryResource []resources) {
		super("Operation.BreakLock", resources);
	}

	protected void runImpl(final IProgressMonitor monitor) throws Exception {
		IRepositoryResource []resources = this.operableData();
		Map<?, ?> splittedSet = SVNUtility.splitRepositoryLocations(resources);
		
		for (Iterator<?> it = splittedSet.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry entry = (Map.Entry)it.next();
			
			final IRepositoryLocation location = (IRepositoryLocation)entry.getKey();
			List<?> values = (List<?>)entry.getValue();
			final String []paths = SVNUtility.asURLArray(values.toArray(new IRepositoryResource[values.size()]), true);

			this.complexWriteToConsole(new Runnable() {
				public void run() {
					BreakLockOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn unlock");
					for (int i = 0; i < paths.length && !monitor.isCanceled(); i++) {
						BreakLockOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " \"" + SVNUtility.decodeURL(paths[i]) + "\"");
					}
					BreakLockOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " --force" + FileUtility.getUsernameParam(location.getUsername()) + "\n");
				}
			});
			
			final ISVNConnector proxy = location.acquireSVNProxy();
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					proxy.unlock(paths, ISVNConnector.Options.FORCE, new SVNProgressMonitor(BreakLockOperation.this, monitor, null));
				}
			}, monitor, splittedSet.size());
			location.releaseSVNProxy(proxy);
		}
	}

}
