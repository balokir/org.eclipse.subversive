/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.common;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.GetLogMessagesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.history.ISVNHistoryViewInfo;
import org.eclipse.team.svn.ui.history.LogMessagesComposite;
import org.eclipse.team.svn.ui.history.SVNHistoryPage;
import org.eclipse.team.svn.ui.history.data.SVNLocalFileRevision;
import org.eclipse.team.svn.ui.history.filter.AuthorNameLogEntryFilter;
import org.eclipse.team.svn.ui.history.filter.ChangeNameLogEntryFilter;
import org.eclipse.team.svn.ui.history.filter.CommentLogEntryFilter;
import org.eclipse.team.svn.ui.history.filter.CompositeLogEntryFilter;
import org.eclipse.team.svn.ui.history.filter.ILogEntryFilter;
import org.eclipse.team.svn.ui.history.model.ILogNode;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.panel.view.HistoryFilterPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Show history for resource in panel, i.e. it can be used in a dialog
 * This is a simplified version of history view, e.g. it doesn't contain actions for affected paths etc.
 * 
 * @author Alexander Gurov
 */
public class SVNHistoryPanel extends AbstractDialogPanel implements ISVNHistoryViewInfo {
	protected LogMessagesComposite history;
	protected SVNLogEntry []logMessages;
	protected IRepositoryResource resource;
	protected long currentRevision;
	protected boolean multiSelect;
	protected boolean useCheckboxes;
	protected long limit;
	protected boolean pagingEnabled;

	protected Text resourceLabel;
	
	protected ToolItem hideUnrelatedItem;
	protected ToolItem stopOnCopyItem;
	protected ToolItem pagingItem;
	protected ToolItem pagingAllItem;
	protected ToolItem filterItem;
	protected ToolItem clearFilterItem;
	protected ToolItem refreshItem;
	protected ToolItem groupByDateItem;

	protected CommentLogEntryFilter commentFilter;
	protected AuthorNameLogEntryFilter authorFilter;
	protected ChangeNameLogEntryFilter changeFilter;
	protected CompositeLogEntryFilter logEntriesFilter;

	protected ISelectionChangedListener tableViewerListener;
	protected IPropertyChangeListener configurationListener;
	protected boolean initialStopOnCopy;
	protected boolean pending;		
	
    public SVNHistoryPanel(String title, String description, String message, GetLogMessagesOperation msgOp, boolean multiSelect, boolean useCheckboxes, long currentRevision) {
    	this.multiSelect = multiSelect;
    	this.useCheckboxes = useCheckboxes;
        this.dialogTitle = title;
        this.dialogDescription = description;
        this.defaultMessage = message;
		this.resource = msgOp.getResource();
		this.currentRevision = currentRevision;
    	this.logMessages = msgOp.getMessages();
    	this.initialStopOnCopy = msgOp.getStopOnCopy();
    	this.authorFilter = new AuthorNameLogEntryFilter();
		this.commentFilter = new CommentLogEntryFilter();
		this.changeFilter = new ChangeNameLogEntryFilter();
		this.logEntriesFilter = new CompositeLogEntryFilter(new ILogEntryFilter [] {this.authorFilter, this.commentFilter, this.changeFilter});		
	}
    
