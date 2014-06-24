/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Lock resources operation implementation
 * 
 * @author Alexander Gurov
 */
public class LockOperation extends AbstractFileOperation {
	protected String message;
	protected long options;

	public LockOperation(File []files, String message, boolean force) {
		this(files, message, force ? ISVNConnector.Options.FORCE : ISVNConnector.Options.NONE);
	}

	public LockOperation(IFileProvider provider, String message, boolean force) {
		this(provider, message, force ? ISVNConnector.Options.FORCE : ISVNConnector.Options.NONE);
	}

	public LockOperation(File []files, String message, long options) {
		super("Operation_LockFile", SVNMessages.class, files); //$NON-NLS-1$
		this.message = message;
		this.options = options & ISVNConnector.CommandMasks.LOCK;
	}

	public LockOperation(IFileProvider provider, String message, long options) {
		super("Operation_LockFile", SVNMessages.class, provider); //$NON-NLS-1$
		this.message = message;
		this.options = options & ISVNConnector.CommandMasks.LOCK;
	}

	protected void runImpl(final IProgressMonitor monitor) throws Exception {
		File []files = this.operableData();

		final List<SVNNotification> problems = new ArrayList<SVNNotification>(); 
		Map<?, ?> wc2Resources = SVNUtility.splitWorkingCopies(files);
		for (Iterator<?> it = wc2Resources.entrySet().iterator(); it.hasNext() && !monitor.isCanceled(); ) {
			Map.Entry entry = (Map.Entry)it.next();
			IRepositoryResource wcRoot = SVNFileStorage.instance().asRepositoryResource((File)entry.getKey(), false);
			final IRepositoryLocation location = wcRoot.getRepositoryLocation();
			
			final String []paths = FileUtility.asPathArray(((List<?>)entry.getValue()).toArray(new File[0]));
			
			this.complexWriteToConsole(new Runnable() {
				public void run() {
					LockOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn lock"); //$NON-NLS-1$
					for (int i = 0; i < paths.length && !monitor.isCanceled(); i++) {
						LockOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " \"" + paths[i] + "\""); //$NON-NLS-1$ //$NON-NLS-2$
					}
					LockOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, ((LockOperation.this.options & ISVNConnector.Options.FORCE) != 0 ? " --force" : "") + " -m \"" + LockOperation.this.message + "\"" + FileUtility.getUsernameParam(location.getUsername()) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
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
						proxy.lock(paths, LockOperation.this.message, LockOperation.this.options, new SVNProgressMonitor(LockOperation.this, monitor, null));	
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
