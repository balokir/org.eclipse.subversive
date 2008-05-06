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

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNNotificationCallback;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNNotification;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IRevisionProvider;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Base implementation for copy and move repository resources operations
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractCopyMoveResourcesOperation extends AbstractRepositoryOperation implements IRevisionProvider {
	protected IRepositoryResource destinationResource;
	protected String message;
	protected String resName;
	protected ArrayList<RevisionPair> revisionsPairs;
	
	public AbstractCopyMoveResourcesOperation(String operationName, IRepositoryResource destinationResource, IRepositoryResource []selectedResources, String message, String resName) {
		super(operationName, selectedResources);
		this.destinationResource = destinationResource;
		this.message = message;
		this.resName = resName;
	}
	
	public RevisionPair []getRevisions() {
		return this.revisionsPairs == null ? null : this.revisionsPairs.toArray(new RevisionPair[this.revisionsPairs.size()]);
	}
	
	protected void runImpl(final IProgressMonitor monitor) throws Exception {
		this.revisionsPairs = new ArrayList<RevisionPair>();
		final String dstUrl = this.destinationResource.getUrl();
		ArrayList<SVNEntryRevisionReference> refsList = new ArrayList<SVNEntryRevisionReference>();
		IRepositoryResource []selectedResources = this.operableData();
		final IRepositoryLocation location = selectedResources[0].getRepositoryLocation();
		final ISVNConnector proxy = location.acquireSVNProxy();
		for (int i = 0; i < selectedResources.length && !monitor.isCanceled(); i++) {
			final IRepositoryResource current = selectedResources[i];
			refsList.add(new SVNEntryRevisionReference(current.getUrl(), current.getPegRevision(), current.getSelectedRevision()));
		}
		final SVNEntryRevisionReference [] refs = refsList.toArray(new SVNEntryRevisionReference[0]); 
		ISVNNotificationCallback notify = new ISVNNotificationCallback() {
			
			protected int i = 0;
			
			public void notify(SVNNotification info) {
				
				if (this.i == refs.length) {
					return;
				}
				String []paths = AbstractCopyMoveResourcesOperation.this.getRevisionPaths(refs[this.i].path, dstUrl + "/" +
						((refs.length == 1) ? AbstractCopyMoveResourcesOperation.this.resName : refs[this.i].path.substring(refs[this.i].path.lastIndexOf("/") + 1)));
				AbstractCopyMoveResourcesOperation.this.revisionsPairs.add(new RevisionPair(info.revision, paths, location));
				String message = SVNTeamPlugin.instance().getResource("Console.CommittedRevision", new String[] {String.valueOf(info.revision)});
				AbstractCopyMoveResourcesOperation.this.writeToConsole(IConsoleStream.LEVEL_OK, message);
				this.i++;
			}
		};
		try {
			SVNUtility.addSVNNotifyListener(proxy, notify);	
			this.runCopyMove(proxy, refs, dstUrl + ((refsList.size() == 1) ? ("/" + this.resName) : ""), monitor);
			SVNUtility.removeSVNNotifyListener(proxy, notify);
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}

	protected abstract String []getRevisionPaths(String srcUrl, String dstUrl);
	protected abstract void runCopyMove(ISVNConnector proxy, SVNEntryRevisionReference [] source, String destinationUrl, IProgressMonitor monitor) throws Exception;
}
