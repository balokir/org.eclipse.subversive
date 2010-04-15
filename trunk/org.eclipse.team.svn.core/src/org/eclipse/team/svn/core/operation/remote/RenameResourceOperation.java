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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.BaseMessages;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNNotificationCallback;
import org.eclipse.team.svn.core.connector.SVNEntryReference;
import org.eclipse.team.svn.core.connector.SVNNotification;
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
		super("Operation_Rename", SVNMessages.class, new IRepositoryResource[] {resource}); //$NON-NLS-1$
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
		ISVNConnector proxy = location.acquireSVNProxy();
		final String newUrl = resource.getParent().getUrl() + "/" + this.newName; //$NON-NLS-1$
		ISVNNotificationCallback notify = new ISVNNotificationCallback() {
			public void notify(SVNNotification info) {
				String []path = new String[] {newUrl};
				RenameResourceOperation.this.revisionPair[0] = new RevisionPair(info.revision, path, location);
				String message = SVNMessages.format(SVNMessages.Console_CommittedRevision, new String[] {String.valueOf(info.revision)});
				RenameResourceOperation.this.writeToConsole(IConsoleStream.LEVEL_OK, message);
			}
		};
		try {
			SVNUtility.addSVNNotifyListener(proxy, notify);
			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn move \"" + resource.getUrl() + "\" \"" + newUrl + "\" -m \"" + this.message + "\"" + FileUtility.getUsernameParam(location.getUsername()) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			proxy.move(new SVNEntryReference[] {SVNUtility.getEntryReference(resource)}, SVNUtility.encodeURL(newUrl), this.message, ISVNConnector.Options.INTERPRET_AS_CHILD, null, new SVNProgressMonitor(this, monitor, null));
		}
		finally {
			SVNUtility.removeSVNNotifyListener(proxy, notify);
		    resource.getRepositoryLocation().releaseSVNProxy(proxy);
		}
	}

	protected String getShortErrorMessage(Throwable t) {
		return BaseMessages.format(super.getShortErrorMessage(t), new Object[] {this.operableData()[0].getName(), this.newName});
	}
	
}
