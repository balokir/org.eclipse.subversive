/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.GetLogMessagesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.StringMatcher;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.LogMessagesComposite;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.panel.view.HistoryFilterPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Select resource revision panel implementation
 * 
 * @author Alexander Gurov
 */
public class SelectRevisionPanel extends AbstractDialogPanel {
	protected LogMessagesComposite history;
	protected SVNLogEntry []logMessages;
	protected IRepositoryResource resource;
	protected long currentRevision;
	protected long []selectedRevisions;
	protected String selectedMessage;
	protected int selectionStyle;
	protected long limit;
	protected boolean pagingEnabled;
	protected SVNLogEntry[] selectedLogMessages;

	protected Text resourceLabel;
	
	protected ToolItem hideUnrelatedItem;
	protected ToolItem stopOnCopyItem;
	protected ToolItem pagingItem;
	protected ToolItem pagingAllItem;
	protected ToolItem filterItem;
	protected ToolItem clearFilterItem;
	protected ToolItem refreshItem;
	protected ToolItem groupByDateItem;
	
	protected boolean isCommentFilterEnabled = false;
	protected String filterByComment;
	protected String filterByAuthor;
	protected ISelectionChangedListener tableViewerListener;
	protected IPropertyChangeListener configurationListener;
	protected boolean initialStopOnCopy;

	public SelectRevisionPanel(GetLogMessagesOperation msgOp, int selectionStyle) {
		this(msgOp, selectionStyle, SVNRevision.INVALID_REVISION_NUMBER);
    }

