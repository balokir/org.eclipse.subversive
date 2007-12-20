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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.connector.SVNEntryStatus;
import org.eclipse.team.svn.core.connector.SVNMergeStatus;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNEntry;
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
    
    protected SyncInfo getSVNSyncInfo(ILocalResource localStatus, IResourceChange remoteStatus) {
    	// provide correct base resource: same as right but with start revision specified
        return new MergeSyncInfo(localStatus, remoteStatus, this.getResourceComparator());
    }

    protected IRemoteStatusOperation getStatusOperation(IResource[] resources, int depth) {
        return this.mergeStatusOp = (this.scope == null ? null : new MergeStatusOperation(this.scope.getMergeSet(), resources));
    }
    
    public void refresh(final IResource []resources, final int depth, IProgressMonitor monitor) {
		if (this.scope != null) {
			this.scope.getMergeSet().setStatuses(new SVNMergeStatus[0]);
		}
    	super.refresh(resources, depth, monitor);
    }
	
	protected IResourceChange handleResourceChange(IRemoteStatusOperation rStatusOp, Object status) {
		final SVNMergeStatus current = (SVNMergeStatus)status;
		IChangeStateProvider provider = new IChangeStateProvider() {
			public long getChangeDate() {
				return current.date;
			}
			public String getChangeAuthor() {
				return current.author;
			}
			public SVNRevision.Number getChangeRevision() {
				return current.revision == SVNRevision.INVALID_REVISION_NUMBER ? null : SVNRevision.fromNumber(current.revision);
			}
			public int getTextChangeType() {
				return current.textStatus;
			}
			public int getPropertiesChangeType() {
				return current.propStatus;
			}
			public int getNodeKind() {
				int kind = SVNUtility.getNodeKind(current.path, current.nodeKind, true);
				// if not exists on repository try to check it with WC kind...
				return kind == SVNEntry.Kind.NONE ? SVNUtility.getNodeKind(current.path, current.nodeKind, false) : kind;
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
		if (provider.getNodeKind() == SVNEntry.Kind.NONE) {
			return null;
		}
		IResourceChange resourceChange = SVNRemoteStorage.instance().asResourceChange(provider, false);
		if (resourceChange == null || resourceChange.getRevision() == SVNRevision.INVALID_REVISION_NUMBER) {
			return null;
		}
		IRepositoryResource originator = this.scope.getMergeSet().from[0];
		String decodedUrl = SVNUtility.decodeURL(current.url);
		originator = provider.getNodeKind() == SVNEntry.Kind.DIR ? (IRepositoryResource)originator.asRepositoryContainer(decodedUrl, false) : originator.asRepositoryFile(decodedUrl, false);
		originator.setSelectedRevision(SVNRevision.fromNumber(current.textStatus == SVNEntryStatus.Kind.DELETED ? current.revision - 1 : current.revision));
		resourceChange.setOriginator(originator);
		resourceChange.setCommentProvider(new ICommentProvider() {
			public String getComment(IResource resource, SVNRevision rev, SVNRevision peg) {
				return current.comment;
			}
		});
		return resourceChange;
	}
	
	protected boolean isIncomig(Object status) {
		return true;
	}
	
    private MergeSubscriber() {
        super();
    }

}
