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
	public enum NodeLock {
		/**
		 * The operation does not require any locks
		 */
		INAPPLICABLE(0),
		/**
		 * The lock state is unknown
		 */
		UNKNOWN(1),
		/**
		 * The lock state are same as before starting the operation
		 */
		UNCHANGED(2),
		/**
		 * The working copy entry was locked
		 */
		LOCKED(3),
		/**
		 * The working copy entry was unlocked
		 */
		UNLOCKED(4);
		
		public final int id;

		public static boolean isStatusKnown(int status) {
			return status >= NodeLock.INAPPLICABLE.id /*0*/ && status <= NodeLock.UNLOCKED.id /*4*/;
		}
		
		public static NodeLock fromId(int id) {
			for (NodeLock kind : values()) {
				if (kind.id == id) {
					return kind;
				}
			}
			throw new IllegalArgumentException("Invalid lock kind: " + id); //$NON-NLS-1$
		}
		
		private NodeLock(int id) {
			this.id = id;
		}
	}

	/**
	 * Notify statuses enumeration
	 */
	public enum NodeStatus {
		/**
		 * Not applicable
		 */
		INAPPLICABLE(0),
		/**
		 * Notifier doesn't know or isn't saying.
		 */
		UNKNOWN(1),
		/**
		 * The state did not change.
		 */
		UNCHANGED(2),
		/**
		 * The item wasn't present.
		 */
		MISSING(3),
		/**
		 * An unversioned item obstructed work.
		 */
		OBSTRUCTED(4),
		/**
		 * Base version was modified.
		 */
		CHANGED(5),
		/**
		 * Modified state had mods merged in.
		 */
		MERGED(6),
		/**
		 * Modified state got conflicting mods.
		 */
		CONFLICTED(7);
		
		public final int id;

		public static boolean isStatusKnown(int status) {
			return status >= NodeStatus.INAPPLICABLE.id /*0*/ && status <= NodeStatus.CONFLICTED.id /*7*/;
		}
		
		/**
		 * The textual representation for the status types
		 */
		public static final String[] statusNames = { "inapplicable", "unknown", "unchanged", "missing", "obstructed", "changed", "merged", "conflicted", };

		/**
		 * The short textual representation for the status types
		 */
		public static final String[] shortStatusNames = { " ", " ", " ", "?", "O", "U", "G", "C", };
		
		public static NodeStatus fromId(int id) {
			for (NodeStatus kind : values()) {
				if (kind.id == id) {
					return kind;
				}
			}
			throw new IllegalArgumentException("Invalid node status kind: " + id); //$NON-NLS-1$
		}
		
		private NodeStatus(int id) {
			this.id = id;
		}
	}

	/**
	 * The connector library actions enumeration
	 */
	public enum PerformedAction {
		_UNKNOWN_ACTION(-1),
		/**
		 * Adding a path to revision control.
		 */
		ADD(0),
		/**
		 * Copying a versioned path.
		 */
		COPY(1),
		/**
		 * Deleting a versioned path.
		 */
		DELETE(2),
		/**
		 * Restoring a missing path from the base version.
		 */
		RESTORE(3),
		/**
		 * Reverting a modified path.
		 */
		REVERT(4),
		/**
		 * A revert operation has failed.
		 */
		FAILED_REVERT(5),
		/**
		 * Resolving a conflict.
		 */
		RESOLVED(6),
		/**
		 * Skipping a path.
		 */
		SKIP(7),

		/*
		 * The update actions are also used for checkouts, switches, and merges.
		 */
		/**
		 * Got a delete in an update.
		 */
		UPDATE_DELETE(8),
		/**
		 * Got an add in an update.
		 */
		UPDATE_ADD(9),
		/**
		 * Got any other action in an update.
		 */
		UPDATE_UPDATE(10),
		/**
		 * The last notification in an update
		 */
		UPDATE_COMPLETED(11),
		/**
		 * About to update an external module, used for checkouts and switches
		 */
		UPDATE_EXTERNAL(12),
		/**
		 * The last notification in a status (including status on externals).
		 */
		STATUS_COMPLETED(13),
		/**
		 * Running status on an external module.
		 */
		STATUS_EXTERNAL(14),
		/**
		 * Committing a modification.
		 */
		COMMIT_MODIFIED(15),
		/**
		 * Committing an addition.
		 */
		COMMIT_ADDED(16),
		/**
		 * Committing a deletion.
		 */
		COMMIT_DELETED(17),
		/**
		 * Committing a replacement.
		 */
		COMMIT_REPLACED(18),
		/**
		 * Transmitting post-fix text-delta data for a file.
		 */
		COMMIT_POSTFIX_TXDELTA(19),
		/**
		 * Processed a single revision's blame.
		 */
		BLAME_REVISION(20),
		/**
		 * @since 1.2 Locking a path
		 */
		LOCKED(21),
		/**
		 * @since 1.2 Unlocking a path
		 */
		UNLOCKED(22),
		/**
		 * @since 1.2 Failed to lock a path
		 */
		FAILED_LOCK(23),
		/**
		 * @since 1.2 Failed to unlock a path
		 */
		FAILED_UNLOCK(24),
		/**
		 * @since 1.5 Tried adding a path that already exists.
		 */
		EXISTS(25),
		/**
		 * @since 1.5 Set the changelist for a path.
		 */
		CHANGELIST_SET(26),
		/**
		 * @since 1.5 Clear the changelist for a path.
		 */
		CHANGELIST_CLEAR(27),
		/**
		 * @since 1.5 A merge operation has begun.
		 */
		MERGE_BEGIN(28),
		/**
		 * @since 1.5 A merge operation from a foreign repository has begun.
		 */
		FOREIGN_MERGE_BEGIN(29),
		/**
		 * @since 1.5 Got a replaced in an update.
		 */
		UPDATE_REPLACED(30),
		/**
	     * @since 1.6
	     * Property added.
	     */
	    PROPERTY_ADDED(31),
	    /**
	     * @since 1.6
	     * Property modified.
	     */
	    PROPERTY_MODIFIED(32),
	    /**
	     * @since 1.6
	     * Property deleted.
	     */
	    PROPERTY_DELETED(33),
	    /**
	     * @since 1.6
	     * Property delete nonexistent.
	     */
	    PROPERTY_DELETED_NONEXISTENT(34),
	    /**
	     * @since 1.6
	     * Revision property set.
	     */
	    REVPROP_SET(35),
	    /**
	     * @since 1.6
	     * Revision property deleted.
	     */
	    REVPROP_DELETE(36),
	    /**
	     * @since 1.6
	     * The last notification in a merge
	     */
	    MERGE_COMPLETED(37),
	    /**
	     * @since 1.6
	     * The path is a tree-conflict victim of the intended action
	     */
	    TREE_CONFLICT(38),
		/**
		 * @since 1.7 A path has moved to another changelist.
		 */
		CHANGELIST_MOVED(39),
		/**
		 * @since 1.7 A path has moved to another changelist.
		 */
		FAILED_EXTERNAL(40),
		/**
		 * @since 1.7 Starting an update operation.
		 */
		UPDATE_STARTED(41),
		/**
		 * @since 1.7 Skipping an obstruction working copy.
		 */
		UPDATE_SKIP_OBSTRUCTION(42),
		/**
		 * @since 1.7 Skipping a working only node.
		 */
		UPDATE_SKIP_WORKING_ONLY(43),
		/**
		 * @since 1.7 Skipped a file or directory to which access couldn't be obtained.
		 */
		UPDATE_SKIP_ACCESS_DENIED(44),
		/**
		 * @since 1.7 An update operation removed an external working copy.
		 */
		UPDATE_EXTERNAL_REMOVED(45),
		/**
		 * @since 1.7 Applying a shadowed add.
		 */
		UPDATE_SHADOWED_ADD(46),
		/**
		 * @since 1.7 Applying a shadowed update.
		 */
		UPDATE_SHADOWED_UPDATE(47),
		/**
		 * @since 1.7 Applying a shadowed delete.
		 */
		UPDATE_SHADOWED_DELETE(48),
		/**
		 * @since 1.7 The mergeinfo on path was updated.
		 */
		MERGE_RECORD_INFO(49),
		/**
		 * @since 1.7 An working copy directory was upgraded to the latest format.
		 */
		UPGRADED_PATH(50),
		/**
		 * @since 1.7 Mergeinfo describing a merge was recorded.
		 */
		MERGE_RECORD_INFO_BEGIN(51),
		/**
		 * @since 1.7 Mergeinfo was removed due to elision.
		 */
		MERGE_ELIDE_INFO(52),
		/**
		 * @since 1.7 A file in the working copy was patched.
		 */
		PATCH(53),
		/**
		 * @since 1.7 A hunk from a patch was applied.
		 */
		PATCH_APPLIED_HUNK(54),
		/**
		 * @since 1.7 A hunk from a patch was rejected.
		 */
		PATCH_REJECTED_HUNK(55),
		/**
		 * @since 1.7 A hunk from a patch was found to be already applied.
		 */
		PATCH_HUNK_ALREADY_APPLIED(56),
		/**
		 * @since 1.7 Committing a non-overwriting copy (path is the target of the copy, not the source).
		 */
		COMMIT_COPIED(57),
		/**
		 * @since 1.7 Committing an overwriting (replace) copy (path is the target of the copy, not the source).
		 */
		COMMIT_COPIED_REPLACED(58),
		/**
		 * @since 1.7 The server has instructed the client to follow a URL redirection.
		 */
		URL_REDIRECT(59),
		/**
		 * @since 1.7 The operation was attempted on a path which doesn't exist.
		 */
		PATH_NONEXISTENT(60),
		/**
		 * @since 1.7 Removing a path by excluding it.
		 */
		EXCLUDE(61),
		/**
		 * @since 1.7 Operation failed because the node remains in conflict.
		 */
		FAILED_CONFLICT(62),
		/**
		 * @since 1.7 Operation failed because an added node is missing.
		 */
		FAILED_MISSING(63),
		/**
		 * @since 1.7 Operation failed because a node is out of date.
		 */
		FAILED_OUT_OF_DATE(64),
		/**
		 * @since 1.7 Operation failed because an added parent is not selected.
		 */
		FAILED_NO_PARENT(65),
		/**
		 * @since 1.7 Operation failed because a node is locked.
		 */
		FAILED_LOCKED(66),
		/**
		 * @since 1.7 Operation failed because the operation was forbidden.
		 */
		FAILED_FORBIDDEN_BY_SERVER(67),
		/**
		 * @since 1.7 Operation skipped the path because it was conflicted.
		 */
		SKIP_CONFLICTED(68),
		/**
		 * @since 1.8 The lock on a file was removed during update.
		 */
		UPDATE_BROKEN_LOCK(69),
		/**
		 * @since 1.8 Operation failed because a node is obstructed.
		 */
		FAILED_OBSTRUCTED(70),
		/**
		 * @since 1.8 Conflict resolver is starting.
		 */
		CONFLICT_RESOLVER_STARTING(71),
		/**
		 * @since 1.8 Conflict resolver is done.
		 */
		CONFLICT_RESOLVER_DONE(72),
		/**
		 * @since 1.8 Operation left local modifications.
		 */
		LEFT_LOCAL_MODIFICATIONS(73),
		/**
		 * @since 1.8 A copy from a foreign repository has started.
		 */
		FOREIGN_COPY_BEGIN(74),
		/**
		 * @since 1.8 A move in the working copy has been broken.
		 */
		MOVE_BROKEN(75);
		
		public final int id;

		/*
		 * Sometime native JavaHL client returns -1 as action (for example when file is replaced in branch then merged into trunk)...
		 */
		public static boolean isActionKnown(int action) {
			return action >= PerformedAction.ADD.id /*0*/ && action <= PerformedAction.MOVE_BROKEN.id /*75*/;
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
				"exclude", "failed conflict", "failed missing", "failed out of date", "failed no parent", "failed by lock", "failed forbidden by server",
				"broken lock removed", "failed by obstruction", "conflict resolver starting", "conflict resolver done", "conflict resolver done", "foreign copy begin", "move broken"};
		
		public static PerformedAction fromId(int id) {
			for (PerformedAction kind : values()) {
				if (kind.id == id) {
					return kind;
				}
			}
			throw new IllegalArgumentException("Invalid action kind: " + id); //$NON-NLS-1$
		}
		
		private PerformedAction(int id) {
			this.id = id;
		}
	}

	/**
	 * The entry path
	 */
	public final String path;

	/**
	 * The action performed with the entry (see {@link PerformedAction}).
	 */
	public final PerformedAction action;

	/**
	 * The entry kind (see {@link SVNEntry.Kind}).
	 */
	public final SVNEntry.Kind kind;

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
	public final NodeStatus contentState;

	/**
	 * The entry properties state (see {@link NodeStatus}).
	 */
	public final NodeStatus propState;

	/**
	 * The entry revision
	 */
	public final long revision;

	/**
	 * the state of the lock of the item (see {@link NodeLock}).
	 */
	public final NodeLock lockState;

	public SVNNotification(String path, PerformedAction action, SVNEntry.Kind kind, String mimeType, SVNLock lock, String errMsg, NodeStatus contentState, NodeStatus propState, NodeLock lockState, long revision) {
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
