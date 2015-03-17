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
 * Changed path information container
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector library
 * is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to do this is
 * providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public class SVNLogPath {
	public enum ChangeType {
		/**
		 * The addition action type identifier
		 */
		ADDED('A'),
		/**
		 * The deletion action type identifier
		 */
		DELETED('D'),
		/**
		 * The replacement action type identifier
		 */
		REPLACED('R'),
		/**
		 * The modification action type identifier
		 */
		MODIFIED('M');
		
		public final char id;
		
		public static ChangeType fromId(char id) {
			for (ChangeType kind : values()) {
				if (kind.id == id) {
					return kind;
				}
			}
			throw new IllegalArgumentException("Invalid change kind: " + id); //$NON-NLS-1$
		}
		
		private ChangeType(char id) {
			this.id = id;
		}
	}

	/**
	 * The path of the changed entry.
	 */
	public final String path;

	/**
	 * The action performed over the entry (see {@link ChangeType}).
	 */
	public final ChangeType action;

	/**
	 * The copy source path. Contains <code>null</code> if resource revision is not copied.
	 */
	public final String copiedFromPath;

	/**
	 * The copy source revision. Contains {@link SVNRevision#INVALID_REVISION_NUMBER} if resource revision is not copied.
	 */
	public final long copiedFromRevision;

	/**
	 * The {@link SVNLogPath} instance could be initialized only once because all fields are final
	 * 
	 * @param path
	 *            the path of the changed entry
	 * @param action
	 *            the action performed over the entry
	 * @param copiedFromPath
	 *            the copy source path
	 * @param copiedFromRevision
	 *            the copy source revision
	 */
	public SVNLogPath(String path, ChangeType action, String copiedFromPath, long copiedFromRevision) {
		this.path = path;
		this.copiedFromRevision = copiedFromRevision;
		this.copiedFromPath = copiedFromPath;
		this.action = action;
	}

	public String toString() {
		return String.valueOf(this.action) + ":" + this.path; //$NON-NLS-1$
	}
	
}
