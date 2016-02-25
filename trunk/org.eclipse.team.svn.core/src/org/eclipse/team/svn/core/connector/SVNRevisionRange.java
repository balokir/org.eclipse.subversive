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

/**
 * The revision range container
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector library
 * is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to do this is
 * providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public class SVNRevisionRange {
	/**
	 * The "from" revision object
	 */
	public final SVNRevision from;

	/**
	 * The "to" revision object
	 */
	public final SVNRevision to;
	
	/**
	 * @since 1.9
	 */
	public final boolean inheritable;

	/**
	 * The {@link SVNRevisionRange} instance could be initialized only once because all fields are final
	 * 
	 * @param from
	 *            the "from" revision object. Greater or equals to zero.
	 * @param to
	 *            the "to" revision object. Greater or equals to zero.
	 * @throws IllegalArgumentException
	 *             if from or to contains negative value
	 */
	public SVNRevisionRange(long from, long to) {
		this(from, to, true);
	}

	/**
	 * The {@link SVNRevisionRange} instance could be initialized only once because all fields are final
	 * 
	 * @param from
	 *            the "from" revision object. Greater or equals to zero.
	 * @param to
	 *            the "to" revision object. Greater or equals to zero.
	 * @param inheritable
	 * @throws IllegalArgumentException
	 *             if from or to contains negative value
	 * @since 1.9
	 */
	public SVNRevisionRange(long from, long to, boolean inheritable) {
		this.from = SVNRevision.fromNumber(from);
		this.to = SVNRevision.fromNumber(to);
		this.inheritable = inheritable;
	}

	/**
	 * The {@link SVNRevisionRange} instance could be initialized only once because all fields are final
	 * 
	 * @param from
	 *            the "from" revision object. Cannot be <code>null</code>.
	 * @param to
	 *            the "to" revision object Cannot be <code>null</code>.
	 * @throws NullPointerException
	 *             if one of arguments (or both) is null
	 */
	public SVNRevisionRange(SVNRevision from, SVNRevision to) {
		this(from, to, true);
	}

	/**
	 * The {@link SVNRevisionRange} instance could be initialized only once because all fields are final
	 * 
	 * @param from
	 *            the "from" revision object. Cannot be <code>null</code>.
	 * @param to
	 *            the "to" revision object Cannot be <code>null</code>.
	 * @param inheritable
	 * @throws NullPointerException
	 *             if one of arguments (or both) is null
	 * @since 1.9
	 */
	public SVNRevisionRange(SVNRevision from, SVNRevision to, boolean inheritable) {
		if (from == null) {
			throw new NullPointerException("The \"from\" field cannot be initialized with null");
		}
		if (to == null) {
			throw new NullPointerException("The \"to\" field cannot be initialized with null");
		}
		this.from = from;
		this.to = to;
		this.inheritable = inheritable;
	}

	/**
	 * The {@link SVNRevisionRange} instance could be initialized only once because all fields are final
	 * 
	 * Accepts a string in one of these forms:
	 * 
	 * {revision} the "from" and "to" fields will be initialized with the same value
	 * 
	 * {revision}-{revision} the first revision will be set into "from" object and the second into the "to" object
	 * 
	 * @param revisionElement
	 *            revision range or single revision
	 * @throws NumberFormatException
	 *             if the string does not contain a parsable <code>long</code>.
	 */
	public SVNRevisionRange(String revisionElement) {
        this.inheritable = !revisionElement.endsWith("*");
        if (!this.inheritable) {
            revisionElement = revisionElement.substring(0, revisionElement.length() - 1);
        }

		int hyphen = revisionElement.indexOf('-');
		if (hyphen > 0) {
			this.from = SVNRevision.fromNumber(Long.parseLong(revisionElement.substring(0, hyphen)));
			this.to = SVNRevision.fromNumber(Long.parseLong(revisionElement.substring(hyphen + 1)));
		}
		else {
			long rev = Long.parseLong(revisionElement.trim());
			this.to = SVNRevision.fromNumber(rev);
			this.from = SVNRevision.fromNumber(rev - 1);
		}
	}

	public String toString() {
		if (this.from.equals(this.to) || 
			this.from.getKind() == SVNRevision.Kind.NUMBER && this.from.getKind() == this.to.getKind() && 
			((SVNRevision.Number)this.from).getNumber() == ((SVNRevision.Number)this.to).getNumber() - 1) {
			return this.from.toString() + (this.inheritable ? "" : "*");
		}
		return this.from.toString() + '-' + this.to.toString() + (this.inheritable ? "" : "*");
	}

	public int hashCode() {
		final int prime = 31;
		int result = this.inheritable ? 1 : 2;
		result = prime * result + this.from.hashCode();
		result = prime * result + this.to.hashCode();
		return result;
	}

	public boolean equals(Object range) {
		if (this == range) {
			return true;
		}
		if (!(range instanceof SVNRevisionRange)) {
			return false;
		}

		SVNRevisionRange other = (SVNRevisionRange) range;
		return this.from.equals(other.from) && this.to.equals(other.to) && this.inheritable == other.inheritable;
	}

}
