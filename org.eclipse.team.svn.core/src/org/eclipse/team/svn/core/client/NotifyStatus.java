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
 * Notify statuses enumeration
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL client library
 * is not EPL compatible and we won't to pin plug-in with concrete client implementation. So, the only way to do this is
 * providing our own client interface which will be covered by concrete client implementation.
 * 
 * @author Alexander Gurov
 */
public class NotifyStatus {
	/**
	 * Not applicable
	 */
	public static final int INAPPLICABLE = 0;

	/**
	 * Notifier doesn't know or isn't saying.
	 */
	public static final int UNKNOWN = 1;

	/**
	 * The state did not change.
	 */
	public static final int UNCHANGED = 2;

	/**
	 * The item wasn't present.
	 */
	public static final int MISSING = 3;

	/**
	 * An unversioned item obstructed work.
	 */
	public static final int OBSTRUCTED = 4;

	/**
	 * Base version was modified.
	 */
	public static final int CHANGED = 5;

	/**
	 * Modified state had mods merged in.
	 */
	public static final int MERGED = 6;

	/**
	 * Modified state got conflicting mods.
	 */
	public static final int CONFLICTED = 7;

	/**
	 * The textual representation for the status types
	 */
	public static final String[] statusNames = { "inapplicable", "unknown", "unchanged", "missing", "obstructed", "changed", "merged", "conflicted", };

	/**
	 * The short textual representation for the status types
	 */
	public static final String[] shortStatusNames = { " ", " ", " ", "?", "O", "U", "G", "C", };

}
