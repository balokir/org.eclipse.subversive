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
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevision.Kind;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.GetLogMessagesOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.core.utility.StringMatcher;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.history.data.SVNLocalFileRevision;
import org.eclipse.team.svn.ui.history.model.ILogNode;
import org.eclipse.team.svn.ui.operation.CorrectRevisionOperation;
import org.eclipse.team.svn.ui.operation.UILoggedOperation;
import org.eclipse.team.svn.ui.panel.view.HistoryFilterPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.ui.history.HistoryPage;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;

/**
 * Internals of the HistoryView. Can be used in Eclipse 3.2 generic HistoryView or in our own HistoryView
 * 
 * @author Alexander Gurov
 */
public class HistoryViewImpl implements ISVNHistoryView {
	public static final int PAGING_ENABLED = 0x01;
	public static final int COMPARE_MODE = 0x02;
	public static final int HIDE_UNRELATED = 0x04;
	public static final int STOP_ON_COPY = 0x08;
	public static final int GROUP_BY_DATE = 0x10;
	/*0x20, 0x40, 0x80 are reserved. Used from LogMessagesComposite*/
	
	protected IResource wcResource;
	protected IRepositoryResource repositoryResource;
	protected HistoryPage page;
	
	protected IViewInfoProvider viewInfoProvider;
	
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
	protected boolean isCommentFilterEnabled = false;
	protected int options = 0;
	protected long currentRevision = 0;
	protected ILocalResource compareWith;
	
	protected IPropertyChangeListener configurationListener;

	protected SVNLogEntry []logMessages;
	protected SVNLocalFileRevision []localHistory;
	
	protected HistoryActionManager actionManager;
	
