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

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.client.SVNEntryStatus;
import org.eclipse.team.svn.core.client.SVNRevision;
import org.eclipse.team.svn.core.client.SVNEntry.NodeKind;
import org.eclipse.team.svn.core.client.SVNEntryStatus.Kind;
import org.eclipse.team.svn.core.operation.local.IRemoteStatusOperation;
import org.eclipse.team.svn.core.operation.local.MergeStatusOperation;
import org.eclipse.team.svn.core.resource.IChangeStateProvider;
import org.eclipse.team.svn.core.resource.ICommentProvider;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.synchronize.AbstractSVNSubscriber;

/**
 * Merge view data provider
 * 
 * @author Alexander Gurov
 */
public class MergeSubscriber extends AbstractSVNSubscriber {
	private static MergeSubscriber instance = null;
	
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
	
	public MergeScope getMergeScope() {
		return this.scope;
	}
	
    public void setMergeScope(MergeScope scope) {
        this.scope = scope;
    }
    
    protected SyncInfo getSVNSyncInfo(IRemoteStorage storage, ILocalResource localStatus, IResourceChange remoteStatus) {
        return new MergeSyncInfo(localStatus, remoteStatus, this.getResourceComparator());
    }

    protected IRemoteStatusOperation getStatusOperation(IResource[] resources, int depth) {
        return this.mergeStatusOp = (this.scope == null ? null : new MergeStatusOperation(this.scope.getMergeSet()));
    }
    
	protected IResourceChange handleResourceChange(IRemoteStorage storage, IRemoteStatusOperation rStatusOp, final SVNEntryStatus current) {
		IChangeStateProvider provider = new IChangeStateProvider() {
			public long getChangeDate() {
				return current.reposLastCmtDate;
			}
			public String getChangeAuthor() {
				return current.reposLastCmtAuthor;
			}
			public SVNRevision.Number getChangeRevision() {
				return current.reposLastCmtRevision == SVNRevision.INVALID_REVISION_NUMBER ? null : SVNRevision.fromNumber(current.reposLastCmtRevision);
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
				return kind == NodeKind.NONE ? SVNUtility.getNodeKind(current.path, current.nodeKind, false) : kind;
			}
			public String getLocalPath() {
				return current.path;
			}
			public String getComment() {
				return null;
			}
			public boolean isCopied() {
				return false;
			}
			public boolean isSwitched() {
				return false;
			}
			public IResource getExact(IResource []set) {
				return FileUtility.selectOneOf(MergeSubscriber.this.scope.getRoots(), set);
			}
		};
		if (provider.getNodeKind() == NodeKind.NONE) {
			return null;
		}
		IResourceChange resourceChange = storage.asResourceChange(provider);
		if (resourceChange == null || resourceChange.getRevision() == SVNRevision.INVALID_REVISION_NUMBER) {
			return null;
		}
		IRepositoryResource originator = this.scope.getMergeSet().from[0];
		String decodedURL = SVNUtility.decodeURL(current.url);
		originator = provider.getNodeKind() == NodeKind.DIR ? (IRepositoryResource)originator.asRepositoryContainer(decodedURL, false) : originator.asRepositoryFile(decodedURL, false);
		originator.setSelectedRevision(SVNRevision.fromNumber(current.repositoryTextStatus == Kind.DELETED ? current.reposLastCmtRevision - 1 : current.reposLastCmtRevision));
		resourceChange.setOriginator(originator);
		resourceChange.setCommentProvider(new ICommentProvider() {
			public String getComment(IResource resource, SVNRevision rev, SVNRevision peg) {
				return current.lockComment;
			}
		});
		return resourceChange;
	}
	
    private MergeSubscriber() {
        super();
    }

}
