/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Rene Link - NPE in Interactive Merge UI
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize.merge;

import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.client.NodeKind;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.client.StatusKind;
import org.eclipse.team.svn.core.operation.local.MergeStatusOperation;
import org.eclipse.team.svn.core.resource.IChangeStateProvider;
import org.eclipse.team.svn.core.resource.IFolderChange;
import org.eclipse.team.svn.core.resource.ILocalFolder;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.ui.synchronize.variant.RemoteFileVariant;
import org.eclipse.team.svn.ui.synchronize.variant.RemoteFolderVariant;
import org.eclipse.team.svn.ui.synchronize.variant.ResourceVariant;

/**
 * Merge sync info: ignores outgoing changes
 * 
 * @author Alexander Gurov
 */
public class MergeSyncInfo extends AbstractSVNSyncInfo {
	protected ILocalResource wcStatus;
	
	public MergeSyncInfo(ILocalResource local, ILocalResource wcStatus, ILocalResource remote, IResourceVariantComparator comparator) {
		super(local, 
				wcStatus == null || wcStatus.getStatus() == IStateFilter.ST_ADDED || wcStatus.getStatus() == IStateFilter.ST_NOTEXISTS ? null : MergeSyncInfo.makeBaseVariant(local),
				AbstractSVNSyncInfo.makeRemoteVariant(local, remote),
				comparator);
		this.wcStatus = wcStatus;
	}
	
	public ILocalResource getWCStatus() {
		return this.wcStatus;
	}
	
    protected int calculateKind() throws TeamException {
        String localKind = this.local == null ? IStateFilter.ST_NOTEXISTS : this.local.getStatus();
        int localMask = this.local == null ? 0 : this.local.getChangeMask();
        String wcKind = this.wcStatus == null ? IStateFilter.ST_NOTEXISTS : this.wcStatus.getStatus();
        int wcMask = this.wcStatus == null ? 0 : this.wcStatus.getChangeMask();
        String remoteKind = 
        	this.getRemote() == null ? 
        	(this.isNonVersioned(localKind, localMask) ? IStateFilter.ST_NOTEXISTS : IStateFilter.ST_NORMAL) : 
        	((ResourceVariant)this.getRemote()).getStatus();
        int remoteMask = this.getRemote() == null ? 0 : ((ResourceVariant)this.getRemote()).getResource().getChangeMask();
        	
        if (this.isLinked(localKind, localMask)) {
    		return SyncInfo.IN_SYNC;
        }
        
        if (this.isReplaced(remoteKind, remoteMask)) {
        	if (this.isNotExists(localKind, localMask) && (this.isNotExists(wcKind, wcMask) || this.isDeleted(wcKind, wcMask))) {
        		return SyncInfo.INCOMING | SyncInfo.ADDITION;
        	}
            if (this.isNotModified(wcKind, wcMask) && this.isNotModified(localKind, localMask)) {
            	return SyncInfo.INCOMING | SyncInfo.CHANGE;
            }
    		return SyncInfo.CONFLICTING | SyncInfo.CHANGE;
        }
    	if (this.isAdded(remoteKind, remoteMask)) {
    		if (this.isNotExists(localKind, localMask)) {
    			// merge performed only with concrete revision, this mean that wcKind also "not exists"
        		return SyncInfo.INCOMING | SyncInfo.ADDITION;
    		}
    		return SyncInfo.CONFLICTING | SyncInfo.ADDITION;
    	}
    	if (this.isModified(remoteKind, remoteMask)) {
    		if (this.isReplaced(localKind, localMask) || this.isReplaced(wcKind, wcMask)) {
    			return SyncInfo.CONFLICTING | SyncInfo.CHANGE;
    		}
            if (this.isAdded(localKind, localMask) || this.isNonVersioned(localKind, localMask) && !this.isNotExists(localKind, localMask)) {
        		return SyncInfo.CONFLICTING | SyncInfo.ADDITION;
            }
        	if (this.isDeleted(localKind, localMask)) {
        		return SyncInfo.CONFLICTING | SyncInfo.DELETION;
        	}
        	if (this.isDeleted(wcKind, wcMask) || this.isNotExists(localKind, localMask) && this.isNotExists(wcKind, wcMask)) {
        		return SyncInfo.INCOMING | SyncInfo.ADDITION;
        	}
            if (this.isNotModified(wcKind, wcMask) && this.isNotModified(localKind, localMask)) {
            	return SyncInfo.INCOMING | SyncInfo.CHANGE;
            }
    		return SyncInfo.CONFLICTING | SyncInfo.CHANGE;
    	}
    	if (this.isDeleted(remoteKind, remoteMask)) {
            if (this.isNotModified(wcKind, wcMask) && this.isNotModified(localKind, localMask)) {
        		return SyncInfo.INCOMING | SyncInfo.DELETION;
            }
            if (this.isModified(wcKind, wcMask) || this.isModified(localKind, localMask)) {
        		return SyncInfo.CONFLICTING | SyncInfo.DELETION;
            }
    		return SyncInfo.IN_SYNC;
    	}
        
    	//if (this.isNotModified(remoteKind)) {...
		return SyncInfo.IN_SYNC;
    }
    
    protected static IResourceVariant makeBaseVariant(final ILocalResource local) {
    	final MergeStatusOperation stOp = MergeSubscriber.instance().getMergeStatusOperation();
    	if (stOp == null || stOp.getCopiedFrom() == null || stOp.getCopiedFrom()[0] == null) {
    		return AbstractSVNSyncInfo.makeBaseVariant(local);
    	}
    	IResourceChange remote = SVNRemoteStorage.instance().asResourceChange(new IChangeStateProvider() {
			public long getChangeDate() {
				return -1;
			}
			public String getChangeAuthor() {
				return null;
			}
			public Revision.Number getChangeRevision() {
				//FIXME works incorrectly for multi-project merge
				return (Revision.Number)stOp.getCopiedFromRevision()[0];
			}
			public int getTextChangeType() {
				return StatusKind.none;
			}
			public int getPropertiesChangeType() {
				return StatusKind.none;
			}
			public int getNodeKind() {
				return local instanceof ILocalFolder ? NodeKind.dir : NodeKind.file;
			}
			public String getLocalPath() {
				return FileUtility.getWorkingCopyPath(local.getResource());
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
		});
    	//FIXME works incorrectly for multi-project merge
    	IRepositoryResource copiedFrom = stOp.getCopiedFrom()[0];
    	IRepositoryResource remoteResource = SVNRemoteStorage.instance().asRepositoryResource(local.getResource());
    	if (!new Path(copiedFrom.getUrl()).isPrefixOf(new Path(remoteResource.getUrl()))) {
    		String baseURL = copiedFrom.getUrl();
    		String tail = remote.getResource().getFullPath().removeFirstSegments(1).toString();
    		String originatorURL = baseURL + "/" + tail;
        	remote.setOriginator(remote instanceof IFolderChange ? (IRepositoryResource)copiedFrom.getRepositoryLocation().asRepositoryContainer(originatorURL, false) : copiedFrom.getRepositoryLocation().asRepositoryFile(originatorURL, false));
    	}
		return (remote instanceof ILocalFolder) ? (IResourceVariant)new RemoteFolderVariant(remote) : new RemoteFileVariant(remote);
	}
	
}
