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
 * Replacement for org.tigris.subversion.javahl.Info2
 * 
 * @author Alexander Gurov
 */
public class Info2 {
    /**
     * the path of the item
     */
    public final String path;
    /**
     * the url of the item
     */
    public final String url;
    /**
     * the revision of the item
     */
    public final long revision;
    /**
     * the item kinds (see NodeKind)
     */
    public final int kind;
    /**
     * the root URL of the repository
     */
    public final String reposRootUrl;
    /**
     * the UUID of the repository
     */
    public final String reposUUID;
    /**
     * the revision of the last change
     */
    public final long lastChangedRevision;
    /**
     * the date of the last change in ns
     */
    public final long lastChangedDate;
    /**
     * the author of the last change
     */
    public final String lastChangedAuthor;
    /**
     * the information about any lock (may be null)
     */
    public final Lock lock;
    /**
     * the flag if the remaining fields are set
     */
    public final boolean hasWcInfo;
    /**
     * the scheduled operation at next commit (see ScheduleKind)
     */
    public final int schedule;
    /**
     * if the item was copied, the source url
     */
    public final String copyFromUrl;
    /**
     * if the item was copied, the source rev
     */
    public final long copyFromRevision;
    /**
     * the last time the item was changed in ns
     */
    public final long textTime;
    /**
     * the last time the properties of the items were changed in ns
     */
    public final long propTime;
    /**
     * the checksum of the item
     */
    public final String checksum;
    /**
     * if the item is in conflict, the filename of the base version file
     */
    public final String conflictOld;
    /**
     * if the item is in conflict, the filename of the last repository version
     * file
     */
    public final String conflictNew;
    /**
     * if the item is in conflict, the filename of the working copy version file
     */
    public final String conflictWrk;
    /**
     * the property reject file
     */
    public final String prejfile;

    public Info2(String path, String url, long rev, int kind, String reposRootUrl,
          String reposUUID, long lastChangedRev, long lastChangedDate,
          String lastChangedAuthor, Lock lock, boolean hasWcInfo, int schedule,
          String copyFromUrl, long copyFromRev, long textTime, long propTime,
          String checksum, String conflictOld, String conflictNew,
          String conflictWrk, String prejfile) {
        this.path = path;
        this.url = url;
        this.revision = rev;
        this.kind = kind;
        this.reposRootUrl = reposRootUrl;
        this.reposUUID = reposUUID;
        this.lastChangedRevision = lastChangedRev;
        this.lastChangedDate = lastChangedDate;
        this.lastChangedAuthor = lastChangedAuthor;
        this.lock = lock;
        this.hasWcInfo = hasWcInfo;
        this.schedule = schedule;
        this.copyFromUrl = copyFromUrl;
        this.copyFromRevision = copyFromRev;
        this.textTime = textTime;
        this.propTime = propTime;
        this.checksum = checksum;
        this.conflictOld = conflictOld;
        this.conflictNew = conflictNew;
        this.conflictWrk = conflictWrk;
        this.prejfile = prejfile;
    }
    
}
