/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *    Thomas Champagne - Bug 217561 : additional date formats for label decorations
 *    Igor Burilo - Bug 211415: Export History log
 *******************************************************************************/

package org.eclipse.team.svn.ui.history;

import java.io.FileOutputStream;
import java.text.ParseException;
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
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.operation.remote.GetLogMessagesOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.events.IResourceStatesListener;
import org.eclipse.team.svn.core.resource.events.ResourceStatesChangedEvent;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.svnstorage.events.IRevisionPropertyChangeListener;
import org.eclipse.team.svn.core.svnstorage.events.RevisonPropertyChangeEvent;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.history.HistoryActionManager.HistoryAction;
import org.eclipse.team.svn.ui.history.data.SVNLocalFileRevision;
import org.eclipse.team.svn.ui.history.filter.AuthorNameLogEntryFilter;
import org.eclipse.team.svn.ui.history.filter.ChangeNameLogEntryFilter;
import org.eclipse.team.svn.ui.history.filter.CommentLogEntryFilter;
import org.eclipse.team.svn.ui.history.filter.CompositeLogEntryFilter;
import org.eclipse.team.svn.ui.history.filter.ILogEntryFilter;
import org.eclipse.team.svn.ui.history.model.ILogNode;
import org.eclipse.team.svn.ui.operation.CorrectRevisionOperation;
import org.eclipse.team.svn.ui.operation.UILoggedOperation;
import org.eclipse.team.svn.ui.panel.view.HistoryFilterPanel;
import org.eclipse.team.svn.ui.panel.view.HistoryRangePanel;
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

import com.ibm.icu.text.SimpleDateFormat;

/**
 * Generic HistoryView page
 * 
 * @author Alexander Gurov
 */
public class SVNHistoryPage extends HistoryPage implements ISVNHistoryView, IResourceStatesListener, IPropertyChangeListener, IRevisionPropertyChangeListener {
	protected IResource wcResource;
	protected IRepositoryResource repositoryResource;
	
	protected SVNRevision endRevision;
	protected SVNRevision startRevision;

	protected LogMessagesComposite history;

	protected CommentLogEntryFilter commentFilter;
	protected AuthorNameLogEntryFilter authorFilter;
	protected ChangeNameLogEntryFilter changeFilter;
	protected CompositeLogEntryFilter logEntriesFilter;

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
	protected Action revisionsRangeDropDownAction;
	protected Action hierarchicalAction;
	protected Action flatAction;
	protected Action compareModeAction;
	protected Action compareModeDropDownAction;
	protected Action groupByDateAction;
	protected Action showLocalAction;
	protected Action showRemoteAction;
	protected Action showBothAction;
	protected Action collapseAllAction;
	protected Action exportLogAction;
	protected Action showLocalActionDropDown;
	protected Action showRemoteActionDropDown;
	protected Action showBothActionDropDown;
	
	protected long limit = 25;
	protected boolean pagingEnabled = false;
	protected boolean pending;
	protected int options = 0;

	protected IResource compareWith;
	protected IRepositoryLocation currentlyInvolvedLocation;

	protected long currentRevision = 0;
	protected SVNLogEntry[] logMessages;
	protected SVNLocalFileRevision[] localHistory;

	protected HistoryActionManager actionManager;

	public SVNHistoryPage(Object input) {
		SVNRemoteStorage.instance().addResourceStatesListener(ResourceStatesChangedEvent.class, this);
		SVNRemoteStorage.instance().addRevisionPropertyChangeListener(this);
		this.actionManager = new HistoryActionManager(this);
		SVNTeamUIPlugin.instance().getPreferenceStore().addPropertyChangeListener(this);
		this.authorFilter = new AuthorNameLogEntryFilter();
		this.commentFilter = new CommentLogEntryFilter();
		this.changeFilter = new ChangeNameLogEntryFilter();
		this.logEntriesFilter = new CompositeLogEntryFilter(new ILogEntryFilter [] {this.authorFilter, this.commentFilter, this.changeFilter});
	}
	
	public void setStartRevision(SVNRevision startRevision)
	{
		this.startRevision = startRevision;
	}
	
	public SVNRevision getStartRevision()
	{
		return this.startRevision;
	}
	
	public void setEndRevision(SVNRevision endRevision)
	{
		this.endRevision = endRevision;
	}
	
