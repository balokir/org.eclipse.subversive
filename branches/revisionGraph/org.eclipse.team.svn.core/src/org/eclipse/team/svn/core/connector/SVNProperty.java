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
		public static final String MIME_TYPE = "svn:mime-type"; //$NON-NLS-1$

		/**
		 * New line-separated list of ignored resource name patterns
		 */
		public static final String IGNORE = "svn:ignore"; //$NON-NLS-1$

		/**
		 * Specify the "end-of-line" characters will be used while reading file lines
		 */
		public static final String EOL_STYLE = "svn:eol-style"; //$NON-NLS-1$

		/**
		 * Keywords to be expanded during commit
		 */
		public static final String KEYWORDS = "svn:keywords"; //$NON-NLS-1$

		/**
		 * Marks the file is executable
		 */
		public static final String EXECUTABLE = "svn:executable"; //$NON-NLS-1$

		/**
		 * The value for svn:executable
		 */
		public static final String EXECUTABLE_VALUE = "*"; //$NON-NLS-1$

		/**
		 * The new line-separated list of folders which contains resources not related to this working copy. The value
		 * could be specified as follows:
		 * 
		 * {Folder_name_without_spaces} [-r{revision_number} | -r {revision_number}] {encoded_URL}
		 */
		public static final String EXTERNALS = "svn:externals"; //$NON-NLS-1$

		/**
		 * Internal property. The revision author.
		 */
		public static final String REV_AUTHOR = "svn:author"; //$NON-NLS-1$

		/**
		 * Internal property. The revision message.
		 */
		public static final String REV_LOG = "svn:log"; //$NON-NLS-1$

		/**
		 * Internal property. The revision date.
		 */
		public static final String REV_DATE = "svn:date"; //$NON-NLS-1$

		/**
		 * Internal property. The revision original date.
		 */
		public static final String REV_ORIGINAL_DATE = "svn:original-date"; //$NON-NLS-1$

		/**
		 * @since 1.2 If set points that lock is required to modify this node.
		 */
		public static final String NEEDS_LOCK = "svn:needs-lock"; //$NON-NLS-1$

	}

	/**
	 * The property name
	 */
	public final String name;

	/**
	 * The textual property value. Not <code>null</code>.
	 */
	public final String value;

	/**
	 * The {@link SVNProperty} instance could be initialized only once because all fields are final
	 * 
	 * @param name
	 *            the property name
	 * @param value
	 *            the value of the textual property
	 */
	public SVNProperty(String name, String value) {
		this.name = name;
		this.value = value;
	}

}
