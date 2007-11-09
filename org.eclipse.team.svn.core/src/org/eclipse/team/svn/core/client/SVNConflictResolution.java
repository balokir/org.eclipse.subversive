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
 * The conflict resolution method description
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL client library
 * is not EPL compatible and we won't to pin plug-in with concrete client implementation. So, the only way to do this is
 * providing our own client interface which will be covered by concrete client implementation.
 * 
 * @author Alexander Gurov
 */
public class SVNConflictResolution {
	/**
	 * Nothing done to resolve the conflict; conflict remains.
	 */
	public static final int POSTPONE = 0;

	/**
	 * Resolve the conflict by choosing the base version.
	 */
	public static final int CHOOSE_BASE = 1;

	/**
	 * Resolve the conflict by choosing the repository version.
	 */
	public static final int CHOOSE_REMOTE = 2;

	/**
	 * Resolve the conflict by choosing local version.
	 */
	public static final int CHOOSE_LOCAL = 3;

	/**
	 * Resolve the conflict by choosing the [manually] merged version.
	 */
	public static final int CHOOSE_MERGED = 4;

	/**
	 * The acceptable choice.
	 */
	public final int choice;

	/**
	 * The path to the result of a merge. Could be <code>null</code>.
	 */
	public final String mergedPath;

	/**
	 * The {@link SVNConflictResolution} instance could be initialized only once because all fields are final
	 * 
	 * @param choice
	 *            the acceptable choice
	 * @param mergedPath
	 *            the path to the result of merge
	 */
	public SVNConflictResolution(int choice, String mergedPath) {
		this.choice = choice;
		this.mergedPath = mergedPath;
	}

}