	public SVNRevision getEndRevision()
	{
		return this.endRevision;
	}
	
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(SVNTeamPreferences.fullHistoryName(SVNTeamPreferences.HISTORY_PAGE_SIZE_NAME)) || 
			event.getProperty().equals(SVNTeamPreferences.fullHistoryName(SVNTeamPreferences.HISTORY_PAGING_ENABLE_NAME))) {
			this.refreshLimitOption(); 
		}
		if (event.getProperty().startsWith(SVNTeamPreferences.DATE_FORMAT_BASE)) {
			this.refresh(ISVNHistoryView.REFRESH_VIEW);
		}
		if (event.getProperty().equals(SVNTeamPreferences.fullMergeName(SVNTeamPreferences.MERGE_INCLUDE_MERGED_NAME))) {
			this.refresh(ISVNHistoryView.REFRESH_REMOTE);
		}
	}
	
	public void revisionPropertyChanged(RevisonPropertyChangeEvent event) {
		if (this.currentlyInvolvedLocation == null || !this.currentlyInvolvedLocation.equals(event.getLocation())){
			return;
		}
		if (event.getProperty().name.equals("svn:author") //$NON-NLS-1$
				|| event.getProperty().name.equals("svn:log") //$NON-NLS-1$
				|| event.getProperty().name.equals("svn:date")){ //$NON-NLS-1$
			for (int i = 0; i < this.logMessages.length; i++) {
				SVNLogEntry current = this.logMessages[i];
				if (SVNRevision.fromNumber(current.revision).equals(event.getRevision())) {
					if (event.getProperty().name.equals("svn:author")) { //$NON-NLS-1$
						this.logMessages[i] = new SVNLogEntry(current.revision,
												  current.date,
												  event.getProperty().value,
												  current.message,
												  current.changedPaths, 
												  current.hasChildren());
						if (current.hasChildren()) {
							this.logMessages[i].addAll(current.getChildren());
						}
					}
					if (event.getProperty().name.equals("svn:log")) { //$NON-NLS-1$
						this.logMessages[i] = new SVNLogEntry(current.revision,
												  current.date,
												  current.author,
												  event.getProperty().value,
												  current.changedPaths, 
												  current.hasChildren());
						if (current.hasChildren()) {
							this.logMessages[i].addAll(current.getChildren());
						}
					}
					if (event.getProperty().name.equals("svn:date")) { //$NON-NLS-1$
						try {
							this.logMessages[i] = new SVNLogEntry(current.revision,
												  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(event.getProperty().value).getTime(), //$NON-NLS-1$
												  current.author,
												  current.message,
												  current.changedPaths, 
												  current.hasChildren());
						}
						catch (ParseException ex) {
							// uninteresting in this context
						}
						if (current.hasChildren()) {
							this.logMessages[i].addAll(current.getChildren());
						}
					}
				}
			}
			UIMonitorUtility.getDisplay().syncExec(new Runnable() {
				public void run() {
					SVNHistoryPage.this.refresh(ISVNHistoryView.REFRESH_VIEW);
				}
			});
		}
	}
	
	public void resourcesStateChanged(ResourceStatesChangedEvent event) {
		if (this.wcResource == null) {
			return;
		}
		ILocalResource local = SVNRemoteStorage.instance().asLocalResource(this.wcResource);
		if (!IStateFilter.SF_INTERNAL_INVALID.accept(local)) {
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
	
	public void addFilter(ILogEntryFilter filter) {
		this.logEntriesFilter.addFilter(filter);
	}
	
	public void removeFilter(ILogEntryFilter filter) {
		this.logEntriesFilter.removeFilter(filter);
	}

	public void showHistory(IResource resource) {
		if (!resource.equals(this.wcResource)) {
			this.clear();

			this.wcResource = resource;
			this.currentlyInvolvedLocation = SVNRemoteStorage.instance().asRepositoryResource(this.wcResource).getRepositoryLocation();

			this.refresh(ISVNHistoryView.REFRESH_ALL);
		}
	}

	public void showHistory(IRepositoryResource remoteResource) {
		if (!remoteResource.equals(this.repositoryResource)) {
			this.clear();
			
			this.repositoryResource = remoteResource;
			this.currentlyInvolvedLocation = this.repositoryResource.getRepositoryLocation();
	
			this.refresh(ISVNHistoryView.REFRESH_ALL);
		}
	}

	public synchronized void clear() {
		this.currentRevision = SVNRevision.INVALID_REVISION_NUMBER;
		this.repositoryResource = null;
		this.wcResource = null;
		this.logMessages = null;
		this.localHistory = null;
		this.authorFilter.setAuthorNameToAccept(null);
		this.commentFilter.setCommentToAccept(null);
		this.pending = false;
		this.startRevision = this.endRevision = null;
		
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
		return SVNHistoryPage.filterMessages(this.logMessages, this.logEntriesFilter);
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
		return this.authorFilter.getAuthorNameToAccept() != null ||
				this.commentFilter.getCommentToAccept() != null ||
				this.changeFilter.getGangedPathToAccept() != null;
	}

	public int getOptions() {
		return this.options;
	}

	public void clearFilter() {
		this.authorFilter.setAuthorNameToAccept(null);
		this.commentFilter.setCommentToAccept(null);
		this.changeFilter.setGangedPathToAccept(null);
		this.clearFilterDropDownAction.setEnabled(false);
		this.history.refresh(LogMessagesComposite.REFRESH_ALL);
	}

	public void setFilter() {
		HistoryFilterPanel panel = new HistoryFilterPanel(this.authorFilter.getAuthorNameToAccept(),
															this.commentFilter.getCommentToAccept(),
															this.changeFilter.getGangedPathToAccept(),
															SVNHistoryPage.getSelectedAuthors(this.logMessages));
		DefaultDialog dialog = new DefaultDialog(this.getPartSite().getShell(), panel);
		if (dialog.open() == 0) {
			this.authorFilter.setAuthorNameToAccept(panel.getAuthor());
			this.commentFilter.setCommentToAccept(panel.getComment());
			this.changeFilter.setGangedPathToAccept(panel.getChangedPath());
			this.clearFilterDropDownAction.setEnabled(this.isFilterEnabled());
			this.history.refresh(LogMessagesComposite.REFRESH_ALL);
		}
	}

	public void refresh(int refreshType) {
		if (this.wcResource != null) {
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(this.wcResource);
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
					UILoggedOperation.reportError(SVNUIMessages.HistoryView_Name, ex);
				}
			}
		}

		if (this.repositoryResource != null && (refreshType == ISVNHistoryView.REFRESH_ALL || refreshType == ISVNHistoryView.REFRESH_REMOTE)) {
			this.logMessages = null;
			this.pending = true;
			this.setButtonsEnablement();
			this.history.refresh(LogMessagesComposite.REFRESH_ALL);
			GetLogMessagesOperation msgOp = this.createRemoteHistoryFetcher();
			if (this.startRevision != null) {
				msgOp.setStartRevision(this.startRevision);
			}
			msgOp.setLimit(this.limit);
			this.fetchRemoteHistory(msgOp);
		}
		else {
			this.setButtonsEnablement();
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
		SVNRemoteStorage.instance().removeRevisionPropertyChangeListener(this);
		SVNTeamUIPlugin.instance().getPreferenceStore().removePropertyChangeListener(this);
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
		return SVNUIMessages.SVNView_ResourceNotSelected;
	}

	public boolean isValidInput(Object object) {
		return SVNHistoryPage.isValidData(object);
	}

	public void refresh() {
		this.refresh(ISVNHistoryView.REFRESH_ALL);
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		return null;
	}

	public void createControl(Composite parent) {
		IActionBars actionBars = this.getHistoryPageSite().getWorkbenchPageSite().getActionBars();
		
		this.groupByDateAction = new HistoryAction(SVNUIMessages.HistoryView_GroupByDate, "icons/views/history/group_by_date.gif", IAction.AS_CHECK_BOX) { //$NON-NLS-1$
			public void run() {
				SVNHistoryPage.this.options ^= ISVNHistoryView.GROUP_BY_DATE;
				SVNHistoryPage.this.groupByDateDropDownAction.setChecked((SVNHistoryPage.this.options & ISVNHistoryView.GROUP_BY_DATE) != 0);
				SVNHistoryPage.saveInt(SVNTeamPreferences.HISTORY_GROUPING_TYPE_NAME, (SVNHistoryPage.this.options & ISVNHistoryView.GROUP_BY_DATE) == 0 ? SVNTeamPreferences.HISTORY_GROUPING_TYPE_NONE : SVNTeamPreferences.HISTORY_GROUPING_TYPE_DATE);
				SVNHistoryPage.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
			}
		};
		
		this.showBothAction = new HistoryAction(SVNUIMessages.HistoryView_ShowBoth, "icons/views/history/both_history_mode.gif", IAction.AS_RADIO_BUTTON) { //$NON-NLS-1$
			public void run() {
				SVNHistoryPage.this.options = SVNHistoryPage.this.options & ~(ISVNHistoryViewInfo.MODE_LOCAL | ISVNHistoryViewInfo.MODE_REMOTE) | ISVNHistoryViewInfo.MODE_BOTH;
				SVNHistoryPage.this.showBothActionDropDown.setChecked(true);
				SVNHistoryPage.this.showLocalActionDropDown.setChecked(false);
				SVNHistoryPage.this.showRemoteActionDropDown.setChecked(false);
				SVNHistoryPage.this.saveShowMode();
				SVNHistoryPage.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
			}
		};
		this.showLocalAction = new HistoryAction(SVNUIMessages.HistoryView_ShowLocal, "icons/views/history/local_history_mode.gif", IAction.AS_RADIO_BUTTON) { //$NON-NLS-1$
			public void run() {
				SVNHistoryPage.this.options = SVNHistoryPage.this.options & ~(ISVNHistoryViewInfo.MODE_REMOTE | ISVNHistoryViewInfo.MODE_BOTH) | ISVNHistoryViewInfo.MODE_LOCAL;
				SVNHistoryPage.this.showLocalActionDropDown.setChecked(true);
				SVNHistoryPage.this.showRemoteActionDropDown.setChecked(false);
				SVNHistoryPage.this.showBothActionDropDown.setChecked(false);
				SVNHistoryPage.this.saveShowMode();
				SVNHistoryPage.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
			}
		};
		this.showRemoteAction = new HistoryAction(SVNUIMessages.HistoryView_ShowRemote, "icons/views/history/remote_history_mode.gif", IAction.AS_RADIO_BUTTON) { //$NON-NLS-1$
			public void run() {
				SVNHistoryPage.this.options = SVNHistoryPage.this.options & ~(ISVNHistoryViewInfo.MODE_LOCAL | ISVNHistoryViewInfo.MODE_BOTH) | ISVNHistoryViewInfo.MODE_REMOTE;
				SVNHistoryPage.this.showRemoteActionDropDown.setChecked(true);
				SVNHistoryPage.this.showLocalActionDropDown.setChecked(false);
				SVNHistoryPage.this.showBothActionDropDown.setChecked(false);
				SVNHistoryPage.this.saveShowMode();
				SVNHistoryPage.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
			}
		};
		
		this.hideUnrelatedAction = new HistoryAction(SVNUIMessages.HistoryView_HideUnrelatedPaths, "icons/views/history/hide_unrelated.gif", IAction.AS_CHECK_BOX) { //$NON-NLS-1$
			public void run() {
				SVNHistoryPage.this.options ^= ISVNHistoryView.HIDE_UNRELATED;
				SVNHistoryPage.this.hideUnrelatedDropDownAction.setChecked((SVNHistoryPage.this.options & ISVNHistoryView.HIDE_UNRELATED) != 0);
				SVNHistoryPage.this.history.refresh(LogMessagesComposite.REFRESH_UI_AFFECTED);
			}
		};
		this.stopOnCopyAction = new HistoryAction(SVNUIMessages.HistoryView_StopOnCopy, "icons/views/history/stop_on_copy.gif", IAction.AS_CHECK_BOX) { //$NON-NLS-1$
			public void run() {
				SVNHistoryPage.this.options ^= ISVNHistoryView.STOP_ON_COPY;
				SVNHistoryPage.this.stopOnCopyDropDownAction.setChecked((SVNHistoryPage.this.options & ISVNHistoryView.STOP_ON_COPY) != 0);
				SVNHistoryPage.this.refresh(ISVNHistoryView.REFRESH_REMOTE);
			}
		};
		
		this.getNextPageAction = new HistoryAction(SVNUIMessages.HistoryView_GetNextPage, "icons/views/history/paging.gif") { //$NON-NLS-1$
			public void run() {
				GetLogMessagesOperation msgOp = SVNHistoryPage.this.createRemoteHistoryFetcher();
				msgOp.setLimit(SVNHistoryPage.this.limit + 1);
				if (SVNHistoryPage.this.logMessages != null) {
					SVNLogEntry lm = SVNHistoryPage.this.logMessages[SVNHistoryPage.this.logMessages.length - 1];
					msgOp.setStartRevision(SVNRevision.fromNumber(lm.revision));
				}
				SVNHistoryPage.this.fetchRemoteHistory(msgOp);
			}
		};
		String msg = this.limit > 0 ? SVNUIMessages.format(SVNUIMessages.HistoryView_ShowNextX, new String[] { String.valueOf(this.limit) }) : SVNUIMessages.HistoryView_ShowNextPage;
		this.getNextPageAction.setToolTipText(msg);
		this.getAllPagesAction = new HistoryAction(SVNUIMessages.HistoryView_ShowAll, "icons/views/history/paging_all.gif") { //$NON-NLS-1$
			public void run() {
				GetLogMessagesOperation msgOp = SVNHistoryPage.this.createRemoteHistoryFetcher();
				msgOp.setLimit(0);
				if (SVNHistoryPage.this.logMessages != null) {
					SVNLogEntry lm = SVNHistoryPage.this.logMessages[SVNHistoryPage.this.logMessages.length - 1];
					msgOp.setStartRevision(SVNRevision.fromNumber(lm.revision));
				}
				SVNHistoryPage.this.fetchRemoteHistory(msgOp);
			}
		};
		
		this.collapseAllAction = new HistoryAction(SVNUIMessages.RepositoriesView_CollapseAll_Label, "icons/common/collapseall.gif") { //$NON-NLS-1$
			public void run() {
				SVNHistoryPage.this.history.collapseAll();
			}
		};
		this.compareModeAction = new HistoryAction(SVNUIMessages.HistoryView_CompareMode, "icons/views/history/compare_mode.gif", IAction.AS_CHECK_BOX) { //$NON-NLS-1$
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
		this.showCommentViewerAction = new HistoryAction(SVNUIMessages.HistoryView_ShowCommentViewer) {
			public void run() {
				SVNHistoryPage.this.history.setCommentViewerVisible(SVNHistoryPage.this.showCommentViewerAction.isChecked());
				SVNHistoryPage.saveBoolean(SVNTeamPreferences.HISTORY_SHOW_MULTILINE_COMMENT_NAME, this.isChecked());
			}
		};
		this.showAffectedPathsViewerAction = new HistoryAction(SVNUIMessages.HistoryView_ShowAffectedPathsViewer) {
			public void run() {
				boolean showAffected = this.isChecked();
				SVNHistoryPage.this.history.setAffectedPathsViewerVisible(showAffected);
				SVNHistoryPage.this.flatAction.setEnabled(showAffected);
				SVNHistoryPage.this.hierarchicalAction.setEnabled(showAffected);
				SVNHistoryPage.saveBoolean(SVNTeamPreferences.HISTORY_SHOW_AFFECTED_PATHS_NAME, showAffected);
			}
		};

		this.hideUnrelatedDropDownAction = new HistoryAction(SVNUIMessages.HistoryView_HideUnrelatedPaths, "icons/views/history/hide_unrelated.gif") { //$NON-NLS-1$
			public void run() {
				SVNHistoryPage.this.options ^= ISVNHistoryView.HIDE_UNRELATED;
				SVNHistoryPage.this.hideUnrelatedAction.setChecked((SVNHistoryPage.this.options & ISVNHistoryView.HIDE_UNRELATED) != 0);
				SVNHistoryPage.this.history.refresh(LogMessagesComposite.REFRESH_UI_AFFECTED);
			}
		};
		this.stopOnCopyDropDownAction = new HistoryAction(SVNUIMessages.HistoryView_StopOnCopy, "icons/views/history/stop_on_copy.gif") { //$NON-NLS-1$
			public void run() {
				SVNHistoryPage.this.options ^= ISVNHistoryView.STOP_ON_COPY;
				SVNHistoryPage.this.stopOnCopyAction.setChecked((SVNHistoryPage.this.options & ISVNHistoryView.STOP_ON_COPY) != 0);
				SVNHistoryPage.this.refresh(ISVNHistoryView.REFRESH_REMOTE);
			}
		};

		this.groupByDateDropDownAction = new HistoryAction(SVNUIMessages.HistoryView_GroupByDate, "icons/views/history/group_by_date.gif") { //$NON-NLS-1$
			public void run() {
				SVNHistoryPage.this.options ^= ISVNHistoryView.GROUP_BY_DATE;
				SVNHistoryPage.this.groupByDateAction.setChecked((SVNHistoryPage.this.options & ISVNHistoryView.GROUP_BY_DATE) != 0);
				SVNHistoryPage.saveInt(SVNTeamPreferences.HISTORY_GROUPING_TYPE_NAME, (SVNHistoryPage.this.options & ISVNHistoryView.GROUP_BY_DATE) == 0 ? SVNTeamPreferences.HISTORY_GROUPING_TYPE_NONE : SVNTeamPreferences.HISTORY_GROUPING_TYPE_DATE);
				SVNHistoryPage.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
			}
		};

		this.exportLogAction = new HistoryAction(SVNUIMessages.HistoryView_ExportLog) {
			public void run() {
				FileDialog dlg = new FileDialog(UIMonitorUtility.getShell(), SWT.PRIMARY_MODAL | SWT.SAVE);
				dlg.setText(SVNUIMessages.ExportLogDialog_Title);
				String caption = SVNHistoryPage.this.getName();
				dlg.setFileName(caption.substring(caption.lastIndexOf('/') + 1) + "_history.log"); //$NON-NLS-1$
				dlg.setFilterExtensions(new String[] {"log", "*.*"}); //$NON-NLS-1$ //$NON-NLS-2$
				String file = dlg.open();
				if (file != null) {
					ILogNode input = (ILogNode)SVNHistoryPage.this.history.getTreeViewer().getInput();
					try {
						FileOutputStream stream = new FileOutputStream(file);
						stream.write(SVNHistoryPage.this.actionManager.getSelectedMessagesAsString(new ILogNode [] {input}).getBytes());
						stream.flush();
						stream.close();
					}
					catch (Exception ex) {
						LoggedOperation.reportError(SVNUIMessages.Operation_ExportLog, ex);
					}
				}
			}
		};
		
		this.showBothActionDropDown = new HistoryAction(SVNUIMessages.HistoryView_ShowBoth, "icons/views/history/both_history_mode.gif", IAction.AS_RADIO_BUTTON) { //$NON-NLS-1$
			public void run() {
				SVNHistoryPage.this.options = SVNHistoryPage.this.options & ~(ISVNHistoryViewInfo.MODE_LOCAL | ISVNHistoryViewInfo.MODE_REMOTE) | ISVNHistoryViewInfo.MODE_BOTH;
				SVNHistoryPage.this.showBothAction.setChecked(true);
				SVNHistoryPage.this.showLocalAction.setChecked(false);
				SVNHistoryPage.this.showRemoteAction.setChecked(false);
				SVNHistoryPage.this.saveShowMode();
				SVNHistoryPage.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
			}
		};
		this.showLocalActionDropDown = new HistoryAction(SVNUIMessages.HistoryView_ShowLocal, "icons/views/history/local_history_mode.gif", IAction.AS_RADIO_BUTTON) { //$NON-NLS-1$
			public void run() {
				SVNHistoryPage.this.options = SVNHistoryPage.this.options & ~(ISVNHistoryViewInfo.MODE_REMOTE | ISVNHistoryViewInfo.MODE_BOTH) | ISVNHistoryViewInfo.MODE_LOCAL;
				SVNHistoryPage.this.showBothAction.setChecked(false);
				SVNHistoryPage.this.showLocalAction.setChecked(true);
				SVNHistoryPage.this.showRemoteAction.setChecked(false);
				SVNHistoryPage.this.saveShowMode();
				SVNHistoryPage.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
			}
		};
		this.showRemoteActionDropDown = new HistoryAction(SVNUIMessages.HistoryView_ShowRemote, "icons/views/history/remote_history_mode.gif", IAction.AS_RADIO_BUTTON) { //$NON-NLS-1$
			public void run() {
				SVNHistoryPage.this.options = SVNHistoryPage.this.options & ~(ISVNHistoryViewInfo.MODE_LOCAL | ISVNHistoryViewInfo.MODE_BOTH) | ISVNHistoryViewInfo.MODE_REMOTE;
				SVNHistoryPage.this.showBothAction.setChecked(false);
				SVNHistoryPage.this.showLocalAction.setChecked(false);
				SVNHistoryPage.this.showRemoteAction.setChecked(true);
				SVNHistoryPage.this.saveShowMode();
				SVNHistoryPage.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
			}
		};

		this.revisionsRangeDropDownAction = new HistoryAction(SVNUIMessages.HistoryView_RevisionsRange) {
			public void run() {
				SVNHistoryPage.this.defineRevisionsRange();
			}
		};

		this.filterDropDownAction = new HistoryAction(SVNUIMessages.HistoryView_QuickFilter, "icons/views/history/filter.gif") { //$NON-NLS-1$
			public void run() {
				SVNHistoryPage.this.setFilter();
			}
		};
		this.clearFilterDropDownAction = new HistoryAction(SVNUIMessages.HistoryView_ClearFilter, "icons/views/history/clear_filter.gif") { //$NON-NLS-1$
			public void run() {
				SVNHistoryPage.this.clearFilter();
			}
		};
		
		this.compareModeDropDownAction = new HistoryAction(SVNUIMessages.HistoryView_CompareMode, "icons/views/history/compare_mode.gif") { //$NON-NLS-1
			public void run() {
				SVNHistoryPage.this.options ^= ISVNHistoryView.COMPARE_MODE;
				SVNHistoryPage.this.compareModeAction.setChecked((SVNHistoryPage.this.options & ISVNHistoryView.COMPARE_MODE) != 0);
				SVNHistoryPage.saveBoolean(SVNTeamPreferences.HISTORY_COMPARE_MODE, (SVNHistoryPage.this.options & ISVNHistoryView.COMPARE_MODE) != 0);
			}
		};

		this.flatAction = new HistoryAction(SVNUIMessages.HistoryView_Flat, "icons/views/history/flat_layout.gif", IAction.AS_RADIO_BUTTON) { //$NON-NLS-1$
			public void run() {
				SVNHistoryPage.this.history.setResourceTreeVisible(false);
				SVNHistoryPage.saveBoolean(SVNTeamPreferences.HISTORY_HIERARCHICAL_LAYOUT, false);
			}
		};
		this.hierarchicalAction = new HistoryAction(SVNUIMessages.HistoryView_Hierarchical, "icons/views/history/tree_layout.gif", IAction.AS_RADIO_BUTTON) { //$NON-NLS-1$
			public void run() {
				SVNHistoryPage.this.history.setResourceTreeVisible(true);
				SVNHistoryPage.saveBoolean(SVNTeamPreferences.HISTORY_HIERARCHICAL_LAYOUT, true);
			}
		};

		IMenuManager actionBarsMenu = actionBars.getMenuManager();
		actionBarsMenu.add(this.showCommentViewerAction);
		actionBarsMenu.add(this.showAffectedPathsViewerAction);
		MenuManager sub = new MenuManager(SVNUIMessages.HistoryView_AffectedPathLayout, IWorkbenchActionConstants.GROUP_MANAGING);
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
		actionBarsMenu.add(this.revisionsRangeDropDownAction);
		actionBarsMenu.add(this.filterDropDownAction);
		actionBarsMenu.add(this.clearFilterDropDownAction);
		actionBarsMenu.add(this.exportLogAction);
		actionBarsMenu.add(new Separator());
		actionBarsMenu.add(this.compareModeDropDownAction);

		this.history = new LogMessagesComposite(parent, true, false, this);

		GridData data = new GridData(GridData.FILL_BOTH);
		this.history.setLayoutData(data);

		this.history.registerActionManager(this.actionManager, this.getPartSite());

		// Setting context help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.team.svn.help.historyViewContext"); //$NON-NLS-1$

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

	public static SVNLogEntry[] filterMessages(SVNLogEntry[] msgs, ILogEntryFilter filter) {
		if (msgs == null) {
			return null;
		}
		ArrayList<SVNLogEntry> filteredMessages = new ArrayList<SVNLogEntry>();
		for (int i = 0; i < msgs.length; i++) {
			if (filter.accept(msgs[i])) {
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
	
	protected GetLogMessagesOperation createRemoteHistoryFetcher() {
		GetLogMessagesOperation msgOp = new GetLogMessagesOperation(this.repositoryResource, this.stopOnCopyAction.isChecked());
		if (this.endRevision != null) {
			msgOp.setEndRevision(this.endRevision);
		}
		
		/* if merge info isn't supported by server then retry without it,
		 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=257669
		 */		
		msgOp.setRetryIfMergeInfoNotSupported(true);
		// set revision range here
		return msgOp;
	}

	protected void fetchRemoteHistory(final GetLogMessagesOperation msgsOp) {
		msgsOp.setIncludeMerged(SVNTeamPreferences.getMergeBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.MERGE_INCLUDE_MERGED_NAME));
		
		final IStructuredSelection selected = (IStructuredSelection) this.history.getTreeViewer().getSelection();
		IActionOperation showOp = new AbstractActionOperation("Operation_HShowHistory", SVNUIMessages.class) { //$NON-NLS-1$
			private long revision = SVNHistoryPage.this.currentRevision;

			protected void runImpl(IProgressMonitor monitor) throws Exception {
				IRepositoryResource resource = SVNHistoryPage.this.repositoryResource;
				if (msgsOp.getExecutionState() != IActionOperation.OK || resource == null) {
					SVNHistoryPage.this.pending = false;
					UIMonitorUtility.getDisplay().syncExec(new Runnable() {
						public void run() {
							SVNHistoryPage.this.history.refresh(LogMessagesComposite.REFRESH_ALL);
						}
					});
					return;
				}
				if (SVNHistoryPage.this.wcResource == null) {
					this.revision = resource.getRevision();
				}

				if (!resource.equals(msgsOp.getResource())) {
					return;
				}

				SVNHistoryPage.this.currentRevision = this.revision;
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
							SVNHistoryPage.this.history.refresh(LogMessagesComposite.REFRESH_UI_AFFECTED);
						}
					}
				});
			}
		};
		CompositeOperation op = new CompositeOperation(showOp.getId(), showOp.getMessagesClass(), true);
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

		boolean compareMode = SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_COMPARE_MODE);
		if (compareMode) {
			this.options |= ISVNHistoryView.COMPARE_MODE;
		}
		this.options |= groupingType == SVNTeamPreferences.HISTORY_GROUPING_TYPE_DATE ? ISVNHistoryView.GROUP_BY_DATE : 0;
		this.options = this.options & ~(ISVNHistoryViewInfo.MODE_BOTH | ISVNHistoryViewInfo.MODE_LOCAL | ISVNHistoryViewInfo.MODE_REMOTE) | revisionMode;
		
		this.hideUnrelatedDropDownAction.setChecked((this.options & ISVNHistoryView.HIDE_UNRELATED) != 0);
		this.hideUnrelatedAction.setChecked((this.options & ISVNHistoryView.HIDE_UNRELATED) != 0);
		this.stopOnCopyDropDownAction.setChecked((this.options & ISVNHistoryView.STOP_ON_COPY) != 0);
		this.stopOnCopyAction.setChecked((this.options & ISVNHistoryView.STOP_ON_COPY) != 0);
		this.groupByDateAction.setChecked((this.options & ISVNHistoryView.GROUP_BY_DATE) != 0);
		this.showBothAction.setChecked((this.options & ISVNHistoryViewInfo.MODE_BOTH) != 0);
		this.showBothActionDropDown.setChecked((this.options & ISVNHistoryViewInfo.MODE_BOTH) != 0);
		this.showLocalAction.setChecked((this.options & ISVNHistoryViewInfo.MODE_LOCAL) != 0);
		this.showLocalActionDropDown.setChecked((this.options & ISVNHistoryViewInfo.MODE_LOCAL) != 0);
		this.showRemoteAction.setChecked((this.options & ISVNHistoryViewInfo.MODE_REMOTE) != 0);
		this.showRemoteActionDropDown.setChecked((this.options & ISVNHistoryViewInfo.MODE_REMOTE) != 0);
		this.groupByDateDropDownAction.setChecked((this.options & ISVNHistoryView.GROUP_BY_DATE) != 0);
		this.compareModeDropDownAction.setChecked((this.options & ISVNHistoryView.COMPARE_MODE) != 0);
		this.compareModeAction.setChecked((this.options & ISVNHistoryView.COMPARE_MODE) != 0);

		this.flatAction.setChecked(!hierarchicalAffectedView);
		this.hierarchicalAction.setChecked(hierarchicalAffectedView);
		this.history.setResourceTreeVisible(hierarchicalAffectedView);

		this.refreshLimitOption();
		this.setButtonsEnablement();
	}
	
	protected void defineRevisionsRange() {
		HistoryRangePanel panel = new HistoryRangePanel(this.repositoryResource, this.startRevision, this.endRevision);
		DefaultDialog dialog = new DefaultDialog(this.getPartSite().getShell(), panel);
		if (dialog.open() == 0) {
			this.startRevision = panel.getStartRevision();
			this.endRevision = panel.getStopRevision();
			this.refresh(ISVNHistoryView.REFRESH_REMOTE);
		}
	}
	
	protected void refreshLimitOption() {
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		
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
	}

	protected void setButtonsEnablement() {
		ILocalResource local = SVNRemoteStorage.instance().asLocalResource(this.wcResource);
		boolean enableRepo = (IStateFilter.SF_ONREPOSITORY.accept(local) || this.repositoryResource != null) && !this.pending;
		
		this.revisionsRangeDropDownAction.setEnabled(this.repositoryResource != null);
		this.filterDropDownAction.setEnabled(enableRepo && this.repositoryResource != null && this.logMessages != null);
		this.clearFilterDropDownAction.setEnabled(this.isFilterEnabled());
		this.getNextPageAction.setEnabled(enableRepo && this.pagingEnabled & ((this.options & ISVNHistoryView.PAGING_ENABLED) != 0));
		this.getAllPagesAction.setEnabled(enableRepo && this.pagingEnabled & ((this.options & ISVNHistoryView.PAGING_ENABLED) != 0));

		this.stopOnCopyAction.setEnabled(enableRepo);
		this.stopOnCopyDropDownAction.setEnabled(enableRepo);
		this.hideUnrelatedAction.setEnabled(enableRepo);
		this.hideUnrelatedDropDownAction.setEnabled(enableRepo);
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
