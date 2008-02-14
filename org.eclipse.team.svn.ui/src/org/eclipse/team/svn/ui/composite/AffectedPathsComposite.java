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

import org.eclipse.compare.CompareUI;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
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
import org.eclipse.jface.viewers.IStructuredContentProvider;
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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryInfo;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
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
import org.eclipse.team.svn.core.operation.remote.GetLogMessagesOperation;
import org.eclipse.team.svn.core.operation.remote.GetRemotePropertiesOperation;
import org.eclipse.team.svn.core.operation.remote.PreparedBranchTagOperation;
import org.eclipse.team.svn.core.operation.remote.management.AddRevisionLinkOperation;
import org.eclipse.team.svn.core.operation.remote.management.SaveRepositoryLocationsOperation;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.remote.BranchTagAction;
import org.eclipse.team.svn.ui.action.remote.CreatePatchAction;
import org.eclipse.team.svn.ui.annotate.AnnotateView;
import org.eclipse.team.svn.ui.annotate.CheckPerspective;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.history.AffectedPathNode;
import org.eclipse.team.svn.ui.history.AffectedPathsContentProvider;
import org.eclipse.team.svn.ui.history.AffectedPathsLabelProvider;
import org.eclipse.team.svn.ui.operation.CompareRepositoryResourcesOperation;
import org.eclipse.team.svn.ui.operation.OpenRemoteFileOperation;
import org.eclipse.team.svn.ui.operation.RefreshRemoteResourcesOperation;
import org.eclipse.team.svn.ui.operation.RefreshRepositoryLocationsOperation;
import org.eclipse.team.svn.ui.operation.ShowHistoryViewOperation;
import org.eclipse.team.svn.ui.operation.ShowPropertiesOperation;
import org.eclipse.team.svn.ui.operation.UILoggedOperation;
import org.eclipse.team.svn.ui.panel.remote.ExportPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.repository.model.RepositoryFolder;
import org.eclipse.team.svn.ui.utility.OverlayedImageDescriptor;
import org.eclipse.team.svn.ui.utility.TableViewerSorter;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.wizard.CreatePatchWizard;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
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
	
	protected SashForm sashForm;
	
	protected TableViewer tableViewer;
	protected Table table;	
	protected TreeViewer treeViewer;
	protected IRepositoryResource repositoryResource;
	protected long currentRevision;
	
	protected MenuManager treeViewerMenuManager;
	protected MenuManager tableViewerMenuManager;
	protected AffectedPathsLabelProvider labelProvider;

	public AffectedPathsComposite(Composite parent, int style) {
		super(parent, style);
		this.setDefaults();
		this.createControls();
	}
	
	public void setResourceTreeVisible(boolean visible) {
		if (visible) {
			this.sashForm.setMaximizedControl(null);
		}
		else {
			this.sashForm.setMaximizedControl(this.tableViewer.getControl());
			AffectedPathsContentProvider provider = (AffectedPathsContentProvider)AffectedPathsComposite.this.treeViewer.getContentProvider();
			AffectedPathNode rootNode;
			if ((rootNode = provider.getRoot()) != null) {
				this.treeViewer.setSelection(new StructuredSelection(rootNode));
			}
		}
	}
	
	public TreeViewer getTreeViewer() {
		return this.treeViewer;
	}
	
	public TableViewer getTableViewer() {
		return this.tableViewer;
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
				IStructuredSelection tSelection = (IStructuredSelection)AffectedPathsComposite.this.treeViewer.getSelection();
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
        this.table = new Table(this.sashForm, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
        this.table.setHeaderVisible(true);
        this.table.setLinesVisible(true);
        GridData data = new GridData(GridData.FILL_BOTH);
        this.table.setLayoutData(data);
        this.table.addMouseTrackListener(new MouseTrackAdapter() {
			public void mouseHover(MouseEvent e) {
				TableItem item = AffectedPathsComposite.this.table.getItem(new Point(e.x, e.y));
				if (item != null) {
					Rectangle rect = item.getBounds(0);
					AffectedPathsComposite.this.table.setToolTipText(rect.contains(e.x, e.y) ? (String)((Object [])item.getData())[0] : "");
				}
			}
		});

        this.table.redraw();
	
        TableLayout layout = new TableLayout();
        this.table.setLayout(layout);
        
        this.tableViewer = new TableViewer(this.table);
        this.sashForm.setWeights(new int[] {25, 75});

		TableViewerSorter sorter = new TableViewerSorter(this.tableViewer, new TableViewerSorter.IColumnComparator() {
            public int compare(Object row1, Object row2, int column) {
            	String []rowData1 = (String [])row1;
                String []rowData2 = (String [])row2;
                return TableViewerSorter.compare(rowData1[column], rowData2[column]);
            }
        });
		this.tableViewer.setSorter(sorter);
		//0.image        
        TableColumn col = new TableColumn(this.table, SWT.NONE);
		col.setText("");
		col.setResizable(false);
		col.addSelectionListener(sorter);
		col.setAlignment(SWT.CENTER);
        layout.addColumnData(new ColumnPixelData(26, false));        
        //1.name
        col = new TableColumn(this.table, SWT.NONE);
        col.setText(SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.Name"));
        col.addSelectionListener(sorter);
        layout.addColumnData(new ColumnWeightData(20, true));
        //2.path
        col = new TableColumn(this.table, SWT.NONE);
        col.setText(SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.Path"));
		col.addSelectionListener(sorter);
        layout.addColumnData(new ColumnWeightData(40, true));
        //3.source path
        col = new TableColumn(this.table, SWT.NONE);
        col.setText(SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.CopiedFrom"));
		col.addSelectionListener(sorter);
        layout.addColumnData(new ColumnWeightData(40, true));
        
        this.tableViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return (Object[])inputElement;
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
		});
		ITableLabelProvider labelProvider = new ITableLabelProvider() {
			protected Map<ImageDescriptor, Image> images = new HashMap<ImageDescriptor, Image>();
		    
			public Image getColumnImage(Object element, int columnIndex) {
				if (columnIndex == 0) {
					String action = ((String [])element)[0];
					String fileName = ((String [])element)[1];
					ImageDescriptor descr = SVNTeamUIPlugin.instance().getWorkbench().getEditorRegistry().getImageDescriptor(fileName);
					Image img = this.images.get(descr);
					if (img == null) {
						img = descr.createImage();
			            CompareUI.disposeOnShutdown(img);
						this.images.put(descr, img);
					}
					switch (action.charAt(0)) {
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
			            CompareUI.disposeOnShutdown(img);
						this.images.put(descr, img);
					}
					return img;
				}
				return null;
			}
			public String getColumnText(Object element, int columnIndex) {
				String []data = (String [])element;
				if (columnIndex == 3) {
					String copiedFrom = data[columnIndex];
					copiedFrom = (copiedFrom.length() > 1 && copiedFrom.startsWith("/")) ? copiedFrom.substring(1) : copiedFrom;
					return data[4].length() > 0 ? (copiedFrom + '@' + data[4]) : copiedFrom;
				}
				return columnIndex != 0 ? data[columnIndex] : "";
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
		};
		this.tableViewer.setLabelProvider(labelProvider);
    }
	
	protected String [][]getSelectedTreeItemPathData() {
		Object selected = null;
	    if (this.treeViewer != null) {
			IStructuredSelection tSelection = (IStructuredSelection)this.treeViewer.getSelection();
			if (tSelection.size() > 0) {
				selected = tSelection.getFirstElement();
			}
	    }

	    return (selected != null ? ((AffectedPathNode)selected).getPathData() : null);
	}
	
	public void setInput(String [][]input, Collection relatedPathPrefixes, Collection relatedParents, long currentRevision) {
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
	
	protected class GetSelectedTreeResource extends AbstractActionOperation implements IRepositoryResourceProvider {
		
		protected IRepositoryResource repositoryResource;
		protected long currentRevision;
		protected Object affectedPathsItem;
		protected IRepositoryResource returnResource;
		
		public GetSelectedTreeResource(IRepositoryResource repositoryResource, long currentRevision, Object affectedPathsItem) {
			super("Get Selected Tree Resource");
			this.repositoryResource = repositoryResource;
			this.currentRevision = currentRevision;
			this.affectedPathsItem = affectedPathsItem;
		}
		
		protected void runImpl(IProgressMonitor monitor) throws Exception {
			String rootUrl = this.repositoryResource.getRepositoryLocation().getRepositoryRootUrl();
			String path = ((AffectedPathNode)this.affectedPathsItem).getFullPath();

			String resourceUrl = rootUrl + (path.startsWith("/") ? "" : "/") + path;
			IRepositoryLocation location = this.repositoryResource.getRepositoryLocation();
			this.returnResource = location.asRepositoryContainer(resourceUrl, false);
			//FIXME check peg revision
			this.returnResource.setSelectedRevision(SVNRevision.fromNumber(this.currentRevision));
			this.returnResource.setPegRevision(SVNRevision.fromNumber(this.currentRevision));
		}		
		public ISchedulingRule getSchedulingRule() {
			return null;
		}
		public IRepositoryResource[] getRepositoryResources() {
			return new IRepositoryResource[] {this.returnResource};
		}
	}
	
	public void registerMenuManager(IWorkbenchPartSite site) {
		//add double click listener for the table viewer
		this.tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				ISelection selection = e.getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection structured = (IStructuredSelection)selection;
					if (structured.size() == 1) {
						AffectedPathsComposite.this.openRemoteResource(structured, OpenRemoteFileOperation.OPEN_DEFAULT, null);
					}
				}
			}
		});
		
		//register context menu for the table viewer
		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(this.tableViewer.getTable());
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				boolean enabled = false;
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				final IStructuredSelection affectedTableSelection = (IStructuredSelection)AffectedPathsComposite.this.tableViewer.getSelection();
				if (affectedTableSelection.size() == 1) {
					String status = ((String [])affectedTableSelection.getFirstElement())[0];
					enabled = status.charAt(0) == 'M';
				}
				Action tAction = null;
				
				IEditorRegistry editorRegistry = SVNTeamUIPlugin.instance().getWorkbench().getEditorRegistry();
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.Open")) {
					public void run() {
						AffectedPathsComposite.this.openRemoteResource(affectedTableSelection, OpenRemoteFileOperation.OPEN_DEFAULT, null);
					}
				});
				String name = ((String [])affectedTableSelection.getFirstElement())[1];
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
    							AffectedPathsComposite.this.openRemoteResource(affectedTableSelection, OpenRemoteFileOperation.OPEN_SPECIFIED, id);
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
						AffectedPathsComposite.this.openRemoteResource(affectedTableSelection, OpenRemoteFileOperation.OPEN_SPECIFIED, EditorsUI.DEFAULT_TEXT_EDITOR_ID);
					}
				});
				descriptor = editorRegistry.findEditor(EditorsUI.DEFAULT_TEXT_EDITOR_ID);
				tAction.setImageDescriptor(descriptor.getImageDescriptor());
				tAction.setEnabled(affectedTableSelection.size() == 1);
				sub.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.SystemEditor")) {
					public void run() {
						AffectedPathsComposite.this.openRemoteResource(affectedTableSelection, OpenRemoteFileOperation.OPEN_EXTERNAL, null);
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
						AffectedPathsComposite.this.openRemoteResource(affectedTableSelection, OpenRemoteFileOperation.OPEN_INPLACE, null);
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
						AffectedPathsComposite.this.openRemoteResource(affectedTableSelection, OpenRemoteFileOperation.OPEN_DEFAULT, null);
					}
				});
				tAction.setImageDescriptor(editorRegistry.getImageDescriptor(name));
				tAction.setEnabled(affectedTableSelection.size() == 1);
				
	        	manager.add(sub);
	        	manager.add(new Separator());
	        	
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.CompareWithPreviousRevision")) {
					public void run() {
						AffectedRepositoryResourceProvider provider = new AffectedRepositoryResourceProvider(affectedTableSelection.getFirstElement(), false);
						AffectedPathsComposite.this.compareWithPreviousRevision(provider);
					}
				});
				tAction.setEnabled(enabled);
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("CreatePatchCommand.label")) {
					public void run() {
						AffectedRepositoryResourceProvider provider = new AffectedRepositoryResourceProvider(affectedTableSelection.getFirstElement(), false);
						AffectedPathsComposite.this.createPatchToPrevious(provider);
					}
				});
				tAction.setEnabled(enabled);
				manager.add(new Separator());
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("ShowPropertiesAction.label")) {
					public void run() {
						AffectedRepositoryResourceProvider provider = new AffectedRepositoryResourceProvider(affectedTableSelection.getFirstElement(), false);
						AffectedPathsComposite.this.showProperties(provider);
					}
				});
				tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/propertiesedit.gif"));
				tAction.setEnabled(affectedTableSelection.size() == 1);
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("ShowResourceHistoryCommand.label")) {
					public void run() {
						AffectedRepositoryResourceProvider provider = new AffectedRepositoryResourceProvider(affectedTableSelection.getFirstElement(), false);
						ProgressMonitorUtility.doTaskExternal(provider, new NullProgressMonitor());
						AffectedPathsComposite.this.showHistory(provider);
					}
				});
				tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history.gif"));
				tAction.setEnabled(affectedTableSelection.size() == 1);
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("ShowAnnotationCommand.label")) {
					public void run() {
						AffectedRepositoryResourceProvider provider = new AffectedRepositoryResourceProvider(affectedTableSelection.getFirstElement(), false);
						AffectedPathsComposite.this.showAnnotation(provider);
					}
				});
				tAction.setEnabled(affectedTableSelection.size() == 1);
				manager.add(new Separator());
				String branchFrom = SVNTeamUIPlugin.instance().getResource("HistoryView.BranchFrom", new String [] {String.valueOf(AffectedPathsComposite.this.currentRevision)});
				String tagFrom = SVNTeamUIPlugin.instance().getResource("HistoryView.BranchFrom", new String [] {String.valueOf(AffectedPathsComposite.this.currentRevision)});
				manager.add(tAction = new Action(branchFrom) {
					public void run() {
						AffectedRepositoryResourceProvider provider = new AffectedRepositoryResourceProvider(affectedTableSelection.getFirstElement(), false);
						ProgressMonitorUtility.doTaskExternal(provider, new NullProgressMonitor());
						AffectedPathsComposite.this.createBranchTag(provider, BranchTagAction.BRANCH_ACTION);
					}
				});
				tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/branch.gif"));
				tAction.setEnabled(enabled);
				manager.add(tAction = new Action(tagFrom) {
					public void run() {
						AffectedRepositoryResourceProvider provider = new AffectedRepositoryResourceProvider(affectedTableSelection.getFirstElement(), false);
						ProgressMonitorUtility.doTaskExternal(provider, new NullProgressMonitor());
						AffectedPathsComposite.this.createBranchTag(provider, BranchTagAction.TAG_ACTION);
					}
				});
				tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/tag.gif"));
				tAction.setEnabled(affectedTableSelection.size() > 0);
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("AddRevisionLinkAction.label")) {
					public void run() {
						AffectedRepositoryResourceProvider provider = new AffectedRepositoryResourceProvider(affectedTableSelection.getFirstElement(), false);
						ProgressMonitorUtility.doTaskExternal(provider, new NullProgressMonitor());
						AffectedPathsComposite.this.addRevisionLink(provider);
					}
				});
				tAction.setEnabled(affectedTableSelection.size() > 0);
				manager.add(new Separator());
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("ExportCommand.label")) {
					public void run() {
						AffectedRepositoryResourceProvider provider = new AffectedRepositoryResourceProvider(affectedTableSelection.getFirstElement(), false);
						ProgressMonitorUtility.doTaskExternal(provider, new NullProgressMonitor());
						AffectedPathsComposite.this.doExport(provider);
					}
				});
				tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/export.gif"));
				tAction.setEnabled(affectedTableSelection.size() > 0);
			}
		});
		menuMgr.setRemoveAllWhenShown(true);
		this.getTableViewer().getTable().setMenu(menu);
		site.registerContextMenu(menuMgr, this.tableViewer);
		
		//register context menu for the tree viewer
        menuMgr = new MenuManager();
		menu = menuMgr.createContextMenu(this.treeViewer.getTree());
		
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        		final IStructuredSelection affectedTableSelection = (IStructuredSelection)AffectedPathsComposite.this.treeViewer.getSelection();
        		AffectedPathNode node = (AffectedPathNode)affectedTableSelection.getFirstElement();
        		Action tAction = null;
        		manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.CompareWithPreviousRevision")) {
					public void run() {
						GetSelectedTreeResource provider = new GetSelectedTreeResource(AffectedPathsComposite.this.repositoryResource, AffectedPathsComposite.this.currentRevision, affectedTableSelection.getFirstElement());
						AffectedPathsComposite.this.compareWithPreviousRevision(provider);
					}
        		});
        		boolean isCompareFoldersAllowed = CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() == ISVNConnectorFactory.APICompatibility.SVNAPI_1_5_x;
        		tAction.setEnabled(isCompareFoldersAllowed && AffectedPathsComposite.this.currentRevision != 0 && affectedTableSelection.size() == 1 && (node.getStatus() == null || node.getStatus().charAt(0) == 'M'));
        		manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("CreatePatchCommand.label")) {
					public void run() {					
						GetSelectedTreeResource op = new GetSelectedTreeResource(AffectedPathsComposite.this.repositoryResource, AffectedPathsComposite.this.currentRevision, affectedTableSelection.getFirstElement());
						ProgressMonitorUtility.doTaskExternalDefault(op, new NullProgressMonitor());
						IRepositoryResource current = op.getRepositoryResources()[0];
						CreatePatchWizard wizard = new CreatePatchWizard(current.getName());
						WizardDialog dialog = new WizardDialog(UIMonitorUtility.getShell(), wizard);
						if (dialog.open() == DefaultDialog.OK) {
							IRepositoryResource previous = (current instanceof RepositoryFolder) ? current.asRepositoryContainer(current.getUrl(), false) : current.asRepositoryFile(current.getUrl(), false);
							previous.setSelectedRevision(SVNRevision.fromNumber(AffectedPathsComposite.this.currentRevision - 1));
							previous.setPegRevision(SVNRevision.fromNumber(AffectedPathsComposite.this.currentRevision));
							UIMonitorUtility.doTaskNowDefault(CreatePatchAction.getCreatePatchOperation(previous, current, wizard), false);
						}
					}
				});
        		tAction.setEnabled(affectedTableSelection.size() == 1 && AffectedPathsComposite.this.currentRevision != 0 && affectedTableSelection.size() == 1 && (node.getStatus() == null || node.getStatus().startsWith("M")));
        		manager.add(new Separator());
        		manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("ShowPropertiesAction.label")) {
					public void run() {
						GetSelectedTreeResource provider = new GetSelectedTreeResource(AffectedPathsComposite.this.repositoryResource, AffectedPathsComposite.this.currentRevision, affectedTableSelection.getFirstElement());
						AffectedPathsComposite.this.showProperties(provider);
					}
        		});
        		tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/propertiesedit.gif"));
        		tAction.setEnabled(AffectedPathsComposite.this.currentRevision != 0 && affectedTableSelection.size() == 1 /*&& (node.getStatus() == null || node.getStatus().charAt(0) == 'M')*/);
        		manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("ShowResourceHistoryCommand.label")) {
					public void run() {
						GetSelectedTreeResource provider = new GetSelectedTreeResource(AffectedPathsComposite.this.repositoryResource, AffectedPathsComposite.this.currentRevision, affectedTableSelection.getFirstElement());
						ProgressMonitorUtility.doTaskExternalDefault(provider, new NullProgressMonitor());
						AffectedPathsComposite.this.showHistory(provider);
					}
        		});
        		tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history.gif"));
        		tAction.setEnabled(AffectedPathsComposite.this.currentRevision != 0 && affectedTableSelection.size() == 1);
        		manager.add(new Separator());
        		manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.BranchFrom", new String [] {String.valueOf(AffectedPathsComposite.this.currentRevision)})) {
        			public void run() {
        				GetSelectedTreeResource provider = new GetSelectedTreeResource(AffectedPathsComposite.this.repositoryResource, AffectedPathsComposite.this.currentRevision, affectedTableSelection.getFirstElement());
						ProgressMonitorUtility.doTaskExternalDefault(provider, new NullProgressMonitor());
						AffectedPathsComposite.this.createBranchTag(provider, BranchTagAction.BRANCH_ACTION);
        			}
        		});
        		tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/branch.gif"));
        		tAction.setEnabled(affectedTableSelection.size() > 0);
        		manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.BranchFrom", new String [] {String.valueOf(AffectedPathsComposite.this.currentRevision)})) {
        			public void run() {
        				GetSelectedTreeResource provider = new GetSelectedTreeResource(AffectedPathsComposite.this.repositoryResource, AffectedPathsComposite.this.currentRevision, affectedTableSelection.getFirstElement());
						ProgressMonitorUtility.doTaskExternalDefault(provider, new NullProgressMonitor());
						AffectedPathsComposite.this.createBranchTag(provider, BranchTagAction.TAG_ACTION);
        			}
        		});
        		tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/tag.gif"));
        		tAction.setEnabled(affectedTableSelection.size() > 0);
        		manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("AddRevisionLinkAction.label")) {
					public void run() {
						GetSelectedTreeResource provider = new GetSelectedTreeResource(AffectedPathsComposite.this.repositoryResource, AffectedPathsComposite.this.currentRevision, affectedTableSelection.getFirstElement());
						ProgressMonitorUtility.doTaskExternal(provider, new NullProgressMonitor());
						AffectedPathsComposite.this.addRevisionLink(provider);
					}
				});
				tAction.setEnabled(affectedTableSelection.size() > 0);
				manager.add(new Separator());
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("ExportCommand.label")) {
					public void run() {
						GetSelectedTreeResource provider = new GetSelectedTreeResource(AffectedPathsComposite.this.repositoryResource, AffectedPathsComposite.this.currentRevision, affectedTableSelection.getFirstElement());
						ProgressMonitorUtility.doTaskExternal(provider, new NullProgressMonitor());
						AffectedPathsComposite.this.doExport(provider);
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
	
	protected void openRemoteResource(IStructuredSelection affectedTableSelection, int openType, String openWith) {
		AffectedRepositoryResourceProvider provider = new AffectedRepositoryResourceProvider(affectedTableSelection.getFirstElement(), true);
		OpenRemoteFileOperation openOp = new OpenRemoteFileOperation(provider, openType, openWith);
		
		CompositeOperation composite = new CompositeOperation(openOp.getId());
		composite.add(provider);
		composite.add(openOp, new IActionOperation[] {provider});
		UIMonitorUtility.doTaskScheduledActive(composite);
	}
	
	protected void showProperties(IRepositoryResourceProvider provider) {
		IResourcePropertyProvider propertyProvider = new GetRemotePropertiesOperation(provider);
		ShowPropertiesOperation showOp = new ShowPropertiesOperation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), provider, propertyProvider);
		
		CompositeOperation composite = new CompositeOperation(showOp.getId());
		composite.add((AbstractActionOperation)provider);
		composite.add(propertyProvider, new IActionOperation[] {(AbstractActionOperation)provider});
		composite.add(showOp, new IActionOperation[] {(AbstractActionOperation)provider, propertyProvider});
		UIMonitorUtility.doTaskScheduledActive(composite);
	}
	
	protected void showHistory(IRepositoryResourceProvider provider) {
		UIMonitorUtility.doTaskBusyDefault(new ShowHistoryViewOperation(provider.getRepositoryResources()[0], 0, 0));
	}
	
	protected void doExport(IRepositoryResourceProvider provider) {
		IRepositoryResource resource = provider.getRepositoryResources()[0];
		ExportPanel panel = new ExportPanel(resource);
		DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
		if (dialog.open() == 0) {
			resource = SVNUtility.copyOf(resource);
			resource.setSelectedRevision(panel.getSelectedRevision());
	    	UIMonitorUtility.doTaskScheduledDefault(new ExportOperation(resource, panel.getLocation()));
	    }
	}
	
	protected void showAnnotation(AffectedRepositoryResourceProvider provider) {
		ProgressMonitorUtility.doTaskExternal(provider, new NullProgressMonitor());
		IRepositoryResource selected = provider.getRepositoryResources()[0];
		if (!(selected instanceof IRepositoryFile)) {
				MessageBox err = new MessageBox(UIMonitorUtility.getShell());
				err.setText(SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.ShowAnnotation.Title"));
				err.setMessage(SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.ShowAnnotation.Message", new String [] {provider.getRepositoryResources()[0].getUrl()}));
				err.open();
				return;
			}
			CheckPerspective.run(UIMonitorUtility.getActivePage().getWorkbenchWindow());
			try {
				IViewPart viewPart = UIMonitorUtility.getActivePage().showView(AnnotateView.VIEW_ID);
			    if (viewPart != null && viewPart instanceof AnnotateView) {
			    	((AnnotateView)viewPart).showEditor(selected);
			    }
			}
			catch (PartInitException ex) {
				UILoggedOperation.reportError("Show annotate view", ex);
			}
	}
	
	protected void createPatchToPrevious(AffectedRepositoryResourceProvider provider) {
		ProgressMonitorUtility.doTaskExternal(provider, new NullProgressMonitor());
		IRepositoryResource current = provider.getRepositoryResources()[0];
		CreatePatchWizard wizard = new CreatePatchWizard(current.getName());
		WizardDialog dialog = new WizardDialog(UIMonitorUtility.getShell(), wizard);
		if (dialog.open() == DefaultDialog.OK) {
			IRepositoryResource previous = (current instanceof RepositoryFolder) ? current.asRepositoryContainer(current.getUrl(), false) : current.asRepositoryFile(current.getUrl(), false);
			previous.setSelectedRevision(SVNRevision.fromNumber(AffectedPathsComposite.this.currentRevision - 1));
			previous.setPegRevision(SVNRevision.fromNumber(AffectedPathsComposite.this.currentRevision));
			UIMonitorUtility.doTaskNowDefault(CreatePatchAction.getCreatePatchOperation(previous, current, wizard), false);
		}
	}
	
	protected void addRevisionLink(IRepositoryResourceProvider provider) {
		CompositeOperation op = new CompositeOperation("Operation.HAddSelectedRevision");
		op.add(new AddRevisionLinkOperation(provider, this.currentRevision));
		op.add(new SaveRepositoryLocationsOperation());
		op.add(new RefreshRepositoryLocationsOperation(new IRepositoryLocation [] {this.repositoryResource.getRepositoryLocation()}, true));
		UIMonitorUtility.doTaskScheduledDefault(op);
	}
	
	protected void createBranchTag(IRepositoryResourceProvider provider, int type) {
		boolean respectProjectStructure = SVNTeamPreferences.getRepositoryBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_NAME);
		
		IRepositoryResource []resources = provider.getRepositoryResources();
		PreparedBranchTagOperation op = BranchTagAction.getBranchTagOperation(resources, this.getShell(), type, respectProjectStructure);

		if (op != null) {
			CompositeOperation composite = new CompositeOperation(op.getId());
			composite.add(op);
			RefreshRemoteResourcesOperation refreshOp = new RefreshRemoteResourcesOperation(new IRepositoryResource[] {op.getDestination().getParent()});
			composite.add(refreshOp, new IActionOperation[] {op});
			UIMonitorUtility.doTaskNowDefault(op, false);
		}
	}
	
	protected void compareWithPreviousRevision(AbstractActionOperation provider) {
		final GetLogMessagesOperation msgsOp = new GetLogMessagesOperation((IRepositoryResourceProvider)provider);
		msgsOp.setLimit(2);
		
		GetResourcesToCompareOperation getResourcesOp = new GetResourcesToCompareOperation("Operation.GetResourcesToCompare", msgsOp, (IRepositoryResourceProvider)provider);
		CompositeOperation composite = new CompositeOperation(getResourcesOp.getId());
		composite.add(provider);
		composite.add(msgsOp, new IActionOperation[] {provider});
		composite.add(getResourcesOp, new IActionOperation[] {msgsOp});
		composite.add(new CompareRepositoryResourcesOperation(getResourcesOp), new IActionOperation[] {getResourcesOp});
		UIMonitorUtility.doTaskScheduledActive(composite);
	}
	
	protected class GetResourcesToCompareOperation extends AbstractActionOperation implements IRepositoryResourceProvider {
    	protected IRepositoryResourceProvider resourceProvider;
    	protected IRepositoryResource remoteResource;
    	protected GetLogMessagesOperation msgsOp;
    	public IRepositoryResource right;

		public GetResourcesToCompareOperation(String operationName, GetLogMessagesOperation msgsOp, IRepositoryResourceProvider resourceProvider) {
			super(operationName);
			this.msgsOp = msgsOp;
			this.resourceProvider = resourceProvider;
			this.right = null;
		}

		protected void runImpl(IProgressMonitor monitor) throws Exception {
			this.remoteResource = this.resourceProvider.getRepositoryResources()[0];
			if (this.remoteResource == null) {
				return;
			}
			SVNLogEntry []msgs = msgsOp.getMessages();
			if (msgs != null && msgs.length == 2) {
				SVNRevision previousRevNum = SVNRevision.fromNumber(msgs[1].revision);
				if (this.remoteResource instanceof IRepositoryContainer) {
					this.right = this.remoteResource.getRepositoryLocation().asRepositoryContainer(this.remoteResource.getUrl(), false);
				}
				else if (this.remoteResource instanceof IRepositoryFile) {
					this.right = this.remoteResource.getRepositoryLocation().asRepositoryFile(this.remoteResource.getUrl(), false);
				}
				//FIXME check peg revision
				this.right.setSelectedRevision(previousRevNum);
				this.right.setPegRevision(previousRevNum);
//				this.right.setPegRevision(this.remoteResource.getPegRevision());
			}
			else {
				String message = SVNTeamUIPlugin.instance().getResource("Error.ResourceDoesNotExists", new String[] {this.remoteResource.getUrl()});
				this.reportStatus(new Status (IStatus.ERROR, SVNTeamPlugin.NATURE_ID, IStatus.OK, message, null));
			}
		}
		
		public IRepositoryResource []getRepositoryResources() {
			return new IRepositoryResource[] {this.right, this.remoteResource};
		}
		
    }

	public IRepositoryResource getRepositoryResource() {
		return this.repositoryResource;
	}

	public void setRepositoryResource(IRepositoryResource repositoryResource) {
		this.repositoryResource = repositoryResource;
	}
	
	private void setDefaults() {
		if (AffectedPathsComposite.ADDITION_OVERLAY == null) {
			AffectedPathsComposite.ADDITION_OVERLAY = SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/addition.gif");
			AffectedPathsComposite.MODIFICATION_OVERLAY = SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/change.gif");
			AffectedPathsComposite.DELETION_OVERLAY = SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/deletion.gif");
			AffectedPathsComposite.REPLACEMENT_OVERLAY = SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/replacement.gif");
		}
	}
	
	protected class GetInfoOperation extends AbstractActionOperation {
		
		protected String url;
		protected long revNum;
		protected SVNEntryInfo resourceInfo;

		public GetInfoOperation(String url, long revNum) {
			super("Operation.GetResourceInfo");
			this.url = url;
			this.revNum = revNum;
		}

		protected void runImpl(IProgressMonitor monitor) throws Exception {
			ISVNConnector proxy = AffectedPathsComposite.this.repositoryResource.getRepositoryLocation().acquireSVNProxy();
			try {
				SVNRevision rev = SVNRevision.fromNumber(this.revNum);
				SVNEntryInfo []infos = SVNUtility.info(proxy, new SVNEntryRevisionReference(SVNUtility.encodeURL(this.url), rev, rev), Depth.EMPTY, new SVNProgressMonitor(this, monitor, null));
				if (infos != null && infos.length > 0) {
					this.resourceInfo = infos[0];
				}
			}
			finally {
				AffectedPathsComposite.this.repositoryResource.getRepositoryLocation().releaseSVNProxy(proxy);
			}   
		}

		public ISchedulingRule getSchedulingRule() {
			return null;
		}

		public SVNEntryInfo getResourceInfo() {
			return this.resourceInfo;
		}
		
	}
	
	protected class AffectedRepositoryResourceProvider extends AbstractActionOperation implements IRepositoryResourceProvider {
		protected IRepositoryResource repositoryResource;
		protected Object affectedPathsItem;
		protected boolean filesOnly;
		
		public AffectedRepositoryResourceProvider(Object affectedPathsItem, boolean filesOnly) {
			super("Operation.GetRepositoryResource");
			this.affectedPathsItem = affectedPathsItem;
			this.filesOnly = filesOnly;
		}
		
		protected void runImpl(IProgressMonitor monitor) throws Exception {
			String path = ((String [])this.affectedPathsItem)[2];
			String name = ((String [])this.affectedPathsItem)[1];
			if (path.trim().length() == 0 && name.equals("ROOT")) {
				name = "";
			}
			// FIXME should be encoded or not?
			String affectedPath = path + "/" + name;
			String rootUrl = AffectedPathsComposite.this.repositoryResource.getRepositoryLocation().getRepositoryRootUrl();
			String resourceUrl = rootUrl + "/" + affectedPath;
			long revision = AffectedPathsComposite.this.currentRevision;
			if (((String [])affectedPathsItem)[0].charAt(0) == 'D') {
				revision = AffectedPathsComposite.this.currentRevision - 1;
			}
			
			GetInfoOperation infoOp = new GetInfoOperation(resourceUrl, revision);
			UIMonitorUtility.doTaskBusyDefault(infoOp);
			this.reportStatus(infoOp.getStatus());
			if (infoOp.getStatus().isOK()) {
				SVNEntryInfo info = infoOp.getResourceInfo();
				
				IRepositoryLocation location = AffectedPathsComposite.this.repositoryResource.getRepositoryLocation();
				
				if (info != null) {
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
						return;					
					} 
					this.repositoryResource = info.kind == Kind.FILE ?  
							(IRepositoryResource)location.asRepositoryFile(resourceUrl, false) : 
							location.asRepositoryContainer(resourceUrl, false);
					this.repositoryResource.setSelectedRevision(SVNRevision.fromNumber(revision));
					this.repositoryResource.setPegRevision(SVNRevision.fromNumber(revision));
				}
			}
		}

		public ISchedulingRule getSchedulingRule() {
			return null;
		}
		
		public IRepositoryResource[] getRepositoryResources() {
			return new IRepositoryResource[] {this.repositoryResource};
		}
		
	}

}
