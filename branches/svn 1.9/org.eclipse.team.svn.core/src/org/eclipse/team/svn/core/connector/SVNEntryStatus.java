/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
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
public abstract class SVNEntryStatus {
	/**
	 * Possible SVN status kinds
	 */
	public enum Kind {
		/**
		 * The entry does not exist
		 */
		NONE(0),
		/**
		 * The entry exists, but uninteresting
		 */
		NORMAL(1),
		/**
		 * The entry content or properties have been modified
		 */
		MODIFIED(2),
		/**
		 * The entry is scheduled for addition
		 */
		ADDED(3),
		/**
		 * The entry is scheduled for deletion
		 */
		DELETED(4),
		/**
		 * The entry is not versioned
		 */
		UNVERSIONED(5),
		/**
		 * The entry is missing (not scheduled for deletion but absent on the file system)
		 */
		MISSING(6),
		/**
		 * The entry was deleted and then re-added
		 */
		REPLACED(7),
		/**
		 * The entry not only locally changed but merged with the repository changes also
		 */
		MERGED(8),
		/**
		 * The entry local is in conflicting state because local and repository changes cannot be merged automatically
		 */
		CONFLICTED(9),
		/**
		 * An unversioned (or inconsistent working copy part) entry is in the way of the versioned entry
		 */
		OBSTRUCTED(10),
		/**
		 * The entry is marked as ignored
		 */
		IGNORED(11),
		/**
		 * The folder entry doesn't contain a complete child entries list
		 */
		INCOMPLETE(12),
		/**
		 * An unversioned path populated by an svn:externals property
		 */
		EXTERNAL(13);
		
		public final int id;
		
		public static Kind fromId(int id) {
			for (Kind kind : values()) {
				if (kind.id == id) {
					return kind;
				}
			}
			throw new IllegalArgumentException("Invalid entry status kind: " + id); //$NON-NLS-1$
		}
		
		private Kind(int id) {
			this.id = id;
		}
	}

	/**
	 * The entry kind (see {@link SVNEntry.Kind})
	 */
	public final SVNEntry.Kind nodeKind;

	/**
	 * The status of the node, based on restructuring changes; if the node
     * has no restructuring changes, it will be set to Kind.NONE (see {@link SVNEntryStatus.Kind})
	 * @since 1.9
	 */
	//public final Kind nodeStatus;

	/**
	 * The entry local content status in compare to base revision, not including restructuring changes. (see {@link SVNEntryStatus.Kind})
	 */
	public final Kind textStatus;

	/**
	 * The entry local properties status in compare to base revision (see {@link SVNEntryStatus.Kind})
	 */
	public final Kind propStatus;

	/**
	 * The {@link SVNChangeStatus} instance could be initialized only once because all fields are final
	 * 
	 * @param nodeKind
	 *            kind of item (directory, file or unknown) (see {@link SVNEntry.Kind})
	 * @param textStatus
	 *            the file or directory status (see {@link SVNEntryStatus.Kind})
	 * @param propStatus
	 *            the property status (see {@link SVNEntryStatus.Kind})
	 */
	public SVNEntryStatus(SVNEntry.Kind nodeKind, Kind textStatus, Kind propStatus) {
		this.nodeKind = nodeKind;
		this.textStatus = textStatus;
		this.propStatus = propStatus;
	}

}
