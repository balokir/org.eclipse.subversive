/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.client;

/**
 * Directory entry information container
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL client library
 * is not EPL compatible and we won't to pin plug-in with concrete client implementation. So, the only way to do this is
 * providing our own client interface which will be covered by concrete client implementation.
 * 
 * @author Alexander Gurov
 */
public class RepositoryEntry {
	/**
	 * The (@link DirEntry} fields acquisition masks. Combined mask could be passed into the
	 * {@link ISVNClientWrapper#list} method.
	 */
	public static class Fields {

		/**
		 * Specify that no fields will be requested.
		 */
		public static final int NONE = 0;

		/**
		 * Specify that all fields will be requested.
		 */
		public static final int ALL = ~NONE;

		/**
		 * Specify that the author field will be requested.
		 */
		public static final int AUTHOR = 0x00020;

		/**
		 * Specify that the date field will be requested.
		 */
		public static final int DATE = 0x00010;

		/**
		 * Specify that the revision field will be requested.
		 */
		public static final int REVISION = 0x00008;

		/**
		 * Specify that the hasProperties field will be requested.
		 */
		public static final int HAS_PROPERTIES = 0x00004;

		/**
		 * Specify that the size field will be requested.
		 */
		public static final int SIZE = 0x00002;

		/**
		 * Specify that the nodeKind field will be requested.
		 */
		public static final int NODE_KIND = 0x00001;

	}

	/**
	 * The path to the directory entry. The entry path is reported relatively to directory path.
	 */
	public final String path;

	/**
	 * The last change revision. Could be {@link Revision#INVALID_REVISION_NUMBER} if access mask does not specify the
	 * revision field.
	 */
	public final long revision;

	/**
	 * The last change date. Could be 0 if access mask does not specify the date field.
	 */
	public final long date;

	/**
	 * The last change author. Could be <code>null</code> if the last commit is performed with anonymous access or if
	 * the access mask does not specify the author field.
	 */
	public final String author;

	/**
	 * True if the entry has properties. Undetermined if access mask does not specify the hasProperties field.
	 */
	public final boolean hasProperties;

	/**
	 * {@link NodeKind#DIR} for directories or {@link NodeKind#FILE} for file. Undetermined if access mask does not
	 * specify the nodeKind field.
	 */
	public final int nodeKind;

	/**
	 * Size in bytes. Valid only for files, for directories always zero. Undetermined if access mask does not specify
	 * the size field.
	 */
	public final long size;

	/**
	 * Information about the entry lock. Could be <code>null</code> if entry has no lock.
	 */
	public final Lock lock;

	/**
	 * The {@link RepositoryEntry} instance could be initialized only once because all fields are final
	 * 
	 * @param path
	 *            the entry path
	 * @param revision
	 *            the last change revision
	 * @param date
	 *            the last change date
	 * @param author
	 *            the last change author
	 * @param hasProperties
	 *            specify if entry has properties
	 * @param nodeKind
	 *            the entry node kind
	 * @param size
	 *            file size in bytes
	 * @param lock
	 *            entry lock information
	 */
	public RepositoryEntry(String path, long revision, long date, String author, boolean hasProperties, int nodeKind, long size, Lock lock) {
		this.date = date;
		this.revision = revision;
		this.hasProperties = hasProperties;
		this.author = author;
		this.nodeKind = nodeKind;
		this.size = size;
		this.path = path;
		this.lock = lock;
	}

}
