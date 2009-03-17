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

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.util.ULocale;

import java.text.ParseException;

/**
 * Revision information container
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector library
 * is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to do this is
 * providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public class SVNRevision {
	/**
	 * The revision definition ways
	 */
	public static final class Kind {
		/**
		 * First existing revision
		 */
		public static final int START = 0;

		/**
		 * Number-based revision
		 */
		public static final int NUMBER = 1;

		/**
		 * Date-based revision
		 */
		public static final int DATE = 2;

		/**
		 * Last committed revision
		 */
		public static final int COMMITTED = 3;

		/**
		 * The revision before last committed
		 */
		public static final int PREVIOUS = 4;

		/**
		 * The working copy base revision
		 */
		public static final int BASE = 5;

		/**
		 * The working copy working revision
		 */
		public static final int WORKING = 6;

		/**
		 * The latest repository revision
		 */
		public static final int HEAD = 7;
	}

	/**
	 * Last committed revision
	 */
	public static final SVNRevision HEAD = new SVNRevision(Kind.HEAD);

	/**
	 * First existing revision
	 */
	public static final SVNRevision START = new SVNRevision(Kind.START);

	/**
	 * Last committed revision, needs working copy
	 */
	public static final SVNRevision COMMITTED = new SVNRevision(Kind.COMMITTED);

	/**
	 * Previous committed revision, needs working copy
	 */
	public static final SVNRevision PREVIOUS = new SVNRevision(Kind.PREVIOUS);

	/**
	 * Base revision of working copy
	 */
	public static final SVNRevision BASE = new SVNRevision(Kind.BASE);

	/**
	 * Working version in working copy
	 */
	public static final SVNRevision WORKING = new SVNRevision(Kind.WORKING);

	/**
	 * Invalid revision number
	 */
	public static final int INVALID_REVISION_NUMBER = -1;

	/**
	 * Invalid revision object
	 */
	public static final SVNRevision.Number INVALID_REVISION = new SVNRevision.Number(INVALID_REVISION_NUMBER);

	/**
	 * Number-based revision
	 */
	public static class Number extends SVNRevision {
		protected long revNumber;

		/**
		 * Returns the revision number
		 * 
		 * @return number
		 */
		public long getNumber() {
			return this.revNumber;
		}

		public String toString() {
			return String.valueOf(this.revNumber);
		}

		public int hashCode() {
			int result = 31;
			result += this.revKind;
			result = 31 * result + (int) this.revNumber;
			result = 31 * result + (int) (this.revNumber >> 32);
			return result;
		}

		public boolean equals(Object target) {
			return super.equals(target) && ((SVNRevision.Number) target).revNumber == this.revNumber;
		}

		protected Number(long number) {
			super(Kind.NUMBER);
			this.revNumber = number;
		}

	}

	/**
	 * Date-based revision
	 */
	public static class Date extends SVNRevision {
		protected long revDate;

		/**
		 * Returns the date of the revision
		 * 
		 * @return the date
		 */
		public long getDate() {
			return this.revDate;
		}

		public String toString() {
			DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, ULocale.getDefault());
			return dateTimeFormat.format(new java.util.Date(this.revDate));
		}

		public int hashCode() {
			int result = 31;
			result += this.revKind;
			result = 31 * result + (int) this.revDate;
			result = 31 * result + (int) (this.revDate >> 32);
			return result;
		}

		public boolean equals(Object target) {
			return super.equals(target) && ((SVNRevision.Date) target).revDate == this.revDate;
		}

		protected Date(long date) {
			super(Kind.DATE);
			this.revDate = date;
		}

	}

	protected int revKind;

	/**
	 * Creates revision object by revision kind
	 * 
	 * @param kind
	 *            revision kind
	 * @return revision object
	 * @throws IllegalArgumentException
	 *             if kind is {@link Kind#DATE}, {@link Kind#NUMBER} or exceeds kind limits
	 */
	public static SVNRevision fromKind(int kind) {
		switch (kind) {
			case Kind.BASE: {
				return SVNRevision.BASE;
			}
			case Kind.WORKING: {
				return SVNRevision.WORKING;
			}
			case Kind.HEAD: {
				return SVNRevision.HEAD;
			}
			case Kind.PREVIOUS: {
				return SVNRevision.PREVIOUS;
			}
			case Kind.START: {
				return SVNRevision.START;
			}
			case Kind.COMMITTED: {
				return SVNRevision.COMMITTED;
			}
			case Kind.DATE: {
				throw new IllegalArgumentException("Use fromDate() method instead");
			}
			case Kind.NUMBER: {
				throw new IllegalArgumentException("Use fromNumber() method instead");
			}
		}
		throw new IllegalArgumentException("Invalid revision kind: " + kind);
	}

	/**
	 * Creates revision object by revision number
	 * 
	 * @param revisionNumber
	 *            revision number
	 * @return revision object
	 * @throws IllegalArgumentException
	 *             if revisionNumber is negative
	 */
	public static SVNRevision.Number fromNumber(long revisionNumber) {
		if (revisionNumber < 0) {
			throw new IllegalArgumentException("Negative revision numbers are not allowed");
		}
		return new SVNRevision.Number(revisionNumber);
	}

	/**
	 * Creates revision object by revision date
	 * 
	 * @param revisionDate
	 *            revision date in milliseconds
	 * @return revision object
	 * @throws IllegalArgumentException
	 *             if revisionDate is zero
	 */
	public static SVNRevision.Date fromDate(long revisionDate) {
		if (revisionDate == -1) {
			throw new IllegalArgumentException("A date must be specified");
		}
		return new SVNRevision.Date(revisionDate);
	}
	
	/**
	 * Creates revision object by revision string
	 * 
	 * @param revisionString
	 *            string representation of one of valid revisions or revision kinds
	 * @return revision object
	 * @throws IllegalArgumentException
	 *             if invalid revision kind or revision value specified
	 */
	public static SVNRevision fromString(String revisionString) {
		revisionString = revisionString.toUpperCase();
		if ("BASE".equals(revisionString)) { //$NON-NLS-1$
			return SVNRevision.BASE;
		}
		if ("WORKING".equals(revisionString)) { //$NON-NLS-1$
			return SVNRevision.WORKING;
		}
		if ("COMMITTED".equals(revisionString)) { //$NON-NLS-1$
			return SVNRevision.COMMITTED;
		}
		if ("HEAD".equals(revisionString)) { //$NON-NLS-1$
			return SVNRevision.HEAD;
		}
		if ("PREVIOUS".equals(revisionString)) { //$NON-NLS-1$
			return SVNRevision.PREVIOUS;
		}
		try {
			return SVNRevision.fromNumber(Long.parseLong(revisionString));
		}
		catch (NumberFormatException ex) {
			// check if revision specified as date (always locale-specific)
			DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, ULocale.getDefault());
			try {
				return SVNRevision.fromDate(dateTimeFormat.parse(revisionString).getTime());
			}
			catch (ParseException e) {
				// do nothing
			}
		}
		throw new IllegalArgumentException("Invalid revision string: " + revisionString);
	}

	/**
	 * Returns the kind of the revision object
	 * 
	 * @return the revision kind
	 */
	public int getKind() {
		return this.revKind;
	}

	public String toString() {
		switch (this.revKind) {
			case Kind.BASE: {
				return "BASE"; //$NON-NLS-1$
			}
			case Kind.COMMITTED: {
				return "COMMITTED"; //$NON-NLS-1$
			}
			case Kind.HEAD: {
				return "HEAD"; //$NON-NLS-1$
			}
			case Kind.PREVIOUS: {
				return "PREV"; //$NON-NLS-1$
			}
			case Kind.WORKING: {
				return "WORKING"; //$NON-NLS-1$
			}
		}
		return "UNSPECIFIED"; //$NON-NLS-1$
	}

	public int hashCode() {
		return this.revKind;
	}

	public boolean equals(Object target) {
		if (this == target) {
			return true;
		}
		if (!this.getClass().equals(target.getClass())) {
			return false;
		}

		return ((SVNRevision) target).revKind == this.revKind;
	}

	protected SVNRevision(int kind) {
		this.revKind = kind;
	}

}