    public SelectRevisionPanel(GetLogMessagesOperation msgOp, int selectionStyle, long currentRevision) {
    	this.selectionStyle = selectionStyle;
        this.dialogTitle = SVNTeamUIPlugin.instance().getResource("SelectRevisionPanel.Title");
        this.dialogDescription = SVNTeamUIPlugin.instance().getResource("SelectRevisionPanel.Description");
        this.defaultMessage = SVNTeamUIPlugin.instance().getResource("SelectRevisionPanel.Message");
		this.resource = msgOp.getResource();
		this.selectedRevisions = null;
		this.currentRevision = currentRevision;
		this.filterByAuthor = "";
		this.filterByComment = "";
    	this.logMessages = msgOp.getMessages();
    	this.initialStopOnCopy = msgOp.getStopOnCopy();
	}
    
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.revisionLinkDialogContext";
	}

	public long []getSelectedRevisions() {
		return this.selectedRevisions;
	}
	public long getSelectedRevision() {
		return this.selectedRevisions[0];
	}

	public String getSelectedMessage() {
		return this.selectedMessage;
	}

    public String getImagePath() {
        return "icons/dialogs/select_revision.gif";
    }
    
    public void postInit() {
        this.manager.setButtonEnabled(0, false);
    }

    public Point getPrefferedSizeImpl() {
        return new Point(700, SWT.DEFAULT);
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
    	
    	this.groupByDateItem.setImage(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/group_by_date.gif").createImage());
    	this.hideUnrelatedItem.setImage(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/hide_unrelated.gif").createImage());
    	this.stopOnCopyItem.setImage(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/stop_on_copy.gif").createImage());
    	this.stopOnCopyItem.setSelection(this.initialStopOnCopy);
    	this.filterItem.setImage(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/filter.gif").createImage());
    	this.clearFilterItem.setDisabledImage(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/clear.gif").createImage());
	    this.clearFilterItem.setImage(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/clear_filter.gif").createImage());
    	this.pagingItem.setImage(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/paging.gif").createImage());
    	this.pagingAllItem.setImage(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/paging_all.gif").createImage());
    	this.refreshItem.setImage(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/refresh.gif").createImage());
    	
    	this.hideUnrelatedItem.setToolTipText(SVNTeamUIPlugin.instance().getResource("SelectRevisionPanel.Unrelated"));
    	this.stopOnCopyItem.setToolTipText(SVNTeamUIPlugin.instance().getResource("SelectRevisionPanel.StopOnCopy"));
    	this.filterItem.setToolTipText(SVNTeamUIPlugin.instance().getResource("SelectRevisionPanel.QuickFilter"));
    	this.clearFilterItem.setToolTipText(SVNTeamUIPlugin.instance().getResource("SelectRevisionPanel.ClearFilter"));
    	this.pagingAllItem.setToolTipText(SVNTeamUIPlugin.instance().getResource("SelectRevisionPanel.ShowAll"));
    	this.refreshItem.setToolTipText(SVNTeamUIPlugin.instance().getResource("SelectRevisionPanel.Refresh"));
    	
    	Composite group = new Composite(parent, SWT.BORDER);
        layout = new GridLayout();
        layout.marginHeight = layout.marginWidth = 0;
        layout.numColumns = 1;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_BOTH));
    	
    	this.history = new LogMessagesComposite(group, 75, this.selectionStyle);
    	data = new GridData(GridData.FILL_BOTH);
    	data.heightHint = 350;
    	this.history.setLayoutData(data);
        this.history.setFocus();
        this.tableViewerListener = new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (SelectRevisionPanel.this.manager != null) {
					if (event != null) {
						SelectRevisionPanel.this.manager.setButtonEnabled(0, !event.getSelection().isEmpty());
					}
					else {
						SelectRevisionPanel.this.manager.setButtonEnabled(0, !SelectRevisionPanel.this.history.getTreeViewer().getSelection().isEmpty());
					}
				}
			}
		};
		this.history.getTreeViewer().addSelectionChangedListener(this.tableViewerListener);
		int type = SVNTeamPreferences.getHistoryInt(store, SVNTeamPreferences.HISTORY_GROUPING_TYPE_NAME);
		SelectRevisionPanel.this.history.setGroupByDate(type == SVNTeamPreferences.HISTORY_GROUPING_TYPE_DATE);
    	
    	if (SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_PAGING_ENABLE_NAME)) {
    		this.limit = SVNTeamPreferences.getHistoryInt(store, SVNTeamPreferences.HISTORY_PAGE_SIZE_NAME);
    		String msg = SVNTeamUIPlugin.instance().getResource("SelectRevisionPanel.ShowNextX", new String[] {String.valueOf(this.limit)});
    	    this.pagingItem.setToolTipText(msg);
    	    this.pagingEnabled = true;
        }
        else {
        	this.limit = 0;
    	    this.pagingItem.setToolTipText(SVNTeamUIPlugin.instance().getResource("SelectRevisionPanel.ShowNextPage"));
    	    this.pagingEnabled = false;
        }
    	this.pagingEnabled = SelectRevisionPanel.this.limit > 0 && this.logMessages.length == SelectRevisionPanel.this.limit;
		this.showMessages(null);
		
        this.setPagingEnabled();
        this.clearFilterItem.setEnabled(false);
    	
    	this.groupByDateItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SelectRevisionPanel.this.history.setGroupByDate(((ToolItem)e.widget).getSelection());
				SelectRevisionPanel.this.history.setTableInput();
			}
    	});
    	this.groupByDateItem.setSelection(type == SVNTeamPreferences.HISTORY_GROUPING_TYPE_DATE);
    	
    	this.hideUnrelatedItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SelectRevisionPanel.this.history.setShowRelatedPathsOnly(SelectRevisionPanel.this.hideUnrelatedItem.getSelection());
			}
    	});
    	this.stopOnCopyItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SelectRevisionPanel.this.refresh();
			}
    	});
    	
        this.filterItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {				
				if (SelectRevisionPanel.this.quickFilter()) {
					SelectRevisionPanel.this.showMessages(null);
				}
			}
    	});
        this.clearFilterItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {				
				SelectRevisionPanel.this.clearFilter();
				SelectRevisionPanel.this.showMessages(null);
			}
    	});
        
    	this.pagingItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {				
				SelectRevisionPanel.this.showNextPage(false);
			}
    	});    	
    	this.pagingAllItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {				
				SelectRevisionPanel.this.showNextPage(true);
			}
    	});
    	
    	this.refreshItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {				
				SelectRevisionPanel.this.refresh();
			}
    	});
        this.showResourceLabel();
    }
    
    public SVNLogEntry[] getSelectedLogMessages() {
        return this.selectedLogMessages;
    }
    
    protected void showResourceLabel() {
		String resourceName = SVNTeamUIPlugin.instance().getResource("SelectRevisionPanel.NotSelected");
		if (this.resource != null) {
		    resourceName = this.resource.getUrl();
		}
		this.resourceLabel.setText(resourceName);
	}

    protected void saveChangesImpl() {
		this.selectedRevisions = this.history.getSelectedRevisions();
		this.selectedMessage = this.history.getSelectedMessage();
		this.selectedLogMessages = this.history.getSelectedLogMessages();
    }
    
    protected void cancelChangesImpl() {
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
    
    protected void setPagingEnabled() {
    	this.pagingEnabled &= this.resource != null && this.limit > 0;
    	this.pagingItem.setEnabled(this.pagingEnabled);
	    this.pagingAllItem.setEnabled(this.pagingEnabled);
	    this.filterItem.setEnabled(this.resource != null && this.logMessages != null);
	    this.clearFilterItem.setEnabled(this.isFilterEnabled() && this.logMessages != null);
    }
    
    protected void showMessages(final GetLogMessagesOperation msgsOp) {
		final IStructuredSelection selected = (IStructuredSelection)this.history.getTreeViewer().getSelection();
    	IActionOperation showOp = new AbstractActionOperation("Operation.ShowMessages") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				 SVNTeamUIPlugin.instance().getWorkbench().getDisplay().syncExec(new Runnable() {
					 public void run() {
						if (msgsOp != null && msgsOp.getExecutionState() == IActionOperation.OK) {
							SelectRevisionPanel.this.pagingEnabled = SelectRevisionPanel.this.limit > 0 && SelectRevisionPanel.this.logMessages == null ? msgsOp.getMessages().length == SelectRevisionPanel.this.limit : msgsOp.getMessages().length == SelectRevisionPanel.this.limit + 1;
							SelectRevisionPanel.this.addPage(msgsOp.getMessages());
						}
						SVNLogEntry[] toShow = SelectRevisionPanel.this.isFilterEnabled() && SelectRevisionPanel.this.logMessages != null ? SelectRevisionPanel.this.filterMessages(SelectRevisionPanel.this.logMessages) : SelectRevisionPanel.this.logMessages;
						SVNRevision current = SelectRevisionPanel.this.currentRevision > 0 ? SVNRevision.fromNumber(SelectRevisionPanel.this.currentRevision) : SVNRevision.fromKind(SVNRevision.HEAD.getKind());
						SelectRevisionPanel.this.history.setLogMessages(current, toShow, SelectRevisionPanel.this.resource);
						SelectRevisionPanel.this.setPagingEnabled();
					 }
				 });
			}
		};
		IActionOperation selectOp = new AbstractActionOperation("Operation.SaveTableSelection") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				SVNTeamUIPlugin.instance().getWorkbench().getDisplay().syncExec(new Runnable() {
					public void run() {
						TreeViewer treeTable = SelectRevisionPanel.this.history.getTreeViewer();
					    if (!treeTable.getTree().isDisposed() && treeTable.getTree().getItems().length > 0) {
					        if (selected.size() != 0) {
					        	treeTable.setSelection(selected);
					        }
					        SelectRevisionPanel.this.history.getHistoryTableListener().selectionChanged(null);
					        ISelectionChangedListener listener = SelectRevisionPanel.this.tableViewerListener;
					        if (listener != null) {
					        	SelectRevisionPanel.this.tableViewerListener.selectionChanged(null);
					        }
					    }
					}
				});
			}
		};
		CompositeOperation op = new CompositeOperation(showOp.getId());
		if (msgsOp != null) {
			op.add(msgsOp);
		}
		op.add(showOp);
		op.add(selectOp, new IActionOperation[] {showOp});
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
    		msgsOp.setSelectedRevision(revision);
    		this.showMessages(msgsOp);
    	}
    }
    protected boolean quickFilter() {
	    boolean okPressed = false;
	    HistoryFilterPanel panel = new HistoryFilterPanel(this.filterByAuthor, this.filterByComment, this.getSelectedAuthors(), this.isCommentFilterEnabled);
	    DefaultDialog dialog = new DefaultDialog(UIMonitorUtility.getDisplay().getActiveShell(), panel);
	    if (dialog.open() == 0) {
	    	okPressed = true;
	        this.filterByAuthor = panel.getAuthor(); 
	        this.filterByComment = panel.getComment();
	        this.isCommentFilterEnabled = panel.isCommentFilterEnabled();
	    }
	    return okPressed;
	}
    
    protected void clearFilter() {
	    this.filterByAuthor = ""; 
	    this.filterByComment = "";
        this.isCommentFilterEnabled = false;
	}
	
	protected boolean isFilterEnabled() {
	    return this.filterByAuthor.length() > 0 || this.isCommentFilterEnabled; 
	}
	
	protected SVNLogEntry[] filterMessages(SVNLogEntry[] msgs) {
	    ArrayList filteredMessages = new ArrayList();
	    for (int i = 0; i < msgs.length; i++) {
			String author = msgs[i].author;
			String message = msgs[i].message;
			StringMatcher authorMatcher = new StringMatcher(this.filterByAuthor);
			StringMatcher commentMatcher = new StringMatcher(this.filterByComment);
			if (this.filterByAuthor.length() > 0 ? authorMatcher.match(author) : true) {
				if (this.isCommentFilterEnabled ? commentMatcher.match(message) : true) {
			        filteredMessages.add(msgs[i]);
			    }
			}
	    }
	    SVNLogEntry []result = (SVNLogEntry [])filteredMessages.toArray(new SVNLogEntry[filteredMessages.size()]);
	    return result.length > 0 ? result : null;	    
	}
	
	protected String[] getSelectedAuthors() {
		List authors = new ArrayList();
		if (this.logMessages != null) {
			for (int i = 0; i < logMessages.length; i++) {
				String current = logMessages[i].author;
				if (current != null && !authors.contains(current)) {
					authors.add(current);
				}
			}
		}
		return (String[])authors.toArray(new String[authors.size()]);
	}
	
	protected void refresh() {
		long revision = this.history.getSelectedRevision();
		this.pagingEnabled = true;
		this.logMessages = null;
		this.showNextPage(false);
		this.history.setSelectedRevision(revision);
	}
}
