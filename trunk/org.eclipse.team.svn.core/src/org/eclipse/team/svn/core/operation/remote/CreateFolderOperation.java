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

package org.eclipse.team.svn.core.operation.remote;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.BaseMessages;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNNotificationCallback;
import org.eclipse.team.svn.core.connector.SVNNotification;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IRevisionProvider;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Create remote folder operation
 * 
 * @author Alexander Gurov
 */
public class CreateFolderOperation extends AbstractRepositoryOperation implements IRevisionProvider {
	protected String []names;
	protected String comment;
	protected RevisionPair []revisionPair;

	public CreateFolderOperation(IRepositoryResource parent, String name, String comment) {
		this(parent, new String[] {name}, comment);
	}

	public CreateFolderOperation(IRepositoryResource parent, String []names, String comment) {
		super("Operation_CreateFolder", new IRepositoryResource[] {parent}); //$NON-NLS-1$
		this.names = names;
		this.comment = comment;
	}

	protected void runImpl(final IProgressMonitor monitor) throws Exception {
		this.revisionPair = new RevisionPair[1];
		IRepositoryResource parent = this.operableData()[0];
		final IRepositoryLocation location = parent.getRepositoryLocation();
		ProgressMonitorUtility.setTaskInfo(monitor, this, parent.getUrl() + "/" + this.names[0]); //$NON-NLS-1$
		
		Set<IRepositoryResource> fullSet = new HashSet<IRepositoryResource>();
		for (int i = 0; i < this.names.length; i++) {
			fullSet.addAll(Arrays.asList(SVNUtility.makeResourceSet(parent, this.names[i], false)));
		}
		IRepositoryResource []toBeCreated = fullSet.toArray(new IRepositoryResource[fullSet.size()]);
		
		final String []childUrls = SVNUtility.asURLArray(toBeCreated, true);
		Arrays.sort(childUrls);
		
		ISVNNotificationCallback notify = new ISVNNotificationCallback() {
			public void notify(SVNNotification info) {
				String [] path = childUrls;
				CreateFolderOperation.this.revisionPair[0] = new RevisionPair(info.revision, path, location);
				String message = SVNMessages.format(SVNMessages.Console_CommittedRevision, new String[] {String.valueOf(info.revision)});
				CreateFolderOperation.this.writeToConsole(IConsoleStream.LEVEL_OK, message);
			}
		};
		
		this.complexWriteToConsole(new Runnable() {
			public void run() {
				CreateFolderOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn mkdir"); //$NON-NLS-1$
				for (int i = 0; i < childUrls.length && !monitor.isCanceled(); i++) {
					CreateFolderOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " \"" + SVNUtility.decodeURL(childUrls[i]) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
				}
				CreateFolderOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " -m \"" + CreateFolderOperation.this.comment + "\"" + FileUtility.getUsernameParam(location.getUsername()) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		});
		
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
			SVNUtility.addSVNNotifyListener(proxy, notify);
			proxy.mkdir(childUrls, this.comment, ISVNConnector.Options.NONE, null, new SVNProgressMonitor(this, monitor, null));
		}
		finally {
			SVNUtility.removeSVNNotifyListener(proxy, notify);
		    parent.getRepositoryLocation().releaseSVNProxy(proxy);
		}
		
		ProgressMonitorUtility.progress(monitor, 1, 1);
	}
	
	public RevisionPair []getRevisions() {
		return this.revisionPair;
	}
	
	
	protected String getShortErrorMessage(Throwable t) {
		return BaseMessages.format(super.getShortErrorMessage(t), new Object[] {this.name});
	}
	
}
