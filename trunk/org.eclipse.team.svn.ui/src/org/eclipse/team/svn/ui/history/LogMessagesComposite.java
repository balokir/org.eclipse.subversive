/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Gabor Liptak - Speedup Pattern's usage
 *******************************************************************************/

package org.eclipse.team.svn.ui.history;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IColorProvider;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.extension.factory.ICommentView;
import org.eclipse.team.svn.ui.history.data.RootHistoryCategory;
import org.eclipse.team.svn.ui.history.data.SVNChangedPathData;
import org.eclipse.team.svn.ui.history.model.CategoryLogNode;
import org.eclipse.team.svn.ui.history.model.ILogNode;
import org.eclipse.team.svn.ui.history.model.SVNLogNode;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.ColumnedViewerComparator;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ITheme;

/**
 * LogMessage's viewer implementation
 * 
 * @author Alexander Gurov
 */
public class LogMessagesComposite extends SashForm {
	public static final int REFRESH_UI_AFFECTED = 1;
	public static final int REFRESH_UI_ALL = 2;
	public static final int REFRESH_ALL = 3;
	
	protected SashForm innerSashForm;
    
	protected TreeViewer historyTable;
	protected ISelectionChangedListener historyTableListener;
	protected Font currentRevisionFont;
	protected AffectedPathsComposite affectedPathsComposite;
	
	protected ICommentView commentViewManager;
	
	protected RootHistoryCategory rootCategory;

	protected HistoryActionManager actionManager;
	protected ISVNHistoryViewInfo info;
	
	public LogMessagesComposite(Composite parent, boolean multiSelect, ISVNHistoryViewInfo info) {
	    super(parent, SWT.VERTICAL);
		
	    this.info = info;
		this.rootCategory = new RootHistoryCategory(info);
	    
		this.initializeFont();
		this.initializeTableView(multiSelect ? SWT.MULTI : SWT.SINGLE);
	}
	
