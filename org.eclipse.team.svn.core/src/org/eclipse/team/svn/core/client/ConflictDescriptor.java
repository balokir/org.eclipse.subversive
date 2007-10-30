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
 * Replacement for org.tigris.subversion.javahl.ConflictDescriptor
 * 
 * @author Alexander Gurov
 */
public class ConflictDescriptor {
	public final String path;

	/**
	 * @see .Kind
	 */
	public final int conflictKind;

	/**
	 * @see org.tigris.subversion.javahl.NodeKind
	 */
	public final int nodeKind;

	public final String propertyName;

	public final boolean isBinary;

	public final String mimeType;

	// svn_wc_conflict_description_t also provides us with an
	// svn_wc_adm_access_t *. However, that is only useful to
	// JNI-based APIs written against svn_wc.h. So, we don't (yet)
	// expose that to JavaHL. We could expose it is a long
	// representing the memory address of the struct, which could be
	// passed off to other JNI APIs.

	/**
	 * @see #Action
	 */
	public final int action;

	/**
	 * @see #Reason
	 */
	public final int reason;

	// File paths, present only when the conflict involves the merging
	// of two files descended from a common ancestor, here are the
	// paths of up to four fulltext files that can be used to
	// interactively resolve the conflict. NOTE: The content of these
	// files will be in repository-normal form (LF line endings and
	// contracted keywords).
	public final String basePath;

	public final String theirPath;

	public final String myPath;

	public final String mergedPath;

	public ConflictDescriptor(String path, int conflictKind, int nodeKind, String propertyName, boolean isBinary, String mimeType, int action, int reason, String basePath,
			String theirPath, String myPath, String mergedPath) {
		this.path = path;
		this.conflictKind = conflictKind;
		this.nodeKind = nodeKind;
		this.propertyName = propertyName;
		this.isBinary = isBinary;
		this.mimeType = mimeType;
		this.action = action;
		this.reason = reason;
		this.basePath = basePath;
		this.theirPath = theirPath;
		this.myPath = myPath;
		this.mergedPath = mergedPath;
	}

	/**
	 * Poor man's enum for <code>svn_wc_conflict_kind_t</code>.
	 */
	public final class Kind {
		/**
		 * Attempting to change text or props.
		 */
		public static final int text = 0;

		/**
		 * Attempting to add object.
		 */
		public static final int property = 1;
	}

	/**
	 * Poor man's enum for <code>svn_wc_conflict_action_t</code>.
	 */
	public final class Action {
		/**
		 * Attempting to change text or props.
		 */
		public static final int edit = 0;

		/**
		 * Attempting to add object.
		 */
		public static final int add = 1;

		/**
		 * Attempting to delete object.
		 */
		public static final int delete = 2;
	}

	/**
	 * Poor man's enum for <code>svn_wc_conflict_reason_t</code>.
	 */
	public final class Reason {
		/**
		 * Local edits are already present.
		 */
		public static final int edited = 0;

		/**
		 * Another object is in the way.
		 */
		public static final int obstructed = 1;

		/**
		 * Object is already schedule-delete.
		 */
		public static final int deleted = 2;

		/**
		 * Object is unknown or missing.
		 */
		public static final int missing = 3;

		/**
		 * Object is unversioned.
		 */
		public static final int unversioned = 4;
	}
}
