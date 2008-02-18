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

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNEntryStatusCallback;
import org.eclipse.team.svn.core.connector.ISVNNotificationCallback;
import org.eclipse.team.svn.core.connector.SVNChangeStatus;
import org.eclipse.team.svn.core.connector.SVNEntry;
import org.eclipse.team.svn.core.connector.SVNEntryStatus;
import org.eclipse.team.svn.core.connector.SVNNotification;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
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
public class RemoteStatusOperation extends AbstractWorkingCopyOperation implements IRemoteStatusOperation, ISVNNotificationCallback {
	protected SVNChangeStatus []statuses;
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
		
		final HashSet<Path> projectPaths = new HashSet<Path>();
		for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
			projectPaths.add(new Path(FileUtility.getWorkingCopyPath(resources[i].getProject())));
		}
		final HashMap result = new HashMap();
		final ISVNEntryStatusCallback cb = new ISVNEntryStatusCallback() {
			public void next(SVNChangeStatus status) {
				result.put(status.path, status);
				String parent = new File(status.path).getParent();
				if (parent != null) {// can be null for drive roots
					Path projectPath = this.getProjectPath(parent);
					if (projectPath != null) {
						this.postStatus(parent, status);
						this.postStatus(projectPath.toString(), status);
					}
				}
			}
			
			private void postStatus(String path, SVNChangeStatus baseStatus) {
				SVNChangeStatus st = (SVNChangeStatus)result.get(path);
				if (st == null || st.reposLastCmtRevision < baseStatus.reposLastCmtRevision) {
					SVNChangeStatus status = this.makeStatus(path, baseStatus);
					result.put(status.path, status);
				}
			}
			
			private SVNChangeStatus makeStatus(String path, SVNChangeStatus status) {
				int deltaSegments = new Path(status.path).segmentCount() - new Path(path).segmentCount();
				return new SVNChangeStatus(path, status.url != null ? new Path(status.url).removeLastSegments(deltaSegments).toString() : null, SVNEntry.Kind.DIR, SVNRevision.INVALID_REVISION_NUMBER, SVNRevision.INVALID_REVISION_NUMBER, 0, null, SVNEntryStatus.Kind.NORMAL, SVNEntryStatus.Kind.NONE, SVNEntryStatus.Kind.NORMAL, SVNEntryStatus.Kind.MODIFIED, false, false, null, null, null, null, SVNRevision.INVALID_REVISION_NUMBER, false, null, null, null, 0, null, status.reposLastCmtRevision, status.reposLastCmtDate, SVNEntry.Kind.DIR, status.reposLastCmtAuthor);
			}
			
			private Path getProjectPath(String path) {
				Path tPath = new Path(path);
				for (Path projectPath : projectPaths) {
					if (projectPath.isPrefixOf(tPath)) {
						return projectPath;
					}
				}
				return null;
			}
		};
		for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
			IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(resources[i]);
			final ISVNConnector proxy = location.acquireSVNProxy();

			SVNUtility.addSVNNotifyListener(proxy, this);
			final IResource current = resources[i];
//			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn status -u \"" + FileUtility.normalizePath(current.getLocation().toString()) + "\\"" + FileUtility.getUsernameParam(location.getUsername()) + "\\n"
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					proxy.status(
							FileUtility.getWorkingCopyPath(current), 
							Depth.infinityOrImmediates(true), ISVNConnector.Options.SERVER_SIDE, null, cb, 
							new SVNProgressMonitor(RemoteStatusOperation.this, monitor, null, false));
				}
			}, monitor, resources.length);
			SVNUtility.removeSVNNotifyListener(proxy, this);
			
			location.releaseSVNProxy(proxy);
		}
		this.statuses = (SVNChangeStatus [])result.values().toArray(new SVNChangeStatus[result.size()]);
	}

	public SVNEntryStatus[]getStatuses() {
		return this.statuses;
	}
	
	public void setPegRevision(IResourceChange change) {
	    IPath resourcePath = FileUtility.getResourcePath(change.getResource());
	    int prefixLength = 0;
	    SVNRevision revision = SVNRevision.INVALID_REVISION;
	    for (Iterator it = this.pegRevisions.entrySet().iterator(); it.hasNext(); ) {
	        Map.Entry entry = (Map.Entry)it.next();
	        IPath rootPath = new Path((String)entry.getKey());
	        int segments = rootPath.segmentCount();
	        if (rootPath.isPrefixOf(resourcePath) && segments > prefixLength) {
	        	prefixLength = segments;
	        	revision = (SVNRevision)entry.getValue();
	        }
	    }
	    if (revision != SVNRevision.INVALID_REVISION) {
	        change.setPegRevision(revision);
	    }
	    else if (change.getResource().getType() == IResource.PROJECT) {
		    IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(change.getResource());
		    change.setPegRevision(remote.getPegRevision());
	    }
	}

    public void notify(SVNNotification info) {
    	if (info.revision != SVNRevision.INVALID_REVISION_NUMBER) {
            this.pegRevisions.put(info.path, SVNRevision.fromNumber(info.revision));
    	}
    }
    
}
