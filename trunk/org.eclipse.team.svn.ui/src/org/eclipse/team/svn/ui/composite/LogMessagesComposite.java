/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Gabor Liptak - Speedup Pattern's usage
 *******************************************************************************/

package org.eclipse.team.svn.ui.composite;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNLogPath;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevision.Kind;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.PatternProvider;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.extension.factory.ICommentView;
import org.eclipse.team.svn.ui.history.SVNLocalFileRevision;
import org.eclipse.team.svn.ui.utility.TableViewerSorter;

/**
 * LogMessage's viewer implementation
 * 
 * @author Alexander Gurov
 */
public class LogMessagesComposite extends SashForm {
	final public static int COLUMN_REVISION = 0;
	final public static int COLUMN_DATE = 1;
	final public static int COLUMN_CHANGES = 2;
	final public static int COLUMN_AUTHOR = 3;
	final public static int COLUMN_LOG_MESSGE = 4;
	
	public static final int SHOW_BOTH = 0x20;
	public static final int SHOW_LOCAL = 0x40;
	public static final int SHOW_REMOTE = 0x80;
	
    protected SashForm innerSashForm;
    protected boolean commentVisible;
    protected boolean affectedVisible;
    protected boolean hierarchicalAffected;
    protected boolean groupByDate;
    protected int revisionMode;
    
	protected TreeViewer historyTable;
	protected ISelectionChangedListener historyTableListener;
	protected long currentRevision;
	protected Font currentRevisionFont;
	protected Map pathData;
	protected boolean showRelatedPathsOnly;
	protected Set relatedPathsPrefixes;
	protected Set relatedParents;
	
	protected AffectedPathsComposite affectedPathsComposite;
	protected ICommentView viewManager;
	
	protected IRepositoryResource repositoryResource;
	
	protected SVNLogEntry []msgs = new SVNLogEntry[0];
	protected SVNLocalFileRevision [] localHistory = new SVNLocalFileRevision[0];
	protected Object [] allHystory = new Object[0];
	protected HistoryCategory[] categoriesBoth = new HistoryCategory[0];
	protected HistoryCategory[] categoriesRemote = new HistoryCategory[0];
	protected HistoryCategory[] categoriesLocal = new HistoryCategory[0];
	
	public LogMessagesComposite(Composite parent, int logPercent, int style) {
	    super(parent, SWT.VERTICAL);
	    
	    this.commentVisible = true;
	    this.affectedVisible = true;

	    this.pathData = new HashMap();
		
		this.initializeTableView(style, logPercent);
		this.initializeFont();
	}
	
	public void setGroupByDate(boolean groupByDate) {
		this.groupByDate = groupByDate;
	}
	
	public void setRevisionMode(int revisionMode) {
		this.revisionMode = revisionMode;
	}
	
	public void setCommentViewerVisible(boolean visible) {
	    if (visible) {
	    	this.innerSashForm.setMaximizedControl(null);
	    }
	    else {
	    	this.innerSashForm.setMaximizedControl(this.historyTable.getControl());
	    }
	}
	
	public void setLocalHistory (SVNLocalFileRevision [] localHistory) {
		this.localHistory = localHistory;
	}
	
	public SVNLocalFileRevision [] getLocalHistory () {
		return this.localHistory;
	}
	
	public void setAffectedPathsViewerVisible(boolean visible) {
	    if (visible) {
	    	this.setMaximizedControl(null);
	    }
	    else {
	    	this.setMaximizedControl(this.innerSashForm);
	    }   
	}
	
	public void setResourceTreeVisible(boolean visible) {
		this.affectedPathsComposite.setResourceTreeVisible(visible);
	}
	
	public void dispose() {
		this.historyTable.removeSelectionChangedListener(this.historyTableListener);
		this.currentRevisionFont.dispose();
		super.dispose();
	}
	
	public TreeViewer getTreeViewer() {
	    return this.historyTable;
	}
	
	public AffectedPathsComposite getAffectedPathsComposite() {
		return this.affectedPathsComposite;
	}
	
	public void setSelectedRevision(long revision) {
		SVNLogEntry newSelection = null;
		for (int i = 0; i < this.msgs.length; i++) {
			if (msgs[i].revision == revision) {
				newSelection = msgs[i];
				break;
			}
		}
		if (newSelection != null) {
			this.historyTable.setSelection(new StructuredSelection(newSelection), true);
		}
		else {
			this.historyTable.setSelection(new StructuredSelection());
		}
	}
	
