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

package org.eclipse.team.svn.core.client;

/**
 * Replacement for org.tigris.subversion.javahl.Status
 * 
 * @author Alexander Gurov
 */
public class Status {
    /**
     * the url for accessing the item
     */
    public final String url;
    /**
     * the path in the working copy
     */
    public final String path;
    /**
     * kind of the item (file, directory or unknonw)
     */
    public final int nodeKind;
    /**
     * the base revision of the working copy
     */
    public final long revision;
    /**
     * the last revision the item was changed before base
     */
    public final long lastChangedRevision;
    /**
     * the last date the item was changed before base (represented in
     * microseconds since the epoch)
     */
    public final long lastChangedDate;
    /**
     * the last author of the last change before base
     */
    public final String lastCommitAuthor;
    /**
     * the file or directory status (See StatusKind)
     */
    public final int textStatus;
    /**
     * the status of the properties (See StatusKind)
     */
    public final int propStatus;
    /**
     * flag is this item is locked locally by subversion
     * (running or aborted operation)
     */
    public final boolean isLocked;
    /**
     * has this item be copied from another item
     */
    public final boolean isCopied;
    /**
     * has the url of the item be switch
     */
    public final boolean isSwitched;
    /**
     * the file or directory status of base (See StatusKind)
     */
    public final int repositoryTextStatus;
    /**
     * the status of the properties base (See StatusKind)
     */
    public final int repositoryPropStatus;
    /**
     * if there is a conflict, the filename of the new version
     * from the repository
     */
    public final String conflictNew;
    /**
     * if there is a conflict, the filename of the common base version
     * from the repository
     */
    public final String conflictOld;
    /**
     * if there is a conflict, the filename of the former working copy
     * version
     */
    public final String conflictWorking;
    /**
     * if copied, the url of the copy source
     */
    public final String urlCopiedFrom;
    /**
     * if copied, the revision number of the copy source
     */
    public final long revisionCopiedFrom;
    /**
     * @since 1.2
     * token specified for the lock (null if not locked)
     */
    public final String lockToken;
    /**
     * @since 1.2
     * owner of the lock (null if not locked)
     */
    public final String lockOwner;
    /**
     * @since 1.2
     * comment specified for the lock (null if not locked)
     */
    public final String lockComment;
    /**
     * @since 1.2
     * date of the creation of the lock (represented in microseconds
     * since the epoch)
     */
    public final long lockCreationDate;
    /**
     * @since 1.2
     * the lock in the repository
     */
    public final Lock reposLock;
    /**
     * @since 1.3
     * Set to the youngest committed revision, or {@link
     * Revision#SVN_INVALID_REVNUM} if not out of date.
     */
    public final long reposLastCmtRevision;
    /**
     * @since 1.3
     * Set to the most recent commit date, or 0 if not out of date.
     */
    public final long reposLastCmtDate;
    /**
     * @since 1.3
     * Set to the node kind of the youngest commit, or {@link
     * NodeKind#none} if not out of date.
     */
    public final int reposKind;
    /**
     * @since 1.3
     * Set to the user name of the youngest commit, or
     * <code>null</code> if not out of date.
     */
    public final String reposLastCmtAuthor;
    
    /**
     * Container for JavaSVN extensions
     */
    public final Object attachment;

    /**
     * this constructor should only called from JNI code
     * @param path                  the file system path of item
     * @param url                   the url of the item
     * @param nodeKind              kind of item (directory, file or unknown
     * @param revision              the revision number of the base
     * @param lastChangedRevision   the last revision this item was changed
     * @param lastChangedDate       the last date this item was changed
     * @param lastCommitAuthor      the author of the last change
     * @param textStatus            the file or directory status (See
     *                              StatusKind)
     * @param propStatus            the property status (See StatusKind)
     * @param repositoryTextStatus  the file or directory status of the base
     * @param repositoryPropStatus  the property status of the base
     * @param locked                if the item is locked (running or aborted
     *                              operation)
     * @param copied                if the item is copy
     * @param conflictOld           in case of conflict, the file name of the
     *                              the common base version
     * @param conflictNew           in case of conflict, the file name of new
     *                              repository version
     * @param conflictWorking       in case of conflict, the file name of the
     *                              former working copy version
     * @param urlCopiedFrom         if copied, the url of the copy source
     * @param revisionCopiedFrom    if copied, the revision number of the copy
     *                              source
     * @param switched              flag if the node has been switched in the 
     *                              path
     * @param lockToken             the token for the current lock if any
     * @param lockOwner             the owner of the current lock is any
     * @param lockComment           the comment of the current lock if any
     * @param lockCreationDate      the date, the lock was created if any
     * @param reposLock             the lock as stored in the repository if
     *                              any
     * @param reposLastCmtRevision  the youngest revision, if out of date
     * @param reposLastCmtDate      the last commit date, if out of date
     * @param reposKind             the kind of the youngest revision, if
     *                              out of date
     * @param reposLastCmtAuthor    the author of the last commit, if out of
     *                              date
     */
    public Status(String path, String url, int nodeKind, long revision,
                  long lastChangedRevision, long lastChangedDate,
                  String lastCommitAuthor, int textStatus, int propStatus,
                  int repositoryTextStatus, int repositoryPropStatus,
                  boolean locked, boolean copied, String conflictOld,
                  String conflictNew, String conflictWorking,
                  String urlCopiedFrom, long revisionCopiedFrom,
                  boolean switched, String lockToken, String lockOwner, 
                  String lockComment, long lockCreationDate, Lock reposLock,
                  long reposLastCmtRevision, long reposLastCmtDate,
                  int reposKind, String reposLastCmtAuthor, Object attachment)
    {
        this.path = path;
        this.url = url;
        this.nodeKind = nodeKind;
        this.revision = revision;
        this.lastChangedRevision = lastChangedRevision;
        this.lastChangedDate = lastChangedDate;
        this.lastCommitAuthor = lastCommitAuthor;
        this.textStatus = textStatus;
        this.propStatus = propStatus;
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
        this.attachment = attachment;
    }

    /**
     * class for kind status of the item or its properties
     * the constants are defined in the interface StatusKind for building
     * reasons
     */
    public static final class Kind implements StatusKind {
        /**
         * Returns the textual representation of the status
         * @param kind of status
         * @return english status
         */
        public static final String getDescription(int kind) {
            switch (kind) {
            case StatusKind.none:
                return "non-svn";
            case StatusKind.normal:
                return "normal";
            case StatusKind.added:
                return "added";
            case StatusKind.missing:
                return "missing";
            case StatusKind.deleted:
                return "deleted";
            case StatusKind.replaced:
                return "replaced";
            case StatusKind.modified:
                return "modified";
            case StatusKind.merged:
                return "merged";
            case StatusKind.conflicted:
                return "conflicted";
            case StatusKind.ignored:
                return "ignored";
            case StatusKind.incomplete:
                return "incomplete";
            case StatusKind.external:
                return "external";
            case StatusKind.unversioned:
            default:
                return "unversioned";
            }
        }
    }

}
