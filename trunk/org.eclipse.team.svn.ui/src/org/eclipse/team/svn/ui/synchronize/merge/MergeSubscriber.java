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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.connector.SVNEntry;
import org.eclipse.team.svn.core.connector.SVNEntryStatus;
import org.eclipse.team.svn.core.connector.SVNMergeStatus;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.local.IRemoteStatusOperation;
import org.eclipse.team.svn.core.operation.local.MergeStatusOperation;
import org.eclipse.team.svn.core.resource.IChangeStateProvider;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
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
	
	protected MergeScope scope;
	protected MergeStatusOperation mergeStatusOp;
    protected RemoteStatusCache baseStatusCache;
	
	public static synchronized MergeSubscriber instance() {
		if (MergeSubscriber.instance == null) {
		    MergeSubscriber.instance = new MergeSubscriber();
		}
		return MergeSubscriber.instance;
	}

	public MergeScope getMergeScope() {
		return this.scope;
	}
	
    public void setMergeScope(MergeScope scope) {
        this.scope = scope;
    }
    
    protected SyncInfo getSVNSyncInfo(ILocalResource localStatus, IResourceChange remoteStatus) {
		IResourceChange baseStatus = SVNRemoteStorage.instance().resourceChangeFromBytes(this.baseStatusCache.getBytes(localStatus.getResource()));
    	// provide correct base resource: same as right but with start revision specified
        return new MergeSyncInfo(localStatus, baseStatus, remoteStatus, this.getResourceComparator());
    }

    protected IRemoteStatusOperation getStatusOperation(IResource[] resources, int depth) {
        return this.mergeStatusOp = (this.scope == null ? null : new MergeStatusOperation(this.scope.getMergeSet(), resources));
    }
    
	protected HashSet<IResource> clearRemoteStatusesImpl(IResource []resources) {
		this.clearRemoteStatusesImpl(this.baseStatusCache, resources);
		return super.clearRemoteStatusesImpl(resources);
	}
	
    public void refresh(final IResource []resources, final int depth, IProgressMonitor monitor) {
		if (this.scope != null) {
			this.baseStatusCache.clearAll();
			this.scope.getMergeSet().setStatuses(new SVNMergeStatus[0]);
		}
    	super.refresh(resources, depth, monitor);
    }
	
	protected IResourceChange handleResourceChange(IRemoteStatusOperation rStatusOp, SVNEntryStatus status) {
		final SVNMergeStatus current = (SVNMergeStatus)status;
		IChangeStateProvider endProvider = new IChangeStateProvider() {
			public long getChangeDate() {
				return current.date;
			}
			public String getChangeAuthor() {
				return current.author;
			}
			public SVNRevision.Number getChangeRevision() {
				return current.endRevision == SVNRevision.INVALID_REVISION_NUMBER ? null : SVNRevision.fromNumber(current.endRevision);
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
				return current.comment;
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
		if (endProvider.getNodeKind() == SVNEntry.Kind.NONE) {
			return null;
		}
		IResourceChange endResourceChange = SVNRemoteStorage.instance().asResourceChange(endProvider, false);
		if (endResourceChange == null) {
			return null;
		}
		if (endResourceChange.getRevision() != SVNRevision.INVALID_REVISION_NUMBER) {
			IRepositoryResource originator = this.scope.getMergeSet().fromEnd[0];
			String decodedUrl = SVNUtility.decodeURL(current.endUrl);
			originator = endProvider.getNodeKind() == SVNEntry.Kind.DIR ? (IRepositoryResource)originator.asRepositoryContainer(decodedUrl, false) : originator.asRepositoryFile(decodedUrl, false);
			originator.setSelectedRevision(SVNRevision.fromNumber(current.textStatus == SVNEntryStatus.Kind.DELETED ? current.endRevision - 1 : current.endRevision));
			endResourceChange.setOriginator(originator);
		}
		
		IChangeStateProvider startProvider = new IChangeStateProvider() {
			public long getChangeDate() {
				return current.date;
			}
			public String getChangeAuthor() {
				return null;
			}
			public SVNRevision.Number getChangeRevision() {
				return current.startRevision == SVNRevision.INVALID_REVISION_NUMBER ? null : SVNRevision.fromNumber(current.startRevision);
			}
			public int getTextChangeType() {
				return current.startRevision == SVNRevision.INVALID_REVISION_NUMBER ? SVNEntryStatus.Kind.NONE : SVNEntryStatus.Kind.NORMAL;
			}
			public int getPropertiesChangeType() {
				return SVNEntryStatus.Kind.NONE;
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
		IResourceChange startResourceChange = SVNRemoteStorage.instance().asResourceChange(startProvider, false);
		if (!current.endUrl.equals(current.startUrl)) {
			if (startResourceChange.getRevision() != SVNRevision.INVALID_REVISION_NUMBER) {
				String decodedUrl = SVNUtility.decodeURL(current.startUrl);
				IRepositoryResource originator = this.scope.getMergeSet().fromStart[0];
				originator = startProvider.getNodeKind() == SVNEntry.Kind.DIR ? (IRepositoryResource)originator.asRepositoryContainer(decodedUrl, false) : originator.asRepositoryFile(decodedUrl, false);
				originator.setSelectedRevision(current.startRevision == SVNRevision.INVALID_REVISION_NUMBER ? SVNRevision.INVALID_REVISION : SVNRevision.fromNumber(current.startRevision));
				startResourceChange.setOriginator(originator);
			}
		}
		else {
			IRepositoryResource base = SVNRemoteStorage.instance().asRepositoryResource(endResourceChange.getResource());
			base.setSelectedRevision(this.scope.getMergeSet().fromStart[0].getSelectedRevision());
			startResourceChange.setOriginator(base);
		}
		this.baseStatusCache.setBytes(startResourceChange.getResource(), SVNRemoteStorage.instance().resourceChangeAsBytes(startResourceChange));
		
		return endResourceChange;
	}
	
	protected boolean isIncoming(SVNEntryStatus status) {
		return true;
	}
	
    private MergeSubscriber() {
        super();
		this.baseStatusCache = new RemoteStatusCache();
    }

}
