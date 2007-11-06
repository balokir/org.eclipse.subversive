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
 * Repository or working copy traversal depth
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL client library
 * is not EPL compatible and we won't to pin plug-in with concrete client implementation. So, the only way to do this is
 * providing our own client interface which will be covered by concrete client implementation.
 * 
 * @author Alexander Gurov
 */
public final class Depth {
	/**
	 * Depth undetermined or ignored.
	 */
	public static final int UNKNOWN = -2;

	/**
	 * Exclude (i.e, don't descend into) directory D.
	 */
	public static final int EXCLUDE = -1;

	/**
	 * Just the named file or folder without entries.
	 */
	public static final int EMPTY = 0;

	/**
	 * The folder and child files.
	 */
	public static final int FILES = 1;

	/**
	 * The folder and all direct child entries.
	 */
	public static final int IMMEDIATES = 2;

	/**
	 * The folder and all descendants at any depth.
	 */
	public static final int INFINITY = 3;

	public static final int infinityOrEmpty(boolean recurse) {
		return (recurse ? Depth.INFINITY : Depth.EMPTY);
	}

	public static final int infinityOrFiles(boolean recurse) {
		return (recurse ? Depth.INFINITY : Depth.FILES);
	}

	public static final int infinityOrImmediates(boolean recurse) {
		return (recurse ? Depth.INFINITY : Depth.IMMEDIATES);
	}

}
