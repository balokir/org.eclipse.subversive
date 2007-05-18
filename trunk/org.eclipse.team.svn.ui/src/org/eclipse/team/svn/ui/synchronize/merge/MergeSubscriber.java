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

package org.eclipse.team.svn.ui.synchronize.merge;

import java.util.HashSet;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.client.NodeKind;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.client.Status;
import org.eclipse.team.svn.core.operation.local.IRemoteStatusOperation;
import org.eclipse.team.svn.core.operation.local.MergeStatusOperation;
import org.eclipse.team.svn.core.resource.IChangeStateProvider;
import org.eclipse.team.svn.core.resource.IFolderChange;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.synchronize.AbstractSVNSubscriber;
import org.eclipse.team.svn.ui.synchronize.RemoteStatusCache;

/**
 * Merge view data provider
 * 
 * @author Alexander Gurov
 */
public class MergeSubscriber extends AbstractSVNSubscriber {
	private static MergeSubscriber instance = null;
	
    protected RemoteStatusCache wcChangesCache;
	protected MergeScope scope;
	protected MergeStatusOperation mergeStatusOp;
	
	public static synchronized MergeSubscriber instance() {
		if (MergeSubscriber.instance == null) {
		    MergeSubscriber.instance = new MergeSubscriber();
		}
		return MergeSubscriber.instance;
	}

	public IRepositoryResource []getRemoteOriginators(IResource []resources) {
		IRemoteStorage storage = SVNRemoteStorage.instance();
		IRepositoryResource []retVal = new IRepositoryResource[resources.length];
		for (int i = 0; i < resources.length; i++) {
			IResourceChange remoteStatus = storage.resourceChangeFromBytes(this.statusCache.getBytes(resources[i]));
			retVal[i] = remoteStatus.getOriginator();
		}
		return retVal;
	}
	
	public MergeStatusOperation getMergeStatusOperation() {
		return this.mergeStatusOp;
	}
	
    public void setMergeScope(MergeScope scope) {
        this.scope = scope;
    }
    
    protected SyncInfo getSVNSyncInfo(IRemoteStorage storage, ILocalResource localStatus, IResourceChange remoteStatus) {
		ILocalResource wcStatus = storage.resourceChangeFromBytes(this.wcChangesCache.getBytes(localStatus.getResource()));
        return new MergeSyncInfo(localStatus, wcStatus, remoteStatus, this.getResourceComparator());
    }

    protected IRemoteStatusOperation getStatusOperation(IResource[] resources, int depth) {
        return this.mergeStatusOp = (this.scope == null ? null : new MergeStatusOperation(this.scope.getRoots(), this.scope.getRepositoryResources(), this.scope.getStartRevision()));
    }
    
	protected HashSet clearRemoteStatusesImpl(IResource []resources) {
		HashSet refreshSet = super.clearRemoteStatusesImpl(resources);
		refreshSet.addAll(this.clearRemoteStatusesImpl(this.wcChangesCache, resources));
		return refreshSet;
	}
	
	protected IResourceChange handleResourceChange(IRemoteStorage storage, IRemoteStatusOperation rStatusOp, final Status current) {
		IResourceChange remoteChange = storage.asResourceChange(new IChangeStateProvider() {
			public long getChangeDate() {
				return current.reposLastCmtDate;
			}
			public String getChangeAuthor() {
				return current.reposLastCmtAuthor;
			}
			public Revision.Number getChangeRevision() {
				return current.reposLastCmtRevision == Revision.SVN_INVALID_REVNUM ? null : (Revision.Number)Revision.getInstance(current.reposLastCmtRevision);
			}
			public int getTextChangeType() {
				return current.repositoryTextStatus;
			}
			public int getPropertiesChangeType() {
				return current.repositoryPropStatus;
			}
			public int getNodeKind() {
				int kind = SVNUtility.getNodeKind(current.path, current.reposKind, true);
				// if not exists on repository try to check it with WC kind...
				return kind == NodeKind.none ? SVNUtility.getNodeKind(current.path, current.nodeKind, false) : kind;
			}
			public String getLocalPath() {
				return current.path;
			}
			public String getComment() {
				return null;
			}
			public boolean isCopied() {
				return current.isCopied;
			}
			public boolean isSwitched() {
				return current.isSwitched;
			}
		});
		rStatusOp.setPegRevision(remoteChange);
		//FIXME works incorrectly for multi-project merge
		if (this.scope.getRoots()[0] instanceof IContainer) {
			String baseURL = this.scope.getRepositoryResources()[0].getUrl();
			String tail = remoteChange.getResource().getFullPath().removeFirstSegments(1).toString();
			String originatorURL = baseURL + "/" + tail;
			IRepositoryRoot root = (IRepositoryRoot)this.scope.getRepositoryResources()[0].getRoot();
			IRepositoryResource originator = remoteChange instanceof IFolderChange ? (IRepositoryResource)root.asRepositoryContainer(originatorURL, false) : root.asRepositoryFile(originatorURL, false);
			remoteChange.setOriginator(originator);
		}
		else {
			remoteChange.setOriginator(this.scope.getRepositoryResources()[0]);
		}
		
		IResourceChange wcChange = storage.asResourceChange(new IChangeStateProvider() {
			public long getChangeDate() {
				return current.lastChangedDate;
			}
			public String getChangeAuthor() {
				return current.lastCommitAuthor;
			}
			public Revision.Number getChangeRevision() {
				return current.lastChangedRevision == Revision.SVN_INVALID_REVNUM ? null : (Revision.Number)Revision.getInstance(current.lastChangedRevision);
			}
			public int getTextChangeType() {
				return current.textStatus;
			}
			public int getPropertiesChangeType() {
				return current.propStatus;
			}
			public int getNodeKind() {
				int kind = SVNUtility.getNodeKind(current.path, current.nodeKind, true);
				// if not exists on WC try to check it with repository kind...
				return kind == NodeKind.none ? SVNUtility.getNodeKind(current.path, current.reposKind, false) : kind;
			}
			public String getLocalPath() {
				return current.path;
			}
			public String getComment() {
				return null;
			}
			public boolean isCopied() {
				return current.isCopied;
			}
			public boolean isSwitched() {
				return current.isSwitched;
			}
		});
		rStatusOp.setPegRevision(wcChange);
		
		this.wcChangesCache.setBytes(wcChange.getResource(), storage.resourceChangeAsBytes(wcChange));
		
		return remoteChange;
	}
	
    private MergeSubscriber() {
        super();
		this.wcChangesCache = new RemoteStatusCache();
    }

}
