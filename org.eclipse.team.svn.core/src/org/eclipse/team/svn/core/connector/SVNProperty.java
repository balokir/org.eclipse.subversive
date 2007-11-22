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
 * Property data container
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector library
 * is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to do this is
 * providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public class SVNProperty {
	/**
	 * Built-in SVN properties
	 */
	public static class BuiltIn {
		/**
		 * The mime type of the binary file
		 */
		public static final String MIME_TYPE = "svn:mime-type";

		/**
		 * New line-separated list of ignored resource name patterns
		 */
		public static final String IGNORE = "svn:ignore";

		/**
		 * Specify the "end-of-line" characters will be used while reading file lines
		 */
		public static final String EOL_STYLE = "svn:eol-style";

		/**
		 * Keywords to be expanded during commit
		 */
		public static final String KEYWORDS = "svn:keywords";

		/**
		 * Marks the file is executable
		 */
		public static final String EXECUTABLE = "svn:executable";

		/**
		 * The value for svn:executable
		 */
		public static final String EXECUTABLE_VALUE = "*";

		/**
		 * The new line-separated list of folders which contains resources not related to this working copy. The value
		 * could be specified as follows:
		 * 
		 * {Folder_name_without_spaces} [-r{revision_number} | -r {revision_number}] {encoded_URL}
		 */
		public static final String EXTERNALS = "svn:externals";

		/**
		 * Internal property. The revision author.
		 */
		public static final String REV_AUTHOR = "svn:author";

		/**
		 * Internal property. The revision message.
		 */
		public static final String REV_LOG = "svn:log";

		/**
		 * Internal property. The revision date.
		 */
		public static final String REV_DATE = "svn:date";

		/**
		 * Internal property. The revision original date.
		 */
		public static final String REV_ORIGINAL_DATE = "svn:original-date";

		/**
		 * @since 1.2 If set points that lock is required to modify this node.
		 */
		public static final String NEEDS_LOCK = "svn:needs-lock";

	}

	/**
	 * The property name
	 */
	public final String name;

	/**
	 * The textual property value. Could be <code>null</code>.
	 */
	public final String value;

	/**
	 * The binary property value. Could be <code>null</code>.
	 */
	public final byte[] data;

	/**
	 * The {@link SVNProperty} instance could be initialized only once because all fields are final
	 * 
	 * @param name
	 *            the property name
	 * @param value
	 *            the value of the textual property
	 * @param data
	 *            the value of the binary property
	 */
	public SVNProperty(String name, String value, byte[] data) {
		this.name = name;
		this.value = value;
		this.data = data;
	}

}
