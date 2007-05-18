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
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.Notify2;
import org.eclipse.team.svn.core.client.NotifyInformation;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IRevisionProvider;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Operation organize import repository resources to file sistem
 * 
 * @author Vladimir Bykov
 */
public class ImportOperation extends AbstractRepositoryOperation implements IRevisionProvider {
	protected String path;
	protected String message;
	protected boolean isRecursive;
	protected RevisionPair []revisionPair;
	
	public ImportOperation(IRepositoryResource resource, String path, String message, boolean isRecursive) {
		super("Operation.Import", new IRepositoryResource[] {resource});
		this.path = path;
		this.message = message;
		this.isRecursive = isRecursive;
	}
	
	public RevisionPair []getRevisions() {
		return this.revisionPair;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		final IRepositoryResource resource = this.operableData()[0];
		final IRepositoryLocation location = resource.getRepositoryLocation();
		this.revisionPair = new RevisionPair[1];
		ISVNClientWrapper proxy = location.acquireSVNProxy();
		Notify2 notify = new Notify2() {
			public void onNotify(NotifyInformation info) {
				if (info.revision != Revision.SVN_INVALID_REVNUM) {
					String []path = new String[] {resource.getUrl()};
					ImportOperation.this.revisionPair[0] = new RevisionPair(info.revision, path, location);
					String message = SVNTeamPlugin.instance().getResource("Console.CommittedRevision");
					ImportOperation.this.writeToConsole(IConsoleStream.LEVEL_OK, MessageFormat.format(message, new String[] {String.valueOf(info.revision)}));
				}
			}
		};
		try {
			SVNUtility.addSVNNotifyListener(proxy, notify);
			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn import \"" + FileUtility.normalizePath(this.path) + "\" \"" + resource.getUrl() + "\"" + (this.isRecursive ? "" : " -N") + " -m \"" + this.message + "\"" + FileUtility.getUsernameParam(location.getUsername()) + "\n");
			proxy.doImport(this.path, SVNUtility.encodeURL(resource.getUrl()), this.message, this.isRecursive, new SVNProgressMonitor(this, monitor, null));
		}
		finally {
			SVNUtility.removeSVNNotifyListener(proxy, notify);
			location.releaseSVNProxy(proxy);
		}
	}
	
	protected String getShortErrorMessage(Throwable t) {
		return MessageFormat.format(super.getShortErrorMessage(t), new String[] {this.operableData()[0].getUrl()});
	}

}
