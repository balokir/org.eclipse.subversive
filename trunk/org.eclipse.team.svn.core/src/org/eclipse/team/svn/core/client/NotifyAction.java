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
 * The client library actions enumeration
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL client library
 * is not EPL compatible and we won't to pin plug-in with concrete client implementation. So, the only way to do this is
 * providing our own client interface which will be covered by concrete client implementation.
 * 
 * @author Alexander Gurov
 */
public class NotifyAction {
	/**
	 * Adding a path to revision control.
	 */
	public static final int ADD = 0;

	/**
	 * Copying a versioned path.
	 */
	public static final int COPY = 1;

	/**
	 * Deleting a versioned path.
	 */
	public static final int DELETE = 2;

	/**
	 * Restoring a missing path from the base version.
	 */
	public static final int RESTORE = 3;

	/**
	 * Reverting a modified path.
	 */
	public static final int REVERT = 4;

	/**
	 * A revert operation has failed.
	 */
	public static final int FAILED_REVERT = 5;

	/**
	 * Resolving a conflict.
	 */
	public static final int RESOLVED = 6;

	/**
	 * Skipping a path.
	 */
	public static final int SKIP = 7;

	/*
	 * The update actions are also used for checkouts, switches, and merges.
	 */

	/**
	 * Got a delete in an update.
	 */
	public static final int UPDATE_DELETE = 8;

	/**
	 * Got an add in an update.
	 */
	public static final int UPDATE_ADD = 9;

	/**
	 * Got any other action in an update.
	 */
	public static final int UPDATE_UPDATE = 10;

	/**
	 * The last notification in an update
	 */
	public static final int UPDATE_COMPLETED = 11;

	/**
	 * About to update an external module, used for checkouts and switches
	 */
	public static final int UPDATE_EXTERNAL = 12;

	/**
	 * The last notification in a status (including status on externals).
	 */
	public static final int STATUS_COMPLETED = 13;

	/**
	 * Running status on an external module.
	 */
	public static final int STATUS_EXTERNAL = 14;

	/**
	 * Committing a modification.
	 */
	public static final int COMMIT_MODIFIED = 15;

	/**
	 * Committing an addition.
	 */
	public static final int COMMIT_ADDED = 16;

	/**
	 * Committing a deletion.
	 */
	public static final int COMMIT_DELETED = 17;

	/**
	 * Committing a replacement.
	 */
	public static final int COMMIT_REPLACED = 18;

	/**
	 * Transmitting post-fix text-delta data for a file.
	 */
	public static final int COMMIT_POSTFIX_TXDELTA = 19;

	/**
	 * Processed a single revision's blame.
	 */
	public static final int BLAME_REVISION = 20;

	/**
	 * @since 1.2 Locking a path
	 */
	public static final int LOCKED = 21;

	/**
	 * @since 1.2 Unlocking a path
	 */
	public static final int UNLOCKED = 22;

	/**
	 * @since 1.2 Failed to lock a path
	 */
	public static final int FAILED_LOCK = 23;

	/**
	 * @since 1.2 Failed to unlock a path
	 */
	public static final int FAILED_UNLOCK = 24;

	/**
	 * @since 1.5 Tried adding a path that already exists.
	 */
	public static final int EXISTS = 25;

	/**
	 * @since 1.5 Set the changelist for a path.
	 */
	public static final int CHANGELIST_SET = 26;

	/**
	 * @since 1.5 Clear the changelist for a path.
	 */
	public static final int CHANGELIST_CLEAR = 27;

	/**
	 * @since 1.5 Changelist operation failed.
	 */
	public static final int CHANGELIST_FAILED = 28;

	/**
	 * @since 1.5 A merge operation has begun.
	 */
	public static final int MERGE_BEGIN = 29;

	/**
	 * Textual representation of the action types
	 */
	public static final String[] actionNames = { "add", "copy", "delete", "restore", "revert", "failed revert", "resolved", "skip", "update delete", "update add",
			"update modified", "update completed", "update external", "status completed", "status external", "sending modified", "sending added   ", "sending deleted ",
			"sending replaced", "transfer", "blame revision processed", "locked", "unlocked", "locking failed", "unlocking failed", "path exists", "changelist set",
			"changelist cleared", "changelist failed", "merge begin", };
}
