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
 * Interface of an ILogMessage filter.
 * Filters to be added to {@link CompositeLogEntryFilter}
 * must implement this interface.
 * 
 * @author Alexei Goncharov
 */
public interface ILogEntryFilter {
	
	/**
	 * Returns if the filter accepts the given log node.
	 * 
	 * @param logEntry - log entry to check
	 * @return - true if accepted, otherwise false
	 */
	boolean accept(SVNLogEntry logEntry);
}