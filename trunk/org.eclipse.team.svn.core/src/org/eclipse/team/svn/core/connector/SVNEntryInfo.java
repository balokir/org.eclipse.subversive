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
 * Repository or working copy entry information container
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector library
 * is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to do this is
 * providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public class SVNEntryInfo {
	/**
	 * Enumeration of operation types which could be scheduled for the working copy entries
	 */
	public class ScheduledOperation {
		/**
		 * No operation scheduled
		 */
		public static final int NORMAL = 0;

		/**
		 * Will be added to repository on commit
		 */
		public static final int ADD = 1;

		/**
		 * Will be deleted from repository on commit
		 */
		public static final int DELETE = 2;

		/**
		 * Will be replaced in repository on commit
		 */
		public static final int REPLACE = 3;
	}
	
	/**
	 * The entry local path.
	 */
	public final String path;

	/**
	 * The entry URL.
	 */
	public final String url;

	/**
	 * The entry revision.
	 */
	public final long revision;

	/**
	 * The entry node kind.
	 */
	public final int kind;

	/**
	 * The repository root URL.
	 */
	public final String reposRootUrl;

	/**
	 * The repository UUID.
	 */
	public final String reposUUID;

	/**
	 * The last change revision.
	 */
	public final long lastChangedRevision;

	/**
	 * The last change date in in nanoseconds.
	 */
	public final long lastChangedDate;

	/**
	 * The last change author.
	 */
	public final String lastChangedAuthor;

	/**
	 * The lock information or <code>null</code>.
	 */
	public final SVNLock lock;

	/**
	 * True if rest of fields are set (for path-based queries).
	 */
	public final boolean hasWcInfo;

	/**
	 * The operation scheduled at next commit (see {@link ScheduledOperation})
	 */
	public final int schedule;

	/**
	 * The copied from URL.
	 */
	public final String copyFromUrl;

	/**
	 * The copied from revision.
	 */
	public final long copyFromRevision;

	/**
	 * The content last change time in nanoseconds.
	 */
	public final long textTime;

	/**
	 * The properties last change time in nanoseconds.
	 */
	public final long propTime;

	/**
	 * The entry checksum.
	 */
	public final String checksum;

	/**
	 * The filename of the base version file (if the entry is in conflicting state).
	 */
	public final String conflictOld;

	/**
	 * The filename of the last repository version file (if the entry is in conflicting state).
	 */
	public final String conflictNew;

	/**
	 * The filename of the working copy version file (if the entry is in conflicting state).
	 */
	public final String conflictWrk;

	/**
	 * The rejected properties file.
	 */
	public final String propertyRejectFile;

	public SVNEntryInfo(String path, String url, long rev, int kind, String reposRootUrl, String reposUUID, long lastChangedRev, long lastChangedDate, String lastChangedAuthor, SVNLock lock,
			boolean hasWcInfo, int schedule, String copyFromUrl, long copyFromRev, long textTime, long propTime, String checksum, String conflictOld, String conflictNew,
			String conflictWrk, String propertyRejectFile) {
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
		this.propertyRejectFile = propertyRejectFile;
	}

}
