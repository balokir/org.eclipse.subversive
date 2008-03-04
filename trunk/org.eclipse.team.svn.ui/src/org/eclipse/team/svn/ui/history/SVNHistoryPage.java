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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.GetLogMessagesOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.events.IResourceStatesListener;
import org.eclipse.team.svn.core.resource.events.ResourceStatesChangedEvent;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.core.utility.StringMatcher;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.history.HistoryActionManager.HistoryAction;
import org.eclipse.team.svn.ui.history.data.SVNLocalFileRevision;
import org.eclipse.team.svn.ui.history.model.ILogNode;
import org.eclipse.team.svn.ui.operation.CorrectRevisionOperation;
import org.eclipse.team.svn.ui.operation.UILoggedOperation;
import org.eclipse.team.svn.ui.panel.view.HistoryFilterPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.repository.model.RepositoryLocation;
import org.eclipse.team.svn.ui.repository.model.RepositoryResource;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.ui.history.HistoryPage;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;

/**
 * Generic HistoryView page
 * 
 * @author Alexander Gurov
 */
public class SVNHistoryPage extends HistoryPage implements ISVNHistoryView, IResourceStatesListener {
	public static final String VIEW_ID = "org.eclipse.team.ui.GenericHistoryView";
	
	protected IResource wcResource;
	protected IRepositoryResource repositoryResource;

	protected LogMessagesComposite history;

	protected String filterByAuthor;
	protected String filterByComment;

	protected Action showCommentViewerAction;
	protected Action showAffectedPathsViewerAction;
	protected Action hideUnrelatedAction;
	protected Action hideUnrelatedDropDownAction;
	protected Action stopOnCopyAction;
	protected Action stopOnCopyDropDownAction;
	protected Action groupByDateDropDownAction;
	protected Action getNextPageAction;
	protected Action getAllPagesAction;
	protected Action clearFilterDropDownAction;
	protected Action filterDropDownAction;
	protected Action hierarchicalAction;
	protected Action flatAction;
	protected Action compareModeAction;
	protected Action compareModeDropDownAction;
	protected Action groupByDateAction;
	protected Action showLocalAction;
	protected Action showRemoteAction;
	protected Action showBothAction;
	protected Action collapseAllAction;
	protected Action showLocalActionDropDown;
	protected Action showRemoteActionDropDown;
	protected Action showBothActionDropDown;
	
	protected long limit = 25;
	protected boolean pagingEnabled = false;
	protected boolean pending;
	protected int options = 0;

	protected IResource compareWith;

	protected long currentRevision = 0;
	protected SVNLogEntry[] logMessages;
	protected SVNLocalFileRevision[] localHistory;

	protected HistoryActionManager actionManager;

	public SVNHistoryPage(Object input) {
		SVNRemoteStorage.instance().addResourceStatesListener(ResourceStatesChangedEvent.class, this);
		
		this.actionManager = new HistoryActionManager(this);
	}
	
	public void resourcesStateChanged(ResourceStatesChangedEvent event) {
		if (this.wcResource == null) {
			return;
		}
		ILocalResource local = SVNRemoteStorage.instance().asLocalResource(this.wcResource);
		if (local != null) {
			if (IStateFilter.SF_ONREPOSITORY.accept(local) && this.logMessages == null){
				this.refreshChanges(ISVNHistoryView.REFRESH_ALL);
			}
			else if (this.wcResource instanceof IFile) {
				this.refreshChanges(ISVNHistoryView.REFRESH_LOCAL);
			}
			if ((event.contains(this.wcResource) || event.contains(this.wcResource.getProject())) &&
				(!this.wcResource.exists() || !FileUtility.isConnected(this.wcResource))) {
				this.disconnectView();
			}
		}
		else {
			this.disconnectView();
		}
	}

	public void showHistory(IResource resource) {
		if (!resource.equals(this.wcResource)) {
			this.clear();

			this.wcResource = resource;

			this.refresh(ISVNHistoryView.REFRESH_ALL);
		}
	}

	public void showHistory(IRepositoryResource remoteResource) {
		if (!remoteResource.equals(this.repositoryResource)) {
			this.clear();

			this.repositoryResource = remoteResource;

			this.refresh(ISVNHistoryView.REFRESH_ALL);
		}
	}

	public void clear() {
		this.currentRevision = SVNRevision.INVALID_REVISION_NUMBER;
		this.repositoryResource = null;
		this.wcResource = null;
		this.logMessages = null;
		this.localHistory = null;
		this.filterByAuthor = null;
		this.filterByComment = null;

		this.setButtonsEnablement();
		this.history.refresh(LogMessagesComposite.REFRESH_ALL);
	}

	public void selectRevision(long revision) {
		this.history.setSelectedRevision(revision);
	}

	public void setCompareWith(IResource compareWith) {
		this.compareWith = compareWith;
	}

	public void setOptions(int mask, int values) {
		this.options = (this.options & ~mask) | (mask & values);
		this.refreshOptionButtons();
	}

	public IResource getResource() {
		return this.wcResource;
	}

	public IRepositoryResource getRepositoryResource() {
		return this.repositoryResource;
	}

