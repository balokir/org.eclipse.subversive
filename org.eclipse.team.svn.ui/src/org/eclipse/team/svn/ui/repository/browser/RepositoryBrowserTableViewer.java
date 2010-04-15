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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.AbstractCopyMoveResourcesOperation;
import org.eclipse.team.svn.core.operation.remote.CopyResourcesOperation;
import org.eclipse.team.svn.core.operation.remote.MoveResourcesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.RemoteResourceTransfer;
import org.eclipse.team.svn.ui.RemoteResourceTransferrable;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.operation.RefreshRemoteResourcesOperation;
import org.eclipse.team.svn.ui.panel.common.CommentPanel;
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
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Repository browser table viewer
 *
 * @author Sergiy Logvin
 */
public class RepositoryBrowserTableViewer extends TableViewer {
	public static final String FMT_REPOSITORY_RESOURCE = ""; //$NON-NLS-1$
	public static final String FMT_REPOSITORY_FILE = "{" + ToolTipVariableSetProvider.NAME_OF_LOCK_OWNER + "}" + //$NON-NLS-1$ //$NON-NLS-2$
													 "{" + ToolTipVariableSetProvider.NAME_OF_LOCK_CREATION_DATE + "}" + //$NON-NLS-1$ //$NON-NLS-2$
													 "{" + ToolTipVariableSetProvider.NAME_OF_LOCK_EXPIRATION_DATE + "}" + //$NON-NLS-1$ //$NON-NLS-2$
													 "{" + ToolTipVariableSetProvider.NAME_OF_LOCK_COMMENT + "}"; //$NON-NLS-1$ //$NON-NLS-2$
	public static final String FMT_REPOSITORY_FOLDER =  ""; //$NON-NLS-1$
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
		RepositoryBrowserTableViewer.noAuthor = SVNMessages.SVNInfo_NoAuthor;
		RepositoryBrowserTableViewer.hasProps = SVNUIMessages.RepositoriesView_Browser_HasProps;
		RepositoryBrowserTableViewer.noProps = SVNUIMessages.RepositoriesView_Browser_NoProps;
		
		this.getTable().setHeaderVisible(true);
		this.getTable().setLinesVisible(true);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		this.getTable().setLayoutData(data);

		this.getTable().setLayout(new TableLayout());
		this.getTable().addMouseTrackListener(new MouseTrackAdapter() {
			public void mouseHover(MouseEvent e) {
				String tooltipText = ""; //$NON-NLS-1$
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
				RepositoryBrowserTableViewer.this.getTable().setToolTipText(""); //$NON-NLS-1$
			}
		});	

