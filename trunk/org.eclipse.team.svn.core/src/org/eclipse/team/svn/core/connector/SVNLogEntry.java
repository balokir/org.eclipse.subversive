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
 * LogEntry information container
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL connector library
 * is not EPL compatible and we won't to pin plug-in with concrete connector implementation. So, the only way to do this is
 * providing our own connector interface which will be covered by concrete connector implementation.
 * 
 * @author Alexander Gurov
 */
public class SVNLogEntry {
	/**
	 * The revision number. Zero or positive.
	 */
	public final long revision;

	/**
	 * The date of the commit. Could be zero if the user who requested the log has no access rights to the specified
	 * resource.
	 */
	public final long date;

	/**
	 * The commit author. Could be <code>null</code> if commit are performed with anonymous access.
	 */
	public final String author;

	/**
	 * The log message for the revision. Could be <code>null</code> if revision has no message.
	 */
	public final String message;

	/**
	 * The set of the items changed by this commit. Could be <code>null</code> when
	 * {@link ISVNConnector#logMessages} is called with discoverPaths set to false or if the user who requested the
	 * log has no access rights to the specified resource.
	 */
	public final SVNLogPath[] changedPaths;

	/**
	 * The {@link SVNLogEntry} instance could be initialized only once because all fields are final
	 * 
	 * @param revision
	 *            the revision number associated with the commit
	 * @param date
	 *            the date of the commit
	 * @param author
	 *            the commit author
	 * @param message
	 *            the commit message text
	 * @param changedPaths
	 *            the set of the items changed by this commit
	 */
	public SVNLogEntry(long revision, long date, String author, String message, SVNLogPath[] changedPaths) {
		this.message = message;
		this.date = date;
		this.revision = revision;
		this.author = author;
		this.changedPaths = changedPaths;
	}
	
	public int hashCode() {
		return (int)this.revision;
	}

	public boolean equals(Object obj) {
		if (obj instanceof SVNLogEntry) {
			return this.revision == ((SVNLogEntry)obj).revision;
		}
		return false;
	}
	
}
