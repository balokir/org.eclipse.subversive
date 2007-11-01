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

package org.eclipse.team.svn.ui.synchronize.update;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.client.NodeKind;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.client.Status;
import org.eclipse.team.svn.core.client.Status.Kind;
import org.eclipse.team.svn.core.operation.local.IRemoteStatusOperation;
import org.eclipse.team.svn.core.operation.local.RemoteStatusOperation;
import org.eclipse.team.svn.core.resource.IChangeStateProvider;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.svnstorage.CommentProvider;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.synchronize.AbstractSVNSubscriber;
import org.eclipse.team.svn.ui.utility.IOperationWrapperFactory;

/**
 * Synchronize view data provider
 * 
 * @author Alexander Gurov
 */
public class UpdateSubscriber extends AbstractSVNSubscriber {
	private static UpdateSubscriber instance = null;
	
	protected Map comments;
	
	public static synchronized UpdateSubscriber instance() {
		if (UpdateSubscriber.instance == null) {
			UpdateSubscriber.instance = new UpdateSubscriber();
		}
		return UpdateSubscriber.instance;
	}

    protected IRemoteStatusOperation getStatusOperation(IResource[] resources, int depth) {
        return new RemoteStatusOperation(resources);
    }

    protected SyncInfo getSVNSyncInfo(IRemoteStorage storage, ILocalResource localStatus, IResourceChange remoteStatus) {
        return new UpdateSyncInfo(localStatus, remoteStatus, this.getResourceComparator());
    }

	protected IResourceChange handleResourceChange(IRemoteStorage storage, IRemoteStatusOperation rStatusOp, final Status current) {
		if (current.textStatus == Kind.EXTERNAL) {
			return null;
		}
		final IResource []scope = rStatusOp.getScope();
		IChangeStateProvider provider = new IChangeStateProvider() {
			public long getChangeDate() {
				return current.reposLastCmtRevision == Revision.SVN_INVALID_REVNUM ? current.lastChangedDate : current.reposLastCmtDate;
			}
			public String getChangeAuthor() {
				return current.reposLastCmtRevision == Revision.SVN_INVALID_REVNUM ? current.lastCommitAuthor : current.reposLastCmtAuthor;
			}
			public Revision.Number getChangeRevision() {
				long changeRev = current.reposLastCmtRevision == Revision.SVN_INVALID_REVNUM ? current.lastChangedRevision : current.reposLastCmtRevision;
				return changeRev == Revision.SVN_INVALID_REVNUM ? null : (Revision.Number)Revision.fromNumber(changeRev);
			}
			public int getTextChangeType() {
				return current.repositoryTextStatus;
			}
			public int getPropertiesChangeType() {
				return current.repositoryPropStatus;
			}
			public int getNodeKind() {
				int kind = SVNUtility.getNodeKind(current.path, current.nodeKind, true);
				return kind == NodeKind.NONE ? SVNUtility.getNodeKind(current.path, current.reposKind, true) : kind;
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
			public IResource getExact(IResource []set) {
				return FileUtility.selectOneOf(scope, set);
			}
		};
		if (provider.getNodeKind() == NodeKind.NONE) {
			return null;
		}
		IResourceChange resourceChange = storage.asResourceChange(provider);
		if (resourceChange == null || resourceChange.getRevision() == Revision.SVN_INVALID_REVNUM) {
			return null;
		}
		rStatusOp.setPegRevision(resourceChange);
		IRepositoryResource originator = storage.asRepositoryResource(resourceChange.getResource());
		if (originator != null) {
			originator.setSelectedRevision(Revision.fromNumber(resourceChange.getRevision()));
			originator.setPegRevision(resourceChange.getPegRevision());
			resourceChange.setOriginator(originator);
		}
		
		resourceChange.setCommentProvider(new CommentProvider() {
			public String getComment(IResource resource, Revision rev, Revision peg) {
				String comment = (String)UpdateSubscriber.this.comments.get(rev);
				if (comment == null) {
					UpdateSubscriber.this.comments.put(rev, comment = super.getComment(resource, rev, peg));
				}
				return comment;
			}
		});
		return resourceChange;
	}
	
	protected IResource[] findChanges(IResource[] resources, int depth, IProgressMonitor monitor, IOperationWrapperFactory operationWrapperFactory) {
		this.comments.clear();
		return super.findChanges(resources, depth, monitor, operationWrapperFactory);
	}
	
	private UpdateSubscriber() {
		super();
		this.comments = new HashMap();
	}

}