	public IResource getCompareWith() {
		return this.compareWith == null ? this.wcResource : this.compareWith;
	}

	public HistoryPage getHistoryPage() {
		return this;
	}

	public SVNLogEntry[] getRemoteHistory() {
		return SVNHistoryPage.filterMessages(this.logMessages, this.filterByAuthor, this.filterByComment);
	}

	public SVNLogEntry[] getFullRemoteHistory() {
		return this.logMessages;
	}

	public SVNLocalFileRevision[] getLocalHistory() {
		return this.localHistory;
	}

	public boolean isAllRemoteHistoryFetched() {
		return !this.getNextPageAction.isEnabled();
	}

	public boolean isFilterEnabled() {
		return this.filterByAuthor != null || this.filterByComment != null;
	}

	public int getOptions() {
		return this.options;
	}

	public void clearFilter() {
		this.filterByAuthor = null;
		this.filterByComment = null;
		this.clearFilterDropDownAction.setEnabled(false);
		this.history.refresh(LogMessagesComposite.REFRESH_ALL);
	}

	public void setFilter() {
		HistoryFilterPanel panel = new HistoryFilterPanel(this.filterByAuthor, this.filterByComment, SVNHistoryPage.getSelectedAuthors(this.logMessages));
		DefaultDialog dialog = new DefaultDialog(this.getPartSite().getShell(), panel);
		if (dialog.open() == 0) {
			this.filterByAuthor = panel.getAuthor();
			this.filterByComment = panel.getComment();
			this.clearFilterDropDownAction.setEnabled(true);
			this.history.refresh(LogMessagesComposite.REFRESH_ALL);
		}
	}

	public void refresh(int refreshType) {
		if (this.wcResource != null) {
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(this.wcResource);
			if (local != null) {
				if (IStateFilter.SF_ONREPOSITORY.accept(local)) {
					this.currentRevision = local.getRevision();
					this.repositoryResource = local.isCopied() ? SVNUtility.getCopiedFrom(this.wcResource) : SVNRemoteStorage.instance().asRepositoryResource(this.wcResource);
				}
				else {
					this.repositoryResource = null;
				}

				if (this.wcResource instanceof IFile && refreshType != ISVNHistoryView.REFRESH_REMOTE && refreshType != ISVNHistoryView.REFRESH_VIEW) {
					try {
						this.fetchLocalHistory(local, new NullProgressMonitor());
					}
					catch (CoreException ex) {
						UILoggedOperation.reportError(SVNTeamUIPlugin.instance().getResource("HistoryView.Name"), ex);
					}
					this.setButtonsEnablement();
				}
			}
		}

		if (this.repositoryResource != null && (refreshType == ISVNHistoryView.REFRESH_ALL || refreshType == ISVNHistoryView.REFRESH_REMOTE)) {
			this.logMessages = null;
			this.pending = true;
			this.setButtonsEnablement();
			this.history.refresh(LogMessagesComposite.REFRESH_ALL);
			GetLogMessagesOperation msgOp = new GetLogMessagesOperation(this.repositoryResource, this.stopOnCopyAction.isChecked());
			msgOp.setLimit(this.limit);
			this.fetchRemoteHistory(msgOp);
		}
		else {
			this.history.refresh(LogMessagesComposite.REFRESH_ALL);
		}
	}

	public long getCurrentRevision() {
		return this.currentRevision;
	}

	public boolean isGrouped() {
		return this.groupByDateAction.isChecked();
	}
	
	public boolean isPending() {
		return this.pending;
	}

	public int getMode() {
		return this.options & ISVNHistoryViewInfo.MODE_MASK;
	}

	public boolean isRelatedPathsOnly() {
		return this.hideUnrelatedAction.isChecked();
	}

	public void dispose() {
		SVNRemoteStorage.instance().removeResourceStatesListener(ResourceStatesChangedEvent.class, this);
		// log messages composite is disposed by HistoryPage.dispose()
		super.dispose();
	}

	public boolean inputSet() {
		if (this.getInput() instanceof IResource) {
			this.showHistory((IResource)this.getInput());
			return true;
		}
		else if (this.getInput() instanceof IRepositoryResource) {
			this.showHistory((IRepositoryResource)this.getInput());
			return true;
		}
		else if (this.getInput() instanceof RepositoryResource) {
			this.showHistory(((RepositoryResource)this.getInput()).getRepositoryResource());
			return true;
		}
		else if (this.getInput() instanceof RepositoryLocation) {
			this.showHistory(((RepositoryLocation)this.getInput()).getRepositoryResource());
			return true;
		}
		return false;
	}
	
	public Control getControl() {
		return this.history;
	}

	public void setFocus() {

	}

	public String getDescription() {
		return this.getName();
	}

	public String getName() {
		if (this.getResource() != null) {
			return this.getResource().getFullPath().toString().substring(1);
		}
		if (this.getRepositoryResource() != null) {
			return this.getRepositoryResource().getUrl();
		}
		return SVNTeamUIPlugin.instance().getResource("SVNView.ResourceNotSelected");
	}

	public boolean isValidInput(Object object) {
		return SVNHistoryPage.isValidData(object);
	}

