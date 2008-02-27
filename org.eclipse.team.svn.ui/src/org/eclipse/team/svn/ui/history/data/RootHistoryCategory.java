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

package org.eclipse.team.svn.ui.history.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Path;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNLogPath;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.history.ISVNHistoryViewInfo;
import org.eclipse.team.svn.ui.history.model.ILogNode;

/**
 * Root history category. Contains all history entries.
 * 
 * @author Alexander Gurov
 */
public class RootHistoryCategory extends HistoryCategory {
	public static String []NO_REMOTE;
	public static String []NO_LOCAL;
	public static String []NO_REVS;
	
	protected Object []allHistory;
	
	protected SVNLocalFileRevision []localHistory;
	protected SVNLogEntry []remoteHistory;
	
	protected HistoryCategory[] categoriesBoth;
	protected HistoryCategory[] categoriesRemote;
	protected HistoryCategory[] categoriesLocal;
	
	protected Map<Object, SVNChangedPathData []> pathData;
	protected Set<String> relatedPathsPrefixes;
	protected Set<String> relatedParents;
	
	protected ISVNHistoryViewInfo info;
	
	public RootHistoryCategory(ISVNHistoryViewInfo info) {
		super(HistoryCategory.CATEGORY_ROOT, null);
		if (RootHistoryCategory.NO_REMOTE == null) {
			RootHistoryCategory.NO_REMOTE = new String[] {SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.NoRemote")};
			RootHistoryCategory.NO_LOCAL = new String[] {SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.NoLocal")};
			RootHistoryCategory.NO_REVS = new String[] {SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.NoRevs")};
		}
		this.info = info;
	    this.pathData = new HashMap<Object, SVNChangedPathData []>();
	}
	
	public SVNLogEntry[] getRemoteHistory() {
		return this.remoteHistory;
	}
	
	public SVNLocalFileRevision[] getLocalHistory() {
		return this.localHistory;
	}

	public Object[] getEntries() {
		switch (this.info.getMode()) {
			case ISVNHistoryViewInfo.MODE_LOCAL: {
				return this.getLocalHistoryInternal();
			}
			case ISVNHistoryViewInfo.MODE_REMOTE: {
				return this.getRemoteHistoryInternal();
			}
		}
		return this.getAllHistoryInternal();
	}
	
	public Collection<String> getRelatedPathPrefixes() {
		return this.relatedPathsPrefixes;
	}
	
	public Collection<String> getRelatedParents() {
		return this.relatedParents;
	}
	
	public SVNChangedPathData []getPathData(ILogNode key) {
		return this.pathData.get(key == null ? null : key.getEntity());
	}
	
	public void refreshModel() {
		this.localHistory = this.info.getLocalHistory();
		this.remoteHistory = this.info.getRemoteHistory();
		if (this.localHistory == null) {
			this.allHistory = this.remoteHistory;
		}
		else if (this.remoteHistory == null) {
			this.allHistory = this.localHistory;
		}
		else {
			this.allHistory = new Object[this.localHistory.length + this.remoteHistory.length];
			System.arraycopy(this.localHistory, 0, this.allHistory, 0, this.localHistory.length);
			System.arraycopy(this.remoteHistory, 0, this.allHistory, this.localHistory.length, this.remoteHistory.length);
		}
		this.collectRelatedNodes();
		this.collectCategoriesAndMapData();
	}
	
	protected void collectRelatedNodes() {
		this.relatedPathsPrefixes = null;
		this.relatedParents = null;
		
		if (this.remoteHistory != null) {
			SVNLogPath []changes = null;
			// msg.changedPaths can be null or empty if user has no rights. So, find first accessible entry.
			for (SVNLogEntry msg : this.remoteHistory) {
				if (msg.changedPaths != null && msg.changedPaths.length > 0) {
					changes = msg.changedPaths;
					break;
				}
			}
			
			if (changes != null) {
				String baseUrl = this.info.getRepositoryResource().getUrl();
				String changePath = changes[0].path;
				int idx = -1;
				// find root trim point for the URL specified
				while (changePath.length() > 0 && (idx = baseUrl.indexOf(changePath)) == -1) {
					changePath = new Path(changePath).removeLastSegments(1).toString();
				}
				if (idx != -1 && idx < baseUrl.length()) {
					// cut root URL from related path
					String relatedPathsPrefix = baseUrl.substring(idx + 1, baseUrl.length());
					
				    this.relatedPathsPrefixes = new HashSet();
				    this.relatedParents = new HashSet();
				    
				    // collect copiedFrom entries
					for (SVNLogEntry msg : this.remoteHistory) {
					    this.relatedPathsPrefixes.add(relatedPathsPrefix);
					    if (msg.changedPaths != null && msg.changedPaths.length > 0) {
						    relatedPathsPrefix = this.getNextPrefix(msg, relatedPathsPrefix);
					    }
				    }
				}
			}
		}
	}
	
	protected String getNextPrefix(SVNLogEntry message, String current) {
		String checked = "/" + current;
		SVNLogPath []changes = message.changedPaths;
		
		for (int i = 0; i < changes.length; i++) {
			if (changes[i].copiedFromPath != null && checked.startsWith(changes[i].path)) {
				String rest = checked.substring(changes[i].path.length());
				String relatedParent = changes[i].copiedFromPath.substring(1);
				this.relatedParents.add(relatedParent);
				this.relatedParents.add(changes[i].path.substring(1));
				return relatedParent + rest;
			}
		}
		
		return current;
	}
	
	protected void collectCategoriesAndMapData() {
		this.pathData.clear();
		
		ArrayList<HistoryCategory> categoriesAll = new ArrayList <HistoryCategory>();
		ArrayList<HistoryCategory> categoriesLocal = new ArrayList <HistoryCategory>();
		ArrayList<HistoryCategory> categoriesRemote = new ArrayList <HistoryCategory>();
		
		ArrayList<Object> todayEntriesAll = new ArrayList<Object>();
		ArrayList<Object> yesterdayEntriesAll = new ArrayList<Object>();
		ArrayList<Object> weekEntriesAll = new ArrayList<Object>();
		ArrayList<Object> monthEntriesAll = new ArrayList<Object>();
		ArrayList<Object> earlierEntriesAll = new ArrayList<Object>();
		
		ArrayList<SVNLocalFileRevision> todayEntriesLocal = new ArrayList<SVNLocalFileRevision>();
		ArrayList<SVNLocalFileRevision> yesterdayEntriesLocal = new ArrayList<SVNLocalFileRevision>();
		ArrayList<SVNLocalFileRevision> weekEntriesLocal = new ArrayList<SVNLocalFileRevision>();
		ArrayList<SVNLocalFileRevision> monthEntriesLocal = new ArrayList<SVNLocalFileRevision>();
		ArrayList<SVNLocalFileRevision> earlierEntriesLocal = new ArrayList<SVNLocalFileRevision>();
		
		ArrayList<SVNLogEntry> todayEntriesRemote = new ArrayList<SVNLogEntry>();
		ArrayList<SVNLogEntry> yesterdayEntriesRemote = new ArrayList<SVNLogEntry>();
		ArrayList<SVNLogEntry> weekEntriesRemote = new ArrayList<SVNLogEntry>();
		ArrayList<SVNLogEntry> monthEntriesRemote = new ArrayList<SVNLogEntry>();
		ArrayList<SVNLogEntry> earlierEntriesRemote = new ArrayList<SVNLogEntry>();
		
		Calendar yesterdayCal = Calendar.getInstance();
		yesterdayCal.set(Calendar.HOUR_OF_DAY, 0);
		yesterdayCal.set(Calendar.MINUTE, 0);
		yesterdayCal.set(Calendar.SECOND, 0);
		long yesterdayDate = yesterdayCal.getTimeInMillis();
		Calendar beforeYesterdayCal = Calendar.getInstance();
		beforeYesterdayCal.roll(Calendar.DAY_OF_YEAR, -1);
		beforeYesterdayCal.set(Calendar.HOUR_OF_DAY, 0);
		beforeYesterdayCal.set(Calendar.MINUTE, 0);
		beforeYesterdayCal.set(Calendar.SECOND, 0);
		long beforeYesterdayDate = beforeYesterdayCal.getTimeInMillis();
		Calendar weekCal = Calendar.getInstance();
		weekCal.roll(Calendar.DAY_OF_YEAR, -7);
		weekCal.set(Calendar.HOUR_OF_DAY, 0);
		weekCal.set(Calendar.MINUTE, 0);
		weekCal.set(Calendar.SECOND, 0);
		long lastWeekDate = weekCal.getTimeInMillis();
		Calendar monthCal = Calendar.getInstance();
		monthCal.set(Calendar.DAY_OF_MONTH, 1);
		monthCal.set(Calendar.HOUR_OF_DAY, 0);
		monthCal.set(Calendar.MINUTE, 0);
		monthCal.set(Calendar.SECOND, 0);
		long lastMonthDate = monthCal.getTimeInMillis();
		
		//Filling timing ArrayLists
		if (this.remoteHistory != null) {
			for (int i = 0; i < this.remoteHistory.length; i++) {
				this.mapPathData(this.remoteHistory[i]);
				if (this.remoteHistory[i].date >= yesterdayDate) {
					todayEntriesAll.add(this.remoteHistory[i]);
					todayEntriesRemote.add(this.remoteHistory[i]);
				}
				else if (this.remoteHistory[i].date < yesterdayDate && this.remoteHistory[i].date >= beforeYesterdayDate) {
					yesterdayEntriesAll.add(this.remoteHistory[i]);
					yesterdayEntriesRemote.add(this.remoteHistory[i]);
				}
				else if (this.remoteHistory[i].date < beforeYesterdayDate && this.remoteHistory[i].date >= lastWeekDate) {
					weekEntriesAll.add(this.remoteHistory[i]);
					weekEntriesRemote.add(this.remoteHistory[i]);
				}
				else if (this.remoteHistory[i].date < lastWeekDate && this.remoteHistory[i].date >= lastMonthDate) {
					monthEntriesAll.add(this.remoteHistory[i]);
					monthEntriesRemote.add(this.remoteHistory[i]);
				}
				else {
					earlierEntriesAll.add(this.remoteHistory[i]);
					earlierEntriesRemote.add(this.remoteHistory[i]);
				}
			}
		}
		if (this.localHistory != null) {
			for (int i = 0; i < this.localHistory.length; i++) {
				if (this.localHistory[i].getTimestamp() >= yesterdayDate) {
					todayEntriesAll.add(this.localHistory[i]);
					todayEntriesLocal.add(this.localHistory[i]);
				}
				else if (this.localHistory[i].getTimestamp() < yesterdayDate && this.localHistory[i].getTimestamp() >= beforeYesterdayDate) {
					yesterdayEntriesAll.add(this.localHistory[i]);
					yesterdayEntriesLocal.add(this.localHistory[i]);
				}
				else if  (this.localHistory[i].getTimestamp() < beforeYesterdayDate && this.localHistory[i].getTimestamp() >= lastWeekDate) {
					weekEntriesAll.add(this.localHistory[i]);
					weekEntriesLocal.add(this.localHistory[i]);
				}
				else if  (this.localHistory[i].getTimestamp() < lastWeekDate && this.localHistory[i].getTimestamp() >= lastMonthDate) {
					monthEntriesAll.add(this.localHistory[i]);
					monthEntriesLocal.add(this.localHistory[i]);
				}
				else {
					earlierEntriesAll.add(this.localHistory[i]);
					earlierEntriesLocal.add(this.localHistory[i]);
				}
			}
		}
		HistoryCategory cat = null;

		//Fill both local and remote 
		if (todayEntriesAll.size() > 0) {
			cat = new HistoryCategory(HistoryCategory.CATEGORY_TODAY, todayEntriesAll.toArray());
			categoriesAll.add(cat);
		}
		if (yesterdayEntriesAll.size() > 0) {
			cat = new HistoryCategory(HistoryCategory.CATEGORY_YESTERDAY, yesterdayEntriesAll.toArray());
			categoriesAll.add(cat);
		}
		if (weekEntriesAll.size() > 0) {
			cat = new HistoryCategory(HistoryCategory.CATEGORY_THIS_WEEK, weekEntriesAll.toArray());
			categoriesAll.add(cat);
		}
		if (monthEntriesAll.size() > 0) {
			cat = new HistoryCategory(HistoryCategory.CATEGORY_THIS_MONTH, monthEntriesAll.toArray());
			categoriesAll.add(cat);
		}
		if (earlierEntriesAll.size() > 0) {
			cat = new HistoryCategory(HistoryCategory.CATEGORY_EARLIER, earlierEntriesAll.toArray());
			categoriesAll.add(cat);
		}
		
		//Fill local
		if (todayEntriesLocal.size() > 0) {
			cat = new HistoryCategory(HistoryCategory.CATEGORY_TODAY, todayEntriesLocal.toArray());
			categoriesLocal.add(cat);
		}
		if (yesterdayEntriesLocal.size() > 0) {
			cat = new HistoryCategory(HistoryCategory.CATEGORY_YESTERDAY, yesterdayEntriesLocal.toArray());
			categoriesLocal.add(cat);
		}
		if (weekEntriesLocal.size() > 0) {
			cat = new HistoryCategory(HistoryCategory.CATEGORY_THIS_WEEK, weekEntriesLocal.toArray());
			categoriesLocal.add(cat);
		}
		if (monthEntriesLocal.size() > 0) {
			cat = new HistoryCategory(HistoryCategory.CATEGORY_THIS_MONTH, monthEntriesLocal.toArray());
			categoriesLocal.add(cat);
		}
		if (earlierEntriesLocal.size() > 0) {
			cat = new HistoryCategory(HistoryCategory.CATEGORY_EARLIER, earlierEntriesLocal.toArray());
			categoriesLocal.add(cat);
		}
		
		//Fill remote
		if (todayEntriesRemote.size() > 0) {
			cat = new HistoryCategory(HistoryCategory.CATEGORY_TODAY, todayEntriesRemote.toArray());
			categoriesRemote.add(cat);
		}
		if (yesterdayEntriesRemote.size() > 0) {
			cat = new HistoryCategory(HistoryCategory.CATEGORY_YESTERDAY, yesterdayEntriesRemote.toArray());
			categoriesRemote.add(cat);
		}
		if (weekEntriesRemote.size() > 0) {
			cat = new HistoryCategory(HistoryCategory.CATEGORY_THIS_WEEK, weekEntriesRemote.toArray());
			categoriesRemote.add(cat);
		}
		if (monthEntriesRemote.size() > 0) {
			cat = new HistoryCategory(HistoryCategory.CATEGORY_THIS_MONTH, monthEntriesRemote.toArray());
			categoriesRemote.add(cat);
		}
		if (earlierEntriesRemote.size() > 0) {
			cat = new HistoryCategory(HistoryCategory.CATEGORY_EARLIER, earlierEntriesRemote.toArray());
			categoriesRemote.add(cat);
		}
		
		this.categoriesBoth = categoriesAll.size() > 0 ? categoriesAll.toArray(new HistoryCategory[categoriesAll.size()]) : null;
		this.categoriesLocal = categoriesLocal.size() > 0 ? categoriesLocal.toArray(new HistoryCategory[categoriesLocal.size()]) : null;
		this.categoriesRemote = categoriesRemote.size() > 0 ? categoriesRemote.toArray(new HistoryCategory[categoriesRemote.size()]) : null;
	}
	
	protected void mapPathData(SVNLogEntry key) {
		SVNChangedPathData [] pathData = new SVNChangedPathData[key.changedPaths == null ? 0 : key.changedPaths.length];
		for (int i = 0; i < pathData.length; i++) {
			String path = key.changedPaths[i].path;
			path = path.startsWith("/") ? path.substring(1) : path;
			int idx = path.lastIndexOf("/");
			pathData[i] = 
				new SVNChangedPathData (
					key.changedPaths[i].action, 
					idx != -1 ? path.substring(idx + 1) : path,
					idx != -1 ? path.substring(0, idx) : "",
					key.changedPaths[i].copiedFromRevision != SVNRevision.INVALID_REVISION_NUMBER ?  key.changedPaths[i].copiedFromPath : "",
					key.changedPaths[i].copiedFromRevision
				);
		}
		this.pathData.put(key, pathData);
	}
	
	protected Object []getLocalHistoryInternal() {
		if (this.localHistory == null) {
			return RootHistoryCategory.NO_LOCAL;
		}
		return this.info.isGrouped() ? this.categoriesLocal : this.localHistory;
	}
	
	protected Object []getRemoteHistoryInternal() {
		if (this.remoteHistory == null) {
			return RootHistoryCategory.NO_REMOTE;
		}
		return this.info.isGrouped() ? this.categoriesRemote : this.remoteHistory;
	}
	
	protected Object []getAllHistoryInternal() {
		if (this.allHistory == null) {
			return RootHistoryCategory.NO_REVS;
		}
		return this.info.isGrouped() && this.categoriesBoth != null ? this.categoriesBoth : this.allHistory;
	}
	
}
