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

import java.text.MessageFormat;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.Info2;
import org.eclipse.team.svn.core.client.LogMessage;
import org.eclipse.team.svn.core.client.NodeKind;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IResourcePropertyProvider;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.remote.GetLogMessagesOperation;
import org.eclipse.team.svn.core.operation.remote.GetRemotePropertiesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.history.AffectedPathNode;
import org.eclipse.team.svn.ui.history.AffectedPathsContentProvider;
import org.eclipse.team.svn.ui.history.AffectedPathsLabelProvider;
import org.eclipse.team.svn.ui.operation.CompareRepositoryResourcesOperation;
import org.eclipse.team.svn.ui.operation.OpenRemoteFileOperation;
import org.eclipse.team.svn.ui.operation.ShowPropertiesOperation;
import org.eclipse.team.svn.ui.utility.TableViewerSorter;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Affected paths composite, contains tree and table viewers of affected paths
 *
 * @author Sergiy Logvin
 */
public class AffectedPathsComposite extends Composite {
	
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
                if (column == 4) {
                	Long rData1 =  new Long(rowData1[column] == "" ? "0" : rowData1[column]);
                	Long rData2 =  new Long(rowData2[column] == "" ? "0" : rowData2[column]);
                	return rData1.compareTo(rData2);
                }
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
        layout.addColumnData(new ColumnWeightData(0, 26, false));        
        //1.name
        col = new TableColumn(this.table, SWT.NONE);
        col.setText(SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.Name"));
        col.addSelectionListener(sorter);
        layout.addColumnData(new ColumnWeightData(20, true));
        //2.path
        col = new TableColumn(this.table, SWT.NONE);
        col.setText(SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.Path"));
		col.addSelectionListener(sorter);
        layout.addColumnData(new ColumnWeightData(35, true));
        //3.source path
        col = new TableColumn(this.table, SWT.NONE);
        col.setText(SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.CopiedFrom"));
		col.addSelectionListener(sorter);
        layout.addColumnData(new ColumnWeightData(25, true));
        //4.source revision
        col = new TableColumn(this.table, SWT.NONE);
        col.setText(SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.CopiedFromRevision"));
        col.setAlignment(SWT.RIGHT);
        col.addSelectionListener(sorter);
        layout.addColumnData(new ColumnWeightData(23, true));
        
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
			public Image getColumnImage(Object element, int columnIndex) {
				if (columnIndex == 0) {
					String action = ((String [])element)[columnIndex];
					switch (action.charAt(0)) {
					case 'A': {
						return LogMessagesComposite.ADDED_FILE_IMAGE;
					}
					case 'M': {
						return LogMessagesComposite.MODIFIED_FILE_IMAGE;
					}
					case 'D': {
						return LogMessagesComposite.DELETED_FILE_IMAGE;
					}
					case 'R': {
						return LogMessagesComposite.REPLACED_FILE_IMAGE;
					}
					}
				}
				return null;
			}
			public String getColumnText(Object element, int columnIndex) {
				if (columnIndex == 3) {
					String copiedFrom = ((String [])element)[columnIndex];
					return (copiedFrom.length() > 1 && copiedFrom.startsWith("/")) ? copiedFrom.substring(1) : copiedFrom;
				}
				return columnIndex != 0 ? ((String [])element)[columnIndex] : "";
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
			this.returnResource.setSelectedRevision(Revision.getInstance(this.currentRevision));
			this.returnResource.setPegRevision(Revision.getInstance(this.currentRevision));
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
						AffectedPathsComposite.this.openRemoteResource(structured);
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
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.Open")) {
					public void run() {
						AffectedPathsComposite.this.openRemoteResource(affectedTableSelection);
					}
				});
				tAction.setEnabled(affectedTableSelection.size() == 1);
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.ShowProperties")) {
					public void run() {
						AffectedRepositoryResourceProvider provider = new AffectedRepositoryResourceProvider(affectedTableSelection.getFirstElement(), false);
						AffectedPathsComposite.this.showProperties(provider);
					}
				});
				tAction.setEnabled(affectedTableSelection.size() == 1);
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.CompareWithPreviousRevision")) {
					public void run() {
						AffectedRepositoryResourceProvider provider = new AffectedRepositoryResourceProvider(affectedTableSelection.getFirstElement(), false);
						AffectedPathsComposite.this.compareWithPreviousRevision(provider);
					}
				});
				tAction.setEnabled(enabled);
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
        		manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.ShowProperties")) {
					public void run() {
						GetSelectedTreeResource provider = new GetSelectedTreeResource(AffectedPathsComposite.this.repositoryResource, AffectedPathsComposite.this.currentRevision, affectedTableSelection.getFirstElement());
						AffectedPathsComposite.this.showProperties(provider);
					}
        		});
        		tAction.setEnabled(AffectedPathsComposite.this.currentRevision != 0 && affectedTableSelection.size() == 1 /*&& (node.getStatus() == null || node.getStatus().charAt(0) == 'M')*/);
        		manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.CompareWithPreviousRevision")) {
					public void run() {
						GetSelectedTreeResource provider = new GetSelectedTreeResource(AffectedPathsComposite.this.repositoryResource, AffectedPathsComposite.this.currentRevision, affectedTableSelection.getFirstElement());
						AffectedPathsComposite.this.compareWithPreviousRevision(provider);
					}
        		});
        		tAction.setEnabled(AffectedPathsComposite.this.currentRevision != 0 && affectedTableSelection.size() == 1 && (node.getStatus() == null || node.getStatus().charAt(0) == 'M'));
            }
        });
        menuMgr.setRemoveAllWhenShown(true);
        this.treeViewer.getTree().setMenu(menu);
        site.registerContextMenu(menuMgr, this.treeViewer);
	}
	
	protected void openRemoteResource(IStructuredSelection affectedTableSelection) {
		AffectedRepositoryResourceProvider provider = new AffectedRepositoryResourceProvider(affectedTableSelection.getFirstElement(), true);
		OpenRemoteFileOperation openOp = new OpenRemoteFileOperation(provider, OpenRemoteFileOperation.OPEN_DEFAULT);
		
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
	
	protected class GetResourcesToCompareOperation extends AbstractNonLockingOperation implements IRepositoryResourceProvider {
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
			LogMessage []msgs = msgsOp.getMessages();
			if (msgs != null && msgs.length == 2) {
				Revision previousRevNum = Revision.getInstance(msgs[1].revision);
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
				String message = SVNTeamUIPlugin.instance().getResource("Error.ResourceDoesNotExists");
				this.reportStatus(new Status (IStatus.ERROR, SVNTeamPlugin.NATURE_ID, IStatus.OK, message = MessageFormat.format(message, new String[] {this.remoteResource.getUrl()}), null));
			}
		}
		
		public IRepositoryResource []getRepositoryResources() {
			return new IRepositoryResource[] {this.remoteResource, this.right};
		}
		
    }

	public IRepositoryResource getRepositoryResource() {
		return this.repositoryResource;
	}

	public void setRepositoryResource(IRepositoryResource repositoryResource) {
		this.repositoryResource = repositoryResource;
	}
	
	protected class GetInfoOperation extends AbstractActionOperation {
		
		protected String url;
		protected long revNum;
		protected Info2 resourceInfo;

		public GetInfoOperation(String url, long revNum) {
			super("Operation.GetResourceInfo");
			this.url = url;
			this.revNum = revNum;
		}

		protected void runImpl(IProgressMonitor monitor) throws Exception {
			ISVNClientWrapper proxy = AffectedPathsComposite.this.repositoryResource.getRepositoryLocation().acquireSVNProxy();
			try {
				Revision rev = Revision.getInstance(this.revNum);
				Info2 []infos = proxy.info2(this.url, rev, rev, false, new SVNProgressMonitor(this, monitor, null));
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

		public Info2 getResourceInfo() {
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
				Info2 info = infoOp.getResourceInfo();
				
				IRepositoryLocation location = (IRepositoryLocation)AffectedPathsComposite.this.repositoryResource.getRepositoryLocation();
				
				if (info != null) {
					if (info.kind == NodeKind.dir && this.filesOnly) {
						final String message = MessageFormat.format(SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.Open.Message"), new String[] {SVNUtility.decodeURL(info.url)});
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
					} else {
						this.repositoryResource = info.kind == NodeKind.file ?  
								(IRepositoryResource)(location).asRepositoryFile(resourceUrl, false) : 
								(location).asRepositoryContainer(resourceUrl, false);
						this.repositoryResource.setSelectedRevision(Revision.getInstance(revision));
						this.repositoryResource.setPegRevision(Revision.getInstance(revision));
					}
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
