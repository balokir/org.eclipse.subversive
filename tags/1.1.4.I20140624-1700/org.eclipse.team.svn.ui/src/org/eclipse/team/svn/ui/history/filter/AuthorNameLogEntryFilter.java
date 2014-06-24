/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.history.filter;

import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.utility.StringMatcher;

/**
 * Author filter for log entry
 * 
 * @author Alexei Goncharov
 */
public class AuthorNameLogEntryFilter implements ILogEntryFilter {
	
	protected String authorNameToAccept;
	
	public AuthorNameLogEntryFilter() {
		this(null);
	}
	
	public AuthorNameLogEntryFilter (String authorNameToAccept) {
		this.authorNameToAccept = authorNameToAccept;
	}

	public boolean accept(SVNLogEntry logEntry) {
		if (this.authorNameToAccept == null) {
			return true;
		}
		StringMatcher matcher = new StringMatcher(this.authorNameToAccept);
		String authorName = logEntry.author == null ? "" : logEntry.author; //$NON-NLS-1$
		return matcher.match(authorName);
	}

	public void setAuthorNameToAccept(String authorNameToAccept) {
		this.authorNameToAccept = authorNameToAccept;
	}
	
	public String getAuthorNameToAccept() {
		return this.authorNameToAccept;
	}
}
