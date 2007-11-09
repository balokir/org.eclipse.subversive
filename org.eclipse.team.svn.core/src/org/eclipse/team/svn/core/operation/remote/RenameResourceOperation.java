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

package org.eclipse.team.svn.core.operation.remote;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.client.ISVNClient;
import org.eclipse.team.svn.core.client.ISVNNotificationCallback;
import org.eclipse.team.svn.core.client.SVNNotification;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IRevisionProvider;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Rename remote resource operation implementation
 * 
 * @author Alexander Gurov
 */
public class RenameResourceOperation extends AbstractRepositoryOperation implements IRevisionProvider {
	protected String message;
	protected String newName;
	protected RevisionPair []revisionPair;
	
	public RenameResourceOperation(IRepositoryResource resource, String newName, String message) {
		super("Operation.Rename", new IRepositoryResource[] {resource});
		this.message = message;
		this.newName = newName;
	}

	public RevisionPair []getRevisions() {
		return this.revisionPair;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRepositoryResource resource = this.operableData()[0];
		this.revisionPair = new RevisionPair[1];
		final IRepositoryLocation location = resource.getRepositoryLocation();
		ISVNClient proxy = location.acquireSVNProxy();
		final String newUrl = resource.getParent().getUrl() + "/" + this.newName;
		ISVNNotificationCallback notify = new ISVNNotificationCallback() {
			public void notify(SVNNotification info) {
				String []path = new String[] {newUrl};
				RenameResourceOperation.this.revisionPair[0] = new RevisionPair(info.revision, path, location);
				String message = SVNTeamPlugin.instance().getResource("Console.CommittedRevision");
				RenameResourceOperation.this.writeToConsole(IConsoleStream.LEVEL_OK, MessageFormat.format(message, new String[] {String.valueOf(info.revision)}));
			}
		};
		try {
			SVNUtility.addSVNNotifyListener(proxy, notify);
			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn move \"" + resource.getUrl() + "\" \"" + newUrl + "\" -m \"" + this.message + "\"" + FileUtility.getUsernameParam(location.getUsername()) + "\n");
			proxy.move(new String[] {SVNUtility.encodeURL(resource.getUrl())}, SVNUtility.encodeURL(newUrl), this.message, true, true, false, true, new SVNProgressMonitor(this, monitor, null));
		}
		finally {
			SVNUtility.removeSVNNotifyListener(proxy, notify);
		    resource.getRepositoryLocation().releaseSVNProxy(proxy);
		}
	}

	protected String getShortErrorMessage(Throwable t) {
		return MessageFormat.format(super.getShortErrorMessage(t), new String[] {this.operableData()[0].getName(), this.newName});
	}
	
}
