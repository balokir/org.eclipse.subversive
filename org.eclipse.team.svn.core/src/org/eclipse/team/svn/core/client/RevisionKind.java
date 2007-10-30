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
 * Replacement for org.tigris.subversion.javahl.RevisionKind
 * 
 * @author Alexander Gurov
 */
public interface RevisionKind {
    /** No revision information given. */
    public static final int unspecified = 0;

    /** revision given as number */
    public static final int number = 1;

    /** revision given as date */
    public static final int date = 2;

    /** rev of most recent change */
    public static final int committed = 3;

    /** (rev of most recent change) - 1 */
    public static final int previous = 4;

    /** .svn/entries current revision */
    public static final int base = 5;

    /** current, plus local mods */
    public static final int working = 6;

    /** repository youngest */
    public static final int head = 7;

}
