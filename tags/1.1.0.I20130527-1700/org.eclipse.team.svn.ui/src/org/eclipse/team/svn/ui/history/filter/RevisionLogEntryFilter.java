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

/**
 * Log entry filter to remove revisions from view
 * 
 * @author Alexei Goncharov
 */
public class RevisionLogEntryFilter implements ILogEntryFilter {
	
	protected long from;
	protected long to;
	
	public boolean accept(SVNLogEntry logEntry) {
		long current = logEntry.revision;
		return (current > this.to || current < this.from);
	}
	
	/**
	 * Sets revisions range to exclude
	 * 
	 * @param from - lower revision
	 * @param to - higher revision
	 */
	public void setRevisionstoHide(long from, long to) {
		this.from = from;
		this.to = to;
	}

}
