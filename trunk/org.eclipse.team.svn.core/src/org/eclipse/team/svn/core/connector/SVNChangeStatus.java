/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.connector;

/**
 * The change status information container
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector library
 * is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to do this is
 * providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public class SVNChangeStatus extends SVNEntryStatus {
	/**
	 * The repository URL of the entry
	 */
	public final String url;

	/**
	 * The working copy path of the entry
	 */
	public final String path;

	/**
	 * The base revision of the entry
	 */
	public final long revision;

	/**
	 * The last revision the entry was changed before base
	 */
	public final long lastChangedRevision;

	/**
	 * The last date the entry was changed before base (represented in microseconds since the epoch)
	 */
	public final long lastChangedDate;

	/**
	 * The last author of the last change before base
	 */
	public final String lastCommitAuthor;

	/**
	 * <code>true/code> if the entry is locked locally by subversion (running or aborted
	 * operation)
	 */
	public final boolean isLocked;

	/**
	 * <code>true/code> if the entry is a copy of another one
	 */
	public final boolean isCopied;

	/**
	 * <code>true/code> if the entry is switch
	 */
	public final boolean isSwitched;

	/**
	 * The entry remote content status in compare to base revision (see {@link SVNEntryStatus.Kind})
	 */
	public final int repositoryTextStatus;

	/**
	 * The entry remote properties status in compare to base revision (see {@link SVNEntryStatus.Kind})
	 */
	public final int repositoryPropStatus;

	/**
	 * The file name of the repository version if the entry changes is in conflicting state
	 */
	public final String conflictNew;

	/**
	 * The file name of the base version if the entry changes is in conflicting state
	 */
	public final String conflictOld;

	/**
	 * The file name of the local version if the entry changes is in conflicting state
	 */
	public final String conflictWorking;

	/**
	 * Points to the URL from which the entry is copied until commit
	 */
	public final String urlCopiedFrom;

	/**
	 * Points to the revision from which the entry is copied until commit
	 */
	public final long revisionCopiedFrom;

	/**
	 * @since 1.2 Token specified for the lock (<code>null</code> if not locked)
	 */
	public final String lockToken;

	/**
	 * @since 1.2 Owner of the lock (<code>null</code> if not locked)
	 */
	public final String lockOwner;

	/**
	 * @since 1.2 Comment specified for the lock (<code>null</code> if not locked)
	 */
	public final String lockComment;

	/**
	 * @since 1.2 The lock creation date in microseconds since the epoch
	 */
	public final long lockCreationDate;

	/**
	 * @since 1.2 The lock in the repository (<code>null</code> if not locked)
	 */
	public final SVNLock reposLock;

	/**
	 * @since 1.3 Set to the youngest committed revision, or {@link SVNRevision#INVALID_REVISION_NUMBER} if not out of
	 *        date.
	 */
	public final long reposLastCmtRevision;

	/**
	 * @since 1.3 Set to the most recent commit date, or 0 if not out of date.
	 */
	public final long reposLastCmtDate;

	/**
	 * @since 1.3 Set to the node kind of the youngest commit, or {@link Kind#NONE} if not out of date.
	 */
	public final int reposKind;

	/**
	 * @since 1.3 Set to the user name of the youngest commit, or <code>null</code> if not out of date.
	 */
	public final String reposLastCmtAuthor;

	/**
	 * The {@link SVNChangeStatus} instance could be initialized only once because all fields are final
	 * 
	 * @param path
	 *            the file system path of item
	 * @param url
	 *            the url of the item
	 * @param nodeKind
	 *            kind of item (directory, file or unknown)
	 * @param revision
	 *            the revision number of the base
	 * @param lastChangedRevision
	 *            the last revision this item was changed
	 * @param lastChangedDate
	 *            the last date this item was changed
	 * @param lastCommitAuthor
	 *            the author of the last change
	 * @param textStatus
	 *            the file or directory status (see {@link SVNEntryStatus.Kind})
	 * @param propStatus
	 *            the property status (see {@link SVNEntryStatus.Kind})
	 * @param repositoryTextStatus
	 *            the file or directory status of the base
	 * @param repositoryPropStatus
	 *            the property status of the base
	 * @param locked
	 *            if the item is locked (running or aborted operation)
	 * @param copied
	 *            if the item is copy
	 * @param conflictOld
	 *            in case of conflict, the file name of the the common base version
	 * @param conflictNew
	 *            in case of conflict, the file name of new repository version
	 * @param conflictWorking
	 *            in case of conflict, the file name of the former working copy version
	 * @param urlCopiedFrom
	 *            if copied, the url of the copy source
	 * @param revisionCopiedFrom
	 *            if copied, the revision number of the copy source
	 * @param switched
	 *            flag if the node has been switched in the path
	 * @param lockToken
	 *            the token for the current lock if any
	 * @param lockOwner
	 *            the owner of the current lock is any
	 * @param lockComment
	 *            the comment of the current lock if any
	 * @param lockCreationDate
	 *            the date, the lock was created if any
	 * @param reposLock
	 *            the lock as stored in the repository if any
	 * @param reposLastCmtRevision
	 *            the youngest revision, if out of date
	 * @param reposLastCmtDate
	 *            the last commit date, if out of date
	 * @param reposKind
	 *            the kind of the youngest revision, if out of date
	 * @param reposLastCmtAuthor
	 *            the author of the last commit, if out of date
	 */
	public SVNChangeStatus(String path, String url, int nodeKind, long revision, long lastChangedRevision, long lastChangedDate, String lastCommitAuthor, int textStatus,
			int propStatus, int repositoryTextStatus, int repositoryPropStatus, boolean locked, boolean copied, String conflictOld, String conflictNew, String conflictWorking,
			String urlCopiedFrom, long revisionCopiedFrom, boolean switched, String lockToken, String lockOwner, String lockComment, long lockCreationDate, SVNLock reposLock,
			long reposLastCmtRevision, long reposLastCmtDate, int reposKind, String reposLastCmtAuthor) {
		super(nodeKind, textStatus, propStatus);
		this.path = path;
		this.url = url;
		this.revision = revision;
		this.lastChangedRevision = lastChangedRevision;
		this.lastChangedDate = lastChangedDate;
		this.lastCommitAuthor = lastCommitAuthor;
		this.isLocked = locked;
		this.isCopied = copied;
		this.repositoryTextStatus = repositoryTextStatus;
		this.repositoryPropStatus = repositoryPropStatus;
		this.conflictOld = conflictOld;
		this.conflictNew = conflictNew;
		this.conflictWorking = conflictWorking;
		this.urlCopiedFrom = urlCopiedFrom;
		this.revisionCopiedFrom = revisionCopiedFrom;
		this.isSwitched = switched;
		this.lockToken = lockToken;
		this.lockOwner = lockOwner;
		this.lockComment = lockComment;
		this.lockCreationDate = lockCreationDate;
		this.reposLock = reposLock;
		this.reposLastCmtRevision = reposLastCmtRevision;
		this.reposLastCmtDate = reposLastCmtDate;
		this.reposKind = reposKind;
		this.reposLastCmtAuthor = reposLastCmtAuthor;
	}

}
