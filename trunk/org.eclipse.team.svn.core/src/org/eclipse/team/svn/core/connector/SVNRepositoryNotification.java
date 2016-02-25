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
	public enum NodeAction {
		UNKNOWN(-1),
		/**
		 * The node was changes
		 */
		CHANGE(0),
		/**
		 * The node was added
		 */
		ADD(1),
		/**
		 * The node was deleted
		 */
		DELETE(2),
		/**
		 * The node was replaced
		 */
		REPLACE(3);
		
		public final int id;

		public static boolean isActionKnown(int action) {
			return action >= NodeAction.CHANGE.id /*0*/ && action <= NodeAction.REPLACE.id /*3*/;
		}
		
		public static NodeAction fromId(int id) {
			for (NodeAction kind : values()) {
				if (kind.id == id) {
					return kind;
				}
			}
			throw new IllegalArgumentException("Invalid node action kind: " + id); //$NON-NLS-1$
		}
		
		private NodeAction(int id) {
			this.id = id;
		}
	}

	/**
	 * The type of action triggering the notification
	 */
	public enum Action {
		UNKNOWN(-1),
		/**
		 * A warning message is waiting.
		 */
		WARNING(0),
		/**
		 * A revision has finished being dumped.
		 */
		DUMP_REV_END(1),
		/**
		 * A revision has finished being verified.
		 */
		VERIFY_REV_END(2),
		/**
		 * Packing of an FSFS shard has commenced.
		 */
		PACK_SHARD_START(3),
		/**
		 * Packing of an FSFS shard is completed.
		 */
		PACK_SHARD_END(4),
		/**
		 * Packing of the shard revprops has commenced.
		 */
		PACK_SHARD_START_REVPROP(5),
		/**
		 * Packing of the shard revprops has completed.
		 */
		PACK_SHARD_END_REVPROP(6),
		/**
		 * A revision has begun loading.
		 */
		LOAD_TXN_START(7),
		/**
		 * A revision has finished loading.
		 */
		LOAD_TXN_COMMITTED(8),
		/**
		 * A node has begun loading.
		 */
		LOAD_NODE_START(9),
		/**
		 * A node has finished loading.
		 */
		LOAD_NODE_END(10),
		/**
		 * A copied node has been encountered.
		 */
		LOAD_COPIED_NODE(11),
		/**
		 * Mergeinfo has been normalized.
		 */
		LOAD_NORMALIZED_MERGEINFO(12),
		/**
		 * The operation has acquired a mutex for the repo.
		 */
		MUTEX_ACQUIRED(13),
		/**
		 * Recover has started.
		 */
		RECOVER_START(14),
		/**
		 * Upgrade has started.
		 */
		UPGRADE_START(15),
		/**
		 * A revision was skipped during loading.
		 * @since 1.8
		 */
		LOAD_SKIPPED_REV(16),
		/**
		 * The structure of a revision is being verified.
		 * @since 1.8
		 */
		VERIFY_REV_STRUCTURE(17);

		

        /**
         * A revprop shard got packed. @
         * @since 1.9
         */
//        pack_revprops,

        /**
         * A non-packed revprop shard got removed.
         * @since 1.9
         */
//        cleanup_revprops,

        /**
         * The repository format got bumped.
         * @since 1.9
         */
//        format_bumped,

        /**
         * A revision range was copied.
         * @since 1.9
         */
//        hotcopy_rev_range;
		
		public final int id;
		
		public static boolean isActionKnown(int action) {
			return action >= Action.WARNING.id /*0*/ && action <= Action.VERIFY_REV_STRUCTURE.id /*17*/;
		}
		
		public static Action fromId(int id) {
			for (Action kind : values()) {
				if (kind.id == id) {
					return kind;
				}
			}
			throw new IllegalArgumentException("Invalid action kind: " + id); //$NON-NLS-1$
		}
		
		private Action(int id) {
			this.id = id;
		}
	}

	/**
	 * The entry path
	 */
	public final String path;

	/**
	 * The action performed with the node (see {@link NodeAction}).
	 */
	public final NodeAction nodeAction;

	/**
	 * The action performed which triggered the event (see {@link Action}).
	 */
	public final Action action;

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

	public SVNRepositoryNotification(String path, NodeAction nodeAction, Action action, long revision, String warning, long shard, long newRevision, long oldRevision) {
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
