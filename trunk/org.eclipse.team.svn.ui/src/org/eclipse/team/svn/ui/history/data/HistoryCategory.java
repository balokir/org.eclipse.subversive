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

package org.eclipse.team.svn.ui.history.data;

import org.eclipse.team.svn.ui.SVNUIMessages;

/**
 * History View entries category
 * 
 * @author Alexander Gurov
 */
public class HistoryCategory {
	public static final int CATEGORY_ROOT = 0;
	
	public static final int CATEGORY_TODAY = 1;
	public static final int CATEGORY_YESTERDAY = 2;
	public static final int CATEGORY_THIS_WEEK = 3;
	public static final int CATEGORY_THIS_MONTH = 4;
	public static final int CATEGORY_EARLIER = 5;
	
	protected int categoryType;
	protected Object[] entries;
	
	public HistoryCategory(int categoryType, Object[] entries) {
		this.entries = entries;
		this.categoryType = categoryType;
	}
	
	public String getName() {
		switch (this.categoryType) {
			case HistoryCategory.CATEGORY_TODAY: return SVNUIMessages.LogMessagesComposite_Group_Today;
			case HistoryCategory.CATEGORY_YESTERDAY: return SVNUIMessages.LogMessagesComposite_Group_Yesterday;
			case HistoryCategory.CATEGORY_THIS_WEEK : return SVNUIMessages.LogMessagesComposite_Group_Week;
			case HistoryCategory.CATEGORY_THIS_MONTH : return SVNUIMessages.LogMessagesComposite_Group_Month;
			case HistoryCategory.CATEGORY_EARLIER : return SVNUIMessages.LogMessagesComposite_Group_Earlier;
		}
		return "";
	}
	
	public Object[] getEntries() {
		return this.entries;
	}
	
	public int hashCode() {
		return this.categoryType;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof HistoryCategory) {
			return this.categoryType == ((HistoryCategory)obj).categoryType;
		}
		return false;
	}
	
}