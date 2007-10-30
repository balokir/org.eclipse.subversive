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
 * Replacement for org.tigris.subversion.javahl.StatusKind
 * 
 * @author Alexander Gurov
 */
public interface StatusKind {
    /** does not exist */
    public static final int none = 0;

    /** exists, but uninteresting */
    public static final int normal = 1;

    /** text or props have been modified */
    public static final int modified = 2;

    /** is scheduled for additon */
    public static final int added = 3;

    /** scheduled for deletion */
    public static final int deleted = 4;

    /** is not a versioned thing in this wc */
    public static final int unversioned = 5;

    /** under v.c., but is missing */
    public static final int missing = 6;

    /** was deleted and then re-added */
    public static final int replaced = 7;

    /** local mods received repos mods */
    public static final int merged = 8;

    /** local mods received conflicting repos mods */
    public static final int conflicted = 9;

    /** an unversioned resource is in the way of the versioned resource */
    public static final int obstructed = 10;

    /** a resource marked as ignored */
    public static final int ignored = 11;

    /** a directory doesn't contain a complete entries list */
    public static final int incomplete = 12;

    /** an unversioned path populated by an svn:externals property */
    public static final int external = 13;

}
