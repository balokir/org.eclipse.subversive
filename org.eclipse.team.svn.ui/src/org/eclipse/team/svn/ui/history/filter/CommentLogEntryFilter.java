/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
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
 * Comment filter for log entry
 * 
 * @author Alexei Goncharov
 */
public class CommentLogEntryFilter implements ILogEntryFilter {
	
	protected String commentToAccept;
	
	public CommentLogEntryFilter() {
		this(null);
	}
	
	public CommentLogEntryFilter (String commentToAccept) {
		this.commentToAccept = commentToAccept;
	}

	public boolean accept(SVNLogEntry logEntry) {
		if (this.commentToAccept == null) {
			return true;
		}
		StringMatcher matcher = new StringMatcher(this.commentToAccept);
		String comment = logEntry.message == null ? "" : logEntry.message;
		return matcher.match(comment);
	}

	public void setCommentToAccept(String commentToAccept) {
		this.commentToAccept = commentToAccept;
	}
	
	public String getCommentToAccept() {
		return this.commentToAccept;
	}
}
