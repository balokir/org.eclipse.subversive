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
 * Repository notification information container
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector library
 * is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to do this is
 * providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public class SVNRepositoryNotification {
	/**
	 * What action was applied to the node
	 */
	public static class NodeAction {
		public static final int UNKNOWN = -1;

		/**
		 * The node was changes
		 */
		public static final int CHANGE = 0;

		/**
		 * The node was added
		 */
		public static final int ADD = 1;

		/**
		 * The node was deleted
		 */
		public static final int DELETE = 2;

		/**
		 * The node was replaced
		 */
		public static final int REPLACE = 3;

		public static boolean isActionKnown(int action) {
			return action >= NodeAction.CHANGE /*0*/ && action <= NodeAction.REPLACE /*3*/;
		}
	}

	/**
	 * The type of action triggering the notification
	 */
	public static class Action {
		public static final int UNKNOWN = -1;

		/**
		 * A warning message is waiting.
		 */
		public static final int WARNING = 0;

		/**
		 * A revision has finished being dumped.
		 */
		public static final int DUMP_REV_END = 1;

		/**
		 * A revision has finished being verified.
		 */
		public static final int VERIFY_REV_END = 2;

		/**
		 * Packing of an FSFS shard has commenced.
		 */
		public static final int PACK_SHARD_START = 3;

		/**
		 * Packing of an FSFS shard is completed.
		 */
		public static final int PACK_SHARD_END = 4;

		/**
		 * Packing of the shard revprops has commenced.
		 */
		public static final int PACK_SHARD_START_REVPROP = 5;

		/**
		 * Packing of the shard revprops has completed.
		 */
		public static final int PACK_SHARD_END_REVPROP = 6;

		/**
		 * A revision has begun loading.
		 */
		public static final int LOAD_TXN_START = 7;

		/**
		 * A revision has finished loading.
		 */
		public static final int LOAD_TXN_COMMITTED = 8;

		/**
		 * A node has begun loading.
		 */
		public static final int LOAD_NODE_START = 9;

		/**
		 * A node has finished loading.
		 */
		public static final int LOAD_NODE_END = 10;

		/**
		 * A copied node has been encountered.
		 */
		public static final int LOAD_COPIED_NODE = 11;

		/**
		 * Mergeinfo has been normalized.
		 */
		public static final int LOAD_NORMALIZED_MERGEINFO = 12;

		/**
		 * The operation has acquired a mutex for the repo.
		 */
		public static final int MUTEX_ACQUIRED = 13;

		/**
		 * Recover has started.
		 */
		public static final int RECOVER_START = 14;

		/**
		 * Upgrade has started.
		 */
		public static final int UPGRADE_START = 15;

		/**
		 * A revision was skipped during loading.
		 * @since 1.8
		 */
		public static final int LOAD_SKIPPED_REV = 16;

		/**
		 * The structure of a revision is being verified.
		 * @since 1.8
		 */
		public static final int VERIFY_REV_STRUCTURE = 17;

		public static boolean isActionKnown(int action) {
			return action >= Action.WARNING /*0*/ && action <= Action.VERIFY_REV_STRUCTURE /*17*/;
		}
	}

	/**
	 * The entry path
	 */
	public final String path;

	/**
	 * The action performed with the node (see {@link NodeAction}).
	 */
	public final int nodeAction;

	/**
	 * The action performed which triggered the event (see {@link Action}).
	 */
	public final int action;

	/**
	 * The revision for the item.
	 */
	public final long revision;

	/**
	 * The warning text message.
	 */
	public final String warning;

	/**
	 * The related shard.
	 */
	public final long shard;

	/**
	 * The entry's new revision.
	 */
	public final long newRevision;

	/**
	 * The entry's old revision.
	 */
	public final long oldRevision;

	public SVNRepositoryNotification(String path, int nodeAction, int action, long revision, String warning, long shard, long newRevision, long oldRevision) {
		this.path = path;
		this.nodeAction = nodeAction;
		this.action = action;
		this.revision = revision;
		this.warning = warning;
		this.shard = shard;
		this.newRevision = newRevision;
		this.oldRevision = oldRevision;
	}

}
