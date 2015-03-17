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

import java.io.File;


/**
 * The conflict description container
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector library
 * is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to do this is
 * providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public class SVNConflictDescriptor {
	/**
	 * Conflict kind: content or properties
	 */
	public enum Kind {
		/**
		 * Conflicting content
		 */
		CONTENT(0),
		/**
		 * Conflicting properties
		 */
		PROPERTIES(1),
		/**
		 * @since 1.7 Tree structure conflict
		 */
		TREE(2);
		
		public final int id;
		
		private Kind(int id) {
			this.id = id;
		}
	}

	/**
	 * The action in result of which conflict occurs
	 */
	public enum Action {
		/**
		 * Modification of content or properties
		 */
		MODIFY(0),
		/**
		 * Adding entry
		 */
		ADD(1),
		/**
		 * Deleting entry
		 */
		DELETE(2),
		/**
		 * Replacing entry
		 */
		REPLACE(3);
		
		public final int id;
		
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
	 * The reason why the conflict occurs
	 */
	public enum Reason {
		/**
		 * The entry is locally modified.
		 */
		MODIFIED(0),
		/**
		 * Another entry is in the way.
		 */
		OBSTRUCTED(1),
		/**
		 * The entry is locally deleted.
		 */
		DELETED(2),
		/**
		 * The entry is missing (deleted from the file system).
		 */
		MISSING(3),
		/**
		 * The unversioned entry at the path in the working copy.
		 */
		UNVERSIONED(4),
	    /**
         * Object is already added or schedule-add.
         * @since 1.6
         */
        ADDED(5),
	    /**
         * Object is already replaced.
         * @since 1.7
         */
        REPLACED(6),
	    /**
         * Object is moved away.
         * @since 1.8
         */
        MOVED_AWAY(7),
	    /**
         * Object is moved here.
         * @since 1.8
         */
        MOVED_HERE(8);
		
		public final int id;
		
		public static Reason fromId(int id) {
			for (Reason kind : values()) {
				if (kind.id == id) {
					return kind;
				}
			}
			throw new IllegalArgumentException("Invalid reason kind: " + id); //$NON-NLS-1$
		}
		
		private Reason(int id) {
			this.id = id;
		}
	}

	public enum Operation {
	    /**
	     * none
	     */
	    NONE(0),
	    /**
	     * update
	     */
	    UPDATE(1),
	    /**
	     * switch 
	     */	   
	    SWITCHED(2),
	    /**
	     * merge 
	     */
	    MERGE(3);
		
		public final int id;
		
		public static Operation fromId(int id) {
			for (Operation kind : values()) {
				if (kind.id == id) {
					return kind;
				}
			}
			throw new IllegalArgumentException("Invalid operation kind: " + id); //$NON-NLS-1$
		}
		
		private Operation(int id) {
			this.id = id;
		}
	}
	
	/**
	 * The conflicted entry path.
	 */
	public final String path;

	/**
	 * The conflict kind (see {@link Kind}).
	 */
	public final Kind conflictKind;

	/**
	 * The node kind (see {@link SVNEntry.Kind}).
	 */
	public final SVNEntry.Kind nodeKind;

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
	public final Action action;

	/**
	 * The reason why the conflict occurs (see {@link Reason}).
	 */
	public final Reason reason;

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
     * @see Operation
     */
	public final Operation operation;
	
    public final SVNConflictVersion srcLeftVersion;
    
    public final SVNConflictVersion srcRightVersion;
	
	/**
	 * The {@link SVNConflictDescriptor} instance could be initialized only once because all fields are final
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
	 * @param srcLeftVersion
	 * @param srcRightVersion
	 */
	public SVNConflictDescriptor(String path, Kind conflictKind, SVNEntry.Kind nodeKind, String propertyName, boolean isBinary, String mimeType, Action action, Reason reason, Operation operation, 
			String basePath, String remotePath, String localPath, String mergedPath, SVNConflictVersion srcLeftVersion, SVNConflictVersion srcRightVersion) {
		this.path = path;
		this.conflictKind = conflictKind;
		this.nodeKind = nodeKind;
		this.propertyName = propertyName;
		this.isBinary = isBinary;
		this.mimeType = mimeType;
		this.action = action;
		this.reason = reason;
		this.operation = operation;
		if (this.path != null && basePath != null && !basePath.startsWith(this.path))
		{
			this.basePath = new File(this.path).getParent() + File.separator + basePath;
		}
		else
		{
			this.basePath = basePath;
		}
		if (this.path != null && remotePath != null && !remotePath.startsWith(this.path))
		{
			this.remotePath = new File(this.path).getParent() + File.separator + remotePath;
		}
		else
		{
			this.remotePath = remotePath;
		}
		if (this.path != null && localPath != null && !localPath.startsWith(this.path))
		{
			this.localPath = new File(this.path).getParent() + File.separator + localPath;
		}
		else
		{
			this.localPath = localPath;
		}
		if (this.path != null && mergedPath != null && !mergedPath.startsWith(this.path))
		{
			this.mergedPath = new File(this.path).getParent() + File.separator + mergedPath;
		}
		else
		{
			this.mergedPath = mergedPath;
		}
		this.srcLeftVersion = srcLeftVersion;
		this.srcRightVersion = srcRightVersion;
	}
	
	/*
	 * Constructor for creating tree conflict descriptor
	 */
	public SVNConflictDescriptor(String path, Action action, Reason reason, Operation operation, SVNConflictVersion srcLeftVersion, SVNConflictVersion srcRightVersion) {		
		this(path, Kind.CONTENT, SVNEntry.Kind.NONE, null, false, null, action, reason, operation, null, null, null, null, srcLeftVersion, srcRightVersion);
	}
	
}
