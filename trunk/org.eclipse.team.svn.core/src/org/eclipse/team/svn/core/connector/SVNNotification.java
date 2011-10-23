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
 * Notification information container
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector library
 * is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to do this is
 * providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public class SVNNotification {
	/**
	 * Enumeration of possible working copy entry locking states
	 */
	public static class NodeLock {
		/**
		 * The operation does not require any locks
		 */
		public static final int INAPPLICABLE = 0;

		/**
		 * The lock state is unknown
		 */
		public static final int UNKNOWN = 1;

		/**
		 * The lock state are same as before starting the operation
		 */
		public static final int UNCHANGED = 2;

		/**
		 * The working copy entry was locked
		 */
		public static final int LOCKED = 3;

		/**
		 * The working copy entry was unlocked
		 */
		public static final int UNLOCKED = 4;

		public static boolean isKnownStatus(int status) {
			return status >= NodeLock.INAPPLICABLE /*0*/ && status <= NodeLock.UNLOCKED /*4*/;
		}
	}

	/**
	 * Notify statuses enumeration
	 */
	public static class NodeStatus {
		/**
		 * Not applicable
		 */
		public static final int INAPPLICABLE = 0;

		/**
		 * Notifier doesn't know or isn't saying.
		 */
		public static final int UNKNOWN = 1;

		/**
		 * The state did not change.
		 */
		public static final int UNCHANGED = 2;

		/**
		 * The item wasn't present.
		 */
		public static final int MISSING = 3;

		/**
		 * An unversioned item obstructed work.
		 */
		public static final int OBSTRUCTED = 4;

		/**
		 * Base version was modified.
		 */
		public static final int CHANGED = 5;

		/**
		 * Modified state had mods merged in.
		 */
		public static final int MERGED = 6;

		/**
		 * Modified state got conflicting mods.
		 */
		public static final int CONFLICTED = 7;

		public static boolean isKnownStatus(int status) {
			return status >= NodeStatus.INAPPLICABLE /*0*/ && status <= NodeStatus.CONFLICTED /*7*/;
		}
		
		/**
		 * The textual representation for the status types
		 */
		public static final String[] statusNames = { "inapplicable", "unknown", "unchanged", "missing", "obstructed", "changed", "merged", "conflicted", };

		/**
		 * The short textual representation for the status types
		 */
		public static final String[] shortStatusNames = { " ", " ", " ", "?", "O", "U", "G", "C", };

	}

	/**
	 * The connector library actions enumeration
	 */
	public static class PerformedAction {
		public static final int _UNNKNOWN_COMMAND = -1;

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
		 * @since 1.5 A merge operation has begun.
		 */
		public static final int MERGE_BEGIN = 28;

		/**
		 * @since 1.5 A merge operation from a foreign repository has begun.
		 */
		public static final int FOREIGN_MERGE_BEGIN = 29;

		/**
		 * @since 1.5 Got a replaced in an update.
		 */
		public static final int UPDATE_REPLACED = 30;
		
		 /**
	     * @since 1.6
	     * Property added.
	     */
	    public static final int PROPERTY_ADDED = 31;

	    /**
	     * @since 1.6
	     * Property modified.
	     */
	    public static final int PROPERTY_MODIFIED = 32;

	    /**
	     * @since 1.6
	     * Property deleted.
	     */
	    public static final int PROPERTY_DELETED = 33;

	    /**
	     * @since 1.6
	     * Property delete nonexistent.
	     */
	    public static final int PROPERTY_DELETED_NONEXISTENT = 34;

	    /**
	     * @since 1.6
	     * Revision property set.
	     */
	    public static final int REVPROP_SET = 35;

	    /**
	     * @since 1.6
	     * Revision property deleted.
	     */
	    public static final int REVPROP_DELETE = 36;

	    /**
	     * @since 1.6
	     * The last notification in a merge
	     */
	    public static final int MERGE_COMPLETED = 37;

	    /**
	     * @since 1.6
	     * The path is a tree-conflict victim of the intended action
	     */
	    public static final int TREE_CONFLICT = 38;

		/**
		 * @since 1.7 A path has moved to another changelist.
		 */
		public static final int CHANGELIST_MOVED = 39;

		/**
		 * @since 1.7 A path has moved to another changelist.
		 */
		public static final int FAILED_EXTERNAL = 40;

		/**
		 * @since 1.7 Starting an update operation.
		 */
		public static final int UPDATE_STARTED = 41;

		/**
		 * @since 1.7 Skipping an obstruction working copy.
		 */
		public static final int UPDATE_SKIP_OBSTRUCTION = 42;

		/**
		 * @since 1.7 Skipping a working only node.
		 */
		public static final int UPDATE_SKIP_WORKING_ONLY = 43;

		/**
		 * @since 1.7 Skipped a file or directory to which access couldn't be obtained.
		 */
		public static final int UPDATE_SKIP_ACCESS_DENIED = 44;

		/**
		 * @since 1.7 An update operation removed an external working copy.
		 */
		public static final int UPDATE_EXTERNAL_REMOVED = 45;

		/**
		 * @since 1.7 Applying a shadowed add.
		 */
		public static final int UPDATE_SHADOWED_ADD = 46;

		/**
		 * @since 1.7 Applying a shadowed update.
		 */
		public static final int UPDATE_SHADOWED_UPDATE = 47;

		/**
		 * @since 1.7 Applying a shadowed delete.
		 */
		public static final int UPDATE_SHADOWED_DELETE = 48;

		/**
		 * @since 1.7 The mergeinfo on path was updated.
		 */
		public static final int MERGE_RECORD_INFO = 49;

		/**
		 * @since 1.7 An working copy directory was upgraded to the latest format.
		 */
		public static final int UPGRADED_PATH = 50;

		/**
		 * @since 1.7 Mergeinfo describing a merge was recorded.
		 */
		public static final int MERGE_RECORD_INFO_BEGIN = 51;

		/**
		 * @since 1.7 Mergeinfo was removed due to elision.
		 */
		public static final int MERGE_ELIDE_INFO = 52;

		/**
		 * @since 1.7 A file in the working copy was patched.
		 */
		public static final int PATCH = 53;

		/**
		 * @since 1.7 A hunk from a patch was applied.
		 */
		public static final int PATCH_APPLIED_HUNK = 54;

		/**
		 * @since 1.7 A hunk from a patch was rejected.
		 */
		public static final int PATCH_REJECTED_HUNK = 55;

		/**
		 * @since 1.7 A hunk from a patch was found to be already applied.
		 */
		public static final int PATCH_HUNK_ALREADY_APPLIED = 56;

		/**
		 * @since 1.7 Committing a non-overwriting copy (path is the target of the copy, not the source).
		 */
		public static final int COMMIT_COPIED = 57;

		/**
		 * @since 1.7 Committing an overwriting (replace) copy (path is the target of the copy, not the source).
		 */
		public static final int COMMIT_COPIED_REPLACED = 58;

		/**
		 * @since 1.7 The server has instructed the client to follow a URL redirection.
		 */
		public static final int URL_REDIRECT = 59;

		/**
		 * @since 1.7 The operation was attempted on a path which doesn't exist.
		 */
		public static final int PATH_NONEXISTENT = 60;

		/**
		 * @since 1.7 Removing a path by excluding it.
		 */
		public static final int EXCLUDE = 61;

		/**
		 * @since 1.7 Operation failed because the node remains in conflict.
		 */
		public static final int FAILED_CONFLICT = 62;

		/**
		 * @since 1.7 Operation failed because an added node is missing.
		 */
		public static final int FAILED_MISSING = 63;

		/**
		 * @since 1.7 Operation failed because a node is out of date.
		 */
		public static final int FAILED_OUT_OF_DATE = 64;

		/**
		 * @since 1.7 Operation failed because an added parent is not selected.
		 */
		public static final int FAILED_NO_PARENT = 65;

		/**
		 * @since 1.7 Operation failed because a node is locked.
		 */
		public static final int FAILED_LOCKED = 66;

		/**
		 * @since 1.7 Operation failed because the operation was forbidden.
		 */
		public static final int FAILED_FORBIDDEN_BY_SERVER = 67;

		/*
		 * Sometime native JavaHL client returns -1 as action (for example when file is replaced in branch then merged into trunk)...
		 */
		public static boolean isKnownAction(int action) {
			return action >= PerformedAction.ADD /*0*/ && action <= PerformedAction.FAILED_FORBIDDEN_BY_SERVER /*67*/;
		}
		
		/**
		 * Textual representation of the action types
		 */
		public static final String[] actionNames = { "add", "copy", "delete", "restore", "revert", "failed revert", "resolved", "skip", "update delete", "update add",
				"update modified", "update completed", "update external", "status completed", "status external", "sending modified", "sending added", "sending deleted",
				"sending replaced", "transfer", "blame revision processed", "locked", "unlocked", "locking failed", "unlocking failed", "path exists", "changelist set",
				"changelist cleared", "merge begin", "foreign merge begin", "replaced",
				"property added", "property modified", "property deleted", "nonexistent property deleted", "revprop set", "revprop deleted", "merge completed", "tree conflict",
				"changelist moved", "failed external", "update started", "update skip obstruction", "update skip working only", "update skip access denied", "update external removed",
				"update shadowed add", "update shadowed update", "update shadowed delete", "merge record info", "upgraded path", "merge record info begin", "Merge elide info", 
				"patch", "patch applied hunk", "patch rejected hunk", "patch hunk already applied", "commit copied", "commit copied replaced", "url redirect", "path nonexistent",
				"exclude", "failed conflict", "failed missing", "failed out of date", "failed no parent", "failed by lock", "failed forbidden by server"};
	}

	/**
	 * The entry path
	 */
	public final String path;

	/**
	 * The action performed with the entry (see {@link PerformedAction}).
	 */
	public final int action;

	/**
	 * The entry kind (see {@link Kind}).
	 */
	public final int kind;

	/**
	 * The entry MIME-type
	 */
	public final String mimeType;

	/**
	 * The entry lock. Could be <code>null</code>
	 */
	public final SVNLock lock;

	/**
	 * The error message for the entry
	 */
	public final String errMsg;

	/**
	 * The entry content state (see {@link NodeStatus}).
	 */
	public final int contentState;

	/**
	 * The entry properties state (see {@link NodeStatus}).
	 */
	public final int propState;

	/**
	 * The entry revision
	 */
	public final long revision;

	/**
	 * the state of the lock of the item (see {@link NodeLock}).
	 */
	public final int lockState;

	public SVNNotification(String path, int action, int kind, String mimeType, SVNLock lock, String errMsg, int contentState, int propState, int lockState, long revision) {
		this.path = path;
		this.action = action;
		this.kind = kind;
		this.mimeType = mimeType;
		this.lock = lock;
		this.errMsg = errMsg;
		this.contentState = contentState;
		this.propState = propState;
		this.revision = revision;
		this.lockState = lockState;
	}

}
