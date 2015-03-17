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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNNotificationCallback;
import org.eclipse.team.svn.core.connector.SVNNotification;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.UnreportableException;
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
        super("Operation_Unlock", SVNMessages.class, resources); //$NON-NLS-1$
    }

    public UnlockOperation(IResourceProvider provider) {
        super("Operation_Unlock", SVNMessages.class, provider); //$NON-NLS-1$
    }
    
    protected void runImpl(final IProgressMonitor monitor) throws Exception {
		IResource []resources = this.operableData();

		final List<SVNNotification> problems = new ArrayList<SVNNotification>();
		IRemoteStorage storage = SVNRemoteStorage.instance();
		Map<?, ?> wc2Resources = SVNUtility.splitWorkingCopies(resources);
		for (Iterator<?> it = wc2Resources.entrySet().iterator(); it.hasNext() && !monitor.isCanceled(); ) {
			Map.Entry entry = (Map.Entry)it.next();
			final IRepositoryLocation location = storage.getRepositoryLocation((IProject)entry.getKey());
			final String []paths = FileUtility.asPathArray(((List<?>)entry.getValue()).toArray(new IResource[0]));

			this.complexWriteToConsole(new Runnable() {
				public void run() {
					UnlockOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn unlock" + ISVNConnector.Options.asCommandLine(ISVNConnector.Options.FORCE)); //$NON-NLS-1$
					for (int i = 0; i < paths.length && !monitor.isCanceled(); i++) {
						UnlockOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " \"" + paths[i] + "\""); //$NON-NLS-1$ //$NON-NLS-2$
					}
					UnlockOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, FileUtility.getUsernameParam(location.getUsername()) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			});
			
			final ISVNConnector proxy = location.acquireSVNProxy();
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					
					ISVNNotificationCallback listener = new ISVNNotificationCallback() {
						public void notify(SVNNotification info) {					
							if (SVNNotification.PerformedAction.FAILED_UNLOCK == info.action) {									
								problems.add(info);				
							}					
						}						
					};
					
					SVNUtility.addSVNNotifyListener(proxy, listener);						
					try {
						proxy.unlock(
								paths, 
								ISVNConnector.Options.FORCE, 
								new SVNProgressMonitor(UnlockOperation.this, monitor, null));						
					} finally {
						SVNUtility.removeSVNNotifyListener(proxy, listener);
					}
				}
			}, monitor, wc2Resources.size());
			location.releaseSVNProxy(proxy);
		}
		
		//check problems
		if (!problems.isEmpty()) {
			StringBuffer res = new StringBuffer();
			Iterator<SVNNotification> iter = problems.iterator();
			while (iter.hasNext()) {
				SVNNotification problem = iter.next();
				res.append(problem.errMsg);
				if (iter.hasNext()) {
					res.append("\n\n");
				}
			}
			throw new UnreportableException(res.toString());
		}
    }

}
