/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.composite;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryInfo;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNLogPath;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.connector.SVNEntry.Kind;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IResourcePropertyProvider;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.remote.ExportOperation;
import org.eclipse.team.svn.core.operation.remote.GetRemotePropertiesOperation;
import org.eclipse.team.svn.core.operation.remote.PreparedBranchTagOperation;
import org.eclipse.team.svn.core.operation.remote.management.AddRevisionLinkOperation;
import org.eclipse.team.svn.core.operation.remote.management.SaveRepositoryLocationsOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.remote.BranchTagAction;
import org.eclipse.team.svn.ui.action.remote.CreatePatchAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.history.AffectedPathNode;
import org.eclipse.team.svn.ui.history.AffectedPathsContentProvider;
import org.eclipse.team.svn.ui.history.AffectedPathsLabelProvider;
import org.eclipse.team.svn.ui.history.SVNChangedPathData;
import org.eclipse.team.svn.ui.operation.CompareRepositoryResourcesOperation;
import org.eclipse.team.svn.ui.operation.OpenRemoteFileOperation;
import org.eclipse.team.svn.ui.operation.RefreshRemoteResourcesOperation;
import org.eclipse.team.svn.ui.operation.RefreshRepositoryLocationsOperation;
import org.eclipse.team.svn.ui.operation.RemoteShowAnnotationOperation;
import org.eclipse.team.svn.ui.operation.ShowHistoryViewOperation;
import org.eclipse.team.svn.ui.operation.ShowPropertiesOperation;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.repository.model.RepositoryFolder;
import org.eclipse.team.svn.ui.utility.ArrayStructuredContentProvider;
import org.eclipse.team.svn.ui.utility.ColumnedViewerComparator;
import org.eclipse.team.svn.ui.utility.OverlayedImageDescriptor;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.wizard.CreatePatchWizard;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.editors.text.EditorsUI;

/**
 * Affected paths composite, contains tree and table viewers of affected paths
 *
 * @author Sergiy Logvin
 */
public class AffectedPathsComposite extends Composite {
	protected static ImageDescriptor ADDITION_OVERLAY;
	protected static ImageDescriptor MODIFICATION_OVERLAY;
	protected static ImageDescriptor DELETION_OVERLAY;
	protected static ImageDescriptor REPLACEMENT_OVERLAY;
	
	final public static int COLUMN_ICON = 0;
	final public static int COLUMN_NAME = 1;
	final public static int COLUMN_PATH = 2;
	final public static int COLUMN_COPIED_FROM = 3;
	
	protected SashForm sashForm;
	
	protected TableViewer tableViewer;
	protected TreeViewer treeViewer;
	protected IRepositoryResource repositoryResource;
	protected long currentRevision;
	
	protected AffectedPathsLabelProvider labelProvider;

	public AffectedPathsComposite(Composite parent, int style) {
		super(parent, style);
		
		if (AffectedPathsComposite.ADDITION_OVERLAY == null) {
			AffectedPathsComposite.ADDITION_OVERLAY = SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/addition.gif");
			AffectedPathsComposite.MODIFICATION_OVERLAY = SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/change.gif");
			AffectedPathsComposite.DELETION_OVERLAY = SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/deletion.gif");
			AffectedPathsComposite.REPLACEMENT_OVERLAY = SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/replacement.gif");
		}
		
		this.createControls();
	}
	
	public IRepositoryResource getRepositoryResource() {
		return this.repositoryResource;
	}

	public void setRepositoryResource(IRepositoryResource repositoryResource) {
		this.repositoryResource = repositoryResource;
	}
	
	public void setResourceTreeVisible(boolean visible) {
		if (visible) {
			this.sashForm.setMaximizedControl(null);
		}
		else {
			this.sashForm.setMaximizedControl(this.tableViewer.getControl());
			AffectedPathsContentProvider provider = (AffectedPathsContentProvider)AffectedPathsComposite.this.treeViewer.getContentProvider();
			AffectedPathNode rootNode = provider.getRoot();
			if (rootNode != null) {
				this.treeViewer.setSelection(new StructuredSelection(rootNode));
			}
		}
	}
	
	public void setInput(SVNChangedPathData []input, Collection relatedPathPrefixes, Collection relatedParents, long currentRevision) {
		this.currentRevision = currentRevision;
		this.labelProvider.setCurrentRevision(currentRevision);
		AffectedPathsContentProvider provider = (AffectedPathsContentProvider)this.treeViewer.getContentProvider();
		provider.initialize(input, relatedPathPrefixes, relatedParents, this.currentRevision);
		if (input != null && (input.length > 0 || currentRevision == 0)) {
			this.treeViewer.setInput("Root");
			
			this.treeViewer.expandAll();
			this.treeViewer.setSelection(new StructuredSelection(provider.getRoot()));
			((Tree)this.treeViewer.getControl()).showSelection();
		}
		else {
			this.treeViewer.setInput(null);
		}
	}
	
