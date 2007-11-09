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
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.client.SVNLogEntry;
import org.eclipse.team.svn.core.client.SVNLogPath;
import org.eclipse.team.svn.core.client.SVNRevision;
import org.eclipse.team.svn.core.client.SVNRevision.Kind;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.PatternProvider;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.extension.factory.ICommentView;
import org.eclipse.team.svn.ui.utility.OverlayedImageDescriptor;
import org.eclipse.team.svn.ui.utility.TableViewerSorter;

/**
 * LogMessage's viewer implementation
 * 
 * @author Alexander Gurov
 */
public class LogMessagesComposite extends SashForm {
	protected static Image FILE_IMAGE;
	protected static Image FOLDER_IMAGE;
	protected static Image ADDED_FILE_IMAGE;
	protected static Image MODIFIED_FILE_IMAGE;
	protected static Image DELETED_FILE_IMAGE;
	protected static Image REPLACED_FILE_IMAGE;
	
    protected SashForm innerSashForm;
    protected boolean commentVisible;
    protected boolean affectedVisible;
    protected boolean hierarchicalAffected;
    
	protected TableViewer historyTable;
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
	
	public LogMessagesComposite(Composite parent, int logPercent, int style) {
	    super(parent, SWT.VERTICAL);
	    
	    this.commentVisible = true;
	    this.affectedVisible = true;

	    this.pathData = new HashMap();
		
		this.initializeTableView(style, logPercent);
		this.initializeFont();
		this.setDefaults();
	}
	
	public void setCommentViewerVisible(boolean visible) {
	    if (visible) {
	    	this.innerSashForm.setMaximizedControl(null);
	    }
	    else {
	    	this.innerSashForm.setMaximizedControl(this.historyTable.getControl());
	    }
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
	}
	
	public TableViewer getTableViewer() {
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
			this.historyTable.setSelection(new StructuredSelection((Object)newSelection), true);
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
				revisions[i++] = ((SVNLogEntry)it.next()).revision;
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
		if (tSelection.size() > 0) {
			return ((SVNLogEntry)tSelection.getFirstElement()).revision;
		}
		return SVNRevision.INVALID_REVISION_NUMBER;
	}
	
	public String getSelectedMessage() {
		IStructuredSelection tSelection = (IStructuredSelection)this.historyTable.getSelection();
		if (tSelection.size() > 0) {
			String message = ((SVNLogEntry)tSelection.getFirstElement()).message;
			return message == null ? "" : message;
		}
		return "";
	}
	
	public String getSelectedMessageNoComment() {
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
		
		for (int i = 0; i < msgs.length; i++) {
			this.mapPathData(msgs[i], msgs[i].changedPaths);
		}
		
		this.setTableInput();
	}
	
