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
import org.eclipse.team.svn.core.connector.SVNLogPath;
import org.eclipse.team.svn.core.utility.StringMatcher;

/**
 * Changed path log entries filter 
 * 
 * @author Alexei Goncharov
 */
public class ChangeNameLogEntryFilter implements ILogEntryFilter {

	protected String changedPathToAccept;

	public ChangeNameLogEntryFilter() {
		this(null);
	}
	
	public ChangeNameLogEntryFilter(String changedPathToAccept) {
		this.changedPathToAccept = changedPathToAccept;
	}
	
	public boolean accept(SVNLogEntry logEntry) {
		if (this.changedPathToAccept == null) {
			return true;
		}
		StringMatcher matcher = new StringMatcher(this.changedPathToAccept);
		SVNLogPath [] paths = logEntry.changedPaths == null ? new SVNLogPath[0] : logEntry.changedPaths;
		for (int i = 0; i < paths.length; i++) {
			if (matcher.match(paths[i].path)) {
				return true;
			}
		}
		return false;
	}

	public void setGangedPathToAccept(String changedPathToAccept) {
		this.changedPathToAccept = changedPathToAccept;
	}
	
	public String getGangedPathToAccept() {
		return this.changedPathToAccept;
	}
	
}