	protected void createControls() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = gridLayout.marginWidth = 0;
		this.setLayout(gridLayout);
		
    	this.sashForm = new SashForm(this, SWT.HORIZONTAL);
    	this.sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
        this.treeViewer = new TreeViewer(this.sashForm, SWT.H_SCROLL | SWT.V_SCROLL);
        this.treeViewer.setContentProvider(new AffectedPathsContentProvider());
        this.treeViewer.setLabelProvider(this.labelProvider = new AffectedPathsLabelProvider());
        this.treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
        	protected Object oldSelection;
        	
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection tSelection = (IStructuredSelection)event.getSelection();
				if (tSelection.size() > 0) {
					Object selection = tSelection.getFirstElement();
					if (this.oldSelection != selection) {						
						AffectedPathsComposite.this.tableViewer.setInput(AffectedPathsComposite.this.getSelectedTreeItemPathData());
						this.oldSelection = selection;
					}
				}
				else {
					AffectedPathsComposite.this.tableViewer.setInput(null);					
				}
			}
        });
        
        final Table table = new Table(this.sashForm, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        GridData data = new GridData(GridData.FILL_BOTH);
        table.setLayoutData(data);
        table.addMouseTrackListener(new MouseTrackAdapter() {
			public void mouseHover(MouseEvent e) {
				TableItem item = table.getItem(new Point(e.x, e.y));
				if (item != null) {
					Rectangle rect = item.getBounds(0);
					String tooltip = "";
					if (rect.contains(e.x, e.y)){
						SVNChangedPathData data = (SVNChangedPathData)item.getData();
						switch (data.action) {
							case SVNLogPath.ChangeType.ADDED: {
								tooltip = SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Add");
								break;
			    			}
			    			case SVNLogPath.ChangeType.MODIFIED: {
			    				tooltip = SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Modify");
			    				break;
			    			}
			    			case SVNLogPath.ChangeType.DELETED: {
			    				tooltip = SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Delete");
			    				break;
			    			}
			    			case SVNLogPath.ChangeType.REPLACED: {
			    				tooltip = SVNTeamUIPlugin.instance().getResource("LogMessagesComposite.Replace");
			    				break;
			    			}
						}
					}
					table.setToolTipText(rect.contains(e.x, e.y) ? tooltip : "");
				}
			}
		});

        TableLayout layout = new TableLayout();
        table.setLayout(layout);
                
        this.tableViewer = new TableViewer(table);
        this.sashForm.setWeights(new int[] {25, 75});
		
        AffectedPathTableComparator tableComparator = new AffectedPathTableComparator(this.tableViewer);
		
		//0.image        
        TableColumn col = new TableColumn(table, SWT.NONE);
		col.setText("");
		col.setResizable(false);
		col.setAlignment(SWT.CENTER);
        layout.addColumnData(new ColumnPixelData(26, false));        
        
        //1.name
        col = new TableColumn(table, SWT.NONE);
        col.setText(SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.Name"));
        col.addSelectionListener(tableComparator);
        layout.addColumnData(new ColumnWeightData(20, true));
        
        //2.path
        col = new TableColumn(table, SWT.NONE);
        col.setText(SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.Path"));
        col.addSelectionListener(tableComparator);
        layout.addColumnData(new ColumnWeightData(40, true));
        
        //3.source path
        col = new TableColumn(table, SWT.NONE);
        col.setText(SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.CopiedFrom"));
        col.addSelectionListener(tableComparator);
        layout.addColumnData(new ColumnWeightData(40, true));
        
        tableComparator.setReversed(false);
        tableComparator.setColumnNumber(AffectedPathsComposite.COLUMN_PATH);
		this.tableViewer.setComparator(tableComparator);
        this.tableViewer.getTable().setSortColumn(this.tableViewer.getTable().getColumn(AffectedPathsComposite.COLUMN_PATH));
        this.tableViewer.getTable().setSortDirection(SWT.UP);
        
        this.tableViewer.setContentProvider(new ArrayStructuredContentProvider());
		ITableLabelProvider tableLabelProvider = new ITableLabelProvider() {
			protected Map<ImageDescriptor, Image> images = new HashMap<ImageDescriptor, Image>();
			
			public Image getColumnImage(Object element, int columnIndex) {
				if (columnIndex == AffectedPathsComposite.COLUMN_ICON) {
					String fileName = ((SVNChangedPathData)element).resourceName;
					ImageDescriptor descr = SVNTeamUIPlugin.instance().getWorkbench().getEditorRegistry().getImageDescriptor(fileName);
					Image img = this.images.get(descr);
					if (img == null) {
						img = descr.createImage();
						this.images.put(descr, img);
					}
					switch (((SVNChangedPathData)element).action) {
						case SVNLogPath.ChangeType.ADDED: {
							descr = new OverlayedImageDescriptor(img, AffectedPathsComposite.ADDITION_OVERLAY, new Point(22, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.CENTER_V);
							break;
						}
						case SVNLogPath.ChangeType.MODIFIED: {
							descr = new OverlayedImageDescriptor(img, AffectedPathsComposite.MODIFICATION_OVERLAY, new Point(22, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.CENTER_V);
							break;
						}
						case SVNLogPath.ChangeType.DELETED: {
							descr = new OverlayedImageDescriptor(img, AffectedPathsComposite.DELETION_OVERLAY, new Point(22, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.CENTER_V);
							break;
						}
						case SVNLogPath.ChangeType.REPLACED: {
							descr = new OverlayedImageDescriptor(img, AffectedPathsComposite.REPLACEMENT_OVERLAY, new Point(22, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.CENTER_V);
							break;
						}
					}
					img = this.images.get(descr);
					if (img == null) {
						img = descr.createImage();
						this.images.put(descr, img);
					}
					return img;
				}
				return null;
			}
			public String getColumnText(Object element, int columnIndex) {
				SVNChangedPathData data = (SVNChangedPathData)element;
				switch (columnIndex) {
					case AffectedPathsComposite.COLUMN_NAME : {
						return data.resourceName;
					}
					case AffectedPathsComposite.COLUMN_PATH : {
						return data.resourcePath;
					}
					case AffectedPathsComposite.COLUMN_COPIED_FROM : {
						return data.copiedFromPath + ((data.copiedFromRevision == SVNRevision.INVALID_REVISION_NUMBER) ? "" : '@' + String.valueOf(data.copiedFromRevision)); 
					}
				}
				return "";
			}

			public void addListener(ILabelProviderListener listener) {
			}

			public void dispose() {
				for (Image img : this.images.values()) {
					img.dispose();
				}
			}

			public boolean isLabelProperty(Object element, String property) {
				return true;
			}

			public void removeListener(ILabelProviderListener listener) {
			}
		};
		this.tableViewer.setLabelProvider(tableLabelProvider);
    }
	
	protected SVNChangedPathData [] getSelectedTreeItemPathData() {
		Object selected = null;
	    if (this.treeViewer != null) {
			IStructuredSelection tSelection = (IStructuredSelection)this.treeViewer.getSelection();
			if (tSelection.size() > 0) {
				selected = tSelection.getFirstElement();
			}
	    }
	    return (selected != null ? ((AffectedPathNode)selected).getPathData() : null);
	}
	
	public void registerMenuManager(IWorkbenchPartSite site) {
		//add double click listener for the table viewer
		this.tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				ISelection selection = e.getSelection();
				if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
					IStructuredSelection structured = (IStructuredSelection)selection;
					AffectedPathsComposite.this.openRemoteResource((SVNChangedPathData)structured.getFirstElement(), OpenRemoteFileOperation.OPEN_DEFAULT, null);
				}
			}
		});
		
		//register context menu for the table viewer
		//FIXME works only with single selection
		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(this.tableViewer.getTable());
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				final IStructuredSelection affectedTableSelection = (IStructuredSelection)AffectedPathsComposite.this.tableViewer.getSelection();
				if (affectedTableSelection.size() == 0) {
					return;
				}
				final SVNChangedPathData firstData = (SVNChangedPathData)affectedTableSelection.getFirstElement();
				Action tAction = null;
				
				IEditorRegistry editorRegistry = SVNTeamUIPlugin.instance().getWorkbench().getEditorRegistry();
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.Open")) {
					public void run() {
						AffectedPathsComposite.this.openRemoteResource(firstData, OpenRemoteFileOperation.OPEN_DEFAULT, null);
					}
				});
				String name = firstData.resourceName;
				tAction.setImageDescriptor(editorRegistry.getImageDescriptor(name));
				tAction.setEnabled(affectedTableSelection.size() == 1);
				
				//FIXME: "Open with" submenu shouldn't be hardcoded after reworking of
				//       the HistoryView. Should be made like the RepositoriesView menu.
				MenuManager sub = new MenuManager(SVNTeamUIPlugin.instance().getResource("HistoryView.OpenWith"), "historyOpenWithMenu");
				sub.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				
				sub.add(new Separator("nonDefaultTextEditors"));
				IEditorDescriptor[] editors = editorRegistry.getEditors(name);
				for (int i = 0; i < editors.length; i++) {
					final String id = editors[i].getId();
    				if (!id.equals(EditorsUI.DEFAULT_TEXT_EDITOR_ID)) {
    					sub.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource(editors[i].getLabel())) {
    						public void run() {
    							AffectedPathsComposite.this.openRemoteResource(firstData, OpenRemoteFileOperation.OPEN_SPECIFIED, id);
    						}
    					});
    					tAction.setImageDescriptor(editors[i].getImageDescriptor());
    					tAction.setEnabled(affectedTableSelection.size() == 1);
    				}
    			}
					
				sub.add(new Separator("variousEditors"));
				IEditorDescriptor descriptor = null;
				sub.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.TextEditor")) {
					public void run() {
						AffectedPathsComposite.this.openRemoteResource(firstData, OpenRemoteFileOperation.OPEN_SPECIFIED, EditorsUI.DEFAULT_TEXT_EDITOR_ID);
					}
				});
				descriptor = editorRegistry.findEditor(EditorsUI.DEFAULT_TEXT_EDITOR_ID);
				tAction.setImageDescriptor(descriptor.getImageDescriptor());
				tAction.setEnabled(affectedTableSelection.size() == 1);
				sub.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.SystemEditor")) {
					public void run() {
						AffectedPathsComposite.this.openRemoteResource(firstData, OpenRemoteFileOperation.OPEN_EXTERNAL, null);
					}
				});
				if (editorRegistry.isSystemExternalEditorAvailable(name)) {
					tAction.setImageDescriptor(editorRegistry.getSystemExternalEditorImageDescriptor(name));
					tAction.setEnabled(affectedTableSelection.size() == 1);
				}
				else {
					tAction.setEnabled(false);
				}
				sub.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.InplaceEditor")) {
					public void run() {
						AffectedPathsComposite.this.openRemoteResource(firstData, OpenRemoteFileOperation.OPEN_INPLACE, null);
					}
				});
				if (editorRegistry.isSystemInPlaceEditorAvailable(name)) {
					tAction.setImageDescriptor(editorRegistry.getSystemExternalEditorImageDescriptor(name));
					tAction.setEnabled(affectedTableSelection.size() == 1);
				}
				else {
					tAction.setEnabled(false);
				}
				sub.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.DefaultEditor")) {
					public void run() {
						AffectedPathsComposite.this.openRemoteResource(firstData, OpenRemoteFileOperation.OPEN_DEFAULT, null);
					}
				});
				tAction.setImageDescriptor(editorRegistry.getImageDescriptor(name));
				tAction.setEnabled(affectedTableSelection.size() == 1);
				
	        	manager.add(sub);
	        	manager.add(new Separator());
	        	
				boolean isPreviousExists = false;
				if (affectedTableSelection.size() == 1) {
					//FIXME copied resources also must be handled
//					isPreviousExists = !(firstData.action == SVNLogPath.ChangeType.ADDED && firstData.copiedFromRevision == SVNRevision.INVALID_REVISION_NUMBER);
					isPreviousExists = firstData.action == SVNLogPath.ChangeType.MODIFIED || firstData.action == SVNLogPath.ChangeType.REPLACED;
				}
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.CompareWithPreviousRevision")) {
					public void run() {
						AffectedRepositoryResourceProvider provider = new AffectedRepositoryResourceProvider(firstData, false);
						AffectedPathsComposite.this.compareWithPreviousRevision(provider, provider);
					}
				});
				tAction.setEnabled(isPreviousExists);
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("CreatePatchCommand.label")) {
					public void run() {
						AffectedRepositoryResourceProvider provider = new AffectedRepositoryResourceProvider(firstData, false);
						AffectedPathsComposite.this.createPatchToPrevious(provider, provider);
					}
				});
				tAction.setEnabled(isPreviousExists);
				manager.add(new Separator());
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("ShowPropertiesAction.label")) {
					public void run() {
						AffectedRepositoryResourceProvider provider = new AffectedRepositoryResourceProvider(firstData, false);
						AffectedPathsComposite.this.showProperties(provider, provider);
					}
				});
				tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/propertiesedit.gif"));
				tAction.setEnabled(affectedTableSelection.size() == 1);
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("ShowResourceHistoryCommand.label")) {
					public void run() {
						AffectedRepositoryResourceProvider provider = new AffectedRepositoryResourceProvider(firstData, false);
						AffectedPathsComposite.this.showHistory(provider, provider);
					}
				});
				tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history.gif"));
				tAction.setEnabled(affectedTableSelection.size() == 1);
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("ShowAnnotationCommand.label")) {
					public void run() {
						AffectedPathsComposite.this.showAnnotation(firstData);
					}
				});
				tAction.setEnabled(affectedTableSelection.size() == 1);
				manager.add(new Separator());
				String branchFrom = SVNTeamUIPlugin.instance().getResource("HistoryView.BranchFrom", new String [] {String.valueOf(AffectedPathsComposite.this.currentRevision)});
				String tagFrom = SVNTeamUIPlugin.instance().getResource("HistoryView.TagFrom", new String [] {String.valueOf(AffectedPathsComposite.this.currentRevision)});
				manager.add(tAction = new Action(branchFrom) {
					public void run() {
						AffectedRepositoryResourceProvider provider = new AffectedRepositoryResourceProvider(firstData, false);
						AffectedPathsComposite.this.createBranchTag(provider, provider, BranchTagAction.BRANCH_ACTION);
					}
				});
				tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/branch.gif"));
				tAction.setEnabled(affectedTableSelection.size() > 0);
				manager.add(tAction = new Action(tagFrom) {
					public void run() {
						AffectedRepositoryResourceProvider provider = new AffectedRepositoryResourceProvider(firstData, false);
						AffectedPathsComposite.this.createBranchTag(provider, provider, BranchTagAction.TAG_ACTION);
					}
				});
				tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/tag.gif"));
				tAction.setEnabled(affectedTableSelection.size() > 0);
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("AddRevisionLinkAction.label")) {
					public void run() {
						AffectedRepositoryResourceProvider provider = new AffectedRepositoryResourceProvider(firstData, false);
						AffectedPathsComposite.this.addRevisionLink(provider, provider);
					}
				});
				tAction.setEnabled(affectedTableSelection.size() > 0);
				manager.add(new Separator());
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("ExportCommand.label")) {
					public void run() {
						AffectedRepositoryResourceProvider provider = new AffectedRepositoryResourceProvider(firstData, false);
						AffectedPathsComposite.this.doExport(provider, provider);
					}
				});
				tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/export.gif"));
				tAction.setEnabled(affectedTableSelection.size() > 0);
			}
		});
		menuMgr.setRemoveAllWhenShown(true);
		this.tableViewer.getTable().setMenu(menu);
		site.registerContextMenu(menuMgr, this.tableViewer);
		
		//register context menu for the tree viewer
        menuMgr = new MenuManager();
		menu = menuMgr.createContextMenu(this.treeViewer.getTree());
		
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        		final IStructuredSelection affectedTableSelection = (IStructuredSelection)AffectedPathsComposite.this.treeViewer.getSelection();
				if (affectedTableSelection.size() == 0) {
					return;
				}
        		final AffectedPathNode node = (AffectedPathNode)affectedTableSelection.getFirstElement();
        		Action tAction = null;
        		manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.CompareWithPreviousRevision")) {
					public void run() {
						GetSelectedTreeResource provider = new GetSelectedTreeResource(node);
						AffectedPathsComposite.this.compareWithPreviousRevision(provider, provider);
					}
        		});
        		boolean isCompareFoldersAllowed = CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() == ISVNConnectorFactory.APICompatibility.SVNAPI_1_5_x;
        		tAction.setEnabled(isCompareFoldersAllowed && AffectedPathsComposite.this.currentRevision != 0 && affectedTableSelection.size() == 1 && (node.getStatus() == '\0' || node.getStatus() == SVNLogPath.ChangeType.MODIFIED));
        		manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("CreatePatchCommand.label")) {
					public void run() {					
						GetSelectedTreeResource provider = new GetSelectedTreeResource(node);
						AffectedPathsComposite.this.createPatchToPrevious(provider, provider);
					}
				});
        		tAction.setEnabled(affectedTableSelection.size() == 1 && AffectedPathsComposite.this.currentRevision != 0 && affectedTableSelection.size() == 1 && (node.getStatus() == '\0' || node.getStatus() == SVNLogPath.ChangeType.MODIFIED));
        		manager.add(new Separator());
        		manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("ShowPropertiesAction.label")) {
					public void run() {
						GetSelectedTreeResource provider = new GetSelectedTreeResource(node);
						AffectedPathsComposite.this.showProperties(provider, provider);
					}
        		});
        		tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/propertiesedit.gif"));
        		tAction.setEnabled(AffectedPathsComposite.this.currentRevision != 0 && affectedTableSelection.size() == 1 /*&& (node.getStatus() == null || node.getStatus().charAt(0) == 'M')*/);
        		manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("ShowResourceHistoryCommand.label")) {
					public void run() {
						GetSelectedTreeResource provider = new GetSelectedTreeResource(node);
						AffectedPathsComposite.this.showHistory(provider, provider);
					}
        		});
        		tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history.gif"));
        		tAction.setEnabled(AffectedPathsComposite.this.currentRevision != 0 && affectedTableSelection.size() == 1);
        		manager.add(new Separator());
        		manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.BranchFrom", new String [] {String.valueOf(AffectedPathsComposite.this.currentRevision)})) {
        			public void run() {
        				GetSelectedTreeResource provider = new GetSelectedTreeResource(node);
						AffectedPathsComposite.this.createBranchTag(provider, provider, BranchTagAction.BRANCH_ACTION);
        			}
        		});
        		tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/branch.gif"));
        		tAction.setEnabled(affectedTableSelection.size() > 0);
        		manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.TagFrom", new String [] {String.valueOf(AffectedPathsComposite.this.currentRevision)})) {
        			public void run() {
        				GetSelectedTreeResource provider = new GetSelectedTreeResource(node);
						AffectedPathsComposite.this.createBranchTag(provider, provider, BranchTagAction.TAG_ACTION);
        			}
        		});
        		tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/tag.gif"));
        		tAction.setEnabled(affectedTableSelection.size() > 0);
        		manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("AddRevisionLinkAction.label")) {
					public void run() {
						GetSelectedTreeResource provider = new GetSelectedTreeResource(node);
						AffectedPathsComposite.this.addRevisionLink(provider, provider);
					}
				});
				tAction.setEnabled(affectedTableSelection.size() > 0);
				manager.add(new Separator());
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("ExportCommand.label")) {
					public void run() {
						GetSelectedTreeResource provider = new GetSelectedTreeResource(node);
						AffectedPathsComposite.this.doExport(provider, provider);
					}
				});
				tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/export.gif"));
				tAction.setEnabled(affectedTableSelection.size() > 0);
            }
        });
        menuMgr.setRemoveAllWhenShown(true);
        this.treeViewer.getTree().setMenu(menu);
        site.registerContextMenu(menuMgr, this.treeViewer);
	}
	
	protected void createBranchTag(IActionOperation preOp, IRepositoryResourceProvider provider, int type) {
		ProgressMonitorUtility.doTaskExternal(preOp, new NullProgressMonitor());
		
		boolean respectProjectStructure = SVNTeamPreferences.getRepositoryBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_NAME);
		
		IRepositoryResource []resources = provider.getRepositoryResources();
		PreparedBranchTagOperation op = BranchTagAction.getBranchTagOperation(resources, this.getShell(), type, respectProjectStructure);

		if (op != null) {
			CompositeOperation composite = new CompositeOperation(op.getId());
			composite.add(op);
			RefreshRemoteResourcesOperation refreshOp = new RefreshRemoteResourcesOperation(new IRepositoryResource[] {op.getDestination().getParent()});
			composite.add(refreshOp, new IActionOperation[] {op});
			UIMonitorUtility.doTaskScheduledDefault(op);
		}
	}
	
	protected void createPatchToPrevious(IActionOperation preOp, IRepositoryResourceProvider provider) {
		ProgressMonitorUtility.doTaskExternal(preOp, new NullProgressMonitor());
		IRepositoryResource current = provider.getRepositoryResources()[0];
		CreatePatchWizard wizard = new CreatePatchWizard(current.getName());
		WizardDialog dialog = new WizardDialog(UIMonitorUtility.getShell(), wizard);
		if (dialog.open() == DefaultDialog.OK) {
			IRepositoryResource previous = (current instanceof RepositoryFolder) ? current.asRepositoryContainer(current.getUrl(), false) : current.asRepositoryFile(current.getUrl(), false);
			previous.setSelectedRevision(SVNRevision.fromNumber(AffectedPathsComposite.this.currentRevision - 1));
			previous.setPegRevision(SVNRevision.fromNumber(AffectedPathsComposite.this.currentRevision));
			UIMonitorUtility.doTaskScheduledDefault(CreatePatchAction.getCreatePatchOperation(previous, current, wizard));
		}
	}
	
	protected void doExport(IActionOperation preOp, IRepositoryResourceProvider provider) {
		DirectoryDialog fileDialog = new DirectoryDialog(this.getShell());
		fileDialog.setText(SVNTeamUIPlugin.instance().getResource("ExportPanel.ExportFolder"));
		fileDialog.setMessage(SVNTeamUIPlugin.instance().getResource("ExportPanel.ExportFolder.Msg"));
		String path = fileDialog.open();
		if (path != null) {
			ExportOperation mainOp = new ExportOperation(provider, path);
			CompositeOperation op = new CompositeOperation(mainOp.getId());
			op.add(preOp);
			op.add(mainOp, new IActionOperation[] {preOp});
	    	UIMonitorUtility.doTaskScheduledDefault(op);
		}		
	}
	
	protected void openRemoteResource(SVNChangedPathData selectedPath, int openType, String openWith) {
		AffectedRepositoryResourceProvider provider = new AffectedRepositoryResourceProvider(selectedPath, true);
		OpenRemoteFileOperation openOp = new OpenRemoteFileOperation(provider, openType, openWith);
		
		CompositeOperation composite = new CompositeOperation(openOp.getId(), true);
		composite.add(provider);
		composite.add(openOp, new IActionOperation[] {provider});
		UIMonitorUtility.doTaskScheduledActive(composite);
	}
	
	protected void showAnnotation(SVNChangedPathData selectedPath) {
		AffectedRepositoryResourceProvider provider = new AffectedRepositoryResourceProvider(selectedPath, true);
		RemoteShowAnnotationOperation mainOp = new RemoteShowAnnotationOperation(provider);
		CompositeOperation op = new CompositeOperation(mainOp.getId(), true);
		op.add(provider);
		op.add(mainOp, new IActionOperation[] {provider});
		UIMonitorUtility.doTaskScheduledActive(op);
	}
	
	protected void showHistory(IActionOperation preOp, IRepositoryResourceProvider provider) {
		ShowHistoryViewOperation mainOp = new ShowHistoryViewOperation(provider, 0, 0);
		CompositeOperation op = new CompositeOperation(mainOp.getId());
		op.add(preOp);
		op.add(mainOp, new IActionOperation[] {preOp});
		UIMonitorUtility.doTaskScheduledActive(op);
	}
	
	protected void showProperties(IActionOperation preOp, IRepositoryResourceProvider provider) {
		IResourcePropertyProvider propertyProvider = new GetRemotePropertiesOperation(provider);
		ShowPropertiesOperation showOp = new ShowPropertiesOperation(UIMonitorUtility.getActivePage(), provider, propertyProvider);
		CompositeOperation op = new CompositeOperation(showOp.getId());
		op.add(preOp);
		op.add(propertyProvider, new IActionOperation[] {preOp});
		op.add(showOp, new IActionOperation[] {preOp, propertyProvider});
		UIMonitorUtility.doTaskScheduledActive(op);
	}
	
	protected void addRevisionLink(IActionOperation preOp, IRepositoryResourceProvider provider) {
		CompositeOperation op = new CompositeOperation("Operation.HAddSelectedRevision");
		op.add(preOp);
		IActionOperation []condition = new IActionOperation[] {preOp};
		op.add(new AddRevisionLinkOperation(provider, this.currentRevision), condition);
		op.add(new SaveRepositoryLocationsOperation(), condition);
		op.add(new RefreshRepositoryLocationsOperation(new IRepositoryLocation [] {this.repositoryResource.getRepositoryLocation()}, true), condition);
		UIMonitorUtility.doTaskScheduledDefault(op);
	}
	
	protected void compareWithPreviousRevision(IActionOperation preOp, final IRepositoryResourceProvider provider) {
		CompareRepositoryResourcesOperation mainOp = new CompareRepositoryResourcesOperation(new IRepositoryResourceProvider() {
			public IRepositoryResource[] getRepositoryResources() {
				IRepositoryResource next = provider.getRepositoryResources()[0];
				IRepositoryResource prev = SVNUtility.copyOf(next);
				prev.setSelectedRevision(SVNRevision.fromNumber(((SVNRevision.Number)next.getSelectedRevision()).getNumber() - 1));
				return new IRepositoryResource[] {prev, next};
			}
		});
		CompositeOperation op = new CompositeOperation(mainOp.getId());
		op.add(preOp);
		op.add(mainOp, new IActionOperation[] {preOp});
		UIMonitorUtility.doTaskScheduledActive(op);
	}
	
	protected class AffectedPathTableComparator extends ColumnedViewerComparator {
        public AffectedPathTableComparator(Viewer tableViewer) {
			super(tableViewer);
		}
        
		public int compare(Viewer viewer, Object row1, Object row2) {
			if (row1 instanceof SVNChangedPathData && row2 instanceof SVNChangedPathData) {
				SVNChangedPathData data1 = (SVNChangedPathData)row1;
				SVNChangedPathData data2 = (SVNChangedPathData)row2;
				switch (this.column) {
					case AffectedPathsComposite.COLUMN_NAME : {
						return ColumnedViewerComparator.compare(data1.resourceName, data2.resourceName, this.isReversed());
					}
					case AffectedPathsComposite.COLUMN_PATH : {
						return ColumnedViewerComparator.compare(data1.resourcePath, data2.resourcePath, this.isReversed());
					}
					case AffectedPathsComposite.COLUMN_COPIED_FROM : {
						String copied1 = data1.copiedFromPath + ((data1.copiedFromRevision == SVNRevision.INVALID_REVISION_NUMBER) ? "" : '@' + String.valueOf(data1.copiedFromRevision));
						String copied2 = data2.copiedFromPath + ((data2.copiedFromRevision == SVNRevision.INVALID_REVISION_NUMBER) ? "" : '@' + String.valueOf(data2.copiedFromRevision));
						return ColumnedViewerComparator.compare(copied1, copied2, this.isReversed());
					}
				}
			}
			return 0;
        }
		
    }
	
	protected class GetSelectedTreeResource extends AbstractActionOperation implements IRepositoryResourceProvider {
		protected AffectedPathNode affectedPathsItem;
		protected IRepositoryResource returnResource;
		
		public GetSelectedTreeResource(AffectedPathNode affectedPathsItem) {
			super("Operation.GetRepositoryResource");
			this.affectedPathsItem = affectedPathsItem;
		}
		
		protected void runImpl(IProgressMonitor monitor) throws Exception {
			String rootUrl = AffectedPathsComposite.this.repositoryResource.getRepositoryLocation().getRepositoryRootUrl();
			String path = this.affectedPathsItem.getFullPath();

			String resourceUrl = rootUrl + (path.startsWith("/") ? "" : "/") + path;
			SVNRevision revision = SVNRevision.fromNumber(AffectedPathsComposite.this.currentRevision);
			
			IRepositoryLocation location = AffectedPathsComposite.this.repositoryResource.getRepositoryLocation();
			this.returnResource = location.asRepositoryContainer(resourceUrl, false);
			this.returnResource.setSelectedRevision(revision);
			this.returnResource.setPegRevision(revision);
		}
		public IRepositoryResource[] getRepositoryResources() {
			return new IRepositoryResource[] {this.returnResource};
		}
	}
	
	protected class AffectedRepositoryResourceProvider extends AbstractActionOperation implements IRepositoryResourceProvider {
		protected IRepositoryResource []repositoryResources;
		protected SVNChangedPathData affectedPathsItem;
		protected boolean filesOnly;
		
		public AffectedRepositoryResourceProvider(SVNChangedPathData affectedPathsItem, boolean filesOnly) {
			super("Operation.GetRepositoryResource");
			this.affectedPathsItem = affectedPathsItem;
			this.filesOnly = filesOnly;
		}
		
		protected void runImpl(IProgressMonitor monitor) throws Exception {
			String affectedPath = this.affectedPathsItem.getFullResourcePath();
			String rootUrl = AffectedPathsComposite.this.repositoryResource.getRepositoryLocation().getRepositoryRootUrl();
			String resourceUrl = rootUrl + "/" + affectedPath;
			SVNRevision revision = SVNRevision.fromNumber(this.affectedPathsItem.action == SVNLogPath.ChangeType.DELETED ? AffectedPathsComposite.this.currentRevision - 1 : AffectedPathsComposite.this.currentRevision);
			
			SVNEntryInfo info = null;
			IRepositoryLocation location = AffectedPathsComposite.this.repositoryResource.getRepositoryLocation();
			ISVNConnector proxy = location.acquireSVNProxy();
			try {
				SVNEntryInfo []infos = SVNUtility.info(proxy, new SVNEntryRevisionReference(SVNUtility.encodeURL(resourceUrl), revision, revision), Depth.EMPTY, new SVNProgressMonitor(this, monitor, null));
				if (infos == null || infos.length == 0) {
					return;
				}
				info = infos[0];
			}
			finally {
				location.releaseSVNProxy(proxy);
			}
			
			if (info.kind == Kind.DIR && this.filesOnly) {
				final String message = SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.Open.Message", new String[] {SVNUtility.decodeURL(info.url)});
				UIMonitorUtility.getDisplay().syncExec(new Runnable() {
					public void run() {
						MessageDialog dialog = new MessageDialog(UIMonitorUtility.getDisplay().getActiveShell(), 
								SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.Open.Title"), 
								null, 
								message,
								MessageDialog.INFORMATION, 
								new String[] {IDialogConstants.OK_LABEL}, 
								0);
						dialog.open();								
					}
				});
				this.reportStatus(new Status(IStatus.WARNING, SVNTeamPlugin.NATURE_ID, IStatus.OK, message, null));
				return;					
			}
			this.repositoryResources = new IRepositoryResource[1];
			this.repositoryResources[0] = info.kind == Kind.FILE ? (IRepositoryResource)location.asRepositoryFile(resourceUrl, false) : location.asRepositoryContainer(resourceUrl, false);
			this.repositoryResources[0].setSelectedRevision(revision);
			this.repositoryResources[0].setPegRevision(revision);
		}
		
		public IRepositoryResource[] getRepositoryResources() {
			return this.repositoryResources;
		}
		
	}

}
