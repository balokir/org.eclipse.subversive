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
 * Replacement for org.tigris.subversion.javahl.Depth
 * 
 * @author Alexander Gurov
 */
public final class Depth {
	/*
	 * The order of these depths is important: the higher the number, the deeper
	 * it descends. This allows us to compare two depths numerically to decide
	 * which should govern.
	 */

	/** Depth undetermined or ignored. */
	public static final int unknown = -2;

	/** Exclude (i.e, don't descend into) directory D. */
	public static final int exclude = -1;

	/**
	 * Just the named directory D, no entries. Updates will not pull in any
	 * files or subdirectories not already present.
	 */
	public static final int empty = 0;

	/**
	 * D + its file children, but not subdirs. Updates will pull in any files
	 * not already present, but not subdirectories.
	 */
	public static final int files = 1;

	/**
	 * D + immediate children (D and its entries). Updates will pull in any
	 * files or subdirectories not already present; those subdirectories'
	 * this_dir entries will have depth-empty.
	 */
	public static final int immediates = 2;

	/**
	 * D + all descendants (full recursion from D). Updates will pull in any
	 * files or subdirectories not already present; those subdirectories'
	 * this_dir entries will have depth-infinity. Equivalent to the pre-1.5
	 * default update behavior.
	 */
	public static final int infinity = 3;

    /**
     * @return A depth value of {@link #infinity} when
     * <code>recurse</code> is <code>true</code>, or {@link #empty}
     * otherwise.
     */
    public static final int infinityOrEmpty(boolean recurse)
    {
        return (recurse ? infinity : empty);
    }

    /**
     * @return A depth value of {@link #infinity} when
     * <code>recurse</code> is <code>true</code>, or {@link #files}
     * otherwise.
     */
    public static final int infinityOrFiles(boolean recurse)
    {
        return (recurse ? infinity : files);
    }

    /**
     * @return A depth value of {@link #infinity} when
     * <code>recurse</code> is <code>true</code>, or {@link
     * #immediates} otherwise.
     */
    public static final int infinityOrImmediates(boolean recurse)
    {
        return (recurse ? infinity : immediates);
    }

    /**
     * @return A depth value of {@link #unknown} when
     * <code>recurse</code> is <code>true</code>, or {@link #empty}
     * otherwise.
     */
    public static final int unknownOrEmpty(boolean recurse)
    {
        return (recurse ? unknown : empty);
    }

    /**
     * @return A depth value of {@link #unknown} when
     * <code>recurse</code> is <code>true</code>, or {@link #files}
     * otherwise.
     */
    public static final int unknownOrFiles(boolean recurse)
    {
        return (recurse ? unknown : files);
    }

    /**
     * @return A depth value of {@link #unknown} when
     * <code>recurse</code> is <code>true</code>, or {@link
     * #immediates} otherwise.
     */
    public static final int unknownOrImmediates(boolean recurse)
    {
        return (recurse ? unknown : immediates);
    }
    
}