    public static GetLogMessagesOperation getMsgsOp(IRepositoryResource resource, boolean stopOnCopy) {
    	GetLogMessagesOperation msgsOp = new GetLogMessagesOperation(resource, stopOnCopy);
		msgsOp.setIncludeMerged(SVNTeamPreferences.getMergeBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.MERGE_INCLUDE_MERGED_NAME));
		
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		if (SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_PAGING_ENABLE_NAME)) {
			msgsOp.setLimit(SVNTeamPreferences.getHistoryInt(store, SVNTeamPreferences.HISTORY_PAGE_SIZE_NAME));
		}
		return msgsOp;
    }	
    
    public String getImagePath() {
        return "icons/dialogs/select_revision.gif"; //$NON-NLS-1$
    }

    public Point getPrefferedSizeImpl() {
        return new Point(700, SWT.DEFAULT);
    }
    
    public boolean isGrouped() {
    	return this.groupByDateItem.getSelection();
    }
    
    public long getCurrentRevision() {
		return this.currentRevision;
    }

    public IRepositoryResource getRepositoryResource() {
    	return this.resource;
    }
    
	public SVNLogEntry[] getRemoteHistory() {
		return SVNHistoryPage.filterMessages(this.logMessages, this.logEntriesFilter);
	}

	public SVNLocalFileRevision[] getLocalHistory() {
		return null;
	}
	
	public IResource getResource() {
		return null;
	}
	
    public int getMode() {
    	return ISVNHistoryViewInfo.MODE_REMOTE;
    }
    
    public boolean isRelatedPathsOnly() {
    	return this.hideUnrelatedItem.getSelection();
    }
    
    public boolean isPending() {
    	return this.pending;
    }
    
    public void addFilter(ILogEntryFilter filter) {
		this.logEntriesFilter.addFilter(filter);
	}
	
	public void removeFilter(ILogEntryFilter filter) {
		this.logEntriesFilter.removeFilter(filter);
	}
    
    public void createControlsImpl(Composite parent) {
    	IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
    	    	
    	GridData data;
        GridLayout layout;
        
        Composite labelAndToolbar = new Composite(parent, SWT.NONE);
        layout = new GridLayout();
        layout.marginHeight = layout.marginWidth = 0;
        layout.numColumns = 2;
        labelAndToolbar.setLayout(layout);
        
        data = new GridData(GridData.FILL_HORIZONTAL);
        labelAndToolbar.setLayoutData(data);  
        
        //Create resource image and label        
        Composite resourceLabelComposite = new Composite(labelAndToolbar, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.horizontalSpacing = 1;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		resourceLabelComposite.setLayout(layout);
		resourceLabelComposite.setLayoutData(data);
		
		this.resourceLabel = new Text(resourceLabelComposite, SWT.LEFT);
		this.resourceLabel.setEditable(false);
		data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		this.resourceLabel.setLayoutData(data);
        
		// Create the toolbar
        ToolBarManager toolBarMgr = new ToolBarManager(SWT.FLAT);
        final ToolBar toolBar = toolBarMgr.createControl(labelAndToolbar);
        data = new GridData();
        data.horizontalAlignment = GridData.END;
        toolBar.setLayoutData(data);
        
        this.refreshItem = new ToolItem(toolBar, SWT.FLAT);
        new ToolItem(toolBar, SWT.SEPARATOR);
    	this.groupByDateItem = new ToolItem(toolBar, SWT.FLAT | SWT.CHECK);
    	new ToolItem(toolBar, SWT.SEPARATOR);
    	this.hideUnrelatedItem = new ToolItem(toolBar, SWT.FLAT | SWT.CHECK);
    	this.stopOnCopyItem = new ToolItem(toolBar, SWT.FLAT | SWT.CHECK);
    	new ToolItem(toolBar, SWT.SEPARATOR);
        this.filterItem = new ToolItem(toolBar, SWT.FLAT);
    	this.clearFilterItem = new ToolItem(toolBar, SWT.FLAT);
    	new ToolItem(toolBar, SWT.SEPARATOR);
    	this.pagingItem = new ToolItem(toolBar, SWT.FLAT);
    	this.pagingAllItem = new ToolItem(toolBar, SWT.FLAT);
    	
    	this.groupByDateItem.setImage(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/group_by_date.gif").createImage()); //$NON-NLS-1$
    	this.hideUnrelatedItem.setImage(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/hide_unrelated.gif").createImage()); //$NON-NLS-1$
    	this.stopOnCopyItem.setImage(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/stop_on_copy.gif").createImage()); //$NON-NLS-1$
    	this.stopOnCopyItem.setSelection(this.initialStopOnCopy);
    	this.filterItem.setImage(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/filter.gif").createImage()); //$NON-NLS-1$
    	this.clearFilterItem.setDisabledImage(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/clear.gif").createImage()); //$NON-NLS-1$
	    this.clearFilterItem.setImage(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/clear_filter.gif").createImage()); //$NON-NLS-1$
    	this.pagingItem.setImage(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/paging.gif").createImage()); //$NON-NLS-1$
    	this.pagingAllItem.setImage(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/paging_all.gif").createImage()); //$NON-NLS-1$
    	this.refreshItem.setImage(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/refresh.gif").createImage()); //$NON-NLS-1$
    	
    	this.hideUnrelatedItem.setToolTipText(SVNUIMessages.SVNHistoryPanel_Unrelated);
    	this.stopOnCopyItem.setToolTipText(SVNUIMessages.SVNHistoryPanel_StopOnCopy);
    	this.filterItem.setToolTipText(SVNUIMessages.SVNHistoryPanel_QuickFilter);
    	this.clearFilterItem.setToolTipText(SVNUIMessages.SVNHistoryPanel_ClearFilter);
    	this.pagingAllItem.setToolTipText(SVNUIMessages.SVNHistoryPanel_ShowAll);
    	this.refreshItem.setToolTipText(SVNUIMessages.SVNHistoryPanel_Refresh);
    	
    	Composite group = new Composite(parent, SWT.BORDER);
        layout = new GridLayout();
        layout.marginHeight = layout.marginWidth = 0;
        layout.numColumns = 1;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_BOTH));
    	
    	this.history = new LogMessagesComposite(group, this.multiSelect, this.useCheckboxes, this);
    	data = new GridData(GridData.FILL_BOTH);
    	data.heightHint = 350;
    	this.history.setLayoutData(data);
        this.history.setFocus();
        
        this.initTableViewerListener();
        
		int type = SVNTeamPreferences.getHistoryInt(store, SVNTeamPreferences.HISTORY_GROUPING_TYPE_NAME);
    	
    	if (SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_PAGING_ENABLE_NAME)) {
    		this.limit = SVNTeamPreferences.getHistoryInt(store, SVNTeamPreferences.HISTORY_PAGE_SIZE_NAME);
    		String msg = SVNUIMessages.format(SVNUIMessages.SVNHistoryPanel_ShowNextX, new String[] {String.valueOf(this.limit)});
    	    this.pagingItem.setToolTipText(msg);
    	    this.pagingEnabled = true;
        }
        else {
        	this.limit = 0;
    	    this.pagingItem.setToolTipText(SVNUIMessages.SVNHistoryPanel_ShowNextPage);
    	    this.pagingEnabled = false;
        }
    	this.pagingEnabled = SVNHistoryPanel.this.limit > 0 && this.logMessages.length == SVNHistoryPanel.this.limit;
		
        this.setPagingEnabled();
        this.clearFilterItem.setEnabled(false);
    	
    	this.groupByDateItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNHistoryPanel.this.history.refresh(LogMessagesComposite.REFRESH_UI_ALL);
			}
    	});
    	this.groupByDateItem.setSelection(type == SVNTeamPreferences.HISTORY_GROUPING_TYPE_DATE);
    	
    	this.hideUnrelatedItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNHistoryPanel.this.history.refresh(LogMessagesComposite.REFRESH_UI_AFFECTED);
			}
    	});
    	this.stopOnCopyItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SVNHistoryPanel.this.refresh();
			}
    	});
    	
        this.filterItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {				
				SVNHistoryPanel.this.setFilter();
			}
    	});
        this.clearFilterItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {				
				SVNHistoryPanel.this.clearFilter();
			}
    	});
        
    	this.pagingItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {				
				SVNHistoryPanel.this.showNextPage(false);
			}
    	});    	
    	this.pagingAllItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {				
				SVNHistoryPanel.this.showNextPage(true);
			}
    	});
    	
    	this.refreshItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {				
				SVNHistoryPanel.this.refresh();
			}
    	});
    	
    	this.pending = false;
    	this.history.refresh(LogMessagesComposite.REFRESH_ALL);
		TreeViewer treeTable = SVNHistoryPanel.this.history.getTreeViewer();
		TreeItem firstItem = treeTable.getTree().getItem(0);
		if (((ILogNode) firstItem.getData()).getType() == ILogNode.TYPE_CATEGORY) {
			firstItem = firstItem.getItem(0);
		}
		treeTable.getTree().setSelection(firstItem);
    	this.history.refresh(LogMessagesComposite.REFRESH_UI_AFFECTED);
    	
        this.showResourceLabel();
    }
    
    /*
     * Can be overridden in subclasses to initialize table viewer listener
     */
    protected void initTableViewerListener() {
    	
    }   
    
    protected void showResourceLabel() {
		String resourceName = SVNUIMessages.SVNHistoryPanel_NotSelected;
		if (this.resource != null) {
		    resourceName = this.resource.getUrl();
		}
		this.resourceLabel.setText(resourceName);
	}

    protected void saveChangesImpl() {		
    }
    
    protected void cancelChangesImpl() {
    }

    protected void addPage(SVNLogEntry[] newMessages) {
    	if (this.logMessages == null) {
    		this.pending = false;
			this.logMessages = newMessages.length > 0 ? newMessages : null;
			this.pagingEnabled = this.limit > 0 && newMessages.length == this.limit;
		}
		else if (newMessages.length > 1) {
			LinkedHashSet<SVNLogEntry> entries = new LinkedHashSet<SVNLogEntry>(Arrays.asList(this.logMessages));
			int oldSize = entries.size();
			entries.addAll(Arrays.asList(newMessages));
			this.logMessages = entries.toArray(new SVNLogEntry[entries.size()]);
			this.pagingEnabled = this.limit > 0 && (newMessages.length == this.limit + 1 || entries.size() - oldSize < newMessages.length - 1);
		}
    }
    
    protected void setPagingEnabled() {
    	this.pagingEnabled &= this.resource != null && this.limit > 0;
    	this.pagingItem.setEnabled(this.pagingEnabled);
	    this.pagingAllItem.setEnabled(this.pagingEnabled);
	    this.filterItem.setEnabled(this.resource != null && this.logMessages != null);
	    this.clearFilterItem.setEnabled(this.isFilterEnabled() && this.logMessages != null);
    }
    
    protected void fetchHistory(final GetLogMessagesOperation msgsOp) {
		msgsOp.setIncludeMerged(SVNTeamPreferences.getMergeBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.MERGE_INCLUDE_MERGED_NAME));
		
		final IStructuredSelection selected = (IStructuredSelection)this.history.getTreeViewer().getSelection();
    	IActionOperation showOp = new AbstractActionOperation("Operation_ShowMessages", SVNUIMessages.class) { //$NON-NLS-1$
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				SVNTeamUIPlugin.instance().getWorkbench().getDisplay().syncExec(new Runnable() {
					public void run() {
						if (msgsOp.getExecutionState() != IActionOperation.OK) {
							SVNHistoryPanel.this.pending = false;
							SVNHistoryPanel.this.history.refresh(LogMessagesComposite.REFRESH_ALL);
							return;
						}
						SVNHistoryPanel.this.addPage(msgsOp.getMessages());
						SVNHistoryPanel.this.history.refresh(LogMessagesComposite.REFRESH_ALL);
						SVNHistoryPanel.this.setPagingEnabled();
						
						TreeViewer treeTable = SVNHistoryPanel.this.history.getTreeViewer();
					    if (!treeTable.getTree().isDisposed() && treeTable.getTree().getItems().length > 0) {
					        if (selected.size() != 0) {
					        	treeTable.setSelection(selected);
					        }
							else {
								TreeItem firstItem = treeTable.getTree().getItem(0);
								if (((ILogNode) firstItem.getData()).getType() == ILogNode.TYPE_CATEGORY) {
									firstItem = firstItem.getItem(0);
								}
								treeTable.getTree().setSelection(firstItem);
							}
					        SVNHistoryPanel.this.history.refresh(LogMessagesComposite.REFRESH_UI_AFFECTED);
					        if (SVNHistoryPanel.this.tableViewerListener != null) {
					        	SVNHistoryPanel.this.tableViewerListener.selectionChanged(null);
					        }
					    }
					}
				});
			}
		};
		CompositeOperation op = new CompositeOperation(showOp.getId(), showOp.getMessagesClass());
		op.add(msgsOp);
		op.add(showOp);
		UIMonitorUtility.doTaskNowDefault(op, true);
    }
    
    protected void showNextPage(boolean showAll) {
    	if (this.resource != null) {
    		final GetLogMessagesOperation msgsOp = new GetLogMessagesOperation(this.resource, this.stopOnCopyItem.getSelection());
    		msgsOp.setLimit(showAll ? 0 : this.logMessages == null ? this.limit : this.limit + 1);
    		SVNRevision revision = this.resource.getSelectedRevision();
    		if (this.logMessages != null && this.logMessages.length > 1) {
    			SVNLogEntry lm = this.logMessages[this.logMessages.length - 1];
    			revision = SVNRevision.fromNumber(lm.revision);
    		}    		
    		msgsOp.setStartRevision(revision);
    		this.fetchHistory(msgsOp);
    	}
    }
    
    protected void setFilter() {
	    HistoryFilterPanel panel = new HistoryFilterPanel(this.authorFilter.getAuthorNameToAccept(),
	    													this.commentFilter.getCommentToAccept(),
	    													this.changeFilter.getGangedPathToAccept(),
	    													SVNHistoryPage.getSelectedAuthors(this.logMessages));
	    DefaultDialog dialog = new DefaultDialog(UIMonitorUtility.getDisplay().getActiveShell(), panel);
	    if (dialog.open() == 0) {
	        this.authorFilter.setAuthorNameToAccept(panel.getAuthor()); 
	        this.commentFilter.setCommentToAccept(panel.getComment());
	        this.clearFilterItem.setEnabled(isFilterEnabled());
			SVNHistoryPanel.this.history.refresh(LogMessagesComposite.REFRESH_ALL);
	    }
	}
    
    protected void clearFilter() {
	    this.authorFilter.setAuthorNameToAccept(null);
	    this.commentFilter.setCommentToAccept(null);
	    this.changeFilter.setGangedPathToAccept(null);
	    this.clearFilterItem.setEnabled(false);
		SVNHistoryPanel.this.history.refresh(LogMessagesComposite.REFRESH_ALL);
	}
	
	protected boolean isFilterEnabled() {
	    return this.authorFilter.getAuthorNameToAccept() != null
	    		|| this.commentFilter.getCommentToAccept() != null
	    		|| this.changeFilter.getGangedPathToAccept() != null; 
	}
	
	protected void refresh() {
		long revision = this.history.getSelectedRevision();
		this.pagingEnabled = true;
		this.logMessages = null;
		this.pending = true;
		this.history.refresh(LogMessagesComposite.REFRESH_ALL);
		this.showNextPage(false);
		this.history.setSelectedRevision(revision);
	}
}