	public void refresh() {
		this.refresh(ISVNHistoryView.REFRESH_ALL);
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

	public void createControl(Composite parent) {
		IActionBars actionBars = this.getHistoryPageSite().getWorkbenchPageSite().getActionBars();
		
		this.groupByDateAction = new HistoryAction("HistoryView.GroupByDate", "icons/views/history/group_by_date.gif", IAction.AS_CHECK_BOX) {
			public void run() {
				SVNHistoryPage.this.options ^= ISVNHistoryView.GROUP_BY_DATE;
				SVNHistoryPage.this.groupByDateDropDownAction.setChecked((SVNHistoryPage.this.options & ISVNHistoryView.GROUP_BY_DATE) != 0);
				SVNHistoryPage.saveInt(SVNTeamPreferences.HISTORY_GROUPING_TYPE_NAME, (SVNHistoryPage.this.options & ISVNHistoryView.GROUP_BY_DATE) == 0 ? SVNTeamPreferences.HISTORY_GROUPING_TYPE_NONE : SVNTeamPreferences.HISTORY_GROUPING_TYPE_DATE);
				SVNHistoryPage.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
			}
		};

		this.showBothAction = new HistoryAction("HistoryView.ShowBoth", "icons/views/history/both_history_mode.gif", IAction.AS_RADIO_BUTTON) {
			public void run() {
				SVNHistoryPage.this.options = SVNHistoryPage.this.options & ~(ISVNHistoryViewInfo.MODE_LOCAL | ISVNHistoryViewInfo.MODE_REMOTE) | ISVNHistoryViewInfo.MODE_BOTH;
				SVNHistoryPage.this.showBothActionDropDown.setChecked(true);
				SVNHistoryPage.this.showLocalActionDropDown.setChecked(false);
				SVNHistoryPage.this.showRemoteActionDropDown.setChecked(false);
				SVNHistoryPage.this.saveShowMode();
				SVNHistoryPage.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
			}
		};
		this.showLocalAction = new HistoryAction("HistoryView.ShowLocal", "icons/views/history/local_history_mode.gif", IAction.AS_RADIO_BUTTON) {
			public void run() {
				SVNHistoryPage.this.options = SVNHistoryPage.this.options & ~(ISVNHistoryViewInfo.MODE_REMOTE | ISVNHistoryViewInfo.MODE_BOTH) | ISVNHistoryViewInfo.MODE_LOCAL;
				SVNHistoryPage.this.showLocalActionDropDown.setChecked(true);
				SVNHistoryPage.this.showRemoteActionDropDown.setChecked(false);
				SVNHistoryPage.this.showBothActionDropDown.setChecked(false);
				SVNHistoryPage.this.saveShowMode();
				SVNHistoryPage.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
			}
		};
		this.showRemoteAction = new HistoryAction("HistoryView.ShowRemote", "icons/views/history/remote_history_mode.gif", IAction.AS_RADIO_BUTTON) {
			public void run() {
				SVNHistoryPage.this.options = SVNHistoryPage.this.options & ~(ISVNHistoryViewInfo.MODE_LOCAL | ISVNHistoryViewInfo.MODE_BOTH) | ISVNHistoryViewInfo.MODE_REMOTE;
				SVNHistoryPage.this.showRemoteActionDropDown.setChecked(true);
				SVNHistoryPage.this.showLocalActionDropDown.setChecked(false);
				SVNHistoryPage.this.showBothActionDropDown.setChecked(false);
				SVNHistoryPage.this.saveShowMode();
				SVNHistoryPage.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
			}
		};
		
		this.hideUnrelatedAction = new HistoryAction("HistoryView.HideUnrelatedPaths", "icons/views/history/hide_unrelated.gif", IAction.AS_CHECK_BOX) {
			public void run() {
				SVNHistoryPage.this.options ^= ISVNHistoryView.HIDE_UNRELATED;
				SVNHistoryPage.this.hideUnrelatedDropDownAction.setChecked((SVNHistoryPage.this.options & ISVNHistoryView.HIDE_UNRELATED) != 0);
				SVNHistoryPage.this.history.refresh(LogMessagesComposite.REFRESH_UI_AFFECTED);
			}
		};
		this.stopOnCopyAction = new HistoryAction("HistoryView.StopOnCopy", "icons/views/history/stop_on_copy.gif", IAction.AS_CHECK_BOX) {
			public void run() {
				SVNHistoryPage.this.options ^= ISVNHistoryView.STOP_ON_COPY;
				SVNHistoryPage.this.stopOnCopyDropDownAction.setChecked((SVNHistoryPage.this.options & ISVNHistoryView.STOP_ON_COPY) != 0);
				SVNHistoryPage.this.refresh(ISVNHistoryView.REFRESH_REMOTE);
			}
		};
		
		this.getNextPageAction = new HistoryAction("HistoryView.GetNextPage", "icons/views/history/paging.gif") {
			public void run() {
				GetLogMessagesOperation msgOp = new GetLogMessagesOperation(SVNHistoryPage.this.repositoryResource, SVNHistoryPage.this.stopOnCopyAction.isChecked());
				msgOp.setLimit(SVNHistoryPage.this.limit + 1);
				if (SVNHistoryPage.this.logMessages != null) {
					SVNLogEntry lm = SVNHistoryPage.this.logMessages[SVNHistoryPage.this.logMessages.length - 1];
					msgOp.setSelectedRevision(SVNRevision.fromNumber(lm.revision));
				}
				SVNHistoryPage.this.fetchRemoteHistory(msgOp);
			}
		};
		String msg = this.limit > 0 ? SVNTeamUIPlugin.instance().getResource("HistoryView.ShowNextX", new String[] { String.valueOf(this.limit) }) : SVNTeamUIPlugin.instance()
				.getResource("HistoryView.ShowNextPage");
		this.getNextPageAction.setToolTipText(msg);
		this.getAllPagesAction = new HistoryAction("HistoryView.ShowAll", "icons/views/history/paging_all.gif") {
			public void run() {
				GetLogMessagesOperation msgOp = new GetLogMessagesOperation(SVNHistoryPage.this.repositoryResource, SVNHistoryPage.this.stopOnCopyAction.isChecked());
				msgOp.setLimit(0);
				if (SVNHistoryPage.this.logMessages != null) {
					SVNLogEntry lm = SVNHistoryPage.this.logMessages[SVNHistoryPage.this.logMessages.length - 1];
					msgOp.setSelectedRevision(SVNRevision.fromNumber(lm.revision));
				}
				SVNHistoryPage.this.fetchRemoteHistory(msgOp);
			}
		};
		
		this.collapseAllAction = new HistoryAction("RepositoriesView.CollapseAll.Label", "icons/common/collapseall.gif") {
			public void run() {
				SVNHistoryPage.this.history.collapseAll();
			}
		};
		this.compareModeAction = new HistoryAction("HistoryView.CompareMode", "icons/views/history/compare_mode.gif", IAction.AS_CHECK_BOX) {
			public void run() {
				SVNHistoryPage.this.options ^= ISVNHistoryView.COMPARE_MODE;
				SVNHistoryPage.this.compareModeDropDownAction.setChecked((SVNHistoryPage.this.options & ISVNHistoryView.COMPARE_MODE) != 0);
				SVNHistoryPage.saveBoolean(SVNTeamPreferences.HISTORY_COMPARE_MODE, (SVNHistoryPage.this.options & ISVNHistoryView.COMPARE_MODE) != 0);
			}
		};
		
		IToolBarManager tbm = actionBars.getToolBarManager();
		tbm.add(new Separator());
		tbm.add(this.groupByDateAction);
		tbm.add(new Separator());
		tbm.add(this.showBothAction);
		tbm.add(this.showLocalAction);
		tbm.add(this.showRemoteAction);
		tbm.add(new Separator());
		tbm.add(this.hideUnrelatedAction);
		tbm.add(this.stopOnCopyAction);
		tbm.add(new Separator());
		tbm.add(this.getNextPageAction);
		tbm.add(this.getAllPagesAction);
		tbm.add(new Separator());
		tbm.add(this.collapseAllAction);
		tbm.add(this.compareModeAction);

		// drop-down menu
		this.showCommentViewerAction = new HistoryAction("HistoryView.ShowCommentViewer") {
			public void run() {
				SVNHistoryPage.this.history.setCommentViewerVisible(SVNHistoryPage.this.showCommentViewerAction.isChecked());
				SVNHistoryPage.saveBoolean(SVNTeamPreferences.HISTORY_SHOW_MULTILINE_COMMENT_NAME, this.isChecked());
			}
		};
		this.showAffectedPathsViewerAction = new HistoryAction("HistoryView.ShowAffectedPathsViewer") {
			public void run() {
				boolean showAffected = this.isChecked();
				SVNHistoryPage.this.history.setAffectedPathsViewerVisible(showAffected);
				SVNHistoryPage.this.flatAction.setEnabled(showAffected);
				SVNHistoryPage.this.hierarchicalAction.setEnabled(showAffected);
				SVNHistoryPage.saveBoolean(SVNTeamPreferences.HISTORY_SHOW_AFFECTED_PATHS_NAME, showAffected);
			}
		};

		this.hideUnrelatedDropDownAction = new HistoryAction("HistoryView.HideUnrelatedPaths", "icons/views/history/hide_unrelated.gif") {
			public void run() {
				SVNHistoryPage.this.options ^= ISVNHistoryView.HIDE_UNRELATED;
				SVNHistoryPage.this.hideUnrelatedAction.setChecked((SVNHistoryPage.this.options & ISVNHistoryView.HIDE_UNRELATED) != 0);
				SVNHistoryPage.this.history.refresh(LogMessagesComposite.REFRESH_UI_AFFECTED);
			}
		};
		this.stopOnCopyDropDownAction = new HistoryAction("HistoryView.StopOnCopy", "icons/views/history/stop_on_copy.gif") {
			public void run() {
				SVNHistoryPage.this.options ^= ISVNHistoryView.STOP_ON_COPY;
				SVNHistoryPage.this.stopOnCopyAction.setChecked((SVNHistoryPage.this.options & ISVNHistoryView.STOP_ON_COPY) != 0);
				SVNHistoryPage.this.refresh(ISVNHistoryView.REFRESH_REMOTE);
			}
		};

		this.groupByDateDropDownAction = new HistoryAction("HistoryView.GroupByDate", "icons/views/history/group_by_date.gif") {
			public void run() {
				SVNHistoryPage.this.options ^= ISVNHistoryView.GROUP_BY_DATE;
				SVNHistoryPage.this.groupByDateAction.setChecked((SVNHistoryPage.this.options & ISVNHistoryView.GROUP_BY_DATE) != 0);
				SVNHistoryPage.saveInt(SVNTeamPreferences.HISTORY_GROUPING_TYPE_NAME, (SVNHistoryPage.this.options & ISVNHistoryView.GROUP_BY_DATE) == 0 ? SVNTeamPreferences.HISTORY_GROUPING_TYPE_NONE : SVNTeamPreferences.HISTORY_GROUPING_TYPE_DATE);
				SVNHistoryPage.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
			}
		};

		this.showBothActionDropDown = new HistoryAction("HistoryView.ShowBoth", "icons/views/history/both_history_mode.gif", IAction.AS_RADIO_BUTTON) {
			public void run() {
				SVNHistoryPage.this.options = SVNHistoryPage.this.options & ~(ISVNHistoryViewInfo.MODE_LOCAL | ISVNHistoryViewInfo.MODE_REMOTE) | ISVNHistoryViewInfo.MODE_BOTH;
				SVNHistoryPage.this.showBothAction.setChecked(true);
				SVNHistoryPage.this.showLocalAction.setChecked(false);
				SVNHistoryPage.this.showRemoteAction.setChecked(false);
				SVNHistoryPage.this.saveShowMode();
				SVNHistoryPage.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
			}
		};
		this.showLocalActionDropDown = new HistoryAction("HistoryView.ShowLocal", "icons/views/history/local_history_mode.gif", IAction.AS_RADIO_BUTTON) {
			public void run() {
				SVNHistoryPage.this.options = SVNHistoryPage.this.options & ~(ISVNHistoryViewInfo.MODE_REMOTE | ISVNHistoryViewInfo.MODE_BOTH) | ISVNHistoryViewInfo.MODE_LOCAL;
				SVNHistoryPage.this.showBothAction.setChecked(false);
				SVNHistoryPage.this.showLocalAction.setChecked(true);
				SVNHistoryPage.this.showRemoteAction.setChecked(false);
				SVNHistoryPage.this.saveShowMode();
				SVNHistoryPage.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
			}
		};
		this.showRemoteActionDropDown = new HistoryAction("HistoryView.ShowRemote", "icons/views/history/remote_history_mode.gif", IAction.AS_RADIO_BUTTON) {
			public void run() {
				SVNHistoryPage.this.options = SVNHistoryPage.this.options & ~(ISVNHistoryViewInfo.MODE_LOCAL | ISVNHistoryViewInfo.MODE_BOTH) | ISVNHistoryViewInfo.MODE_REMOTE;
				SVNHistoryPage.this.showBothAction.setChecked(false);
				SVNHistoryPage.this.showLocalAction.setChecked(false);
				SVNHistoryPage.this.showRemoteAction.setChecked(true);
				SVNHistoryPage.this.saveShowMode();
				SVNHistoryPage.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
			}
		};

		this.filterDropDownAction = new HistoryAction("HistoryView.QuickFilter", "icons/views/history/filter.gif") {
			public void run() {
				SVNHistoryPage.this.setFilter();
			}
		};
		this.clearFilterDropDownAction = new HistoryAction("HistoryView.ClearFilter", "icons/views/history/clear_filter.gif") {
			public void run() {
				SVNHistoryPage.this.clearFilter();
			}
		};

		this.compareModeDropDownAction = new HistoryAction("HistoryView.CompareMode", "icons/views/history/compare_mode.gif") {
			public void run() {
				SVNHistoryPage.this.options ^= ISVNHistoryView.COMPARE_MODE;
				SVNHistoryPage.this.compareModeAction.setChecked((SVNHistoryPage.this.options & ISVNHistoryView.COMPARE_MODE) != 0);
				SVNHistoryPage.saveBoolean(SVNTeamPreferences.HISTORY_COMPARE_MODE, (SVNHistoryPage.this.options & ISVNHistoryView.COMPARE_MODE) != 0);
			}
		};

		this.flatAction = new HistoryAction("HistoryView.Flat", "icons/views/history/flat_layout.gif", IAction.AS_RADIO_BUTTON) {
			public void run() {
				SVNHistoryPage.this.history.setResourceTreeVisible(false);
				SVNHistoryPage.saveBoolean(SVNTeamPreferences.HISTORY_HIERARCHICAL_LAYOUT, false);
			}
		};
		this.hierarchicalAction = new HistoryAction("HistoryView.Hierarchical", "icons/views/history/tree_layout.gif", IAction.AS_RADIO_BUTTON) {
			public void run() {
				SVNHistoryPage.this.history.setResourceTreeVisible(true);
				SVNHistoryPage.saveBoolean(SVNTeamPreferences.HISTORY_HIERARCHICAL_LAYOUT, true);
			}
		};

		IMenuManager actionBarsMenu = actionBars.getMenuManager();
		actionBarsMenu.add(this.showCommentViewerAction);
		actionBarsMenu.add(this.showAffectedPathsViewerAction);
		MenuManager sub = new MenuManager(SVNTeamUIPlugin.instance().getResource("HistoryView.AffectedPathLayout"), IWorkbenchActionConstants.GROUP_MANAGING);
		sub.add(this.flatAction);
		sub.add(this.hierarchicalAction);
		actionBarsMenu.add(sub);
		actionBarsMenu.add(new Separator());
		actionBarsMenu.add(this.groupByDateDropDownAction);
		actionBarsMenu.add(new Separator());
		actionBarsMenu.add(this.showBothActionDropDown);
		actionBarsMenu.add(this.showLocalActionDropDown);
		actionBarsMenu.add(this.showRemoteActionDropDown);
		actionBarsMenu.add(new Separator());
		actionBarsMenu.add(this.hideUnrelatedDropDownAction);
		actionBarsMenu.add(this.stopOnCopyDropDownAction);
		actionBarsMenu.add(new Separator());
		actionBarsMenu.add(this.filterDropDownAction);
		actionBarsMenu.add(this.clearFilterDropDownAction);
		actionBarsMenu.add(new Separator());
		actionBarsMenu.add(this.compareModeDropDownAction);

		this.history = new LogMessagesComposite(parent, true, this);

		GridData data = new GridData(GridData.FILL_BOTH);
		this.history.setLayoutData(data);

		this.history.registerActionManager(this.actionManager, this.getPartSite());

		// Setting context help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.team.svn.help.historyViewContext");

		this.refreshOptionButtons();
	}

	public static String[] getSelectedAuthors(SVNLogEntry []logMessages) {
		HashSet<String> authors = new HashSet<String>();
		if (logMessages != null) {
			for (SVNLogEntry entry : logMessages) {
				if (entry.author != null) {
					authors.add(entry.author);
				}
			}
		}
		return authors.toArray(new String[authors.size()]);
	}

	public static SVNLogEntry[] filterMessages(SVNLogEntry[] msgs, String filterByAuthor, String filterByComment) {
		if (msgs == null) {
			return null;
		}
		ArrayList<SVNLogEntry> filteredMessages = new ArrayList<SVNLogEntry>();
		StringMatcher authorMatcher = filterByAuthor == null ? null : new StringMatcher(filterByAuthor);
		StringMatcher commentMatcher = filterByComment == null ? null : new StringMatcher(filterByComment);
		for (int i = 0; i < msgs.length; i++) {
			String author = msgs[i].author == null ? "" : msgs[i].author;
			String message = msgs[i].message == null ? "" : msgs[i].message;
			if ((authorMatcher == null || authorMatcher.match(author)) && (commentMatcher == null || commentMatcher.match(message))) {
				filteredMessages.add(msgs[i]);
			}
		}
		if (filteredMessages.size() == 0) {
			return null;
		}
		return filteredMessages.toArray(new SVNLogEntry[filteredMessages.size()]);
	}

	public static boolean isValidData(Object object) {
		return 
			object instanceof IRepositoryResource || 
			object instanceof RepositoryResource ||
			object instanceof RepositoryLocation ||
			object instanceof IResource && FileUtility.isConnected((IResource)object);
	}

	protected IWorkbenchPartSite getPartSite() {
		IWorkbenchPart part = this.getHistoryPageSite().getPart();
		if (part == null) {
			return null;
		}
		IWorkbenchPartSite site = part.getSite();
		while (site == null) {
			try {
				// await while site is initialized, see IWorkbenchPart.getSite() documentation
				Thread.sleep(100);
			}
			catch (InterruptedException ex) {
				break;
			}
			site = part.getSite();
		}
		return site;
	}

	protected void fetchRemoteHistory(final GetLogMessagesOperation msgsOp) {
		final IStructuredSelection selected = (IStructuredSelection) this.history.getTreeViewer().getSelection();
		IActionOperation showOp = new AbstractActionOperation("Operation.HShowHistory") {
			private long revision = SVNHistoryPage.this.currentRevision;

			protected void runImpl(IProgressMonitor monitor) throws Exception {
				if (msgsOp.getExecutionState() != IActionOperation.OK) {
					SVNHistoryPage.this.pending = false;
					UIMonitorUtility.getDisplay().syncExec(new Runnable() {
						public void run() {
							SVNHistoryPage.this.history.refresh(LogMessagesComposite.REFRESH_ALL);
						}
					});
					return;
				}
				if (SVNHistoryPage.this.wcResource == null) {
					this.revision = SVNHistoryPage.this.getRepositoryResource().getRevision();
				}

				if (SVNHistoryPage.this.repositoryResource == null || !SVNHistoryPage.this.repositoryResource.equals(msgsOp.getResource())) {
					return;
				}

				SVNHistoryPage.this.currentRevision = revision;
				SVNHistoryPage.this.addPage(msgsOp.getMessages());

				UIMonitorUtility.getDisplay().syncExec(new Runnable() {
					public void run() {
						SVNHistoryPage.this.history.refresh(LogMessagesComposite.REFRESH_ALL);
						SVNHistoryPage.this.setButtonsEnablement();

						TreeViewer treeTable = SVNHistoryPage.this.history.getTreeViewer();
						if (!treeTable.getTree().isDisposed() && treeTable.getTree().getItems().length > 0) {
							if (selected.size() != 0) {
								treeTable.setSelection(selected, true);
							}
							else {
								TreeItem firstItem = treeTable.getTree().getItem(0);
								if (((ILogNode) firstItem.getData()).getType() == ILogNode.TYPE_CATEGORY) {
									firstItem = firstItem.getItem(0);
								}
								treeTable.getTree().setSelection(firstItem);
							}
						}
					}
				});
			}
		};
		CompositeOperation op = new CompositeOperation(showOp.getId(), true);
		op.add(new CorrectRevisionOperation(msgsOp, this.repositoryResource, this.currentRevision, this.wcResource));
		op.add(msgsOp);
		op.add(showOp);

		ProgressMonitorUtility.doTaskScheduled(op, false);
	}

	protected void fetchLocalHistory(ILocalResource local, IProgressMonitor monitor) throws CoreException {
		IFile file = (IFile) this.wcResource;
		ArrayList<SVNLocalFileRevision> history = new ArrayList<SVNLocalFileRevision>();
		IFileState[] states = file.getHistory(monitor);
		if (states.length > 0 || IStateFilter.SF_NOTONREPOSITORY.accept(local)) {
			history.add(new SVNLocalFileRevision(file));
		}
		for (IFileState state : states) {
			history.add(new SVNLocalFileRevision(state));
		}
		this.localHistory = history.size() == 0 ? null : history.toArray(new SVNLocalFileRevision[history.size()]);
	}

	protected void addPage(SVNLogEntry[] newMessages) {
		if (this.logMessages == null) {
			if (newMessages.length > 0) {
				this.pending = false;
				this.logMessages = newMessages;
				this.pagingEnabled = this.limit > 0 && newMessages.length == this.limit;
			}
		}
		else if (newMessages.length > 1) {
			LinkedHashSet<SVNLogEntry> entries = new LinkedHashSet<SVNLogEntry>(Arrays.asList(this.logMessages));
			int oldSize = entries.size();
			entries.addAll(Arrays.asList(newMessages));
			this.logMessages = entries.toArray(new SVNLogEntry[entries.size()]);
			this.pagingEnabled = this.limit > 0 && (newMessages.length == this.limit + 1 || entries.size() - oldSize < newMessages.length - 1);
		}
	}

	protected void refreshOptionButtons() {
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();

		boolean showMultiline = SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_SHOW_MULTILINE_COMMENT_NAME);
		this.showCommentViewerAction.setChecked(showMultiline);
		this.showCommentViewerAction.run();

		boolean showAffected = SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_SHOW_AFFECTED_PATHS_NAME);
		this.showAffectedPathsViewerAction.setChecked(showAffected);
		this.showAffectedPathsViewerAction.run();

