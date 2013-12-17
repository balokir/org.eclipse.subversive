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
		super("Operation_Lock", SVNMessages.class, resources); //$NON-NLS-1$
		this.message = message;
		this.force = force;
	}

	public LockOperation(IResourceProvider provider, String message, boolean force) {
		super("Operation_Lock", SVNMessages.class, provider); //$NON-NLS-1$
		this.message = message;
		this.force = force;
	}

    protected void runImpl(final IProgressMonitor monitor) throws Exception {
		IResource []resources = this.operableData();
		
		final List<SVNNotification> problems = new ArrayList<SVNNotification>(); 
		Map<?, ?> wc2Resources = SVNUtility.splitWorkingCopies(resources);
		for (Iterator<?> it = wc2Resources.entrySet().iterator(); it.hasNext() && !monitor.isCanceled(); ) {
			Map.Entry entry = (Map.Entry)it.next();
			final IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation((IProject)entry.getKey());
			final String []paths = FileUtility.asPathArray(((List<?>)entry.getValue()).toArray(new IResource[0]));
			
			this.complexWriteToConsole(new Runnable() {
				public void run() {
					LockOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn lock"); //$NON-NLS-1$
					for (int i = 0; i < paths.length && !monitor.isCanceled(); i++) {
						LockOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " \"" + paths[i] + "\""); //$NON-NLS-1$ //$NON-NLS-2$
					}
					LockOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, (LockOperation.this.force ? " --force" : "") + " -m \"" + LockOperation.this.message + "\"" + FileUtility.getUsernameParam(location.getUsername()) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				}
			});
			
			final ISVNConnector proxy = location.acquireSVNProxy();
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					
					/*
					 * Lock operation errors are handled in different way than other errors.
					 * No exception is thrown in case certain file couldn't be locked,
					 * but instead error event is dispatched. 
					 * It is implemented in this way because lock operation could be
					 * performed on multiple files at once and some of them could be locked,
					 * while some not - caller will receive LOCKED events for successfully locked
					 * files and LOCK_FAILED for those that wasn't locked. 
					 */
					ISVNNotificationCallback listener = new ISVNNotificationCallback() {
						//SVNEventAction.LOCK_FAILED 23
						//SVNEventAction.LOCKED 21
						
						protected final static int FAILED = 23;
						
						public void notify(SVNNotification info) {					
							if (FAILED == info.action) {									
								problems.add(info);				
							}					
						}						
					};
					
					SVNUtility.addSVNNotifyListener(proxy, listener);					
					try {
						proxy.lock(
								paths, 
								LockOperation.this.message, 
								LockOperation.this.force ? ISVNConnector.Options.FORCE : ISVNConnector.Options.NONE, 
								new SVNProgressMonitor(LockOperation.this, monitor, null));	
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