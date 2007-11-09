/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Vladimir Bykov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.remote;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.client.ISVNClient;
import org.eclipse.team.svn.core.client.ISVNNotificationCallback;
import org.eclipse.team.svn.core.client.SVNNotification;
import org.eclipse.team.svn.core.client.SVNRevision;
import org.eclipse.team.svn.core.client.ISVNClient.Depth;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IRevisionProvider;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Create new remote file by importing it from local file system 
 * 
 * @author Vladimir Bykov
 */
public class CreateFileOperation extends AbstractRepositoryOperation implements IRevisionProvider {
	protected String path;
	protected String message;
	protected String []fileNames;
	protected RevisionPair []revisionPair;
	
	public CreateFileOperation(IRepositoryResource resource, String path, String message, String []fileNames) {
		super("Operation.CreateFile", new IRepositoryResource[] {resource});
		this.path = path;
		this.message = message;
		this.fileNames = fileNames;
	}
	
	public RevisionPair []getRevisions() {
		return this.revisionPair;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		final IRepositoryResource resource = this.operableData()[0];
		final IRepositoryLocation location = resource.getRepositoryLocation();
		this.revisionPair = new RevisionPair[1];
		final ISVNClient proxy = location.acquireSVNProxy();
		ISVNNotificationCallback notify = new ISVNNotificationCallback() {
			public void notify(SVNNotification info) {
				if (info.revision != SVNRevision.INVALID_REVISION_NUMBER) {
					String []path = new String[] {resource.getUrl()};
					CreateFileOperation.this.revisionPair[0] = new RevisionPair(info.revision, path, location);
					String message = SVNTeamPlugin.instance().getResource("Console.CommittedRevision");
					CreateFileOperation.this.writeToConsole(IConsoleStream.LEVEL_OK, MessageFormat.format(message, new String[] {String.valueOf(info.revision)}));
				}
			}
		};
		try {
			SVNUtility.addSVNNotifyListener(proxy, notify);
			for (int i = 0; i < this.fileNames.length; i++) {
				final String []currentFile = new String[] {this.fileNames[i]};
				this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws Exception {
						String path = FileUtility.normalizePath(CreateFileOperation.this.path + "/" + currentFile[0]);
						String url = resource.getUrl() + "/" + currentFile[0];
						CreateFileOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn import \"" + path + "\" \"" + url + "\" -m \"" + CreateFileOperation.this.message + "\"" + FileUtility.getUsernameParam(location.getUsername()) + " -N\n");
						proxy.doImport(path, 
								SVNUtility.encodeURL(url), 
								CreateFileOperation.this.message, 
								Depth.FILES,
								true, 
								true, 
								new SVNProgressMonitor(CreateFileOperation.this, monitor, null));		
					}}, monitor, this.fileNames.length);
			}
		}
		finally {
			SVNUtility.removeSVNNotifyListener(proxy, notify);
			location.releaseSVNProxy(proxy);
		}
	}

}