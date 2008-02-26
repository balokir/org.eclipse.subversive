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

package org.eclipse.team.svn.ui.history;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevision.Kind;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.extension.factory.ICommentView;
import org.eclipse.team.svn.ui.history.data.RootHistoryCategory;
import org.eclipse.team.svn.ui.history.data.SVNChangedPathData;
import org.eclipse.team.svn.ui.history.data.SVNLocalFileRevision;
import org.eclipse.team.svn.ui.history.model.CategoryLogNode;
import org.eclipse.team.svn.ui.history.model.ILogNode;
import org.eclipse.team.svn.ui.history.model.SVNLogNode;
import org.eclipse.team.svn.ui.utility.ColumnedViewerComparator;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * LogMessage's viewer implementation
 * 
 * @author Alexander Gurov
 */
public class LogMessagesComposite extends SashForm {
	protected SashForm innerSashForm;
    
	protected TreeViewer historyTable;
	protected ISelectionChangedListener historyTableListener;
	protected long currentRevision;
	protected Font currentRevisionFont;
	protected AffectedPathsComposite affectedPathsComposite;
	
	protected ICommentView viewManager;
	
	protected boolean showRelatedPathsOnly;
	
	protected HistoryActionManager actionManager;
	
	protected RootHistoryCategory rootCategory;

	public LogMessagesComposite(Composite parent, boolean multiSelect) {
	    super(parent, SWT.VERTICAL);
		
		this.actionManager = new HistoryActionManager();
		this.rootCategory = new RootHistoryCategory();
	    
		this.initializeFont();
		this.initializeTableView(multiSelect ? SWT.MULTI : SWT.SINGLE);
	}
	
	public void setGroupByDate(boolean groupByDate) {
		this.rootCategory.setGrouped(groupByDate);
	}
	
	public void setRevisionMode(int revisionMode) {
		this.rootCategory.setMode(revisionMode);
	}
	
	public void setLocalHistory(SVNLocalFileRevision []localHistory) {
		this.rootCategory.setLocalHistory(localHistory);
	}
	
	public SVNLocalFileRevision []getLocalHistory () {
		return this.rootCategory.getLocalHistory();
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
		if (this.historyTable.getContentProvider() != null) {
			this.historyTable.setInput(null);
		}
		this.currentRevisionFont.dispose();
		super.dispose();
	}
	
	public TreeViewer getTreeViewer() {
	    return this.historyTable;
	}
	
	public void registerActionManager(IWorkbenchPartSite site) {
		this.affectedPathsComposite.registerActionManager(this.actionManager, site);
	}
	
