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
 * The conflict description container
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL client library
 * is not EPL compatible and we won't to pin plug-in with concrete client implementation. So, the only way to do this is
 * providing our own client interface which will be covered by concrete client implementation.
 * 
 * @author Alexander Gurov
 */
public class ConflictDescriptor {
	/**
	 * Conflict kind: content or properties
	 */
	public static class Kind {
		/**
		 * Conflicting content
		 */
		public static final int CONTENT = 0;

		/**
		 * Conflicting properties
		 */
		public static final int PROPERTIES = 1;
	}

	/**
	 * The action in result of which conflict occurs
	 */
	public static class Action {
		/**
		 * Modification of content or properties
		 */
		public static final int MODIFY = 0;

		/**
		 * Adding entry
		 */
		public static final int ADD = 1;

		/**
		 * Deleting entry
		 */
		public static final int DELETE = 2;
	}

	/**
	 * The reason why the conflict occurs
	 */
	public static class Reason {
		/**
		 * The entry is locally modified.
		 */
		public static final int MODIFIED = 0;

		/**
		 * Another entry is in the way.
		 */
		public static final int OBSTRUCTED = 1;

		/**
		 * The entry is locally deleted.
		 */
		public static final int DELETED = 2;

		/**
		 * The entry is missing (deleted from the file system).
		 */
		public static final int MISSING = 3;

		/**
		 * The unversioned entry at the path in the working copy.
		 */
		public static final int UNVERSIONED = 4;
	}

	/**
	 * The conflicted entry path.
	 */
	public final String path;

	/**
	 * The conflict kind (see {@link Kind}).
	 */
	public final int conflictKind;

	/**
	 * The node kind (see {@link NodeKind}).
	 */
	public final int nodeKind;

	/**
	 * The conflicting property name.
	 */
	public final String propertyName;

	/**
	 * True if entry is binary.
	 */
	public final boolean isBinary;

	/**
	 * The MIME-type of the entry.
	 */
	public final String mimeType;

	/**
	 * The action in result of which conflict occurs (see {@link Action}).
	 */
	public final int action;

	/**
	 * The reason why the conflict occurs (see {@link Reason}).
	 */
	public final int reason;

	/**
	 * The base revision content path.
	 */
	public final String basePath;

	/**
	 * The repository revision content path.
	 */
	public final String remotePath;

	/**
	 * The local version content path.
	 */
	public final String localPath;

	/**
	 * The auto-merged content path.
	 */
	public final String mergedPath;

	/**
	 * The {@link ConflictDescriptor} instance could be initialized only once because all fields are final
	 * 
	 * @param path
	 *            the entry path
	 * @param conflictKind
	 *            the conflict kind
	 * @param nodeKind
	 *            the entry node kind
	 * @param propertyName
	 *            the conflicting property name
	 * @param isBinary
	 *            is entry binary or not
	 * @param mimeType
	 *            the entry MIME-type
	 * @param action
	 *            the action which involves conflict
	 * @param reason
	 *            the conflict reason
	 * @param basePath
	 *            the base version content path
	 * @param remotePath
	 *            the repository version content path
	 * @param localPath
	 *            the local version content path
	 * @param mergedPath
	 *            the auto-merged content path
	 */
	public ConflictDescriptor(String path, int conflictKind, int nodeKind, String propertyName, boolean isBinary, String mimeType, int action, int reason, String basePath,
			String remotePath, String localPath, String mergedPath) {
		this.path = path;
		this.conflictKind = conflictKind;
		this.nodeKind = nodeKind;
		this.propertyName = propertyName;
		this.isBinary = isBinary;
		this.mimeType = mimeType;
		this.action = action;
		this.reason = reason;
		this.basePath = basePath;
		this.remotePath = remotePath;
		this.localPath = localPath;
		this.mergedPath = mergedPath;
	}

}
