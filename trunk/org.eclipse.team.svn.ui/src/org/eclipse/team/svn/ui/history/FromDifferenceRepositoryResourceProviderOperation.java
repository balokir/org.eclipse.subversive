/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.ui.history;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.connector.SVNDiffStatus;
import org.eclipse.team.svn.core.connector.SVNEntry;
import org.eclipse.team.svn.core.connector.SVNEntry.Kind;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNEntryStatus;
import org.eclipse.team.svn.core.connector.SVNRevisionRange;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.remote.AbstractRepositoryOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.resource.IRepositoryResourceWithStatusProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNUIMessages;

/** 
 * @author Igor Burilo
 */
public class FromDifferenceRepositoryResourceProviderOperation extends AbstractRepositoryOperation implements IRepositoryResourceWithStatusProvider {
	protected IRepositoryResource [] repositoryResources;
	protected IRepositoryResource [] repositoryResourcesToDelete;
	protected IRepositoryResource newer;
	protected IRepositoryResource older;
	protected IRepositoryLocation location;
	protected HashMap<String, String> url2status;
	
	public FromDifferenceRepositoryResourceProviderOperation(IRepositoryResource first, IRepositoryResource second) {
		this(new IRepositoryResourceProvider.DefaultRepositoryResourceProvider(new IRepositoryResource[] {first, second}));
	}
	
	public FromDifferenceRepositoryResourceProviderOperation(IRepositoryResourceProvider provider) {//(HashMap<SVNLogPath, Long> paths, SVNLogEntry selectedLogEntry) {
		super("Operation_GetRepositoryResource", SVNUIMessages.class, provider); //$NON-NLS-1$				
		this.url2status = new HashMap<String, String>();
	}
	
	public IRepositoryResourceProvider getDeletionsProvider() {
		return new IRepositoryResourceProvider() {
			public IRepositoryResource[] getRepositoryResources() {
				return FromDifferenceRepositoryResourceProviderOperation.this.repositoryResourcesToDelete;
			}
		};
	}
	
	protected IRepositoryResource createResourceFor(Kind kind, String url) {
		IRepositoryResource retVal = null;
		if (kind == SVNEntry.Kind.FILE) {
			retVal = this.location.asRepositoryFile(url, false);
		}
		else if (kind == SVNEntry.Kind.DIR) {
			retVal = this.location.asRepositoryContainer(url, false);
		}
		if (retVal == null) {
			throw new RuntimeException(SVNUIMessages.Error_CompareUnknownNodeKind);
		}
		return retVal;
	}
	
	protected IRepositoryResource getResourceForStatus(SVNDiffStatus status) {
		String url = SVNUtility.decodeURL(status.pathNext);
		return this.createResourceFor(SVNUtility.getNodeKind(status.pathPrev, status.nodeKind, false), url);
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRepositoryResource[] operable = this.operableData();
		this.newer = operable[0];
		this.older = operable[1];
		this.location = this.newer.getRepositoryLocation();
		
		HashSet<IRepositoryResource> resourcesToReturn = new HashSet<IRepositoryResource>();
		HashSet<IRepositoryResource> resourcesToDelete = new HashSet<IRepositoryResource>();
		ArrayList<SVNDiffStatus> statusesList = new ArrayList<SVNDiffStatus>();
		ISVNConnector proxy = this.location.acquireSVNProxy();
		SVNEntryRevisionReference refPrev = SVNUtility.getEntryRevisionReference(this.older);
		SVNEntryRevisionReference refNext = SVNUtility.getEntryRevisionReference(this.newer);
		ProgressMonitorUtility.setTaskInfo(monitor, this, SVNMessages.Progress_Running);
		try {
			if (SVNUtility.useSingleReferenceSignature(refPrev, refNext)) {
				SVNUtility.diffStatus(proxy, statusesList, refPrev, new SVNRevisionRange(refPrev.revision, refNext.revision), SVNDepth.INFINITY, ISVNConnector.Options.NONE, new SVNProgressMonitor(this, monitor, null, false));
			}
			else {
				SVNUtility.diffStatus(proxy, statusesList, refPrev, refNext, SVNDepth.INFINITY, ISVNConnector.Options.NONE, new SVNProgressMonitor(this, monitor, null, false));
			}
		}
		finally {
			this.location.releaseSVNProxy(proxy);
		}
		
		for (SVNDiffStatus status : statusesList) {
			IRepositoryResource resourceToAdd = this.getResourceForStatus(status);
			resourceToAdd.setSelectedRevision(this.newer.getSelectedRevision());
			resourceToAdd.setPegRevision(this.newer.getPegRevision());
			resourcesToReturn.add(resourceToAdd);
			String strStatus = SVNRemoteStorage.getCompoundStatusString(status.propStatus, status.textStatus, true);
			this.url2status.put(resourceToAdd.getUrl(), strStatus);
			if (status.textStatus == SVNEntryStatus.Kind.DELETED) {
				resourcesToDelete.add(resourceToAdd);
			}
		}
		this.repositoryResources = resourcesToReturn.toArray(new IRepositoryResource[0]);
		this.repositoryResourcesToDelete = resourcesToDelete.toArray(new IRepositoryResource[0]);
	}
	
	public IRepositoryResource[] getRepositoryResources() {
		return this.repositoryResources;
	}

	public Map<String, String> getStatusesMap() {
		return this.url2status;
	}
	
}
