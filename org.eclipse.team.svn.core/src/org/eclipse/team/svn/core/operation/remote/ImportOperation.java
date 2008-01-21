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
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNNotificationCallback;
import org.eclipse.team.svn.core.connector.SVNNotification;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
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
		ISVNConnector proxy = location.acquireSVNProxy();
		ISVNNotificationCallback notify = new ISVNNotificationCallback() {
			public void notify(SVNNotification info) {
				if (info.revision != SVNRevision.INVALID_REVISION_NUMBER) {
					String []path = new String[] {resource.getUrl()};
					ImportOperation.this.revisionPair[0] = new RevisionPair(info.revision, path, location);
					String message = SVNTeamPlugin.instance().getResource("Console.CommittedRevision", new String[] {String.valueOf(info.revision)});
					ImportOperation.this.writeToConsole(IConsoleStream.LEVEL_OK, message);
				}
			}
		};
		try {
			SVNUtility.addSVNNotifyListener(proxy, notify);
			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn import \"" + FileUtility.normalizePath(this.path) + "\" \"" + resource.getUrl() + "\"" + (this.isRecursive ? "" : " -N") + " -m \"" + this.message + "\"" + FileUtility.getUsernameParam(location.getUsername()) + "\n");
			proxy.doImport(this.path, SVNUtility.encodeURL(resource.getUrl()), this.message, Depth.infinityOrFiles(this.isRecursive), ISVNConnector.Options.INCLUDE_IGNORED | ISVNConnector.Options.IGNORE_UNKNOWN_NODE_TYPES, new SVNProgressMonitor(this, monitor, null));
		}
		finally {
			SVNUtility.removeSVNNotifyListener(proxy, notify);
			location.releaseSVNProxy(proxy);
		}
	}
	
	protected String getShortErrorMessage(Throwable t) {
		return MessageFormat.format(super.getShortErrorMessage(t), new Object[] {this.operableData()[0].getUrl()});
	}

}
