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
 * The basic status information container
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector library
 * is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to do this is
 * providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public class SVNEntryStatus {
	/**
	 * Possible SVN status kinds
	 */
	public static class Kind {
		/**
		 * The entry does not exist
		 */
		public static final int NONE = 0;
	
		/**
		 * The entry exists, but uninteresting
		 */
		public static final int NORMAL = 1;
	
		/**
		 * The entry content or properties have been modified
		 */
		public static final int MODIFIED = 2;
	
		/**
		 * The entry is scheduled for addition
		 */
		public static final int ADDED = 3;
	
		/**
		 * The entry is scheduled for deletion
		 */
		public static final int DELETED = 4;
	
		/**
		 * The entry is not versioned
		 */
		public static final int UNVERSIONED = 5;
	
		/**
		 * The entry is missing (not scheduled for deletion but absent on the file system)
		 */
		public static final int MISSING = 6;
	
		/**
		 * The entry was deleted and then re-added
		 */
		public static final int REPLACED = 7;
	
		/**
		 * The entry not only locally changed but merged with the repository changes also
		 */
		public static final int MERGED = 8;
	
		/**
		 * The entry local is in conflicting state because local and repository changes cannot be merged automatically
		 */
		public static final int CONFLICTED = 9;
	
		/**
		 * An unversioned (or inconsistent working copy part) entry is in the way of the versioned entry
		 */
		public static final int OBSTRUCTED = 10;
	
		/**
		 * The entry is marked as ignored
		 */
		public static final int IGNORED = 11;
	
		/**
		 * The folder entry doesn't contain a complete child entries list
		 */
		public static final int INCOMPLETE = 12;
	
		/**
		 * An unversioned path populated by an svn:externals property
		 */
		public static final int EXTERNAL = 13;
	}

	/**
	 * The entry kind (see {@link Kind})
	 */
	public final int nodeKind;

	/**
	 * The entry local content status in compare to base revision (see {@link SVNEntryStatus.Kind})
	 */
	public final int textStatus;

	/**
	 * The entry local properties status in compare to base revision (see {@link SVNEntryStatus.Kind})
	 */
	public final int propStatus;

	/**
	 * The {@link SVNChangeStatus} instance could be initialized only once because all fields are final
	 * 
	 * @param nodeKind
	 *            kind of item (directory, file or unknown)
	 * @param textStatus
	 *            the file or directory status (see {@link SVNEntryStatus.Kind})
	 * @param propStatus
	 *            the property status (see {@link SVNEntryStatus.Kind})
	 */
	public SVNEntryStatus(int nodeKind, int textStatus, int propStatus) {
		this.nodeKind = nodeKind;
		this.textStatus = textStatus;
		this.propStatus = propStatus;
	}

}
