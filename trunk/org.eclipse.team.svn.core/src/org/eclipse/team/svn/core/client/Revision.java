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

import java.text.DateFormat;
import java.util.Locale;

/**
 * Revision information container
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL client library
 * is not EPL compatible and we won't to pin plug-in with concrete client implementation. So, the only way to do this is
 * providing our own client interface which will be covered by concrete client implementation.
 * 
 * @author Alexander Gurov
 */
public class Revision {
	/**
	 * The revision definition ways
	 */
	public static final class Kind {
		/**
		 * First existing revision
		 */
		public static final int UNSPECIFIED = 0;

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
	public static final Revision HEAD = new Revision(Kind.HEAD);

	/**
	 * First existing revision
	 */
	public static final Revision START = new Revision(Kind.UNSPECIFIED);

	/**
	 * Last committed revision, needs working copy
	 */
	public static final Revision COMMITTED = new Revision(Kind.COMMITTED);

	/**
	 * Previous committed revision, needs working copy
	 */
	public static final Revision PREVIOUS = new Revision(Kind.PREVIOUS);

	/**
	 * Base revision of working copy
	 */
	public static final Revision BASE = new Revision(Kind.BASE);

	/**
	 * Working version in working copy
	 */
	public static final Revision WORKING = new Revision(Kind.WORKING);

	/**
	 * Invalid revision number
	 */
	public static final int INVALID_REVISION_NUMBER = -1;

	/**
	 * Invalid revision object
	 */
	public static final Revision.Number INVALID_REVISION = new Revision.Number(INVALID_REVISION_NUMBER);

	/**
	 * Number-based revision
	 */
	public static class Number extends Revision {
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
			return super.equals(target) && ((Revision.Number) target).revNumber == this.revNumber;
		}

		protected Number(long number) {
			super(Kind.NUMBER);
			this.revNumber = number;
		}

	}

	/**
	 * Date-based revision
	 */
	public static class Date extends Revision {
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
			DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());
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
			return super.equals(target) && ((Revision.Date) target).revDate == this.revDate;
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
	public static Revision fromKind(int kind) {
		switch (kind) {
		case Kind.BASE: {
			return Revision.BASE;
		}
		case Kind.WORKING: {
			return Revision.WORKING;
		}
		case Kind.HEAD: {
			return Revision.HEAD;
		}
		case Kind.PREVIOUS: {
			return Revision.PREVIOUS;
		}
		case Kind.UNSPECIFIED: {
			return Revision.START;
		}
		case Kind.COMMITTED: {
			return Revision.COMMITTED;
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
	public static Revision.Number fromNumber(long revisionNumber) {
		if (revisionNumber < 0) {
			throw new IllegalArgumentException("Negative revision numbers are not allowed");
		}
		return new Revision.Number(revisionNumber);
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
	public static Revision.Date fromDate(long revisionDate) {
		if (revisionDate == 0) {
			throw new IllegalArgumentException("A date must be specified");
		}
		return new Revision.Date(revisionDate);
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
			return "BASE";
		}
		case Kind.COMMITTED: {
			return "COMMITTED";
		}
		case Kind.HEAD: {
			return "HEAD";
		}
		case Kind.PREVIOUS: {
			return "PREV";
		}
		case Kind.WORKING: {
			return "WORKING";
		}
		}
		// Kind.UNSPECIFIED
		return "UNSPECIFIED";
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

		return ((Revision) target).revKind == this.revKind;
	}

	protected Revision(int kind) {
		this.revKind = kind;
	}

}
