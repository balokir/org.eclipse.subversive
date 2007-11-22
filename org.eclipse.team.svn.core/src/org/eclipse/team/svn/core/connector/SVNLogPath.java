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

package org.eclipse.team.svn.core.connector;

/**
 * Changed path information container
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL client library
 * is not EPL compatible and we won't to pin plug-in with concrete client implementation. So, the only way to do this is
 * providing our own client interface which will be covered by concrete client implementation.
 * 
 * @author Alexander Gurov
 */
public class SVNLogPath {
	public static class ChangeType {
		/**
		 * The addition action type identifier
		 */
		public static final char ADDED = 'A';

		/**
		 * The deletion action type identifier
		 */
		public static final char DELETED = 'D';

		/**
		 * The replacement action type identifier
		 */
		public static final char REPLACED = 'R';

		/**
		 * The modification action type identifier
		 */
		public static final char MODIFIED = 'M';
	}

	/**
	 * The path of the changed entry.
	 */
	public final String path;

	/**
	 * The action performed over the entry (see {@link ChangeType}).
	 */
	public final char action;

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
	public SVNLogPath(String path, char action, String copiedFromPath, long copiedFromRevision) {
		this.path = path;
		this.copiedFromRevision = copiedFromRevision;
		this.copiedFromPath = copiedFromPath;
		this.action = action;
	}

}
