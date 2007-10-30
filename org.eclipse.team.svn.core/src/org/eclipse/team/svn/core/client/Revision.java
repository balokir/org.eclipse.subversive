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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Replacement for org.tigris.subversion.javahl.Revision
 * 
 * @author Alexander Gurov
 */
public class Revision {
    /**
     * kind of revision specified
     */
    protected int revKind;

    /**
     * Internally create a new revision
     * @param kind      kind of revision
     * @param marker    marker to differtiate from the public deprecated version
     */
    protected Revision(int kind, boolean marker) {
        if (kind < RevisionKind.unspecified || kind > RevisionKind.head) {
            throw new IllegalArgumentException(kind + " is not a legel revision kind");
        }
        this.revKind = kind;
    }

    /**
     * Returns the kind of the Revsion
     * @return kind
     */
    public int getKind() {
        return this.revKind;
    }

    /**
     * return the textual representation of the revision
     * @return english text
     */
    public String toString() {
        switch(this.revKind) {
            case Kind.base : return "BASE";
            case Kind.committed : return "COMMITTED";
            case Kind.head : return "HEAD";
            case Kind.previous : return "PREV";
            case Kind.working : return "WORKING";
        }
        return super.toString();
    }

    /**
     * compare to revision objects
     * @param target
     * @return if both object have equal content
     */
    public boolean equals(Object target) {
        if (this == target) {
            return true;
        }
        if (!(target instanceof Revision)) {
            return false;
        }

        return ((Revision)target).revKind == this.revKind;        
    }

    /**
     * Creates a Revision.Number object
     * @param revisionNumber    the revision number of the new object
     * @return  the new object
     * @throws IllegalArgumentException If the specified revision
     * number is invalid.
     */
    public static Revision getInstance(long revisionNumber) {
        return new Revision.Number(revisionNumber);
    }

    /**
     * Creates a Revision.DateSpec objet
     * @param revisionDate  the date of the new object
     * @return  the new object
     */
    public static Revision getInstance(Date revisionDate) {
        return new Revision.DateSpec(revisionDate);
    }

    /**
     * last commited revision
     */
    public static final Revision HEAD = new Revision(Kind.head, true);
    /**
     * first existing revision
     */
    public static final Revision START = new Revision(Kind.unspecified, true);
    /**
     * last committed revision, needs working copy
     */
    public static final Revision COMMITTED = new Revision(Kind.committed, true);
    /**
     * previous committed revision, needs working copy
     */
    public static final Revision PREVIOUS = new Revision(Kind.previous, true);
    /**
     * base revision of working copy
     */
    public static final Revision BASE = new Revision(Kind.base, true);
    /**
     * working version in working copy
     */
    public static final Revision WORKING = new Revision(Kind.working, true);
    /**
     * Marker revision number for no real revision
     */
    public static final int SVN_INVALID_REVNUM = -1;

    /**
     * class to specify a Revision by number
     */
    public static class Number extends Revision {
        /**
         * the revision number
         */
        protected long revNumber;

        /**
         * create a revision by number object
         * @param number the number
         * @throws IllegalArgumentException If the specified revision
         * number is invalid.
         */
        public Number(long number) {
            super(Kind.number, true);
            if(number < 0) {
                throw new IllegalArgumentException("negative revision numbers are not allowed");
            }
            this.revNumber = number;
        }

        /**
         * Returns the revision number
         * @return number
         */
        public long getNumber() {
            return this.revNumber;
        }

        /**
         * return the textual representation of the revision
         * @return english text
         */
        public String toString() {
            return Long.toString(this.revNumber);
        }

        /**
         * compare to revision objects
         * @param target
         * @return if both object have equal content
         */
        public boolean equals(Object target) {
            if (!super.equals(target)) {
                return false;
            }

            return ((Revision.Number)target).revNumber == this.revNumber;        
        }
    }

    /**
     * class to specify a revision by a date
     */
    public static class DateSpec extends Revision {
        /**
         * the date
         */
        protected Date revDate;

        /**
         * create a revision by date object
         * @param date
         */
        public DateSpec(Date date) {
            super(Kind.date, true);
            if (date == null) {
                throw new IllegalArgumentException("a date must be specified");
            }
            this.revDate = date;
        }
        /**
         * Returns the date of the revision
         * @return the date
         */
        public Date getDate() {
            return this.revDate;
        }

        /**
         * return the textual representation of the revision
         * @return english text
         */
        public String toString() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
            return '{' + dateFormat.format(this.revDate) + '}';
        }

        /**
         * compare to revision objects
         * @param target
         * @return if both object have equal content
         */
        public boolean equals(Object target) {
            if (!super.equals(target)) {
                return false;
            }
            return ((Revision.DateSpec)target).revDate.equals(this.revDate);        
        }
        
    }

    /**
     * Various ways of specifying revisions.
     *
     * Note:
     * In contexts where local mods are relevant, the `working' kind
     * refers to the uncommitted "working" revision, which may be modified
     * with respect to its base revision.  In other contexts, `working'
     * should behave the same as `committed' or `current'.
     *
     * the values are defined in RevisionKind because of building reasons
     */
    public static final class Kind implements RevisionKind {
    }
    
}
