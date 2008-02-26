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

package org.eclipse.team.svn.ui.history;

import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.ui.history.HistoryPage;

/**
 * Allow to manage history view
 * 
 * @author Alexander Gurov
 */
public interface ISVNHistoryView extends ISVNHistoryViewInfo {
	public HistoryPage getHistoryPage();
	public ILocalResource getCompareWith();
	public SVNLogEntry []getFullRemoteHistory();
	public boolean isAllRemoteHistoryFetched();
	public void clearFilter();
	public void setFilter();
	public void refresh();
	public boolean isFilterEnabled();
	public int getOptions();
}