	public HistoryViewImpl(IResource wcResource, IRepositoryResource repositoryResource, IViewInfoProvider viewInfoProvider) {
		this.wcResource = wcResource;
		this.repositoryResource = repositoryResource;
		
		this.viewInfoProvider = viewInfoProvider;
		
		this.filterByAuthor = "";
		this.filterByComment = "";
		
		this.actionManager = new HistoryActionManager(this);
		
		this.configurationListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().startsWith(SVNTeamPreferences.HISTORY_BASE)) {
					HistoryViewImpl.this.refreshOptionButtons();
				}
			}
		};
		SVNTeamUIPlugin.instance().getPreferenceStore().addPropertyChangeListener(this.configurationListener);
	}
	
	public IWorkbenchPartSite getSite() {
		return this.viewInfoProvider.getPartSite();
	}
	
	public IActionBars getActionBars() {
		return this.viewInfoProvider.getActionBars();
	}

	public void setCompareWith(ILocalResource compareWith) {
		this.compareWith = compareWith;
	}
	
	public void setOptions(int mask, int values) {
		this.options = (this.options & ~mask) | (mask & values);
        IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
        SVNTeamPreferences.setHistoryBoolean(store, SVNTeamPreferences.HISTORY_COMPARE_MODE, (HistoryViewImpl.this.options & HistoryViewImpl.COMPARE_MODE) != 0);
        SVNTeamPreferences.setHistoryBoolean(store, SVNTeamPreferences.HISTORY_PAGING_ENABLE_NAME, (HistoryViewImpl.this.options & HistoryViewImpl.PAGING_ENABLED) != 0);
        this.refreshOptionButtons();
	}
	
	public IResource getResource() {
		return this.wcResource;
	}
	
	public IRepositoryResource getRepositoryResource() {
		return this.repositoryResource;
	}

	public void dispose() {
		SVNTeamUIPlugin.instance().getPreferenceStore().removePropertyChangeListener(this.configurationListener);
		this.history.dispose();
	}

	public Control getControl() {
		return this.history;
	}

	public void createPartControl(Composite parent) {
	    this.createToolBar();
		
		this.history = new LogMessagesComposite(parent, true, this);
		
		GridData data = new GridData(GridData.FILL_BOTH);
		this.history.setLayoutData(data);
		
        this.history.registerActionManager(this.actionManager, this.getSite());
        
        //drop-down menu
        IActionBars actionBars = this.getActionBars();	    
	    IMenuManager actionBarsMenu = actionBars.getMenuManager();
	    this.showCommentViewerAction = new Action (SVNTeamUIPlugin.instance().getResource("HistoryView.ShowCommentViewer")) {
	        public void run() {
				IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
				boolean showMultiline = SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_SHOW_MULTILINE_COMMENT_NAME);
				SVNTeamPreferences.setHistoryBoolean(store, SVNTeamPreferences.HISTORY_SHOW_MULTILINE_COMMENT_NAME, !showMultiline);
				SVNTeamUIPlugin.instance().savePluginPreferences();
	        }	        
	    };
	    this.showAffectedPathsViewerAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.ShowAffectedPathsViewer")) {
	        public void run() {
				IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
				boolean showAffected = SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_SHOW_AFFECTED_PATHS_NAME);
				SVNTeamPreferences.setHistoryBoolean(store, SVNTeamPreferences.HISTORY_SHOW_AFFECTED_PATHS_NAME, !showAffected);
				SVNTeamUIPlugin.instance().savePluginPreferences();
	        }
	    };
	    
	    this.hideUnrelatedDropDownAction = new Action (SVNTeamUIPlugin.instance().getResource("HistoryView.HideUnrelatedPaths")) {
	        public void run() {
	        	HistoryViewImpl.this.options ^= HistoryViewImpl.HIDE_UNRELATED;
	        	HistoryViewImpl.this.history.refresh(LogMessagesComposite.REFRESH_UI_AFFECTED);
	        	HistoryViewImpl.this.hideUnrelatedAction.setChecked((HistoryViewImpl.this.options & HistoryViewImpl.HIDE_UNRELATED) != 0);
	        }	        
	    };
	    this.hideUnrelatedDropDownAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/hide_unrelated.gif"));
	    this.stopOnCopyDropDownAction = new Action (SVNTeamUIPlugin.instance().getResource("HistoryView.StopOnCopy")) {
	    	public void run() {
	    		HistoryViewImpl.this.options ^= HistoryViewImpl.STOP_ON_COPY;
	    		HistoryViewImpl.this.stopOnCopyAction.setChecked((HistoryViewImpl.this.options & HistoryViewImpl.STOP_ON_COPY) != 0);
	    		HistoryViewImpl.this.refresh();
	        }
	    };
	    this.stopOnCopyDropDownAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/stop_on_copy.gif"));
	    this.groupByDateDropDownAction = new Action (SVNTeamUIPlugin.instance().getResource("HistoryView.GroupByDate")) {
	    	public void run() {
	    		HistoryViewImpl.this.options ^= HistoryViewImpl.GROUP_BY_DATE;
	    		HistoryViewImpl.this.groupByDateAction.setChecked((HistoryViewImpl.this.options & HistoryViewImpl.GROUP_BY_DATE) != 0);
	    		HistoryViewImpl.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
				IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
				int type = SVNTeamPreferences.getHistoryInt(store, SVNTeamPreferences.HISTORY_GROUPING_TYPE_NAME);
				SVNTeamPreferences.setHistoryInt(store, SVNTeamPreferences.HISTORY_GROUPING_TYPE_NAME, type == SVNTeamPreferences.HISTORY_GROUPING_TYPE_DATE ? SVNTeamPreferences.HISTORY_GROUPING_TYPE_NONE : SVNTeamPreferences.HISTORY_GROUPING_TYPE_DATE);
	        }
	    };	    
	    this.groupByDateDropDownAction.setToolTipText(SVNTeamUIPlugin.instance().getResource("HistoryView.GroupByDate"));
	    this.groupByDateDropDownAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/group_by_date.gif"));
	    this.showBothActionDropDown = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.ShowBoth"), IAction.AS_RADIO_BUTTON) {
	        public void run() {
	        	HistoryViewImpl.this.options = HistoryViewImpl.this.options & ~(ISVNHistoryViewInfo.MODE_LOCAL | ISVNHistoryViewInfo.MODE_REMOTE) | ISVNHistoryViewInfo.MODE_BOTH;
	        	HistoryViewImpl.this.showBothAction.setChecked(true);
	        	HistoryViewImpl.this.showLocalActionDropDown.setChecked(false);
	        	HistoryViewImpl.this.showRemoteActionDropDown.setChecked(false);
	        	HistoryViewImpl.this.showLocalAction.setChecked(false);
	        	HistoryViewImpl.this.showRemoteAction.setChecked(false);
	        	HistoryViewImpl.this.setRevMode();
		        HistoryViewImpl.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
	        }
	    };
	    this.showBothActionDropDown.setToolTipText(SVNTeamUIPlugin.instance().getResource("HistoryView.ShowBoth"));
	    this.showBothActionDropDown.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/both_history_mode.gif"));
	    this.showRemoteActionDropDown = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.ShowRemote"), IAction.AS_RADIO_BUTTON) {
	        public void run() {
	        	HistoryViewImpl.this.options = HistoryViewImpl.this.options & ~(ISVNHistoryViewInfo.MODE_LOCAL | ISVNHistoryViewInfo.MODE_BOTH) | ISVNHistoryViewInfo.MODE_REMOTE;
	        	HistoryViewImpl.this.showRemoteAction.setChecked(true);
	        	HistoryViewImpl.this.showLocalActionDropDown.setChecked(false);
	        	HistoryViewImpl.this.showBothActionDropDown.setChecked(false);
	        	HistoryViewImpl.this.showLocalAction.setChecked(false);
	        	HistoryViewImpl.this.showBothAction.setChecked(false);
	        	HistoryViewImpl.this.setRevMode();
		        HistoryViewImpl.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
	        }
	    };
	    this.showRemoteActionDropDown.setToolTipText(SVNTeamUIPlugin.instance().getResource("HistoryView.ShowRemote"));
	    this.showRemoteActionDropDown.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/remote_history_mode.gif"));
	    this.showLocalActionDropDown = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.ShowLocal"), IAction.AS_RADIO_BUTTON) {
	        public void run() {
	        	HistoryViewImpl.this.options = HistoryViewImpl.this.options & ~(ISVNHistoryViewInfo.MODE_REMOTE | ISVNHistoryViewInfo.MODE_BOTH) | ISVNHistoryViewInfo.MODE_LOCAL;
	        	HistoryViewImpl.this.showLocalAction.setChecked(true);
	        	HistoryViewImpl.this.showRemoteActionDropDown.setChecked(false);
	        	HistoryViewImpl.this.showBothActionDropDown.setChecked(false);
	        	HistoryViewImpl.this.showRemoteAction.setChecked(false);
	        	HistoryViewImpl.this.showBothAction.setChecked(false);
	        	HistoryViewImpl.this.setRevMode();
		        HistoryViewImpl.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
	        }
	    };
	    this.showLocalActionDropDown.setToolTipText(SVNTeamUIPlugin.instance().getResource("HistoryView.ShowLocal"));
	    this.showLocalActionDropDown.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/local_history_mode.gif"));
	    this.filterDropDownAction = new Action (SVNTeamUIPlugin.instance().getResource("HistoryView.QuickFilter")) {
	    	public void run() {
	    		HistoryViewImpl.this.setFilter();
	        }
	    };
	    this.filterDropDownAction.setToolTipText(SVNTeamUIPlugin.instance().getResource("HistoryView.QuickFilter"));
	    this.filterDropDownAction.setHoverImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/filter.gif"));
	    this.clearFilterDropDownAction = new Action (SVNTeamUIPlugin.instance().getResource("HistoryView.ClearFilter")) {
	    	public void run() {
	    		HistoryViewImpl.this.clearFilter();
	    		HistoryViewImpl.this.showHistoryImpl(null, false);
	        }
	    };
	    this.clearFilterDropDownAction.setDisabledImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/clear.gif"));
	    this.clearFilterDropDownAction.setHoverImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/clear_filter.gif"));
	    this.compareModeDropDownAction = new Action (SVNTeamUIPlugin.instance().getResource("HistoryView.CompareMode")) {
	    	public void run() {
	    		HistoryViewImpl.this.options ^= HistoryViewImpl.COMPARE_MODE;
	            IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
	            SVNTeamPreferences.setHistoryBoolean(store, SVNTeamPreferences.HISTORY_COMPARE_MODE, (HistoryViewImpl.this.options & HistoryViewImpl.COMPARE_MODE) != 0);
	            HistoryViewImpl.this.compareModeAction.setChecked((HistoryViewImpl.this.options & HistoryViewImpl.COMPARE_MODE) != 0);
	        }
	    };
	    this.compareModeDropDownAction.setHoverImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/compare_mode.gif"));
	    actionBarsMenu.add(this.showCommentViewerAction);
	    actionBarsMenu.add(this.showAffectedPathsViewerAction);
	    MenuManager sub = new MenuManager(SVNTeamUIPlugin.instance().getResource("HistoryView.AffectedPathLayout"), IWorkbenchActionConstants.GROUP_MANAGING);
		sub.add(this.flatAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.Flat"), Action.AS_RADIO_BUTTON) {
			public void run() {
				IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
				SVNTeamPreferences.setHistoryBoolean(store, SVNTeamPreferences.HISTORY_HIERARCHICAL_LAYOUT, false);
			}
		});
		this.flatAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/flat_layout.gif"));		
		
		sub.add(this.hierarchicalAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.Hierarchical"), Action.AS_RADIO_BUTTON) {
			public void run() {
				IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
				SVNTeamPreferences.setHistoryBoolean(store, SVNTeamPreferences.HISTORY_HIERARCHICAL_LAYOUT, true);
				
			}
		});
		this.hierarchicalAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/tree_layout.gif"));
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
        
		this.viewInfoProvider.setDescription(this.getResourceLabel());
		
	    this.refreshOptionButtons();
	    
	    //Setting context help
	    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.team.svn.help.historyViewContext");
	}
	
	public void selectRevision(long revision) {
		this.history.setSelectedRevision(revision);
	}

	public void showHistory(IResource resource, boolean background) {
		this.wcResource = resource;
		this.logMessages = null;
		this.localHistory = null;
		this.history.refresh(LogMessagesComposite.REFRESH_UI_AND_MODEL);
		
		if (resource != null) {
			if (resource instanceof IFile) {
				try {
					this.refreshLocalHistory((IFile)resource);
				} catch (CoreException ex) {		
					UILoggedOperation.reportError("Get Local History", ex);
				};
			}
		}
	    long currentRevision = SVNRevision.INVALID_REVISION_NUMBER;
	    IRepositoryResource remote = null;
		if (resource != null) {
			remote = SVNRemoteStorage.instance().asRepositoryResource(resource);
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
			if (local != null) {
				currentRevision = local.getRevision();
				if (local.isCopied()) {
					remote = SVNUtility.getCopiedFrom(resource);
				}
			}
		}
		this.showHistoryImpl(currentRevision, remote, background);
	}
	
	public void showHistory(IRepositoryResource remoteResource) {
		this.showHistory(remoteResource, false);
	}
	
	public void showHistory(IRepositoryResource remoteResource, boolean background) {
		this.wcResource = null;
		this.logMessages = null;
		this.localHistory = null;
		this.history.refresh(LogMessagesComposite.REFRESH_UI_AND_MODEL);
		        		
		this.showHistoryImpl(SVNRevision.INVALID_REVISION_NUMBER, remoteResource, background);
	}
	
	public void updateViewInput(IRepositoryResource resource) {
		if (this.repositoryResource != null && this.repositoryResource.equals(resource)) {
			return;
		}
		if (resource != null) {
			if (this.repositoryResource != null && resource.getUrl().equals(this.repositoryResource.getUrl())) {
				if (resource.getSelectedRevision().getKind() == Kind.NUMBER) {
					SVNRevision currentRevision = resource.getSelectedRevision();
					this.repositoryResource.setSelectedRevision(SVNRevision.HEAD);
					this.showHistoryImpl(((SVNRevision.Number)currentRevision).getNumber(), this.repositoryResource, true);
					return;
				}
			}
			resource.setSelectedRevision(SVNRevision.HEAD);
			this.showHistory(resource, true);
		}
	}
	
	public void updateViewInput(IResource resource) {
		if (resource != null) {
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
			if (local != null && IStateFilter.SF_ONREPOSITORY.accept(local)) {
				if (local.getResource().equals(this.wcResource)) {
					IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(local.getResource());
					if (this.repositoryResource != null && !this.repositoryResource.equals(remote)) {
						this.updateViewInput(remote);
					}
					return;
				}
				this.showHistory((IResource)null, true);
				this.refresh();
				this.showHistory(resource, true);
			}
		}
	}
	
	public void clear() {
		this.repositoryResource = null;
		this.wcResource = null;
		this.logMessages = null;
		this.localHistory = null;
		this.history.refresh(LogMessagesComposite.REFRESH_UI_AND_MODEL);
		this.disableButtons();
	}
	
	protected void showHistoryImpl(long currentRevision, IRepositoryResource remote, boolean background) {
		if (this.repositoryResource != null) {
			if (this.repositoryResource.equals(remote)) {
				this.logMessages = null;
			}
			else {
				this.clearFilter();	
			}
		}
		
		if (this.wcResource == null || IStateFilter.SF_ONREPOSITORY.accept(SVNRemoteStorage.instance().asLocalResource(this.wcResource))) {
			this.repositoryResource = remote;
		}
		else {
			this.repositoryResource = null;
		}
		
		this.enableButtons();
		this.currentRevision = currentRevision;

		this.viewInfoProvider.setDescription(this.getResourceLabel());
		
		if (this.repositoryResource == null) {
			this.history.refresh(LogMessagesComposite.REFRESH_UI_AND_MODEL);
			if (this.wcResource != null) {
				this.showHistoryImpl(null, background);
			}
		}
		else {
			this.logMessages = null;
			GetLogMessagesOperation msgOp = new GetLogMessagesOperation(this.repositoryResource, this.stopOnCopyAction.isChecked());
			msgOp.setLimit(this.limit);
			this.showHistoryImpl(msgOp, background);
		}
	}
	
	protected void showHistoryImpl(final GetLogMessagesOperation msgsOp, boolean background) {
		TreeViewer treeTable = this.history.getTreeViewer();
		final IStructuredSelection selected = (IStructuredSelection)treeTable.getSelection();
		IActionOperation showOp = new AbstractActionOperation("Operation.HShowHistory") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				if (HistoryViewImpl.this.getResource() == null) {
					try {
						HistoryViewImpl.this.currentRevision = HistoryViewImpl.this.getRepositoryResource().getRevision();
					} 
					catch (Exception e){

					} 
				}
				if (HistoryViewImpl.this.historyForTheOtherResource(msgsOp)) {
					return;
				}
				if (msgsOp != null && msgsOp.getExecutionState() == IActionOperation.OK) {
					HistoryViewImpl.this.pagingEnabled = HistoryViewImpl.this.limit > 0 && HistoryViewImpl.this.logMessages == null ? msgsOp.getMessages().length == HistoryViewImpl.this.limit : msgsOp.getMessages().length == HistoryViewImpl.this.limit + 1;
					HistoryViewImpl.this.addPage(msgsOp.getMessages());
				}
				UIMonitorUtility.getDisplay().syncExec(new Runnable() {
					public void run() {
						HistoryViewImpl.this.history.refresh(LogMessagesComposite.REFRESH_UI_AND_MODEL);
						HistoryViewImpl.this.enableButtons();
					}
				});
			}
		};
		IActionOperation selectOp = new AbstractActionOperation("Operation.HSaveTableSelection") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				UIMonitorUtility.getDisplay().syncExec(new Runnable() {
					public void run() {
						if (HistoryViewImpl.this.historyForTheOtherResource(msgsOp) && msgsOp != null) {
							return;
						}
					    TreeViewer treeTable = HistoryViewImpl.this.history.getTreeViewer();
					    if (!treeTable.getTree().isDisposed() && treeTable.getTree().getItems().length > 0) {
					    	if (selected.size() != 0) {
						        treeTable.setSelection(selected, true);
						    }
					    	else {
					    		TreeItem firstItem = treeTable.getTree().getItem(0);
					    		if (((ILogNode)firstItem.getData()).getType() == ILogNode.TYPE_CATEGORY) {
					    			firstItem = firstItem.getItem(0);
					    		}
					    		treeTable.getTree().setSelection(firstItem);
					    	}
						    HistoryViewImpl.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
					    }
					}
				});
			}
		};
		CompositeOperation op = new CompositeOperation(showOp.getId());
		if (msgsOp != null) {
			op.add(new CorrectRevisionOperation(msgsOp, this.repositoryResource, this.currentRevision, this.wcResource));
			op.add(msgsOp);
		}
		op.add(showOp);
		op.add(selectOp, new IActionOperation[] {showOp});
		if (background) {
			ProgressMonitorUtility.doTaskScheduled(op, false);
		}
		else {
			UIMonitorUtility.doTaskScheduledDefault(this.getSite().getPart(), op);
		}
	}
	
	/*
	 * Checks if Get Log Messages Operation was called for the one resource, 
	 * but History View is already connected to another.
	 */
	protected boolean historyForTheOtherResource(GetLogMessagesOperation msgsOp) {
		return this.repositoryResource == null || this.repositoryResource != null && msgsOp != null && !this.repositoryResource.equals(msgsOp.getResource());
	}
	
	protected String[] getSelectedAuthors() {
		List authors = new ArrayList();
		if (this.logMessages != null) {
			for (int i = 0; i < this.logMessages.length; i++) {
				String current = this.logMessages[i].author;
				if (current != null && !authors.contains(current)) {
					authors.add(current);
				}
			}
		}
		return (String[])authors.toArray(new String[authors.size()]);
	}
	
	protected SVNLogEntry[] filterMessages(SVNLogEntry[] msgs) {
		if (msgs == null) {
			return null;
		}
		ArrayList filteredMessages = new ArrayList();
	    for (int i = 0; i < msgs.length; i++) {
			String author = msgs[i].author;
			String message = msgs[i].message;
			StringMatcher authorMatcher = new StringMatcher(this.filterByAuthor);
			StringMatcher commentMatcher = new StringMatcher(this.filterByComment);
			if ((this.filterByAuthor.length() == 0 || authorMatcher.match(author)) && 
				(!this.isCommentFilterEnabled || commentMatcher.match(message))) {
				filteredMessages.add(msgs[i]);
			}
	    }
	    return (SVNLogEntry [])filteredMessages.toArray(new SVNLogEntry[filteredMessages.size()]);
	}
	
	protected Action getCompareModeAction() {
		this.compareModeAction = new Action (SVNTeamUIPlugin.instance().getResource("HistoryView.CompareMode"), IAction.AS_CHECK_BOX) {
	        public void run() {
	        	HistoryViewImpl.this.options ^= HistoryViewImpl.COMPARE_MODE;
	            IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
	            SVNTeamPreferences.setHistoryBoolean(store, SVNTeamPreferences.HISTORY_COMPARE_MODE, (HistoryViewImpl.this.options & HistoryViewImpl.COMPARE_MODE) != 0);
	            HistoryViewImpl.this.compareModeDropDownAction.setChecked((HistoryViewImpl.this.options & HistoryViewImpl.COMPARE_MODE) != 0);
	        }
	    };
	    this.compareModeAction.setToolTipText(SVNTeamUIPlugin.instance().getResource("HistoryView.CompareMode"));
	    this.compareModeAction.setHoverImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/compare_mode.gif"));
	    
	    IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
	    this.options = 
	    	SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_COMPARE_MODE) ?
			(this.options | HistoryViewImpl.COMPARE_MODE) :
			(this.options & ~HistoryViewImpl.COMPARE_MODE);
	    this.compareModeAction.setChecked((this.options & HistoryViewImpl.COMPARE_MODE) != 0);
	    
	    return this.compareModeAction;
	}
	
	protected void addPage(SVNLogEntry[] newMessages) {
		if (this.logMessages == null) {
			this.logMessages = newMessages;
		}
		else {
			List oldList = new ArrayList(Arrays.asList(this.logMessages));
			List newList = Arrays.asList(newMessages);
			if (newList.size() > 1) {
				newList = newList.subList(1, newList.size());
				oldList.addAll(newList);
			}		
			this.logMessages = (SVNLogEntry [])oldList.toArray(new SVNLogEntry[oldList.size()]);		
		}
	}
	
	protected Action getStopOnCopyAction() {
		this.stopOnCopyAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.StopOnCopy"), IAction.AS_CHECK_BOX) {
	        public void run() {
	        	HistoryViewImpl.this.refresh();
	        	HistoryViewImpl.this.options = this.isChecked() ? (HistoryViewImpl.this.options | HistoryViewImpl.STOP_ON_COPY) : (HistoryViewImpl.this.options & ~HistoryViewImpl.STOP_ON_COPY);
	        	HistoryViewImpl.this.stopOnCopyDropDownAction.setChecked((HistoryViewImpl.this.options & HistoryViewImpl.STOP_ON_COPY) != 0);
	        }
	    };
	    this.stopOnCopyAction.setToolTipText(SVNTeamUIPlugin.instance().getResource("HistoryView.StopOnCopy"));
	    this.stopOnCopyAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/stop_on_copy.gif"));
	    return this.stopOnCopyAction;		
	}
	
	protected Action getGroupByDateAction() {
		this.groupByDateAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.GroupByDate"), IAction.AS_CHECK_BOX) {
	        public void run() {
	        	HistoryViewImpl.this.options = this.isChecked() ? (HistoryViewImpl.this.options | HistoryViewImpl.GROUP_BY_DATE) : (HistoryViewImpl.this.options & ~HistoryViewImpl.GROUP_BY_DATE);
	        	HistoryViewImpl.this.groupByDateDropDownAction.setChecked((HistoryViewImpl.this.options & HistoryViewImpl.GROUP_BY_DATE) != 0);
	        	HistoryViewImpl.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
				IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
				int type = SVNTeamPreferences.getHistoryInt(store, SVNTeamPreferences.HISTORY_GROUPING_TYPE_NAME);
				SVNTeamPreferences.setHistoryInt(store, SVNTeamPreferences.HISTORY_GROUPING_TYPE_NAME, type == SVNTeamPreferences.HISTORY_GROUPING_TYPE_DATE ? SVNTeamPreferences.HISTORY_GROUPING_TYPE_NONE : SVNTeamPreferences.HISTORY_GROUPING_TYPE_DATE);
	        }
	    };
	    this.groupByDateAction.setToolTipText(SVNTeamUIPlugin.instance().getResource("HistoryView.GroupByDate"));
	    this.groupByDateAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/group_by_date.gif"));
	    return this.groupByDateAction;		
	}
	
	protected Action getCollapseAllAction() {
		this.collapseAllAction = new Action(SVNTeamUIPlugin.instance().getResource("RepositoriesView.CollapseAll.Label"), IAction.AS_PUSH_BUTTON) {
			public void run() {
				HistoryViewImpl.this.history.collapseAll();
			}
		};
		this.collapseAllAction.setToolTipText(SVNTeamUIPlugin.instance().getResource("RepositoriesView.CollapseAll.ToolTip"));
	    this.collapseAllAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/collapseall.gif"));
		return this.collapseAllAction;		
	}
	
	protected Action getShowBothAction() {
		this.showBothAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.ShowBoth"), IAction.AS_RADIO_BUTTON) {
	        public void run() {
	        	HistoryViewImpl.this.options = HistoryViewImpl.this.options & ~(ISVNHistoryViewInfo.MODE_LOCAL | ISVNHistoryViewInfo.MODE_REMOTE) | ISVNHistoryViewInfo.MODE_BOTH;
	        	HistoryViewImpl.this.showBothActionDropDown.setChecked(true);
	        	HistoryViewImpl.this.showLocalActionDropDown.setChecked(false);
	        	HistoryViewImpl.this.showRemoteActionDropDown.setChecked(false);
	        	HistoryViewImpl.this.showLocalAction.setChecked(false);
	        	HistoryViewImpl.this.showRemoteAction.setChecked(false);
	        	HistoryViewImpl.this.setRevMode();
		        HistoryViewImpl.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
	        }
	    };
	    this.showBothAction.setToolTipText(SVNTeamUIPlugin.instance().getResource("HistoryView.ShowBoth"));
	    this.showBothAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/both_history_mode.gif"));
	    return this.showBothAction;		
	}
	
	protected Action getShowRemoteAction() {
		this.showRemoteAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.ShowRemote"), IAction.AS_RADIO_BUTTON) {
	        public void run() {
	        	HistoryViewImpl.this.options = HistoryViewImpl.this.options & ~(ISVNHistoryViewInfo.MODE_LOCAL | ISVNHistoryViewInfo.MODE_BOTH) | ISVNHistoryViewInfo.MODE_REMOTE;
	        	HistoryViewImpl.this.showRemoteActionDropDown.setChecked(true);
	        	HistoryViewImpl.this.showLocalActionDropDown.setChecked(false);
	        	HistoryViewImpl.this.showBothActionDropDown.setChecked(false);
	        	HistoryViewImpl.this.showLocalAction.setChecked(false);
	        	HistoryViewImpl.this.showBothAction.setChecked(false);
	        	HistoryViewImpl.this.setRevMode();
		        HistoryViewImpl.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
	        }
	    };
	    this.showRemoteAction.setToolTipText(SVNTeamUIPlugin.instance().getResource("HistoryView.ShowRemote"));
	    this.showRemoteAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/remote_history_mode.gif"));
	    return this.showRemoteAction;		
	}
	
	protected Action getShowLocalAction() {
		this.showLocalAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.ShowLocal"), IAction.AS_RADIO_BUTTON) {
	        public void run() {
	        	HistoryViewImpl.this.options = HistoryViewImpl.this.options & ~(ISVNHistoryViewInfo.MODE_REMOTE | ISVNHistoryViewInfo.MODE_BOTH) | ISVNHistoryViewInfo.MODE_LOCAL;
	        	HistoryViewImpl.this.showLocalActionDropDown.setChecked(true);
	        	HistoryViewImpl.this.showRemoteActionDropDown.setChecked(false);
	        	HistoryViewImpl.this.showBothActionDropDown.setChecked(false);
	        	HistoryViewImpl.this.showRemoteAction.setChecked(false);
	        	HistoryViewImpl.this.showBothAction.setChecked(false);
	        	HistoryViewImpl.this.setRevMode();
		        HistoryViewImpl.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
	        }
	    };
	    this.showLocalAction.setToolTipText(SVNTeamUIPlugin.instance().getResource("HistoryView.ShowLocal"));
	    this.showLocalAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/local_history_mode.gif"));
	    return this.showLocalAction;		
	}
	
	protected void setRevMode() {
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		int prefToSet;
		if ((this.options & ISVNHistoryViewInfo.MODE_BOTH) != 0) {
			prefToSet = SVNTeamPreferences.HISTORY_REVISION_MODE_BOTH;
		}
		else if ((this.options & ISVNHistoryViewInfo.MODE_LOCAL) != 0) {
			prefToSet = SVNTeamPreferences.HISTORY_REVISION_MODE_LOCAL;
		}
		else {
			prefToSet = SVNTeamPreferences.HISTORY_REVISION_MODE_REMOTE;
		}
		SVNTeamPreferences.setHistoryInt(store, SVNTeamPreferences.HISTORY_REVISION_MODE_NAME, prefToSet);
	}
	
	protected Action getHideUnrelatedAction() {
		this.hideUnrelatedAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.HideUnrelatedPaths"), IAction.AS_CHECK_BOX) {
	        public void run() {
	        	HistoryViewImpl.this.history.refresh(LogMessagesComposite.REFRESH_UI_AFFECTED);
	        	HistoryViewImpl.this.hideUnrelatedDropDownAction.setChecked(this.isChecked());
	        	HistoryViewImpl.this.options = this.isChecked() ? (HistoryViewImpl.this.options | HistoryViewImpl.HIDE_UNRELATED) : (HistoryViewImpl.this.options & ~HistoryViewImpl.HIDE_UNRELATED);
	        }
	    };
	    this.hideUnrelatedAction.setToolTipText(SVNTeamUIPlugin.instance().getResource("HistoryView.HideUnrelatedPaths"));
	    this.hideUnrelatedAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/hide_unrelated.gif"));
	    return this.hideUnrelatedAction;		
	}
	
	protected Action getPagingAction() {
		this.getNextPageAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.GetNextPage")) {
	        public void run() {
	        	GetLogMessagesOperation msgOp = new GetLogMessagesOperation(HistoryViewImpl.this.repositoryResource, HistoryViewImpl.this.stopOnCopyAction.isChecked());
	        	msgOp.setLimit(HistoryViewImpl.this.limit + 1);
	    		if (HistoryViewImpl.this.logMessages != null) {
	    			SVNLogEntry lm = HistoryViewImpl.this.logMessages[HistoryViewImpl.this.logMessages.length - 1];
	    			msgOp.setSelectedRevision(SVNRevision.fromNumber(lm.revision));
	    		}
	    		HistoryViewImpl.this.showHistoryImpl(msgOp, false);
	        }
	    };
	    String msg = this.limit > 0 ? SVNTeamUIPlugin.instance().getResource("HistoryView.ShowNextX", new String[] {String.valueOf(this.limit)}) : SVNTeamUIPlugin.instance().getResource("HistoryView.ShowNextPage");
	    this.getNextPageAction.setToolTipText(msg);
	    this.getNextPageAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/paging.gif"));
	    return this.getNextPageAction;
	}
	
	protected Action getPagingAllAction() {
		this.getAllPagesAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.ShowAll")) {
	        public void run() {
	        	GetLogMessagesOperation msgOp = new GetLogMessagesOperation(HistoryViewImpl.this.repositoryResource, HistoryViewImpl.this.stopOnCopyAction.isChecked());
	    		msgOp.setLimit(0);
	    		if (HistoryViewImpl.this.logMessages != null) {
	    			SVNLogEntry lm = HistoryViewImpl.this.logMessages[HistoryViewImpl.this.logMessages.length - 1];
	    			msgOp.setSelectedRevision(SVNRevision.fromNumber(lm.revision));
	    		}
	    		HistoryViewImpl.this.showHistoryImpl(msgOp, false);	        	
	        }
	    };
	    this.getAllPagesAction.setToolTipText(SVNTeamUIPlugin.instance().getResource("HistoryView.ShowAll"));
	    this.getAllPagesAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/paging_all.gif"));
	    return this.getAllPagesAction;		
	}
	
	protected void createToolBar() {
	    IActionBars actionBars = this.getActionBars();

	    IToolBarManager tbm = actionBars.getToolBarManager();
	    tbm.add(new Separator());
	    tbm.add(this.getGroupByDateAction());
	    tbm.add(new Separator());
	    tbm.add(this.getShowBothAction());
	    tbm.add(this.getShowLocalAction());
	    tbm.add(this.getShowRemoteAction());
        tbm.add(new Separator());
        tbm.add(this.getHideUnrelatedAction());           
        tbm.add(this.getStopOnCopyAction());           
        tbm.add(new Separator());
        tbm.add(this.getPagingAction());
        tbm.add(this.getPagingAllAction());
        tbm.add(new Separator());
        tbm.add(this.getCollapseAllAction());
        tbm.add(this.getCompareModeAction());
                
        tbm.update(true);
	}
	
    protected void refreshOptionButtons() {
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		boolean showMultiline = SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_SHOW_MULTILINE_COMMENT_NAME);
		boolean showAffected = SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_SHOW_AFFECTED_PATHS_NAME);
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
		
		this.showCommentViewerAction.setChecked(showMultiline);
        this.history.setCommentViewerVisible(showMultiline);	          

        this.showAffectedPathsViewerAction.setChecked(showAffected);
        this.history.setAffectedPathsViewerVisible(showAffected);
        
        this.hideUnrelatedDropDownAction.setChecked((this.options & HistoryViewImpl.HIDE_UNRELATED) != 0);
        this.hideUnrelatedAction.setChecked((this.options & HistoryViewImpl.HIDE_UNRELATED) != 0);
        this.stopOnCopyDropDownAction.setChecked((this.options & HistoryViewImpl.STOP_ON_COPY) != 0);
        this.stopOnCopyAction.setChecked((this.options & HistoryViewImpl.STOP_ON_COPY) != 0);
        this.options |= groupingType == SVNTeamPreferences.HISTORY_GROUPING_TYPE_DATE ? HistoryViewImpl.GROUP_BY_DATE : 0;
        this.options = this.options & ~(ISVNHistoryViewInfo.MODE_BOTH | ISVNHistoryViewInfo.MODE_LOCAL | ISVNHistoryViewInfo.MODE_REMOTE) | revisionMode;
        this.setRevMode();
        this.groupByDateAction.setChecked((this.options & HistoryViewImpl.GROUP_BY_DATE) != 0);
        this.showBothAction.setChecked((this.options & ISVNHistoryViewInfo.MODE_BOTH) != 0);
        this.showBothActionDropDown.setChecked((this.options & ISVNHistoryViewInfo.MODE_BOTH) != 0);
        this.showLocalAction.setChecked((this.options & ISVNHistoryViewInfo.MODE_LOCAL) != 0);
        this.showLocalActionDropDown.setChecked((this.options & ISVNHistoryViewInfo.MODE_LOCAL) != 0);
        this.showRemoteAction.setChecked((this.options & ISVNHistoryViewInfo.MODE_REMOTE) != 0);
        this.showRemoteActionDropDown.setChecked((this.options & ISVNHistoryViewInfo.MODE_REMOTE) != 0);
        this.groupByDateDropDownAction.setChecked((this.options & HistoryViewImpl.GROUP_BY_DATE) != 0);
        this.compareModeDropDownAction.setChecked((this.options & HistoryViewImpl.COMPARE_MODE) != 0);
        this.compareModeAction.setChecked((this.options & HistoryViewImpl.COMPARE_MODE) != 0);
        
        this.flatAction.setChecked(!hierarchicalAffectedView);
        this.hierarchicalAction.setChecked(hierarchicalAffectedView);
        this.flatAction.setEnabled(showAffected);
        this.hierarchicalAction.setEnabled(showAffected);
        
        this.history.setResourceTreeVisible(hierarchicalAffectedView);

        if (SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_PAGING_ENABLE_NAME)) {
    	    this.limit = SVNTeamPreferences.getHistoryInt(store, SVNTeamPreferences.HISTORY_PAGE_SIZE_NAME);
        	this.getNextPageAction.setToolTipText("Show Next " + this.limit);
        	this.options |= HistoryViewImpl.PAGING_ENABLED;
        }
        else {
        	this.limit = 0;
    	    this.getNextPageAction.setToolTipText("Show Next Page");
    	    this.options &= ~HistoryViewImpl.PAGING_ENABLED;
    	}
        this.enableButtons();
    }
    
    public void setHistoryPage(HistoryPage page) {
    	this.page = page;
    }
    
    protected void enableButtons() {
	    ILocalResource local = SVNRemoteStorage.instance().asLocalResource(this.wcResource);
	    boolean isConnected = this.wcResource != null || this.repositoryResource != null;
	    boolean enableRepo = local != null && IStateFilter.SF_ONREPOSITORY.accept(local) || this.repositoryResource != null;
	    
	    this.filterDropDownAction.setEnabled(enableRepo && this.repositoryResource != null && this.logMessages != null);
	    this.clearFilterDropDownAction.setEnabled(this.isFilterEnabled());
	    this.getNextPageAction.setEnabled(enableRepo && this.pagingEnabled & ((this.options & HistoryViewImpl.PAGING_ENABLED) != 0));
	    this.getAllPagesAction.setEnabled(enableRepo && this.pagingEnabled & ((this.options & HistoryViewImpl.PAGING_ENABLED) != 0));
	    
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
    
    protected void disableButtons() {
	    this.filterDropDownAction.setEnabled(false);
	    this.clearFilterDropDownAction.setEnabled(false);
	    this.getNextPageAction.setEnabled(false);
	    this.getAllPagesAction.setEnabled(false);
	    this.stopOnCopyAction.setEnabled(false);
	    this.stopOnCopyDropDownAction.setEnabled(false);
	    this.hideUnrelatedAction.setEnabled(false);
	    this.hideUnrelatedDropDownAction.setEnabled(false);
	    this.collapseAllAction.setEnabled(false);
	    this.compareModeAction.setEnabled(false);
	    this.compareModeDropDownAction.setEnabled(false);
	    this.showBothAction.setEnabled(false);
	    this.showBothActionDropDown.setEnabled(false);
	    this.showLocalAction.setEnabled(false);
	    this.showLocalActionDropDown.setEnabled(false);
	    this.showRemoteAction.setEnabled(false);
	    this.showRemoteActionDropDown.setEnabled(false);
	    this.groupByDateAction.setEnabled(false);
	    this.groupByDateDropDownAction.setEnabled(false);
    }
    
    protected void disconnectView() {
    	UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				HistoryViewImpl.this.showHistory((IResource)null, false);
			}
		});
    }
	
	protected void refreshLocalHistory(IFile resource) throws CoreException {
		ArrayList<SVNLocalFileRevision> history = new ArrayList<SVNLocalFileRevision>();
		IFileState [] states = resource.getHistory(null);
		if (states.length > 0 || IStateFilter.SF_NOTONREPOSITORY.accept(SVNRemoteStorage.instance().asLocalResource(resource))) {
			history.add(new SVNLocalFileRevision(resource));
		}
		for (int i = 0; i < states.length; i++) {
			history.add(new SVNLocalFileRevision(states[i]));
		}
		this.localHistory = history.size() == 0 ? null : history.toArray(new SVNLocalFileRevision[history.size()]);
		HistoryViewImpl.this.history.refresh(LogMessagesComposite.REFRESH_UI_AND_MODEL);
	}
	
	public SVNLogEntry [] getEntries() {
		return this.logMessages;
	}
	
	public String getResourceLabel() {
		String viewDescription = SVNTeamUIPlugin.instance().getResource("HistoryView.Name");
		String resourceName;
		if (this.wcResource != null) {
		    String path = this.wcResource.getFullPath().toString();
		    if (path.startsWith("/")) {
		    	path = path.substring(1);
		    }
			resourceName = SVNTeamUIPlugin.instance().getResource("SVNView.ResourceSelected", new String[] {viewDescription, path});
		}
		else if (this.repositoryResource != null) {
			resourceName = SVNTeamUIPlugin.instance().getResource("SVNView.ResourceSelected", new String[] {viewDescription, this.repositoryResource.getUrl()});
		}
		else {
			resourceName = SVNTeamUIPlugin.instance().getResource("SVNView.ResourceNotSelected");
		}
		return resourceName;
	}

	public ILocalResource getCompareWith() {
		return this.compareWith;
	}

	public HistoryPage getHistoryPage() {
		return this.page;
	}

	public SVNLogEntry[] getFullRemoteHistory() {
		return this.logMessages;
	}

	public SVNLogEntry[] getRemoteHistory() {
		return this.filterMessages(this.logMessages);
	}

	public SVNLocalFileRevision[] getLocalHistory() {
		return this.localHistory;
	}

	public boolean isAllRemoteHistoryFetched() {
		return !this.getNextPageAction.isEnabled();
	}

	public boolean isFilterEnabled() {
	    return this.filterByAuthor.length() > 0 || this.isCommentFilterEnabled; 
	}
	
	public int getOptions() {
		return this.options;
	}
	
	public void clearFilter() {
	    this.filterByAuthor = ""; 
	    this.filterByComment = "";
        this.isCommentFilterEnabled = false;
    	this.showHistoryImpl(null, false);
	}
	
	public void setFilter() {
		HistoryFilterPanel panel = new HistoryFilterPanel(this.filterByAuthor, this.filterByComment, this.getSelectedAuthors(), this.isCommentFilterEnabled);
	    DefaultDialog dialog = new DefaultDialog(this.getSite().getShell(), panel);
	    if (dialog.open() == 0) {
	        this.filterByAuthor = panel.getAuthor(); 
	        this.filterByComment = panel.getComment();
	        this.isCommentFilterEnabled = panel.isCommentFilterEnabled();
        	this.showHistoryImpl(null, false);
	    }
	}
	
	public void refresh() {
		if (this.wcResource == null) {
			this.showHistory(this.repositoryResource, true);
		}
		else {
			this.showHistory(this.wcResource, true);
		}
	}
	
	public SVNRevision getCurrentRevision() {
		if (this.currentRevision == SVNRevision.INVALID_REVISION_NUMBER) {
			return this.wcResource == null ? SVNRevision.HEAD : SVNRevision.INVALID_REVISION;
		}
		return SVNRevision.fromNumber(this.currentRevision);
	}
	
	public boolean isGrouped() {
		return this.groupByDateAction.isChecked();
	}

	public int getMode() {
		return this.options & ISVNHistoryViewInfo.MODE_MASK;
	}
	
	public boolean isRelatedPathsOnly() {
		return this.hideUnrelatedAction.isChecked();
	}
	
}
