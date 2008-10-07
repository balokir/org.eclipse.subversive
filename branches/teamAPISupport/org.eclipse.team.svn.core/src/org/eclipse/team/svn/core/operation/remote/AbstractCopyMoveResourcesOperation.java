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
		IRepositoryResource []selectedResources = this.operableData();
		final SVNEntryRevisionReference []refs = new SVNEntryRevisionReference[selectedResources.length]; 
		for (int i = 0; i < selectedResources.length; i++) {
			refs[i] = SVNUtility.getEntryRevisionReference(selectedResources[i]);
		}
		final IRepositoryLocation location = selectedResources[0].getRepositoryLocation();
		final String dstUrl = this.destinationResource.getUrl() + (this.resName != null && this.resName.length() > 0 ? "/" + this.resName : (selectedResources.length > 1 ? "" : "/" + selectedResources[0].getName()));
		ISVNNotificationCallback notify = new ISVNNotificationCallback() {
			private int i = 0;
			public void notify(SVNNotification info) {
				if (this.i == refs.length) {
					return;
				}
				String []paths = AbstractCopyMoveResourcesOperation.this.getRevisionPaths(refs[this.i].path, dstUrl);
				AbstractCopyMoveResourcesOperation.this.revisionsPairs.add(new RevisionPair(info.revision, paths, location));
				String message = SVNTeamPlugin.instance().getResource("Console.CommittedRevision", new String[] {String.valueOf(info.revision)});
				AbstractCopyMoveResourcesOperation.this.writeToConsole(IConsoleStream.LEVEL_OK, message);
				this.i++;
			}
		};
		ISVNConnector proxy = location.acquireSVNProxy();
		//NOTE NPE in SVN Kit if parents exists and MAKE_PARENTS is specified
		//NOTE JavaHL is crashed when empty folder is copied independently from MAKE_PARENTS option
		SVNUtility.addSVNNotifyListener(proxy, notify);	
		try {
			this.runCopyMove(proxy, refs, SVNUtility.encodeURL(dstUrl), monitor);
		}
		finally {
			SVNUtility.removeSVNNotifyListener(proxy, notify);
			location.releaseSVNProxy(proxy);
		}
	}

	protected abstract String []getRevisionPaths(String srcUrl, String dstUrl);
	protected abstract void runCopyMove(ISVNConnector proxy, SVNEntryRevisionReference [] source, String destinationUrl, IProgressMonitor monitor) throws Exception;
}