		this.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK, new Transfer [] {RemoteResourceTransfer.getInstance()}, new TransferDragSourceListener() {
			
			public void dragFinished(DragSourceEvent event) {
			}

			public void dragSetData(DragSourceEvent event) {
				if (RemoteResourceTransfer.getInstance().isSupportedType(event.dataType)) {
					IStructuredSelection selection = (IStructuredSelection)RepositoryBrowserTableViewer.this.getSelection();
					ArrayList<IRepositoryResource> resources = new ArrayList<IRepositoryResource>();
					for (Iterator<?> it = selection.iterator(); it.hasNext();) {
						resources.add(((RepositoryResource)it.next()).getRepositoryResource());
					}
					event.data = new RemoteResourceTransferrable(resources.toArray(new IRepositoryResource[0]), 0);
				}
			}

			public void dragStart(DragSourceEvent event) {
				IStructuredSelection selection = (IStructuredSelection)RepositoryBrowserTableViewer.this.getSelection();
				boolean canBeDragged = selection.size() > 0;
				for (Iterator<?> it = selection.iterator(); it.hasNext();) {
					if (!(it.next() instanceof RepositoryResource)) {
						canBeDragged = false;
					}
				}
				event.doit = canBeDragged; 
			}
			
			public Transfer getTransfer() {
				return RemoteResourceTransfer.getInstance();
			}
		});

		this.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer [] {RemoteResourceTransfer.getInstance()}, new DropTargetAdapter() {
			
			protected int expectedOperation = DND.DROP_MOVE;
			
			public void dragOperationChanged(DropTargetEvent event) {
				this.expectedOperation = event.detail;
			}
			
			public void dragEnter(DropTargetEvent event) {
				this.expectedOperation = event.detail;
			}
			
			public void dragOver(DropTargetEvent event) {
				Table repositoryTable = (Table)((DropTarget)event.widget).getControl();
				TableItem aboveItem = repositoryTable.getItem(repositoryTable.toControl(event.x, event.y));
				if (aboveItem == null) {
					event.detail = DND.DROP_NONE;
					return;
				}
				Object aboveObject = aboveItem.getData();
				if (!(aboveObject instanceof RepositoryResource) || aboveObject instanceof RepositoryFile) {
					event.detail = DND.DROP_NONE;
					return;
				}
				RepositoryResource aboveResource = (RepositoryResource)aboveObject;
				if (aboveResource.getRepositoryResource().getSelectedRevision() != SVNRevision.HEAD) {
					event.detail = DND.DROP_NONE;
					return;
				}
				IStructuredSelection selection = (IStructuredSelection)RepositoryBrowserTableViewer.this.getSelection();
				for (Iterator<?> it = selection.iterator(); it.hasNext();) {
					RepositoryResource current = (RepositoryResource)it.next();
					if (aboveResource == current || aboveResource == current.getParent()) {
						event.detail = DND.DROP_NONE;
						return;
					}
				}
				event.detail = this.expectedOperation;
			}
			
			public void drop(DropTargetEvent event) {
				Table repositoryTable = (Table)((DropTarget)event.widget).getControl();
				RepositoryResource aboveResource = (RepositoryResource)repositoryTable.getItem(repositoryTable.toControl(event.x, event.y)).getData();
				CommentPanel commentPanel = new CommentPanel(event.detail == DND.DROP_MOVE ? SVNUIMessages.MoveToAction_Select_Title : SVNUIMessages.CopyToAction_Select_Title);
				DefaultDialog dialog = new DefaultDialog(UIMonitorUtility.getShell(), commentPanel);
				if (dialog.open() == IDialogConstants.OK_ID) {
					AbstractCopyMoveResourcesOperation mainOp = event.detail == DND.DROP_MOVE
							? new MoveResourcesOperation(aboveResource.getRepositoryResource(), ((RemoteResourceTransferrable)event.data).resources, commentPanel.getMessage(), null)
							: new CopyResourcesOperation(aboveResource.getRepositoryResource(), ((RemoteResourceTransferrable)event.data).resources, commentPanel.getMessage(), null);
					CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
					op.add(mainOp);
					ArrayList<IRepositoryResource> toRefresh = new ArrayList<IRepositoryResource>();
					toRefresh.add(aboveResource.getRepositoryResource());
					if (event.detail == DND.DROP_MOVE) {
						toRefresh.addAll(Arrays.asList(((RemoteResourceTransferrable)event.data).resources));
					}
					op.add(new RefreshRemoteResourcesOperation(SVNUtility.getCommonParents(toRefresh.toArray(new IRepositoryResource[0]))), new IActionOperation [] {mainOp});
					ProgressMonitorUtility.doTaskScheduled(op);
				}
			}
		});
		
		
		RepositoryBrowserTableComparator comparator = new RepositoryBrowserTableComparator(this);
		
		this.createColumn(comparator, SVNUIMessages.RepositoriesView_Browser_Name, SWT.NONE, SWT.LEFT, true, new ColumnWeightData(18, true));
		this.createColumn(comparator, SVNUIMessages.RepositoriesView_Browser_Revision, SWT.NONE, SWT.RIGHT, true, new ColumnWeightData(9, true));
		this.createColumn(comparator, SVNUIMessages.RepositoriesView_Browser_LastChangedAt, SWT.NONE, SWT.LEFT, true, new ColumnWeightData(17, true));
		this.createColumn(comparator, SVNUIMessages.RepositoriesView_Browser_LastChangedBy, SWT.NONE, SWT.LEFT, true, new ColumnWeightData(14, true));
		this.createColumn(comparator, SVNUIMessages.RepositoriesView_Browser_Size, SWT.NONE, SWT.RIGHT, true, new ColumnWeightData(10, true));
		this.createColumn(comparator, SVNUIMessages.RepositoriesView_Browser_HasProperties, SWT.NONE, SWT.LEFT, true, new ColumnWeightData(12, true));
		this.createColumn(comparator, SVNUIMessages.RepositoriesView_Browser_LockOwner, SWT.NONE, SWT.LEFT, true, new ColumnWeightData(13, true));
		
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
