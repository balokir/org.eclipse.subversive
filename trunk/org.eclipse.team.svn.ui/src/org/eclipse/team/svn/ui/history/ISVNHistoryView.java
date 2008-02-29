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

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.ui.history.HistoryPage;

/**
 * Allow to manage history view
 * 
 * @author Alexander Gurov
 */
public interface ISVNHistoryView extends ISVNHistoryViewInfo {
	public static final int REFRESH_VIEW = 0;
	public static final int REFRESH_LOCAL = 1;
	public static final int REFRESH_REMOTE = 2;
	public static final int REFRESH_ALL = 3;
	
	public static final int PAGING_ENABLED = 0x01;
	public static final int COMPARE_MODE = 0x02;
	public static final int HIDE_UNRELATED = 0x04;
	public static final int STOP_ON_COPY = 0x08;
	public static final int GROUP_BY_DATE = 0x10;
	/* 0x20, 0x40 and 0x80 are reserved for LogMessagesComposite */
	
	public HistoryPage getHistoryPage();
	public IResource getCompareWith();
	public SVNLogEntry []getFullRemoteHistory();
	public boolean isAllRemoteHistoryFetched();
	public void clearFilter();
	public void setFilter();
	public void refresh(int refreshType);
	public boolean isFilterEnabled();
	public int getOptions();
}