	public long []getSelectedRevisions() {
		IStructuredSelection tSelection = (IStructuredSelection)this.historyTable.getSelection();
		if (tSelection.size() > 0) {
			long []revisions = new long[tSelection.size()];
			int i = 0;
			for (Iterator it = tSelection.iterator(); it.hasNext(); ) {
				Object entry = it.next();
				if (entry instanceof SVNLogEntry) {
					revisions[i++] = ((SVNLogEntry)entry).revision;
				}
			}
			return revisions;
		}
		return new long[0];
	}
	
	public SVNLogEntry []getSelectedLogMessages() {
		IStructuredSelection tSelection = (IStructuredSelection)this.historyTable.getSelection();
		if (tSelection.size() > 0) {
			return (SVNLogEntry [])tSelection.toList().toArray(new SVNLogEntry[0]);
		}
		return new SVNLogEntry[0];
	}
	
	public long getSelectedRevision() {
		IStructuredSelection tSelection = (IStructuredSelection)this.historyTable.getSelection();
		if (tSelection.size() > 0 && (tSelection.getFirstElement() instanceof SVNLogEntry )) {
			return ((SVNLogEntry)tSelection.getFirstElement()).revision;
		}
		return SVNRevision.INVALID_REVISION_NUMBER;
	}
	
	public String getSelectedMessage() {
		IStructuredSelection tSelection = (IStructuredSelection)this.historyTable.getSelection();
		if (tSelection.size() > 0 &&  (tSelection.getFirstElement() instanceof SVNLogEntry)) {
			String message = ((SVNLogEntry)tSelection.getFirstElement()).message;
			return message == null ? "" : message;
		}
		return "";
	}
	
	public String getSelectedMessageNoComment() {
		IStructuredSelection tSelection = (IStructuredSelection)this.historyTable.getSelection();
		if (tSelection.size() > 0 && ((tSelection.getFirstElement() instanceof HistoryCategory)|| (tSelection.getFirstElement() instanceof SVNLocalFileRevision))) {
			return "";
		}
		String message = this.getSelectedMessage();
		return message.length() == 0 ? SVNTeamPlugin.instance().getResource("SVNInfo.NoComment") : message;
	}
	
	public String [][]getSelectedPathData() {
	    Object selected = null;
	    if (this.historyTable != null) {
			IStructuredSelection tSelection = (IStructuredSelection)this.historyTable.getSelection();
			if (tSelection.size() > 0) {
				selected = tSelection.getFirstElement();
			}
	    }
	    
		return (selected != null ? (String [][])this.pathData.get(selected) : null);
	}
	
	public ICommentView getCommentView() {
		return this.viewManager;
	}
	
    public IRepositoryResource getRepositoryResource() {
		return this.repositoryResource;
	}

	public ISelectionChangedListener getHistoryTableListener() {
		return historyTableListener;
	}

	public boolean isShowRelatedPathsOnly() {
		return this.showRelatedPathsOnly;
	}

	public void setShowRelatedPathsOnly(boolean showRelatedPathsOnly) {
		this.showRelatedPathsOnly = showRelatedPathsOnly;
		if (this.historyTableListener != null) {
			this.historyTableListener.selectionChanged(null);
		}
	}
    
	public void setLogMessages(SVNRevision currentRevision, SVNLogEntry []msgs, IRepositoryResource repositoryResource) {
		this.msgs = msgs;
		this.repositoryResource = repositoryResource;
		this.currentRevision = SVNRevision.INVALID_REVISION_NUMBER;
		this.pathData.clear();
		
		if (msgs == null || msgs.length == 0) {
			this.msgs = new SVNLogEntry[0];
			this.categoriesBoth = new HistoryCategory[0];
			this.setTableInput();
			this.historyTableListener.selectionChanged(null);
			return;
		}
		
		if (repositoryResource != null) {
			SVNLogPath []changes = null;
			// msgs[i].getChangedPaths() can be null or empty if user has no rights. So, find first accessible entry.
			for (int i = 0; i < msgs.length; i++) {
				if (msgs[i].changedPaths != null && msgs[i].changedPaths.length > 0) {
					changes = msgs[i].changedPaths;
					break;
				}
			}
			
			this.relatedPathsPrefixes = null;
			
			if (changes != null) {
				String baseUrl = repositoryResource.getUrl();
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
				    for (int i = 0; i < msgs.length; i++) {
					    this.relatedPathsPrefixes.add(relatedPathsPrefix);
					    if (msgs[i].changedPaths != null && msgs[i].changedPaths.length > 0) {
						    relatedPathsPrefix = this.getNextPrefix(msgs[i], relatedPathsPrefix, relatedParents);
					    }
				    }
				}
			}
		}
		
