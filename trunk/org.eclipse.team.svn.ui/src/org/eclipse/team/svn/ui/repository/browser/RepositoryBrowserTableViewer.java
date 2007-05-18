/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elena Matokhina - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.repository.browser;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.client.Lock;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource.Info;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.repository.model.IToolTipProvider;
import org.eclipse.team.svn.ui.repository.model.RepositoryBranches;
import org.eclipse.team.svn.ui.repository.model.RepositoryFictiveWorkingDirectory;
import org.eclipse.team.svn.ui.repository.model.RepositoryFile;
import org.eclipse.team.svn.ui.repository.model.RepositoryFolder;
import org.eclipse.team.svn.ui.repository.model.RepositoryResource;
import org.eclipse.team.svn.ui.repository.model.RepositoryRoot;
import org.eclipse.team.svn.ui.repository.model.RepositoryTags;
import org.eclipse.team.svn.ui.repository.model.RepositoryTrunk;
import org.eclipse.team.svn.ui.repository.model.ToolTipVariableSetProvider;
import org.eclipse.team.svn.ui.utility.TableViewerSorter;

/**
 * Repository browser table viewer
 *
 * @author Elena Matokhina
 */
public class RepositoryBrowserTableViewer extends TableViewer {
	public static final String FMT_REPOSITORY_RESOURCE = "";
	public static final String FMT_REPOSITORY_FILE = "{" + ToolTipVariableSetProvider.NAME_OF_LOCK_OWNER + "}" +
													 "{" + ToolTipVariableSetProvider.NAME_OF_LOCK_CREATION_DATE + "}" +
													 "{" + ToolTipVariableSetProvider.NAME_OF_LOCK_EXPIRATION_DATE + "}" +
													 "{" + ToolTipVariableSetProvider.NAME_OF_LOCK_COMMENT + "}";
	public static final String FMT_REPOSITORY_FOLDER =  "";
	public static final String FMT_REPOSITORY_BRANCHES = RepositoryBrowserTableViewer.FMT_REPOSITORY_FOLDER;
	public static final String FMT_REPOSITORY_ROOT = RepositoryBrowserTableViewer.FMT_REPOSITORY_FOLDER;
	public static final String FMT_REPOSITORY_TAGS = RepositoryBrowserTableViewer.FMT_REPOSITORY_FOLDER;
	public static final String FMT_REPOSITORY_TRUNK = RepositoryBrowserTableViewer.FMT_REPOSITORY_FOLDER;

	private static final Map class2Format = new HashMap();

	static {
		RepositoryBrowserTableViewer.class2Format.put(RepositoryResource.class, RepositoryBrowserTableViewer.FMT_REPOSITORY_RESOURCE);
		RepositoryBrowserTableViewer.class2Format.put(RepositoryFile.class, RepositoryBrowserTableViewer.FMT_REPOSITORY_FILE);
		RepositoryBrowserTableViewer.class2Format.put(RepositoryFolder.class, RepositoryBrowserTableViewer.FMT_REPOSITORY_FOLDER);
		RepositoryBrowserTableViewer.class2Format.put(RepositoryBranches.class, RepositoryBrowserTableViewer.FMT_REPOSITORY_BRANCHES);
		RepositoryBrowserTableViewer.class2Format.put(RepositoryRoot.class, RepositoryBrowserTableViewer.FMT_REPOSITORY_ROOT);
		RepositoryBrowserTableViewer.class2Format.put(RepositoryTags.class, RepositoryBrowserTableViewer.FMT_REPOSITORY_TAGS);
		RepositoryBrowserTableViewer.class2Format.put(RepositoryTrunk.class, RepositoryBrowserTableViewer.FMT_REPOSITORY_TRUNK);
	}
	
	protected static String hasProps;
	protected static String noProps;
	protected static String noAuthor;

	protected int nameColumnIdx;
	protected int revisionColumnIdx;
	protected int dateColumnIdx;
	protected int authorColumnIdx;
	protected int lockOwnerColumnIdx;
	protected int propertiesColumnIdx;
	protected int sizeColumnIdx;
	
	protected int columnIndexCounter;
	
	public RepositoryBrowserTableViewer(Table contentsTable) {
		super(contentsTable);
	}
	
	public RepositoryBrowserTableViewer (Composite parent, int style) {
		super(new Table(parent, style));
		this.columnIndexCounter = 0;		
	}

