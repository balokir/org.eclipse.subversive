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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.BaseMessages;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNNotificationCallback;
import org.eclipse.team.svn.core.connector.SVNNotification;
import org.eclipse.team.svn.core.connector.SVNRevision;
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
 * @author Sergiy Logvin
 */
public class ImportOperation extends AbstractRepositoryOperation implements IRevisionProvider {
	protected String path;
	protected String message;
	protected int depth;
	protected RevisionPair []revisionPair;
	
	public ImportOperation(IRepositoryResource resource, String path, String message, int depth) {
		super("Operation_Import", SVNMessages.class, new IRepositoryResource[] {resource}); //$NON-NLS-1$
		this.path = path;
		this.message = message;
		this.depth = depth;
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
					String message = SVNMessages.format(SVNMessages.Console_CommittedRevision, new String[] {String.valueOf(info.revision)});
					ImportOperation.this.writeToConsole(IConsoleStream.LEVEL_OK, message);
				}
			}
		};
		try {
			SVNUtility.addSVNNotifyListener(proxy, notify);
			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn import \"" + FileUtility.normalizePath(this.path) + "\" \"" + SVNUtility.getDepthArg(this.depth) + " -m \"" + this.message + "\"" + FileUtility.getUsernameParam(location.getUsername()) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			proxy.doImport(this.path, SVNUtility.encodeURL(resource.getUrl()), this.message, this.depth, ISVNConnector.Options.INCLUDE_IGNORED | ISVNConnector.Options.IGNORE_UNKNOWN_NODE_TYPES, null, new SVNProgressMonitor(this, monitor, null));
		}
		finally {
			SVNUtility.removeSVNNotifyListener(proxy, notify);
			location.releaseSVNProxy(proxy);
		}
	}
	
	protected String getShortErrorMessage(Throwable t) {
		return BaseMessages.format(super.getShortErrorMessage(t), new Object[] {this.operableData()[0].getUrl()});
	}

}