	public TreeViewer getTreeViewer() {
	    return this.historyTable;
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
	
	public void collapseAll() {
	    this.historyTable.collapseAll();
	}
	
	public void expandAll() {
	    this.historyTable.expandAll();
	}
	
	public void registerActionManager(HistoryActionManager manager, IWorkbenchPartSite site) {
		this.actionManager = manager;
		
		manager.logMessagesManager.installKeyBindings(this.historyTable);
		manager.logMessagesManager.installDefaultAction(this.historyTable);
		manager.logMessagesManager.installMenuActions(this.historyTable, site);
		
		this.affectedPathsComposite.registerActionManager(manager, site);
	}
	
	public void setSelectedRevision(long revision) {
		SVNLogEntry []msgs = this.rootCategory.getRemoteHistory();
		if (msgs != null) {
			for (SVNLogEntry msg : msgs) {
				if (msg.revision == revision) {
					this.historyTable.setSelection(new StructuredSelection(new SVNLogNode(msg, null)), true);
					return;
				}
			}
		}
		this.historyTable.setSelection(new StructuredSelection());
	}
	
	public SVNLogEntry []getSelectedLogMessages() {
		IStructuredSelection tSelection = (IStructuredSelection)this.historyTable.getSelection();
		ArrayList<SVNLogEntry> entries = new ArrayList<SVNLogEntry>();
		for (Iterator it = tSelection.iterator(); it.hasNext(); ) {
			ILogNode node = (ILogNode)it.next();
			if (node.getType() == ILogNode.TYPE_SVN) {
				entries.add((SVNLogEntry)node.getEntity());
			}
		}
		return entries.toArray(new SVNLogEntry[entries.size()]);
	}
	
	public long getSelectedRevision() {
		IStructuredSelection tSelection = (IStructuredSelection)this.historyTable.getSelection();
		if (tSelection.size() > 0) {
			ILogNode node = (ILogNode)tSelection.getFirstElement();
			if (node.getType() == ILogNode.TYPE_SVN) {
				return ((SVNLogEntry)node.getEntity()).revision;
			}
		}
		return SVNRevision.INVALID_REVISION_NUMBER;
	}
	
	public void refresh(int refreshType) {
		if (refreshType == LogMessagesComposite.REFRESH_ALL) {
			if (this.info.getResource() != null) {
				this.commentViewManager.usedFor(this.info.getResource());
			}
			else {
				this.commentViewManager.usedFor(this.info.getRepositoryResource());
			}
			
			this.rootCategory.refreshModel();
		}
		
		this.refreshImpl(refreshType);
	}
	
	public void dispose() {
		this.historyTable.removeSelectionChangedListener(this.historyTableListener);
		if (this.historyTable.getContentProvider() != null) {
			this.historyTable.setInput(null);
		}
		this.currentRevisionFont.dispose();
		super.dispose();
	}
	
	protected void refreshImpl(int refreshType) {
		if (!this.historyTable.getTree().isDisposed()) {
			if (refreshType != LogMessagesComposite.REFRESH_UI_AFFECTED) {
				Object []entries = this.rootCategory.getEntries();
				this.historyTable.getTree().setLinesVisible(entries != RootHistoryCategory.NO_REMOTE && entries != RootHistoryCategory.NO_LOCAL && entries != RootHistoryCategory.NO_REVS);
		        this.historyTable.setInput(new CategoryLogNode(this.rootCategory));
			}
			this.historyTableListener.selectionChanged(null);
		}
	}
	
	private void initializeTableView(int style) {
		this.innerSashForm = new SashForm(this, SWT.VERTICAL);
		
		Tree treeTable = new Tree(this.innerSashForm, style | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		treeTable.setHeaderVisible(true);
		treeTable.setLinesVisible(true);
		TableLayout layout = new TableLayout();
		treeTable.setLayout(layout);
		
		this.commentViewManager = ExtensionsManager.getInstance().getCurrentMessageFactory().getCommentView();
		this.commentViewManager.createCommentView(this.innerSashForm, SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
		
		this.affectedPathsComposite = new AffectedPathsComposite(this, SWT.NONE);
		
		this.innerSashForm.setWeights(new int[] {75, 25});
		this.setWeights(new int[] {70, 30});		
		
		this.historyTable = new TreeViewer(treeTable);

		//creating a comparator now to get listeners for columns
		HistoryTableComparator comparator = new HistoryTableComparator(this.historyTable);
		
		//revision
		TreeColumn col = new TreeColumn(treeTable, SWT.NONE);
		col.setResizable(true);
		col.setAlignment(SWT.RIGHT);
		col.setText(SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Revision"));
		col.addSelectionListener(comparator);
		layout.addColumnData(new ColumnWeightData(14, true));
	
		// creation date
		col = new TreeColumn(treeTable, SWT.NONE);
		col.setResizable(true);
		col.setText(SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Date"));
		col.addSelectionListener(comparator);
		layout.addColumnData(new ColumnWeightData(15, true));
	
		//file count
		col = new TreeColumn(treeTable, SWT.NONE);
		col.setResizable(true);
		col.setAlignment(SWT.RIGHT);
		col.setText(SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Changes"));
		col.addSelectionListener(comparator);
		layout.addColumnData(new ColumnWeightData(7, true));
		
		// author
		col = new TreeColumn(treeTable, SWT.NONE);
		col.setResizable(true);
		col.setText(SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Author"));
		col.addSelectionListener(comparator);
		layout.addColumnData(new ColumnWeightData(14, true));
	
		//comment
		col = new TreeColumn(treeTable, SWT.NONE);
		col.setResizable(true);
		col.setText(SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Comment"));
		col.addSelectionListener(comparator);
		layout.addColumnData(new ColumnWeightData(50, true));
		
		//adding a comparator and initializing default sort column and direction
		this.historyTable.setComparator(comparator);
		comparator.setColumnNumber(ILogNode.COLUMN_DATE);
		comparator.setReversed(true);
		this.historyTable.getTree().setSortColumn(this.historyTable.getTree().getColumn(ILogNode.COLUMN_DATE));
		this.historyTable.getTree().setSortDirection(SWT.DOWN);
		
		this.historyTable.setContentProvider(new ITreeContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			public Object[] getElements(Object inputElement) {
				return inputElement == null ? new Object[0] : ((ILogNode)inputElement).getChildren();
			}
			public Object[] getChildren(Object parentElement) {
				return ((ILogNode)parentElement).getChildren();
			}
			public Object getParent(Object element) {
				return ((ILogNode)element).getParent();
			}
			public boolean hasChildren(Object element) {
				return ((ILogNode)element).hasChildren();
			}
			public void dispose () {
			}
		});
		this.historyTable.setLabelProvider(new HistoryLabelProvider());
		
		this.historyTableListener = new ISelectionChangedListener() {
        	protected ILogNode oldSelection;
        	protected boolean hideUnrelated = LogMessagesComposite.this.info.isRelatedPathsOnly();
        	
			public void selectionChanged(SelectionChangedEvent event) {
				if (LogMessagesComposite.this.isDisposed()) {
					return;
				}
				IStructuredSelection tSelection = (IStructuredSelection)LogMessagesComposite.this.historyTable.getSelection();
				if (tSelection.size() > 0) {
					ILogNode selection = (ILogNode)tSelection.getFirstElement();
					if (this.oldSelection != selection || this.hideUnrelated != LogMessagesComposite.this.info.isRelatedPathsOnly()) {
						String message = selection.getComment();
						if (message == null || message.length() == 0) {
							message = selection.getType() == ILogNode.TYPE_SVN ? SVNTeamPlugin.instance().getResource("SVNInfo.NoComment") : "";
						}
						LogMessagesComposite.this.commentViewManager.setComment(message);

						Collection<String> relatedPrefixes = LogMessagesComposite.this.info.isRelatedPathsOnly() ? LogMessagesComposite.this.rootCategory.getRelatedPathPrefixes() : null;
						Collection<String> relatedParents = LogMessagesComposite.this.info.isRelatedPathsOnly() ? LogMessagesComposite.this.rootCategory.getRelatedParents() : null;
						SVNChangedPathData []pathData = LogMessagesComposite.this.rootCategory.getPathData(selection);
						long revision = LogMessagesComposite.this.getSelectedRevision();
						
						if (LogMessagesComposite.this.actionManager != null) {
							LogMessagesComposite.this.actionManager.setSelectedRevision(revision);
						}
						LogMessagesComposite.this.affectedPathsComposite.setInput(pathData, relatedPrefixes, relatedParents, revision);
						
						this.oldSelection = selection;
						this.hideUnrelated = LogMessagesComposite.this.info.isRelatedPathsOnly();
					}
				}
				else {
					LogMessagesComposite.this.commentViewManager.setComment("");
					LogMessagesComposite.this.affectedPathsComposite.setInput(null, null, null, -1);
					this.oldSelection = null;
				}
			}
		};
        this.historyTable.addSelectionChangedListener(this.historyTableListener);
		
		this.historyTable.setAutoExpandLevel(2); //auto-expand all categories
        this.historyTable.setInput(new CategoryLogNode(this.rootCategory));
	}
	
	private void initializeFont() {
		Font defaultFont = JFaceResources.getDefaultFont();
		FontData[] data = defaultFont.getFontData();
		for (int i = 0; i < data.length; i++) {
			data[i].setStyle(SWT.BOLD);
		}
		this.currentRevisionFont = new Font(this.getDisplay(), data);
	}
	
	protected class HistoryLabelProvider implements ITableLabelProvider, IFontProvider, IColorProvider {
		private Color mergedRevisionsForeground;
		private Map<ImageDescriptor, Image> images;
		protected IPropertyChangeListener configurationListener;
		
		public HistoryLabelProvider() {
			this.images = new HashMap<ImageDescriptor, Image>();
			this.configurationListener = new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					if (event.getProperty().startsWith(SVNTeamPreferences.DECORATION_BASE)) {
						UIMonitorUtility.getDisplay().syncExec(new Runnable() {
							public void run() {
								HistoryLabelProvider.this.loadConfiguration();
								LogMessagesComposite.this.refresh(LogMessagesComposite.REFRESH_UI_ALL);
							}
						});
					}
				}
			};
			this.loadConfiguration();
			PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().addPropertyChangeListener(this.configurationListener);
		}
		
		protected void loadConfiguration() {
			if (this.mergedRevisionsForeground != null) {
				this.mergedRevisionsForeground.dispose();
			}
			ITheme current = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
			this.mergedRevisionsForeground = new Color(Display.getCurrent(), current.getColorRegistry().get(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.NAME_OF_MERGED_REVISIONS_FOREGROUND_COLOR)).getRGB());
		}
		
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				ILogNode node = (ILogNode)element;
				ImageDescriptor descr = node.getImageDescriptor();
				Image retVal = this.images.get(descr);
				if (descr != null && retVal == null) {
					this.images.put(descr, retVal = descr.createImage());
				}
				return retVal;
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			return ((ILogNode)element).getLabel(columnIndex, ILogNode.LABEL_TRIM, LogMessagesComposite.this.info.getCurrentRevision());
		}

		public Font getFont(Object element) {
			return ((ILogNode)element).requiresBoldFont(LogMessagesComposite.this.info.getCurrentRevision()) ? LogMessagesComposite.this.currentRevisionFont : null;
		}
		
		public void dispose() {
			PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().removePropertyChangeListener(this.configurationListener);
			for (Image img : this.images.values()) {
				img.dispose();
			}
			this.mergedRevisionsForeground.dispose();
		}

		public boolean isLabelProperty(Object element, String property) {
			return true;
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void removeListener(ILabelProviderListener listener) {
		}

		public Color getBackground(Object element) {
			return null;
		}

		public Color getForeground(Object element) {
			ILogNode node = (ILogNode)element;
			return node.getType() == ILogNode.TYPE_SVN && node.getParent() instanceof SVNLogNode ? this.mergedRevisionsForeground : null;
		}

	}
    
	protected class HistoryTableComparator extends ColumnedViewerComparator {
		public HistoryTableComparator(Viewer treeViewer) {
			super(treeViewer);
		}
		
		public int compareImpl(Viewer viewer, Object row1, Object row2) {
			ILogNode node1 = (ILogNode)row1;
			ILogNode node2 = (ILogNode)row2;
			switch (this.column) {
				case (ILogNode.COLUMN_REVISION) : {
					long rev1 = node1.getRevision();
					long rev2 = node2.getRevision();
					if (rev1 != SVNRevision.INVALID_REVISION_NUMBER && rev2 != SVNRevision.INVALID_REVISION_NUMBER) {
						return rev1 < rev2 ? -1 : rev1 > rev2 ? 1 : 0;
					}
				}
				case (ILogNode.COLUMN_DATE) : {
					return node1.getTimeStamp() < node2.getTimeStamp() ? -1 : node1.getTimeStamp() > node2.getTimeStamp() ? 1 : 0;
	            }
				case (ILogNode.COLUMN_CHANGES) : {
					return node1.getChangesCount() < node2.getChangesCount() ? -1 : node1.getChangesCount() > node2.getChangesCount() ? 1 : 0;
	            }
				case (ILogNode.COLUMN_AUTHOR) : {
					return ColumnedViewerComparator.compare(node1.getAuthor(), node2.getAuthor());
	            }
				case (ILogNode.COLUMN_COMMENT) : {
					return ColumnedViewerComparator.compare(node1.getComment(), node2.getComment());
	            }
			}
			return 0;
        }
    }
	
}
