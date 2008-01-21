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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNNotificationCallback;
import org.eclipse.team.svn.core.connector.SVNNotification;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IRevisionProvider;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Delete remote resources operation
 * 
 * @author Alexander Gurov
 */
public class DeleteResourcesOperation extends AbstractRepositoryOperation implements IRevisionProvider {
	protected String message;
	protected ArrayList revisionsPairs;
	
	public DeleteResourcesOperation(IRepositoryResource []resources, String message) {
		super("Operation.DeleteRemote", resources);
		this.message = message;
	}

	protected void runImpl(final IProgressMonitor monitor) throws Exception {
		this.revisionsPairs = new ArrayList();
		IRepositoryResource []resources = SVNUtility.shrinkChildNodes(this.operableData());
		
		Map repository2Resources = SVNUtility.splitRepositoryLocations(resources);
		
		for (Iterator it = repository2Resources.entrySet().iterator(); it.hasNext() && !monitor.isCanceled(); ) {
			Map.Entry entry = (Map.Entry)it.next();
			final IRepositoryLocation location = (IRepositoryLocation)entry.getKey();
			final String []paths = SVNUtility.asURLArray((IRepositoryResource [])((List)entry.getValue()).toArray(new IRepositoryResource[0]), true);
			
			this.complexWriteToConsole(new Runnable() {
				public void run() {
					DeleteResourcesOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn delete");
					for (int i = 0; i < paths.length && !monitor.isCanceled(); i++) {
						DeleteResourcesOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " \"" + SVNUtility.decodeURL(paths[i]) + "\"");
					}
					DeleteResourcesOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, " -m \"" + DeleteResourcesOperation.this.message + "\"" + FileUtility.getUsernameParam(location.getUsername()) + "\n");
				}
			});
			
			final ISVNConnector proxy = location.acquireSVNProxy();
			ISVNNotificationCallback notify = new ISVNNotificationCallback() {
				public void notify(SVNNotification info) {
					DeleteResourcesOperation.this.revisionsPairs.add(new RevisionPair(info.revision, paths, location));
					String message = SVNTeamPlugin.instance().getResource("Console.CommittedRevision", new String[] {String.valueOf(info.revision)});
					DeleteResourcesOperation.this.writeToConsole(IConsoleStream.LEVEL_OK, message);
				}
			};
			SVNUtility.addSVNNotifyListener(proxy, notify);
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					proxy.remove(paths, DeleteResourcesOperation.this.message, ISVNConnector.Options.FORCE, new SVNProgressMonitor(DeleteResourcesOperation.this, monitor, null));
				}
			}, monitor, repository2Resources.size());
			SVNUtility.removeSVNNotifyListener(proxy, notify);
			
			location.releaseSVNProxy(proxy);
		}
	}
	
	public RevisionPair[] getRevisions() {
	 	return this.revisionsPairs == null ? null : (RevisionPair[])this.revisionsPairs.toArray(new RevisionPair [this.revisionsPairs.size()]);
	}
	
}
