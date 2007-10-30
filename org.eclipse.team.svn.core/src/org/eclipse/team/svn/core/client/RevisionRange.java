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
 * Replacement for org.tigris.subversion.javahl.RevisionRange
 * 
 * @author Alexander Gurov
 */
public class RevisionRange implements Comparable {
	public final Revision from;

	public final Revision to;

	/**
	 * Creates a new instance. Called by native library.
	 */
	public RevisionRange(long from, long to) {
		this.from = Revision.getInstance(from);
		this.to = Revision.getInstance(to);
	}

	public RevisionRange(Revision from, Revision to) {
		this.from = from;
		this.to = to;
	}

	/**
	 * Accepts a string in one of these forms: n m-n Parses the results into a
	 * from and to revision
	 * 
	 * @param revisionElement
	 *            revision range or single revision
	 */
	public RevisionRange(String revisionElement) {
		int hyphen = revisionElement.indexOf('-');
		if (hyphen > 0) {
			long fromRev = Long.parseLong(revisionElement.substring(0, hyphen));
			long toRev = Long.parseLong(revisionElement.substring(hyphen + 1));
			this.from = new Revision.Number(fromRev);
			this.to = new Revision.Number(toRev);
		}
		else {
			long revNum = Long.parseLong(revisionElement.trim());
			this.from = new Revision.Number(revNum);
			this.to = this.from;
		}
	}

	public String toString() {
		if (from != null && to != null) {
			if (from.equals(to)) {
				return from.toString();
			}
			else {
				return from.toString() + '-' + to.toString();
			}
		}
		return super.toString();
	}

	public static Long getRevisionAsLong(Revision rev) {
		long val = 0;
		if (rev != null && rev instanceof Revision.Number) {
			val = ((Revision.Number) rev).getNumber();
		}
		return new Long(val);
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		return result;
	}

	/**
	 * @param range
	 *            The RevisionRange to compare this object to.
	 */
	public boolean equals(Object range) {
		if (this == range) {
			return true;
		}
		if (!super.equals(range)) {
			return false;
		}
		if (getClass() != range.getClass()) {
			return false;
		}

		final RevisionRange other = (RevisionRange) range;

		if (from == null) {
			if (other.from != null) {
				return false;
			}
		}
		else if (!from.equals(other.from)) {
			return false;
		}

		if (to == null) {
			if (other.to != null) {
				return false;
			}
		}
		else if (!to.equals(other.to)) {
			return false;
		}

		return true;
	}

	/**
	 * @param range
	 *            The RevisionRange to compare this object to.
	 */
	public int compareTo(Object range) {
		if (this == range) {
			return 0;
		}

		Revision other = ((RevisionRange) range).from;
		return RevisionRange.getRevisionAsLong(this.from).compareTo(RevisionRange.getRevisionAsLong(other));
	}
}
