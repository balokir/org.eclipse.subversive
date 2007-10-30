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
 * Replacement for org.tigris.subversion.javahl.ConflictResult
 * 
 * @since 1.5
 */
public class ConflictResult {
	/**
	 * Nothing done to resolve the conflict; conflict remains.
	 */
	public static final int postpone = 0;

	/**
	 * Resolve the conflict by choosing the base file.
	 */
	public static final int chooseBase = 1;

	/**
	 * Resolve the conflict by choosing the incoming (repository) version of the
	 * object.
	 */
	public static final int chooseTheirs = 2;

	/**
	 * Resolve the conflict by choosing own (local) version of the object.
	 */
	public static final int chooseMine = 3;

	/**
	 * Resolve the conflict by choosing the merged object (potentially manually
	 * edited).
	 */
	public static final int chooseMerged = 4;

	/**
	 * A value corresponding to the <code>svn_wc_conflict_choice_t</code>
	 * enum.
	 */
	public final int choice;

	/**
	 * The path to the result of a merge, or <code>null</code>.
	 */
	public final String mergedPath;

	/**
	 * Create a new conflict result instace.
	 */
	public ConflictResult(int choice, String mergedPath) {
		this.choice = choice;
		this.mergedPath = mergedPath;
	}

}