	public String getSelectedMessagesAsString() {
		String historyText = "";
		LogMessagesLabelProvider provider = new LogMessagesLabelProvider(true);
		Object[] selectedItems = ((IStructuredSelection)this.historyTable.getSelection()).toArray();
		for (int i = 0; i < selectedItems.length; i++) {
			Object rowItems = selectedItems[i];
			// The first column is never shown
			for (int j = 1; j < 6; j++) {
				historyText += provider.getColumnText(rowItems, j);
				historyText += (j < 6 ? "\t" : "");
			}
			historyText += System.getProperty("line.separator");
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
	
	protected void setTableInput() {
		if (!this.historyTable.getTable().isDisposed()) {
			this.historyTable.setInput(this.msgs);
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
		
		throw new RuntimeException(MessageFormat.format(SVNTeamUIPlugin.instance().getResource("Error.InvalidLogAction"), new String[] {String.valueOf(action)}));
	}
	
	private void setDefaults() {
		SVNTeamUIPlugin instance = SVNTeamUIPlugin.instance();
		LogMessagesComposite.FILE_IMAGE = instance.getImageDescriptor("icons/views/history/file.gif").createImage();
		LogMessagesComposite.FOLDER_IMAGE = instance.getImageDescriptor("icons/views/history/affected_folder.gif").createImage();
		LogMessagesComposite.ADDED_FILE_IMAGE = (new OverlayedImageDescriptor(LogMessagesComposite.FILE_IMAGE, instance.getImageDescriptor("icons/overlays/addition.gif"), new Point(22, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.CENTER_V)).createImage();
		LogMessagesComposite.MODIFIED_FILE_IMAGE = (new OverlayedImageDescriptor(LogMessagesComposite.FILE_IMAGE, instance.getImageDescriptor("icons/overlays/change.gif"), new Point(22, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.CENTER_V)).createImage();
		LogMessagesComposite.DELETED_FILE_IMAGE = (new OverlayedImageDescriptor(LogMessagesComposite.FILE_IMAGE, instance.getImageDescriptor("icons/overlays/deletion.gif"), new Point(22, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.CENTER_V)).createImage();
		LogMessagesComposite.REPLACED_FILE_IMAGE = (new OverlayedImageDescriptor(LogMessagesComposite.FILE_IMAGE, instance.getImageDescriptor("icons/overlays/replacement.gif"), new Point(22, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.CENTER_V)).createImage();
		CompareUI.disposeOnShutdown(LogMessagesComposite.FILE_IMAGE);
		CompareUI.disposeOnShutdown(LogMessagesComposite.FOLDER_IMAGE);
		CompareUI.disposeOnShutdown(LogMessagesComposite.ADDED_FILE_IMAGE);
		CompareUI.disposeOnShutdown(LogMessagesComposite.MODIFIED_FILE_IMAGE);
		CompareUI.disposeOnShutdown(LogMessagesComposite.DELETED_FILE_IMAGE);
		CompareUI.disposeOnShutdown(LogMessagesComposite.REPLACED_FILE_IMAGE);
	}
	
	private void initializeTableView(int style, int logPercent) {
		GridData data = null;
		
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		this.setLayoutData(data);
		
		this.innerSashForm = new SashForm(this, SWT.VERTICAL);
		Table table = new Table(innerSashForm, style | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		data = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(data);
		
		this.viewManager = ExtensionsManager.getInstance().getCurrentMessageFactory().getCommentView();
		this.viewManager.createCommentView(this.innerSashForm, SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
		this.affectedPathsComposite = new AffectedPathsComposite(this, SWT.FILL);
		this.affectedPathsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.innerSashForm.setWeights(new int[] {75, 25});
		this.setWeights(new int[] {logPercent, 100 - logPercent});		
		
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		
		this.historyTable = new TableViewer(table);

		TableViewerSorter sorter = new TableViewerSorter(this.historyTable, new TableViewerSorter.IColumnComparator() {
            public int compare(Object row1, Object row2, int column) {
                SVNLogEntry rowData1 = (SVNLogEntry)row1;
                SVNLogEntry rowData2 = (SVNLogEntry)row2;
                if (column == 1) {
                    return new Long(rowData1.revision).compareTo(new Long(rowData2.revision));
                }
                if (column == 2) {
					return new Long(rowData1.date).compareTo(new Long(rowData2.date));
                }
                if (column == 3) {
                	int files1 = rowData1.changedPaths == null ? 0 : rowData1.changedPaths.length;
                	int files2 = rowData2.changedPaths == null ? 0 : rowData2.changedPaths.length;
                	return new Integer(files1).compareTo(new Integer(files2));
                }
                if (column == 4) {
                    return TableViewerSorter.compare(rowData1.author == null ? "" : rowData1.author, rowData2.author == null ? "" : rowData2.author);
                }
                return TableViewerSorter.compare(rowData1.message == null ? "" : rowData1.message, rowData2.message == null ? "" : rowData2.message);
            }
        });
		this.historyTable.setSorter(sorter);
		
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setResizable(false);
		col.setText("");		
		layout.addColumnData(new ColumnWeightData(0, 0, false));
		
		//revision
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setAlignment(SWT.RIGHT);
		col.setText(SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Revision"));
		col.addSelectionListener(sorter);
		layout.addColumnData(new ColumnWeightData(10, true));
	
		// creation date
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Date"));
		col.addSelectionListener(sorter);
		layout.addColumnData(new ColumnWeightData(20, true));
	
		//file count
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setAlignment(SWT.RIGHT);
		col.setText(SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Changes"));
		col.addSelectionListener(sorter);
		layout.addColumnData(new ColumnWeightData(10, true));
		
		// author
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Author"));
		col.addSelectionListener(sorter);
		layout.addColumnData(new ColumnWeightData(13, true));
	
		//comment
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Comment"));
		col.addSelectionListener(sorter);
		layout.addColumnData(new ColumnWeightData(46, true));

		this.historyTable.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return LogMessagesComposite.this.msgs;
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		this.historyTable.setLabelProvider(new LogMessagesLabelProvider(false));
		
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
		this.currentRevisionFont = new Font(this.historyTable.getTable().getDisplay(), data);
	}
	
    private class LogMessagesLabelProvider implements ITableLabelProvider, IFontProvider {
		private DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());
		private boolean fullMessage;
		
		public LogMessagesLabelProvider(boolean fullMessage) {
			this.fullMessage = fullMessage;
		}
		
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			SVNLogEntry row = (SVNLogEntry)element;
			switch (columnIndex) {
				case 1: {
					return LogMessagesComposite.this.revisionToString(row.revision);
				}
				case 2: {
					return row.date == 0 ? SVNTeamPlugin.instance().getResource("SVNInfo.NoDate") : this.dateTimeFormat.format(new Date(row.date));
				}
				case 3: {
					return String.valueOf(row.changedPaths != null ? row.changedPaths.length : 0);
				}
				case 4: {
					return row.author == null || row.author.length() == 0 ? SVNTeamPlugin.instance().getResource("SVNInfo.NoAuthor") : row.author;
				}
				case 5: {
					return row.message == null || row.message.length() == 0 ? SVNTeamPlugin.instance().getResource("SVNInfo.NoComment") : (this.fullMessage ? LogMessagesComposite.flattenMultiLineText(row.message, " ") : FileUtility.formatMultilineText(row.message));
				}
				default: {
					return "";
				}
			}
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return true;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

		public Font getFont(Object element) {
			SVNLogEntry row = (SVNLogEntry)element;
			if (LogMessagesComposite.this.currentRevision == row.revision && !LogMessagesComposite.this.currentRevisionFont.isDisposed()) {
				return LogMessagesComposite.this.currentRevisionFont;
			}
			return null;
		}
		
	}
    
}