		if (currentRevision != null && currentRevision != SVNRevision.INVALID_REVISION) {
			if (currentRevision.getKind() == Kind.HEAD) {
				this.currentRevision = Math.max(msgs[0].revision, msgs[msgs.length - 1].revision);
			}
			else if (currentRevision.getKind() == Kind.NUMBER) {
				this.currentRevision = ((SVNRevision.Number)currentRevision).getNumber();
			}
		}
		this.collectCategoriesAndMapData(msgs);
		this.setTableInput();
	}
	
	public void collectCategoriesAndMapData(SVNLogEntry [] entries) {
		ArrayList<HistoryCategory> categoriesAll = new ArrayList <HistoryCategory>();
		ArrayList<HistoryCategory> categoriesLocal = new ArrayList <HistoryCategory>();
		ArrayList<HistoryCategory> categoriesRemote = new ArrayList <HistoryCategory>();
		ArrayList<Object> allEntries = new ArrayList<Object>();
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
		long lastMonthDate = monthCal.getTimeInMillis();
		
		//Filling timing ArrayLists
		for (int i = 0; i < entries.length; i++) {
			this.mapPathData(entries[i], entries[i].changedPaths);
			if (entries[i].date > yesterdayDate) {
				todayEntriesAll.add(entries[i]);
				todayEntriesRemote.add(entries[i]);
			}
			else if (entries[i].date < yesterdayDate && entries[i].date > beforeYesterdayDate) {
				yesterdayEntriesAll.add(entries[i]);
				yesterdayEntriesRemote.add(entries[i]);
			}
			else if  (entries[i].date < beforeYesterdayDate && entries[i].date > lastWeekDate) {
				weekEntriesAll.add(entries[i]);
				weekEntriesRemote.add(entries[i]);
			}
			else if  (entries[i].date < lastWeekDate && entries[i].date > lastMonthDate) {
				monthEntriesAll.add(entries[i]);
				monthEntriesRemote.add(entries[i]);
			}
			else {
				earlierEntriesAll.add(entries[i]);
				earlierEntriesRemote.add(entries[i]);
			}
			allEntries.add(entries[i]);
		}
		for (int i = 0; i < localHistory.length; i++) {
			if (localHistory[i].getTimestamp() > yesterdayDate) {
				todayEntriesAll.add(localHistory[i]);
				todayEntriesLocal.add(localHistory[i]);
			}
			else if (localHistory[i].getTimestamp() < yesterdayDate && localHistory[i].getTimestamp() > beforeYesterdayDate) {
				yesterdayEntriesAll.add(localHistory[i]);
				yesterdayEntriesLocal.add(localHistory[i]);
			}
			else if  (localHistory[i].getTimestamp() < beforeYesterdayDate && localHistory[i].getTimestamp() > lastWeekDate) {
				weekEntriesAll.add(localHistory[i]);
				weekEntriesLocal.add(localHistory[i]);
			}
			else if  (localHistory[i].getTimestamp() < lastWeekDate && localHistory[i].getTimestamp() > lastMonthDate) {
				monthEntriesAll.add(localHistory[i]);
				monthEntriesLocal.add(localHistory[i]);
			}
			else {
				earlierEntriesAll.add(localHistory[i]);
				earlierEntriesLocal.add(localHistory[i]);
			}
			allEntries.add(localHistory[i]);
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
		this.categoriesBoth = categoriesAll.toArray(new HistoryCategory[0]);
		this.categoriesLocal = categoriesLocal.toArray(new HistoryCategory[0]);
		this.categoriesRemote = categoriesRemote.toArray(new HistoryCategory[0]);
		this.allHystory = allEntries.toArray();
	}
	
	public String getSelectedMessagesAsString() {
		String historyText = "";
		HistoryLabelProvider provider = new HistoryLabelProvider(true);
		Object[] selectedItems = ((IStructuredSelection)this.historyTable.getSelection()).toArray();
		for (int i = 0; i < selectedItems.length; i++) {
			Object rowItems = selectedItems[i];
			if (rowItems instanceof HistoryCategory) {
				Object [] entries = ((HistoryCategory)rowItems).getEntries();
				for (int j = 0; j < entries.length; j++) {
					for (int k = 0; k < 5; k++) {
						historyText += provider.getColumnText(entries[j], k);
						historyText += (k < 5 ? "\t" : "");
					}
					historyText += System.getProperty("line.separator");
				}
			}
			else {
				for (int j = 0; j < 5; j++) {
					historyText += provider.getColumnText(rowItems, j);
					historyText += (j < 5 ? "\t" : "");
				}
				historyText += System.getProperty("line.separator");
			}
		}
		return historyText;
	}
	
	protected static String flattenMultiLineText(String input, String lineSeparatorReplacement) {
		String retVal = PatternProvider.replaceAll(input, "\r\n", lineSeparatorReplacement);
		retVal = PatternProvider.replaceAll(retVal, "\n", lineSeparatorReplacement);
		retVal = PatternProvider.replaceAll(retVal, "\r", lineSeparatorReplacement);
		return retVal;
	}
	
	protected String revisionToString(long revision) {
		String retVal = String.valueOf(revision);
		if (this.currentRevision == revision) {
			retVal = "*" + retVal;
		}
		return retVal;
	}
	
	protected String getNextPrefix(SVNLogEntry message, String current, Set relatedParents) {
		String checked = "/" + current;
		SVNLogPath []changes = message.changedPaths;
		
		for (int i = 0; i < changes.length; i++) {
			if (changes[i].copiedFromPath != null && checked.startsWith(changes[i].path)) {
				String rest = checked.substring(changes[i].path.length());
				String relatedParent = changes[i].copiedFromPath.substring(1);
				relatedParents.add(relatedParent);
				relatedParents.add(changes[i].path.substring(1));
				return relatedParent + rest;
			}
		}
		
		return current;
	}
	
	public void setTableInput() {
		if (!this.historyTable.getTree().isDisposed()) {
			if (this.groupByDate) {
				if (this.revisionMode == LogMessagesComposite.SHOW_BOTH) {
					this.historyTable.setInput(this.categoriesBoth);
				}
				else if (this.revisionMode == LogMessagesComposite.SHOW_LOCAL) {
					this.historyTable.setInput(this.categoriesLocal);
				}
				else {
					this.historyTable.setInput(this.categoriesRemote);
				}
			}
			else {
				if (this.revisionMode == LogMessagesComposite.SHOW_BOTH) {
					this.historyTable.setInput(this.allHystory);
				}
				else if (this.revisionMode == LogMessagesComposite.SHOW_LOCAL) {
					this.historyTable.setInput(this.localHistory);
				}
				else {
					this.historyTable.setInput(this.msgs);
				}
			}
		}
	}
	
	protected void mapPathData(Object key, SVNLogPath []paths) {
		String [][]pathData = new String[paths == null ? 0 : paths.length][];
		for (int i = 0; i < pathData.length; i++) {
			String path = paths[i].path;
			path = path.startsWith("/") ? path.substring(1) : path;
			int idx = path.lastIndexOf("/");
			pathData[i] = 
				new String[] {
					this.getAction(paths[i].action), 
					idx != -1 ? path.substring(idx + 1) : path,
					idx != -1 ? path.substring(0, idx) : "",
					paths[i].copiedFromRevision != SVNRevision.INVALID_REVISION_NUMBER ?  paths[i].copiedFromPath : "",
					paths[i].copiedFromRevision != SVNRevision.INVALID_REVISION_NUMBER ?  "" + paths[i].copiedFromRevision : ""
				};
		}
		this.pathData.put(key, pathData);
	}
	
	protected String getAction(char action) {
		switch (action) {
			case 'A': {
				return SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Add");
			}
			case 'M': {
				return SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Modify");
			}
			case 'D': {
				return SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Delete");
			}
			case 'R': {
				return SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Replace");
			}
		}
		
		throw new RuntimeException(SVNTeamUIPlugin.instance().getResource("Error.InvalidLogAction", new String[] {String.valueOf(action)}));
	}
	
	private void initializeTableView(int style, int logPercent) {
		GridData data = null;
		
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		this.setLayoutData(data);
		
		this.innerSashForm = new SashForm(this, SWT.VERTICAL);
		Tree treeTable = new Tree(innerSashForm, style | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		treeTable.setHeaderVisible(true);
		treeTable.setLinesVisible(true);
		TableLayout layout = new TableLayout();
		treeTable.setLayout(layout);
		
		data = new GridData(GridData.FILL_BOTH);
		treeTable.setLayoutData(data);
		
		this.viewManager = ExtensionsManager.getInstance().getCurrentMessageFactory().getCommentView();
		this.viewManager.createCommentView(this.innerSashForm, SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
		this.affectedPathsComposite = new AffectedPathsComposite(this, SWT.FILL);
		this.affectedPathsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.innerSashForm.setWeights(new int[] {75, 25});
		this.setWeights(new int[] {logPercent, 100 - logPercent});		
		
		this.historyTable = new TreeViewer(treeTable);
				
		HistoryComparator comparator = new HistoryComparator(LogMessagesComposite.COLUMN_DATE);
		comparator.setReversed(true);
		this.historyTable.setComparator(comparator);
		
		//revision
		TreeColumn col = new TreeColumn(treeTable, SWT.NONE);
		col.setResizable(true);
		col.setAlignment(SWT.RIGHT);
		col.setText(SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Revision"));
		col.addSelectionListener(this.getColumnListener(this.historyTable));
		layout.addColumnData(new ColumnWeightData(14, true));
	
		// creation date
		col = new TreeColumn(treeTable, SWT.NONE);
		col.setResizable(true);
		col.setText(SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Date"));
		col.addSelectionListener(this.getColumnListener(this.historyTable));
		layout.addColumnData(new ColumnWeightData(15, true));
	
		//file count
		col = new TreeColumn(treeTable, SWT.NONE);
		col.setResizable(true);
		col.setAlignment(SWT.RIGHT);
		col.setText(SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Changes"));
		col.addSelectionListener(this.getColumnListener(this.historyTable));
		layout.addColumnData(new ColumnWeightData(11, true));
		
		// author
		col = new TreeColumn(treeTable, SWT.NONE);
		col.setResizable(true);
		col.setText(SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Author"));
		col.addSelectionListener(this.getColumnListener(this.historyTable));
		layout.addColumnData(new ColumnWeightData(13, true));
	
		//comment
		col = new TreeColumn(treeTable, SWT.NONE);
		col.setResizable(true);
		col.setText(SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Comment"));
		col.addSelectionListener(this.getColumnListener(this.historyTable));
		layout.addColumnData(new ColumnWeightData(46, true));
		this.historyTable.setContentProvider(new ITreeContentProvider() {
			
			protected Object input;
			
			public void dispose () {
			}
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof HistoryCategory) {
					return ((HistoryCategory)parentElement).getEntries();
				}
				return null;
			}
			public Object getParent(Object element) {
				return null;
			}
			public boolean hasChildren(Object element) {
				return (element instanceof HistoryCategory);
			}
			public Object[] getElements(Object inputElement) {
				return (Object [])this.input;
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				this.input = newInput;
			}
		});
		this.historyTable.setLabelProvider(new HistoryLabelProvider(false));
		
		this.historyTableListener = new ISelectionChangedListener() {
        	protected Object oldSelection;
        	protected boolean hideUnrelated = LogMessagesComposite.this.showRelatedPathsOnly;
        	
			public void selectionChanged(SelectionChangedEvent event) {
				if (LogMessagesComposite.this.isDisposed()) {
					return;
				}
				IStructuredSelection tSelection = (IStructuredSelection)LogMessagesComposite.this.historyTable.getSelection();
				if (tSelection.size() > 0) {
					Object selection = tSelection.getFirstElement();
					if (this.oldSelection != selection || this.hideUnrelated != LogMessagesComposite.this.showRelatedPathsOnly)
					{
						String message = LogMessagesComposite.this.getSelectedMessageNoComment();
						LogMessagesComposite.this.viewManager.setComment(message);
						LogMessagesComposite.this.affectedPathsComposite.setRepositoryResource(LogMessagesComposite.this.repositoryResource);
						LogMessagesComposite.this.affectedPathsComposite.setInput(LogMessagesComposite.this.getSelectedPathData(), LogMessagesComposite.this.getRelatedPathPrefixes(), LogMessagesComposite.this.getRelatedParents(), LogMessagesComposite.this.getSelectedRevision());
						this.oldSelection = selection;
						this.hideUnrelated = LogMessagesComposite.this.showRelatedPathsOnly;
					}
				}
				else {
					LogMessagesComposite.this.viewManager.setComment("");
					LogMessagesComposite.this.affectedPathsComposite.setInput(null, null, null, -1);
					this.oldSelection = null;
				}
			}
		};
        this.historyTable.addSelectionChangedListener(this.historyTableListener);
		
		this.setTableInput();
		this.historyTable.refresh();
		this.historyTable.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
	}
	
	private SelectionListener getColumnListener(final TreeViewer treeViewer) {
		return new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int column = treeViewer.getTree().indexOf((TreeColumn) e.widget);
				HistoryComparator oldSorter = (HistoryComparator) treeViewer.getComparator();
				TreeColumn treeColumn = ((TreeColumn)e.widget);
				if (oldSorter != null && column == oldSorter.getColumnNumber()) {
					oldSorter.setReversed(!oldSorter.isReversed());
					treeViewer.getTree().setSortColumn(treeColumn);
					treeViewer.getTree().setSortDirection(oldSorter.isReversed() ? SWT.DOWN : SWT.UP);
					treeViewer.refresh();
				} else {
				    treeViewer.getTree().setSortColumn(treeColumn);
                    treeViewer.getTree().setSortDirection(SWT.UP);
					treeViewer.setComparator(new HistoryComparator(column));
				}
			}
		};
	}
	
	private Collection getRelatedPathPrefixes() {
		return this.showRelatedPathsOnly ? this.relatedPathsPrefixes : null;
	}
	
	private Collection getRelatedParents() {
		return this.showRelatedPathsOnly ? this.relatedParents : null;
	}
	
	private void initializeFont() {
		Font defaultFont = JFaceResources.getDefaultFont();
		FontData[] data = defaultFont.getFontData();
		for (int i = 0; i < data.length; i++) {
			data[i].setStyle(SWT.BOLD);
		}               
		this.currentRevisionFont = new Font(this.historyTable.getTree().getDisplay(), data);
	}
	
	private class HistoryLabelProvider implements ITableLabelProvider, IFontProvider {
		private DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());
		private boolean fullMessage;
		private Image remoteRevisionImage = null;
		private Image localRevisionImage = null;
		private Image groupImage = null;
		
		public HistoryLabelProvider(boolean fullMessage) {
			this.fullMessage = fullMessage;
		}
		
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				if (element instanceof SVNLogEntry) {
					if (this.remoteRevisionImage == null) {
						this.remoteRevisionImage = SVNTeamUIPlugin.instance().getImageDescriptor("icons/objects/repository.gif").createImage();
					}
					return this.remoteRevisionImage;
				}
				else if (element instanceof HistoryCategory) {
					if (this.groupImage == null) {
						this.groupImage = SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/group_by_date.gif").createImage();
					}
					return this.groupImage;
				}
				else if (element instanceof SVNLocalFileRevision) {
					if (this.localRevisionImage == null) {
						this.localRevisionImage = SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/local_rev.gif").createImage();
					}
					return this.localRevisionImage;
				}
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof SVNLogEntry) {
				SVNLogEntry row = (SVNLogEntry)element;
				switch (columnIndex) {
					case LogMessagesComposite.COLUMN_REVISION: {
						return LogMessagesComposite.this.revisionToString(row.revision);
					}
					case LogMessagesComposite.COLUMN_DATE: {
						return row.date == 0 ? SVNTeamPlugin.instance().getResource("SVNInfo.NoDate") : this.dateTimeFormat.format(new Date(row.date));
					}
					case LogMessagesComposite.COLUMN_CHANGES: {
						return String.valueOf(row.changedPaths != null ? row.changedPaths.length : 0);
					}
					case LogMessagesComposite.COLUMN_AUTHOR: {
						return row.author == null || row.author.length() == 0 ? SVNTeamPlugin.instance().getResource("SVNInfo.NoAuthor") : row.author;
					}
					case LogMessagesComposite.COLUMN_LOG_MESSGE: {
						return row.message == null || row.message.length() == 0 ? SVNTeamPlugin.instance().getResource("SVNInfo.NoComment") : (this.fullMessage ? LogMessagesComposite.flattenMultiLineText(row.message, " ") : FileUtility.formatMultilineText(row.message));
					}
					default: {
						return "";
					}
				}
			}
			else if (element instanceof SVNLocalFileRevision) {
				SVNLocalFileRevision row = (SVNLocalFileRevision)element;
				switch (columnIndex) {
					case LogMessagesComposite.COLUMN_DATE: {
						return this.dateTimeFormat.format(new Date(row.getTimestamp()));
					}
					case LogMessagesComposite.COLUMN_REVISION: {
						if (row.isCurrentState()) {
							return SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.CurrentRevision", new String [] {String.valueOf(LogMessagesComposite.this.currentRevision)});
						}
						return "";
					}
					case LogMessagesComposite.COLUMN_LOG_MESSGE: {
						return row.getComment();
					}
					default: {
						return "";
					}
				}
			}
			else if (columnIndex == LogMessagesComposite.COLUMN_REVISION) {
				return ((HistoryCategory)element).getName();
			}
			return "";
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
			if (this.remoteRevisionImage != null) {
				this.remoteRevisionImage.dispose();
				this.remoteRevisionImage = null;
			}
			if (this.groupImage != null) {
				this.groupImage.dispose();
				this.groupImage = null;
			}
			if (this.localRevisionImage != null) {
				this.localRevisionImage.dispose();
				this.localRevisionImage = null;
			}
		}

		public boolean isLabelProperty(Object element, String property) {
			return true;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

		public Font getFont(Object element) {
			if (element instanceof HistoryCategory) {
				if (!LogMessagesComposite.this.currentRevisionFont.isDisposed()) {
					return LogMessagesComposite.this.currentRevisionFont;
				}
				return null;
			}
			if (element instanceof SVNLocalFileRevision) {
				if (element.equals(LogMessagesComposite.this.localHistory[0]) && !LogMessagesComposite.this.currentRevisionFont.isDisposed()) {
					return LogMessagesComposite.this.currentRevisionFont;
				}
				return null;
			}
			SVNLogEntry row = (SVNLogEntry)element;
			if (LogMessagesComposite.this.currentRevision == row.revision && !LogMessagesComposite.this.currentRevisionFont.isDisposed()) {
				return LogMessagesComposite.this.currentRevisionFont;
			}
			return null;
		}
		
	}
    
	public class HistoryComparator extends ViewerComparator {
        protected int column;
        protected boolean reversed;
		
		HistoryComparator(int column) {
			super();
			this.reversed = false;
			this.column = column;
		}
		
		public boolean isReversed() {
			return this.reversed;
		}

		public void setReversed(boolean reversed) {
			this.reversed = reversed;
		}
		
		public int getColumnNumber() {
			return this.column;
		}

		
		public int compare(Viewer viewer, Object row1, Object row2) {
			
			//One entry is from local history and another is from log entry
			if ((row1 instanceof SVNLogEntry) && (row2 instanceof SVNLocalFileRevision)) {
				SVNLogEntry rowData1 = (SVNLogEntry)row1;
				SVNLocalFileRevision rowData2 = (SVNLocalFileRevision)row2;
				if (this.column == LogMessagesComposite.COLUMN_DATE) {
	            	if (this.reversed) {
	            		return new Long(rowData2.getTimestamp()).compareTo(new Long(rowData1.date));
	            	}
					return new Long(rowData1.date).compareTo(new Long(rowData2.getTimestamp()));
	            }
				if (this.column == LogMessagesComposite.COLUMN_REVISION) {
					if (rowData2.isCurrentState()) {
						int retVal = 0;
						if (this.reversed) {
							retVal = new Long(LogMessagesComposite.this.currentRevision).compareTo(new Long(rowData1.revision));
							if (retVal == 0) {
								return new Long(rowData2.getTimestamp()).compareTo(new Long(rowData1.date));
							}
	            			return retVal; 
	            		}
						retVal = new Long(rowData1.revision).compareTo(new Long(LogMessagesComposite.this.currentRevision));
						if (retVal == 0) {
							return new Long(rowData1.date).compareTo(new Long(rowData2.getTimestamp()));
						}
            			return retVal;
					}
	            }
				if (reversed) {
					return -1;
				}
				return 1;
			}
			if ((row2 instanceof SVNLogEntry) && (row1 instanceof SVNLocalFileRevision)) {
				SVNLocalFileRevision rowData1 = (SVNLocalFileRevision)row1;
				SVNLogEntry rowData2 = (SVNLogEntry)row2;
				if (this.column == LogMessagesComposite.COLUMN_DATE) {
	            	if (this.reversed) {
	            		return new Long(rowData2.date).compareTo(new Long(rowData1.getTimestamp()));
	            	}
					return new Long(rowData1.getTimestamp()).compareTo(new Long(rowData2.date));
	            }
				if (this.column == LogMessagesComposite.COLUMN_REVISION) {
					int retVal = 0;
					if (rowData1.isCurrentState()) {
						if (this.reversed) {
							retVal = new Long(rowData2.revision).compareTo(new Long(LogMessagesComposite.this.currentRevision));
							if (retVal == 0) {
								return new Long(rowData2.date).compareTo(new Long(rowData1.getTimestamp()));
							}
							return retVal;
	            		}
						retVal = new Long(LogMessagesComposite.this.currentRevision).compareTo(new Long(rowData2.revision));
						if (retVal == 0) {
							return new Long(rowData1.getTimestamp()).compareTo(new Long(rowData2.date));
						}
						return retVal;
					}
	            }
				if (reversed) {
					return 1;
				}
				return -1;
			}
			
			//Both are log entries
			if ((row1 instanceof SVNLogEntry) && (row2 instanceof SVNLogEntry)) {
	            SVNLogEntry rowData1 = (SVNLogEntry)row1;
	            SVNLogEntry rowData2 = (SVNLogEntry)row2;
	            if (this.column == LogMessagesComposite.COLUMN_REVISION) {
	            	if (this.reversed) {
	            		return new Long(rowData2.revision).compareTo(new Long(rowData1.revision));
	            	}
	            	return new Long(rowData1.revision).compareTo(new Long(rowData2.revision));
	            }
	            if (this.column == LogMessagesComposite.COLUMN_DATE) {
	            	if (this.reversed) {
	            		return new Long(rowData2.date).compareTo(new Long(rowData1.date));
	            	}
					return new Long(rowData1.date).compareTo(new Long(rowData2.date));
	            }
	            if (this.column == LogMessagesComposite.COLUMN_CHANGES) {
	            	int files1 = rowData1.changedPaths == null ? 0 : rowData1.changedPaths.length;
	            	int files2 = rowData2.changedPaths == null ? 0 : rowData2.changedPaths.length;
	            	if (this.reversed) {
	            		return new Integer(files2).compareTo(new Integer(files1));
	            	}
	            	return new Integer(files1).compareTo(new Integer(files2));
	            }
	            if (this.column == LogMessagesComposite.COLUMN_AUTHOR) {
	            	if (this.reversed) {
	            		return TableViewerSorter.compare(rowData2.author == null ? "" : rowData2.author, rowData1.author == null ? "" : rowData1.author);
	            	}
	            	return TableViewerSorter.compare(rowData1.author == null ? "" : rowData1.author, rowData2.author == null ? "" : rowData2.author);
	            }
	            if (this.reversed) {
	            	return TableViewerSorter.compare(rowData2.message == null ? "" : rowData2.message, rowData1.message == null ? "" : rowData1.message);
	            }
	            return TableViewerSorter.compare(rowData1.message == null ? "" : rowData1.message, rowData2.message == null ? "" : rowData2.message);
			}
			
			//Both are from local history
			if ((row1 instanceof SVNLocalFileRevision) && (row2 instanceof SVNLocalFileRevision)) {
				SVNLocalFileRevision rowData1 = (SVNLocalFileRevision)row1;
				SVNLocalFileRevision rowData2 = (SVNLocalFileRevision)row2;
	            if (this.column == LogMessagesComposite.COLUMN_REVISION) {
					if (rowData1.isCurrentState()) {
						if (this.reversed) {
							return -1;
	            		}
						return 1;
					}
					if (rowData2.isCurrentState()) {
						if (this.reversed) {
							return 1;
	            		}
						return -1;
					}
	            }
	            if (this.reversed) {
            		return new Long(rowData2.getTimestamp()).compareTo(new Long(rowData1.getTimestamp()));
            	}
				return new Long(rowData1.getTimestamp()).compareTo(new Long(rowData2.getTimestamp()));
			}
			
			//One of the rows is a category.
			return 0;
        }
    }
	
    public class HistoryCategory {
    	final public static int CATEGORY_TODAY = 1;
    	final public static int CATEGORY_YESTERDAY = 2;
    	final public static int CATEGORY_THIS_WEEK = 3;
    	final public static int CATEGORY_THIS_MONTH = 4;
    	final public static int CATEGORY_EARLIER = 5;
    	protected int categoryType;
    	protected Object[] entries;
    	
    	public HistoryCategory(int categoryType, Object[] entries) {
    		this.entries = entries;
    		this.categoryType = categoryType;
    	}
    	
    	public String getName() {
    		switch (this.categoryType) {
    			case HistoryCategory.CATEGORY_TODAY: return SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Group.Today");
    			case HistoryCategory.CATEGORY_YESTERDAY: return SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Group.Yesterday");
    			case HistoryCategory.CATEGORY_THIS_WEEK : return SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Group.Week");
    			case HistoryCategory.CATEGORY_THIS_MONTH : return SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Group.Month");
    			case HistoryCategory.CATEGORY_EARLIER : return SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Group.Earlier");
    		}
    		return null;
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
    
}