	public void setSelectedRevision(long revision) {
		if (this.rootCategory.getRemoteHistory() != null) {
			for (SVNLogEntry msg : this.rootCategory.getRemoteHistory()) {
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
	
	public ICommentView getCommentView() {
		return this.viewManager;
	}
	
	public ISelectionChangedListener getHistoryTableListener() {
		return this.historyTableListener;
	}

	public void setTableInput() {
		if (!this.historyTable.getTree().isDisposed()) {
			Object []entries = this.rootCategory.getEntries();
			this.historyTable.getTree().setLinesVisible(entries != RootHistoryCategory.NO_REMOTE && entries != RootHistoryCategory.NO_LOCAL && entries != RootHistoryCategory.NO_REVS);
	        this.historyTable.setInput(new CategoryLogNode(this.rootCategory));
		}
	}
	
	public void clear() {
		this.rootCategory.clear();
		this.historyTableListener.selectionChanged(null);
	}
	
	public void setLogMessages(SVNRevision currentRevision, SVNLogEntry []msgs, IRepositoryResource repositoryResource) {
		this.rootCategory.setRepositoryResource(repositoryResource);
		this.rootCategory.setRemoteHistory(msgs);
		this.actionManager.setRepositoryResource(repositoryResource);
		this.currentRevision = SVNRevision.INVALID_REVISION_NUMBER;
		
		if (msgs == null || msgs.length == 0) {
			this.setTableInput();
			this.historyTableListener.selectionChanged(null);
			return;
		}
		
		if (currentRevision != null && currentRevision != SVNRevision.INVALID_REVISION) {
			if (currentRevision.getKind() == Kind.HEAD) {
				this.currentRevision = Math.max(msgs[0].revision, msgs[msgs.length - 1].revision);
			}
			else if (currentRevision.getKind() == Kind.NUMBER) {
				this.currentRevision = ((SVNRevision.Number)currentRevision).getNumber();
			}
		}
		
		this.setTableInput();
	}
	
	public String getSelectedMessagesAsString() {
		String historyText = "";
		HistoryLabelProvider provider = new HistoryLabelProvider(true);
		HashSet<ILogNode> processed = new HashSet<ILogNode>();
		for (Iterator it = ((IStructuredSelection)this.historyTable.getSelection()).iterator(); it.hasNext(); ) {
			ILogNode node = (ILogNode)it.next();
			historyText += this.toString(processed, node, provider);
			if (node.hasChildren()) {
				ILogNode []children = node.getChildren();
				for (int j = 0; j < children.length; j++) {
					historyText += this.toString(processed, children[j], provider);
				}
			}
		}
		return historyText;
	}
	
	protected String toString(HashSet<ILogNode> processed, ILogNode node, HistoryLabelProvider provider) {
		if (processed.contains(node)) {
			return "";
		}
		processed.add(node);
		String historyText = provider.getColumnText(node, 0);
		for (int i = 1; i < ILogNode.NUM_OF_COLUMNS; i++) {
			historyText += "\t" + provider.getColumnText(node, i);
		}
		return historyText + System.getProperty("line.separator");
	}
	
	private void initializeTableView(int style) {
		this.innerSashForm = new SashForm(this, SWT.VERTICAL);
		
		Tree treeTable = new Tree(this.innerSashForm, style | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		treeTable.setHeaderVisible(true);
		treeTable.setLinesVisible(true);
		TableLayout layout = new TableLayout();
		treeTable.setLayout(layout);
		
		this.viewManager = ExtensionsManager.getInstance().getCurrentMessageFactory().getCommentView();
		this.viewManager.createCommentView(this.innerSashForm, SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
		
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
		this.historyTable.setLabelProvider(new HistoryLabelProvider(false));
		
		this.historyTableListener = new ISelectionChangedListener() {
        	protected ILogNode oldSelection;
        	protected boolean hideUnrelated = LogMessagesComposite.this.showRelatedPathsOnly;
        	
			public void selectionChanged(SelectionChangedEvent event) {
				if (LogMessagesComposite.this.isDisposed()) {
					return;
				}
				IStructuredSelection tSelection = (IStructuredSelection)LogMessagesComposite.this.historyTable.getSelection();
				if (tSelection.size() > 0) {
					ILogNode selection = (ILogNode)tSelection.getFirstElement();
					if (this.oldSelection != selection || this.hideUnrelated != LogMessagesComposite.this.showRelatedPathsOnly)
					{
						String message = selection.getComment();
						message = message == null || message.length() == 0 ? SVNTeamPlugin.instance().getResource("SVNInfo.NoComment") : message;
						LogMessagesComposite.this.viewManager.setComment(message);
						long revision = LogMessagesComposite.this.getSelectedRevision();
						
						Collection<String> relatedPrefixes = LogMessagesComposite.this.showRelatedPathsOnly ? LogMessagesComposite.this.rootCategory.getRelatedPathPrefixes() : null;
						Collection<String> relatedParents = LogMessagesComposite.this.showRelatedPathsOnly ? LogMessagesComposite.this.rootCategory.getRelatedParents() : null;
						SVNChangedPathData []pathData = LogMessagesComposite.this.rootCategory.getPathData(selection);
						
						LogMessagesComposite.this.actionManager.setSelectedRevision(revision);
						LogMessagesComposite.this.affectedPathsComposite.setInput(pathData, relatedPrefixes, relatedParents, revision);
						
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
		
		this.historyTable.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
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
	
	private class HistoryLabelProvider implements ITableLabelProvider, IFontProvider {
		private boolean fullMessage;
		private Map<ImageDescriptor, Image> images;
		
		public HistoryLabelProvider(boolean fullMessage) {
			this.fullMessage = fullMessage;
			this.images = new HashMap<ImageDescriptor, Image>();
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
			return ((ILogNode)element).getLabel(columnIndex, this.fullMessage ? ILogNode.LABEL_FLAT : ILogNode.LABEL_TRIM, LogMessagesComposite.this.currentRevision);
		}

		public Font getFont(Object element) {
			if (((ILogNode)element).requiresBoldFont(LogMessagesComposite.this.currentRevision)) {
				return LogMessagesComposite.this.currentRevisionFont;
			}
			return null;
		}
		
		public void dispose() {
			for (Image img : this.images.values()) {
				img.dispose();
			}
		}

		public boolean isLabelProperty(Object element, String property) {
			return true;
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void removeListener(ILabelProviderListener listener) {
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
