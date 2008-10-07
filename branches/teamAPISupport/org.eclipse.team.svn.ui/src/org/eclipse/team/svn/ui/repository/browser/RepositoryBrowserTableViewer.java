/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
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
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.repository.model.IToolTipProvider;
import org.eclipse.team.svn.ui.repository.model.RepositoryBranches;
import org.eclipse.team.svn.ui.repository.model.RepositoryFile;
import org.eclipse.team.svn.ui.repository.model.RepositoryFolder;
import org.eclipse.team.svn.ui.repository.model.RepositoryResource;
import org.eclipse.team.svn.ui.repository.model.RepositoryRoot;
import org.eclipse.team.svn.ui.repository.model.RepositoryTags;
import org.eclipse.team.svn.ui.repository.model.RepositoryTrunk;
import org.eclipse.team.svn.ui.repository.model.ToolTipVariableSetProvider;
import org.eclipse.team.svn.ui.utility.ColumnedViewerComparator;

/**
 * Repository browser table viewer
 *
 * @author Sergiy Logvin
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
	
	public static final int COLUMN_NAME = 0;
	public static final int COLUMN_REVISION = 1;
	public static final int COLUMN_LAST_CHANGE_DATE = 2;
	public static final int COLUMN_LAST_CHANGE_AUTHOR = 3;
	public static final int COLUMN_SIZE = 4;
	public static final int COLUMN_HAS_PROPS = 5;
	public static final int COLUMN_LOCK_OWNER = 6;

	private static final Map<Class<?>, String> class2Format = new HashMap<Class<?>, String>();

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
	
	public RepositoryBrowserTableViewer(Table contentsTable) {
		super(contentsTable);
	}
	
	public RepositoryBrowserTableViewer(Composite parent, int style) {
		super(parent, style);
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
						tooltipText = ((IToolTipProvider)data).getToolTipMessage(RepositoryBrowserTableViewer.class2Format.get(data.getClass()));
					}
				}
				RepositoryBrowserTableViewer.this.getTable().setToolTipText(tooltipText);
			}			

			public void mouseExit(MouseEvent e) {
				RepositoryBrowserTableViewer.this.getTable().setToolTipText("");
			}
		});	

		RepositoryBrowserTableComparator comparator = new RepositoryBrowserTableComparator(this);
		
		this.createColumn(comparator, SVNTeamUIPlugin.instance().getResource("RepositoriesView.Browser.Name"), SWT.NONE, SWT.LEFT, true, new ColumnWeightData(18, true));
		this.createColumn(comparator, SVNTeamUIPlugin.instance().getResource("RepositoriesView.Browser.Revision"), SWT.NONE, SWT.RIGHT, true, new ColumnWeightData(9, true));
		this.createColumn(comparator, SVNTeamUIPlugin.instance().getResource("RepositoriesView.Browser.LastChangedAt"), SWT.NONE, SWT.LEFT, true, new ColumnWeightData(17, true));
		this.createColumn(comparator, SVNTeamUIPlugin.instance().getResource("RepositoriesView.Browser.LastChangedBy"), SWT.NONE, SWT.LEFT, true, new ColumnWeightData(14, true));
		this.createColumn(comparator, SVNTeamUIPlugin.instance().getResource("RepositoriesView.Browser.Size"), SWT.NONE, SWT.RIGHT, true, new ColumnWeightData(10, true));
		this.createColumn(comparator, SVNTeamUIPlugin.instance().getResource("RepositoriesView.Browser.HasProperties"), SWT.NONE, SWT.LEFT, true, new ColumnWeightData(12, true));
		this.createColumn(comparator, SVNTeamUIPlugin.instance().getResource("RepositoriesView.Browser.LockOwner"), SWT.NONE, SWT.LEFT, true, new ColumnWeightData(13, true));
		
		this.setComparator(comparator);
		comparator.setColumnNumber(RepositoryBrowserTableViewer.COLUMN_NAME);
		comparator.setReversed(false);
		this.getTable().setSortDirection(SWT.UP);
		this.getTable().setSortColumn(this.getTable().getColumn(RepositoryBrowserTableViewer.COLUMN_NAME));
	}
	
	protected void createColumn(ColumnedViewerComparator comparator, String name, int style, int alignment, boolean resizable, ColumnWeightData data) {
		TableColumn column = new TableColumn(this.getTable(), style);
		column.setText(name);
		column.setResizable(resizable);
		column.setAlignment(alignment);
		((TableLayout)this.getTable().getLayout()).addColumnData(data);
		column.addSelectionListener(comparator);
	}
}
