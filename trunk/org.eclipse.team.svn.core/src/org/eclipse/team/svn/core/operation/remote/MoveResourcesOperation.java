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
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNNotificationCallback;
import org.eclipse.team.svn.core.connector.SVNNotification;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Move remote resources
 * 
 * @author Alexander Gurov
 */
public class MoveResourcesOperation extends AbstractCopyMoveResourcesOperation {
	public MoveResourcesOperation(IRepositoryResource destinationResource, IRepositoryResource[] selectedResources, String message) {
		super("Operation.MoveRemote", destinationResource, selectedResources, message);
	}

	protected void runImpl(final IProgressMonitor monitor) throws Exception {
		this.revisionsPairs = new ArrayList();
		final String dstUrl = this.destinationResource.getUrl();
		IRepositoryResource []selectedResources = this.operableData();
		final IRepositoryLocation location = selectedResources[0].getRepositoryLocation();
		final ISVNConnector proxy = location.acquireSVNProxy();
		try {
			for (int i = 0; i < selectedResources.length && !monitor.isCanceled(); i++) {
				final IRepositoryResource current = selectedResources[i];
				ISVNNotificationCallback notify = new ISVNNotificationCallback() {
					public void notify(SVNNotification info) {
						String []paths = new String [] {current.getUrl(), dstUrl + "/" + current.getName()};
						MoveResourcesOperation.this.revisionsPairs.add(new RevisionPair(info.revision, paths, location));
						String message = SVNTeamPlugin.instance().getResource("Console.CommittedRevision");
						MoveResourcesOperation.this.writeToConsole(IConsoleStream.LEVEL_OK, MessageFormat.format(message, new String[] {String.valueOf(info.revision)}));
					}
				};
				SVNUtility.addSVNNotifyListener(proxy, notify);
				
				this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws Exception {
						MoveResourcesOperation.this.processEntry(proxy, SVNUtility.encodeURL(current.getUrl()), SVNUtility.encodeURL(dstUrl + "/" + current.getName()), current, monitor);
					}
				}, monitor, selectedResources.length);
				
				SVNUtility.removeSVNNotifyListener(proxy, notify);
			}
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}

	protected String[] getRevisionPaths(String srcUrl, String dstUrl) {
		return new String [] {srcUrl, dstUrl};
	}

	protected void processEntry(ISVNConnector proxy, String sourceUrl, String destinationUrl, IRepositoryResource current, IProgressMonitor monitor) throws Exception {
		this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn move \"" + SVNUtility.decodeURL(sourceUrl) + "\" \"" + SVNUtility.decodeURL(destinationUrl) + "\" -m \"" + this.message + "\"" + FileUtility.getUsernameParam(current.getRepositoryLocation().getUsername()) + "\n");
		proxy.move(new String[] {sourceUrl}, destinationUrl, this.message, ISVNConnector.Options.INTERPRET_AS_CHILD, new SVNProgressMonitor(this, monitor, null));
	}

}
