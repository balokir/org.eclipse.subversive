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

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.team.svn.core.connector.SVNLogEntry;

/**
 * Composite log entries filter.
 * 
 * @author Alexei Goncharov
 */
public class CompositeLogEntryFilter implements ILogEntryFilter {

	protected HashSet<ILogEntryFilter> filtersSet;
	
	public CompositeLogEntryFilter() {
		this.filtersSet = new HashSet<ILogEntryFilter>();
	}
	
	public CompositeLogEntryFilter(ILogEntryFilter [] filters) {
		this.filtersSet = new HashSet<ILogEntryFilter>(Arrays.asList(filters));
	}
	
	public boolean accept(SVNLogEntry logEntry) {
		for (ILogEntryFilter current : this.filtersSet) {
			if (!current.accept(logEntry)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Adds a filter to filters set
	 */
	public void addFilter(ILogEntryFilter filter) {
		this.filtersSet.add(filter);
	}
	
	/**
	 * Removes a filter from filters set
	 */
	public void removeFilter(ILogEntryFilter filter) {
		this.filtersSet.remove(filter);
	}
}