	public void initialize() {
		RepositoryBrowserTableViewer.noAuthor = SVNTeamPlugin.instance().getResource("SVNInfo.NoAuthor");
		RepositoryBrowserTableViewer.hasProps = SVNTeamUIPlugin.instance().getResource("RepositoriesView.Browser.HasProps");
		RepositoryBrowserTableViewer.noProps = SVNTeamUIPlugin.instance().getResource("RepositoriesView.Browser.NoProps");
		
		this.getTable().setHeaderVisible(true);
		this.getTable().setLinesVisible(true);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		this.getTable().setLayoutData(data);

		this.getTable().setLayout(new TableLayout());
		this.getTable().addMouseTrackListener(new MouseTrackAdapter() {
			public void mouseHover(MouseEvent e) {
				String tooltipText = "";
				TableItem item = RepositoryBrowserTableViewer.this.getTable().getItem(new Point(e.x, e.y));
				if (item != null) {
					Object data = item.getData();
					if (data != null && data instanceof IToolTipProvider) {
						tooltipText = ((IToolTipProvider)data).getToolTipMessage((String)RepositoryBrowserTableViewer.class2Format.get(data.getClass()));
					}
				}
				RepositoryBrowserTableViewer.this.getTable().setToolTipText(tooltipText);
			}			

			public void mouseExit(MouseEvent e) {
				RepositoryBrowserTableViewer.this.getTable().setToolTipText("");
			}
		});	
			
		TableViewerSorter sorter = new TableViewerSorter(this, new TableViewerSorter.IColumnComparator() {
			public int compare(Object row1, Object row2, int column) {
				int ordering = ((TableViewerSorter)RepositoryBrowserTableViewer.this.getSorter()).getOrdering(column);
				if (row1 instanceof RepositoryFictiveWorkingDirectory) {
					return (ordering != TableViewerSorter.ORDER_REVERSED) ? -1 : 1;
				}
				if (row2 instanceof RepositoryFictiveWorkingDirectory) {
					return (ordering != TableViewerSorter.ORDER_REVERSED) ? 1 : -1;
				}
				IRepositoryResource rowData1 = ((RepositoryResource)row1).getRepositoryResource();
				IRepositoryResource rowData2 = ((RepositoryResource)row2).getRepositoryResource();
				Info info1 = (rowData1).getInfo();
				Info info2 = (rowData2).getInfo();
				if (column == RepositoryBrowserTableViewer.this.getNameColumnIndex()) {
					boolean cnd1 = rowData1 instanceof IRepositoryContainer;
                    boolean cnd2 = rowData2 instanceof IRepositoryContainer;
                    if (cnd1 && !cnd2) {
                        return -1;
                    }
                    else if (cnd2 && !cnd1) {
                        return 1;
                    }
    				String name1 = rowData1.getName();
    				String name2 = rowData2.getName();
                    return TableViewerSorter.compare(name1, name2);
				}
				else if (column == RepositoryBrowserTableViewer.this.getRevisionColumnIndex()) {
					try {
						Long c1 = new Long(rowData1.getRevision()); 
						Long c2 = new Long(rowData2.getRevision());
						return c1.compareTo(c2);
					}
					catch (Exception ex) {
						// not interesting in this context, will never happen
					}
				} else if (info1 != null && info2 != null) {
					if (column == RepositoryBrowserTableViewer.this.getDateColumnIndex()) {
						Long c1 = new Long(info1.lastChangedDate != null ? info1.lastChangedDate.getTime() : 0); 
						Long c2 = new Long(info2.lastChangedDate != null ? info2.lastChangedDate.getTime() : 0);
						return c1.compareTo(c2);
					} else if (column == RepositoryBrowserTableViewer.this.getAuthorColumnIndex()) {
						String author1 = info1.lastAuthor;
						String author2 = info2.lastAuthor;
						author1 = (author1 != null) ? author1 : RepositoryBrowserTableViewer.noAuthor;
						author2 = (author2 != null) ? author2 : RepositoryBrowserTableViewer.noAuthor;
						return TableViewerSorter.compare(author1, author2);
					} else if (column == RepositoryBrowserTableViewer.this.getLockOwnerColumnIndex()) {
						Lock lock1 = info1.lock;
						Lock lock2 = info2.lock;
						String lockOwner1 = (lock1 == null) ? "" : lock1.owner;
						String lockOwner2 = (lock2 == null) ? "" : lock2.owner;
						return TableViewerSorter.compare(lockOwner1, lockOwner2);
					} else if (column == RepositoryBrowserTableViewer.this.getPropertiesColumnIndex()) {
						boolean hasProps1 = info1.hasProperties;
						boolean hasProps2 = info2.hasProperties;
						String c1 = (hasProps1) ? RepositoryBrowserTableViewer.hasProps : RepositoryBrowserTableViewer.noProps;
						String c2 = (hasProps2) ? RepositoryBrowserTableViewer.hasProps : RepositoryBrowserTableViewer.noProps;
						return TableViewerSorter.compare(c1, c2);
					} else if (column == RepositoryBrowserTableViewer.this.getSizeColumnIndex()) {
						Long c1 = new Long (info1.fileSize);
						Long c2 = new Long (info2.fileSize);
						return c1.compareTo(c2);
					}					
				}
				return 0;
			}
		});
		
		this.setSorter(sorter);
		
		//0. name
		this.nameColumnIdx = this.createColumn(sorter, SVNTeamUIPlugin.instance().getResource("RepositoriesView.Browser.Name"), SWT.NONE, SWT.LEFT, true, new ColumnWeightData(18, true), this.columnIndexCounter++);
		
		//1. revision
		this.revisionColumnIdx = this.createColumn(sorter, SVNTeamUIPlugin.instance().getResource("RepositoriesView.Browser.Revision"), SWT.NONE, SWT.RIGHT, true, new ColumnWeightData(9, true), this.columnIndexCounter++);
		
		//2. last changed date
		this.dateColumnIdx = this.createColumn(sorter, SVNTeamUIPlugin.instance().getResource("RepositoriesView.Browser.LastChangedAt"), SWT.NONE, SWT.LEFT, true, new ColumnWeightData(17, true), this.columnIndexCounter++);
			
		//3. last changed author
		this.authorColumnIdx = this.createColumn(sorter, SVNTeamUIPlugin.instance().getResource("RepositoriesView.Browser.LastChangedBy"), SWT.NONE, SWT.LEFT, true, new ColumnWeightData(14, true), this.columnIndexCounter++);
		
		//4. size
		this.sizeColumnIdx = this.createColumn(sorter, SVNTeamUIPlugin.instance().getResource("RepositoriesView.Browser.Size"), SWT.NONE, SWT.RIGHT, true, new ColumnWeightData(10, true), this.columnIndexCounter++);
		
		//5. has properties
		this.propertiesColumnIdx = this.createColumn(sorter, SVNTeamUIPlugin.instance().getResource("RepositoriesView.Browser.HasProperties"), SWT.NONE, SWT.LEFT, true, new ColumnWeightData(12, true), this.columnIndexCounter++);
		
		//6. lock owner
		this.lockOwnerColumnIdx = this.createColumn(sorter, SVNTeamUIPlugin.instance().getResource("RepositoriesView.Browser.LockOwner"), SWT.NONE, SWT.LEFT, true, new ColumnWeightData(13, true), this.columnIndexCounter++);
	}
	
	protected int createColumn(TableViewerSorter sorter, String name, int style, int alignment, boolean resizable, ColumnWeightData data, int index) {
		TableColumn column = new TableColumn(this.getTable(), style);
		column.setText(name);
		column.setResizable(resizable);
		column.setAlignment(alignment);
		((TableLayout)this.getTable().getLayout()).addColumnData(data);
		if (sorter != null) {
			column.addSelectionListener(sorter);
		}
		return index++;
	}
	
	public int getAuthorColumnIndex() {
		return this.authorColumnIdx;
	}
	
	public int getColumnIndexCounter() {
		return this.columnIndexCounter;
	}
	
	public int getLockOwnerColumnIndex() {
		return this.lockOwnerColumnIdx;
	}

	public int getNameColumnIndex() {
		return this.nameColumnIdx;
	}
	
	public int getRevisionColumnIndex() {
		return this.revisionColumnIdx;
	}

	public int getPropertiesColumnIndex() {
		return this.propertiesColumnIdx;
	}

	public int getSizeColumnIndex() {
		return this.sizeColumnIdx;
	}

	public int getDateColumnIndex() {
		return this.dateColumnIdx;
	}
	
}