		boolean hierarchicalAffectedView = SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_HIERARCHICAL_LAYOUT);
		int groupingType = SVNTeamPreferences.getHistoryInt(store, SVNTeamPreferences.HISTORY_GROUPING_TYPE_NAME);
		int revisionMode = SVNTeamPreferences.getHistoryInt(store, SVNTeamPreferences.HISTORY_REVISION_MODE_NAME);
		if (revisionMode == 0) {
			revisionMode = ISVNHistoryViewInfo.MODE_BOTH;
		}
		else if (revisionMode == 1) {
			revisionMode = ISVNHistoryViewInfo.MODE_REMOTE;
		}
		else {
			revisionMode = ISVNHistoryViewInfo.MODE_LOCAL;
		}

		this.hideUnrelatedDropDownAction.setChecked((this.options & ISVNHistoryView.HIDE_UNRELATED) != 0);
		this.hideUnrelatedAction.setChecked((this.options & ISVNHistoryView.HIDE_UNRELATED) != 0);
		this.stopOnCopyDropDownAction.setChecked((this.options & ISVNHistoryView.STOP_ON_COPY) != 0);
		this.stopOnCopyAction.setChecked((this.options & ISVNHistoryView.STOP_ON_COPY) != 0);
		this.options |= groupingType == SVNTeamPreferences.HISTORY_GROUPING_TYPE_DATE ? ISVNHistoryView.GROUP_BY_DATE : 0;
		this.options = this.options & ~(ISVNHistoryViewInfo.MODE_BOTH | ISVNHistoryViewInfo.MODE_LOCAL | ISVNHistoryViewInfo.MODE_REMOTE) | revisionMode;
		this.groupByDateAction.setChecked((this.options & ISVNHistoryView.GROUP_BY_DATE) != 0);
		this.showBothAction.setChecked((this.options & ISVNHistoryViewInfo.MODE_BOTH) != 0);
		this.showBothActionDropDown.setChecked((this.options & ISVNHistoryViewInfo.MODE_BOTH) != 0);
		this.showLocalAction.setChecked((this.options & ISVNHistoryViewInfo.MODE_LOCAL) != 0);
		this.showLocalActionDropDown.setChecked((this.options & ISVNHistoryViewInfo.MODE_LOCAL) != 0);
		this.showRemoteAction.setChecked((this.options & ISVNHistoryViewInfo.MODE_REMOTE) != 0);
		this.showRemoteActionDropDown.setChecked((this.options & ISVNHistoryViewInfo.MODE_REMOTE) != 0);
		this.groupByDateDropDownAction.setChecked((this.options & ISVNHistoryView.GROUP_BY_DATE) != 0);
		boolean compareMode = SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_COMPARE_MODE);
		if (compareMode) {
			this.options |= ISVNHistoryView.COMPARE_MODE;
		}
		this.compareModeDropDownAction.setChecked((this.options & ISVNHistoryView.COMPARE_MODE) != 0);
		this.compareModeAction.setChecked((this.options & ISVNHistoryView.COMPARE_MODE) != 0);

		this.flatAction.setChecked(!hierarchicalAffectedView);
		this.hierarchicalAction.setChecked(hierarchicalAffectedView);
		this.history.setResourceTreeVisible(hierarchicalAffectedView);

		if (SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_PAGING_ENABLE_NAME)) {
			this.limit = SVNTeamPreferences.getHistoryInt(store, SVNTeamPreferences.HISTORY_PAGE_SIZE_NAME);
			this.getNextPageAction.setToolTipText("Show Next " + this.limit);
			this.options |= ISVNHistoryView.PAGING_ENABLED;
		}
		else {
			this.limit = 0;
			this.getNextPageAction.setToolTipText("Show Next Page");
			this.options &= ~ISVNHistoryView.PAGING_ENABLED;
		}
		this.setButtonsEnablement();
	}

	protected void setButtonsEnablement() {
		ILocalResource local = SVNRemoteStorage.instance().asLocalResource(this.wcResource);
		boolean isConnected = this.wcResource != null || this.repositoryResource != null;
		boolean enableRepo = (local != null && IStateFilter.SF_ONREPOSITORY.accept(local) || this.repositoryResource != null) && !this.pending;

		this.filterDropDownAction.setEnabled(enableRepo && this.repositoryResource != null && this.logMessages != null);
		this.clearFilterDropDownAction.setEnabled(this.isFilterEnabled());
		this.getNextPageAction.setEnabled(enableRepo && this.pagingEnabled & ((this.options & ISVNHistoryView.PAGING_ENABLED) != 0));
		this.getAllPagesAction.setEnabled(enableRepo && this.pagingEnabled & ((this.options & ISVNHistoryView.PAGING_ENABLED) != 0));

		this.stopOnCopyAction.setEnabled(enableRepo);
		this.stopOnCopyDropDownAction.setEnabled(enableRepo);
		this.hideUnrelatedAction.setEnabled(enableRepo);
		this.hideUnrelatedDropDownAction.setEnabled(enableRepo);
		this.collapseAllAction.setEnabled(isConnected);
		this.compareModeAction.setEnabled(isConnected);
		this.compareModeDropDownAction.setEnabled(isConnected);
		this.showBothAction.setEnabled(isConnected);
		this.showBothActionDropDown.setEnabled(isConnected);
		this.showLocalAction.setEnabled(isConnected);
		this.showLocalActionDropDown.setEnabled(isConnected);
		this.showRemoteAction.setEnabled(isConnected);
		this.showRemoteActionDropDown.setEnabled(isConnected);
		this.groupByDateAction.setEnabled(isConnected);
		this.groupByDateDropDownAction.setEnabled(isConnected);
	}

	protected void saveShowMode() {
		int prefToSet = SVNTeamPreferences.HISTORY_REVISION_MODE_REMOTE;
		if ((this.options & ISVNHistoryViewInfo.MODE_BOTH) != 0) {
			prefToSet = SVNTeamPreferences.HISTORY_REVISION_MODE_BOTH;
		}
		else if ((this.options & ISVNHistoryViewInfo.MODE_LOCAL) != 0) {
			prefToSet = SVNTeamPreferences.HISTORY_REVISION_MODE_LOCAL;
		}
		SVNHistoryPage.saveInt(SVNTeamPreferences.HISTORY_REVISION_MODE_NAME, prefToSet);
	}
	
	protected static void saveBoolean(String name, boolean value) {
		SVNTeamPreferences.setHistoryBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), name, value);
		SVNTeamUIPlugin.instance().savePluginPreferences();
	}
	
	protected static void saveInt(String name, int value) {
		SVNTeamPreferences.setHistoryInt(SVNTeamUIPlugin.instance().getPreferenceStore(), name, value);
		SVNTeamUIPlugin.instance().savePluginPreferences();
	}
	
    protected void disconnectView() {
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				SVNHistoryPage.this.clear();
			}
		});
    }
    
    protected void refreshChanges(final int refreshType) {
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				SVNHistoryPage.this.refresh(refreshType);
			}
		});
    }
    
}
