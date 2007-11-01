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

package org.eclipse.team.svn.core.operation.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.svn.core.client.Depth;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.INotificationCallback;
import org.eclipse.team.svn.core.client.Notification;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.client.Status;
import org.eclipse.team.svn.core.client.IStatusCallback;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * This operation fetch remote resource statuses
 * 
 * @author Alexander Gurov
 */
public class RemoteStatusOperation extends AbstractWorkingCopyOperation implements IRemoteStatusOperation, INotificationCallback {
	protected Status []statuses;
	protected Map pegRevisions;

	public RemoteStatusOperation(IResource []resources) {
		super("Operation.UpdateStatus", resources);
		this.pegRevisions = new HashMap();
	}

	public RemoteStatusOperation(IResourceProvider provider) {
		super("Operation.UpdateStatus", provider);
		this.pegRevisions = new HashMap();
	}

	public IResource []getScope() {
		return this.operableData();
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []resources = FileUtility.shrinkChildNodes(this.operableData());

		final List result = new ArrayList();
		final IStatusCallback cb = new IStatusCallback() {
			public void nextStatus(Status status) {
				result.add(status);
			}
		};
		for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
			IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(resources[i]);
			final ISVNClientWrapper proxy = location.acquireSVNProxy();

			SVNUtility.addSVNNotifyListener(proxy, this);
			final IResource current = resources[i];
//			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn status -u \"" + FileUtility.normalizePath(current.getLocation().toString()) + "\\"" + FileUtility.getUsernameParam(location.getUsername()) + "\\n"
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					proxy.status(
							FileUtility.getWorkingCopyPath(current), 
							Depth.unknownOrImmediates(true), true, false, false, false, cb, 
							new SVNProgressMonitor(RemoteStatusOperation.this, monitor, null, false));
				}
			}, monitor, resources.length);
			SVNUtility.removeSVNNotifyListener(proxy, this);
			
			location.releaseSVNProxy(proxy);
		}
		this.statuses = (Status [])result.toArray(new Status[result.size()]);
	}

	public Status []getStatuses() {
		return this.statuses;
	}
	
	public void setPegRevision(IResourceChange change) {
	    IPath resourcePath = FileUtility.getResourcePath(change.getResource());
	    for (Iterator it = this.pegRevisions.entrySet().iterator(); it.hasNext(); ) {
	        Map.Entry entry = (Map.Entry)it.next();
	        IPath rootPath = new Path((String)entry.getKey());
	        if (rootPath.isPrefixOf(resourcePath)) {
	            change.setPegRevision((Revision)entry.getValue());
	            return;
	        }
	    }
	    IRemoteStorage storage = SVNRemoteStorage.instance();
	    if (change.getResource().getType() == IResource.PROJECT) {
		    IRepositoryResource remote = storage.asRepositoryResource(change.getResource());
		    change.setPegRevision(remote.getPegRevision());
	    }
	}

    public void notify(Notification info) {
    	if (info.revision != Revision.SVN_INVALID_REVNUM) {
            this.pegRevisions.put(info.path, Revision.fromNumber(info.revision));
    	}
    }
    
}
