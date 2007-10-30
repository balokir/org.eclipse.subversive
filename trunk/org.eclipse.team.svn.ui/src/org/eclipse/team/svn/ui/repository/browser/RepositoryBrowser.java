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

package org.eclipse.team.svn.ui.repository.browser;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.AbstractSVNView;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractSVNTeamAction;
import org.eclipse.team.svn.ui.action.remote.DeleteAction;
import org.eclipse.team.svn.ui.action.remote.OpenFileAction;
import org.eclipse.team.svn.ui.action.remote.management.DiscardRepositoryLocationAction;
import org.eclipse.team.svn.ui.action.remote.management.DiscardRevisionLinksAction;
import org.eclipse.team.svn.ui.operation.RefreshRemoteResourcesOperation;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.repository.RepositoriesView;
import org.eclipse.team.svn.ui.repository.RepositoryTreeViewer;
import org.eclipse.team.svn.ui.repository.model.IParentTreeNode;
import org.eclipse.team.svn.ui.repository.model.IResourceTreeNode;
import org.eclipse.team.svn.ui.repository.model.RepositoryError;
import org.eclipse.team.svn.ui.repository.model.RepositoryFictiveNode;
import org.eclipse.team.svn.ui.repository.model.RepositoryFictiveWorkingDirectory;
import org.eclipse.team.svn.ui.repository.model.RepositoryFile;
import org.eclipse.team.svn.ui.repository.model.RepositoryFolder;
import org.eclipse.team.svn.ui.repository.model.RepositoryLocation;
import org.eclipse.team.svn.ui.repository.model.RepositoryPending;
import org.eclipse.team.svn.ui.repository.model.RepositoryResource;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Repository browser
 *
 * @author Sergiy Logvin
 */
public class RepositoryBrowser extends AbstractSVNView implements ISelectionChangedListener, RepositoryTreeViewer.IRefreshListener {
	public static final String VIEW_ID = RepositoryBrowser.class.getName();
	
	protected static RepositoryBrowser instance = null;
	protected IPartListener2 partListener;
	protected RepositoryResource inputElement;
	protected Object rawInputElement;
	protected RepositoryFile selectedFile;

	protected IRepositoryLocation location;
	protected RepositoryBrowserTableViewer tableViewer;
	protected RepositoryBrowserContentProvider contentProvider;
	
	public RepositoryBrowser() {
		super(SVNTeamUIPlugin.instance().getResource("RepositoriesView.Browser.Description"));
		RepositoryBrowser.instance = this;
	}
	
	public IRepositoryResource getResource() {
		return this.repositoryResource;
	}

	public void selectionChanged(SelectionChangedEvent event) {
		Object firstElement = this.getSelectedElement(event.getSelection());
		if (firstElement != null) {
			boolean selectedFileChanged = false;
			if (firstElement instanceof RepositoryFile) {
				if (!((RepositoryFile)firstElement).equals(this.selectedFile)) {
					selectedFileChanged = true;
					this.selectedFile = (RepositoryFile)firstElement;
				}
	    		RepositoriesView instance = RepositoriesView.instance();
	    		if (instance != null) {
					RepositoryTreeViewer repositoryTree = instance.getRepositoryTree();
					TreeItem[] items = repositoryTree.getIdenticalNodes(firstElement, true);
					if (items != null && items.length != 0 && items[0] != null) {
						TreeItem item = items[0].getParentItem();
						if (item != null) {
							firstElement = item.getData();
						}
						else {
							firstElement = repositoryTree.getInput();
						}
					}
	    		}
			}
			else {
				if (this.selectedFile != null) {
					this.selectedFile = null;
					selectedFileChanged = true;
				}
			}
			
			if (firstElement instanceof RepositoryLocation) {
				this.rawInputElement = firstElement;
				firstElement = ((RepositoryLocation)firstElement).getResourceWrapper();
			}
			else {
				this.rawInputElement = null;
			}
			if (firstElement instanceof RepositoryResource) {
				if (this.getResource() == null ||
					!this.getResource().equals(((RepositoryResource)firstElement).getRepositoryResource()) ||
					selectedFileChanged) {
					this.connectTo((RepositoryResource)firstElement);
					this.refreshTableView();
				}
				return;
			}
		}
		this.disconnect();
	}
	
	public void refreshed(Object data) {
		this.refreshTableView();
	}

	public void setFocus() {

	}
	
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		this.tableViewer = new RepositoryBrowserTableViewer(parent, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);	
		this.tableViewer.initialize();
		this.contentProvider = new RepositoryBrowserContentProvider();
		this.tableViewer.setContentProvider(this.contentProvider);
		this.tableViewer.setLabelProvider(new RepositoryBrowserLabelProvider(this.tableViewer));
		
        MenuManager menuMgr = RepositoriesView.newMenuInstance(this.tableViewer);
        this.tableViewer.getTable().setMenu(menuMgr.createContextMenu(this.tableViewer.getTable()));
        this.getSite().registerContextMenu(menuMgr, this.tableViewer);
        
		IActionBars actionBars = this.getViewSite().getActionBars();
		IToolBarManager tbm = actionBars.getToolBarManager();
	    Action refreshAction = new Action(SVNTeamUIPlugin.instance().getResource("SVNView.Refresh.Label")) {
			public void run() {
				RepositoryBrowser.this.handleRefresh();
			}
		};
		refreshAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/refresh.gif"));
		refreshAction.setToolTipText(SVNTeamUIPlugin.instance().getResource("SVNView.Refresh.ToolTip"));
		tbm.add(refreshAction);
		
		this.tableViewer.getControl().addKeyListener(new KeyAdapter() {
        	public void keyPressed(KeyEvent event) {
        		if (event.keyCode == SWT.F5) {
        			RepositoryBrowser.this.handleRefresh();
        		}
    			if (RepositoryBrowser.this.tableViewer.getSelection() instanceof IStructuredSelection) {
        			IStructuredSelection selection = (IStructuredSelection)RepositoryBrowser.this.tableViewer.getSelection();
	        		if (event.keyCode == SWT.DEL) {
	        			RepositoryBrowser.this.handleDeleteKey(selection);
	        			RepositoriesView.refresh(RepositoryBrowser.this.repositoryResource);
	        		}
    			}
    			if (event.keyCode == SWT.BS) {
    				RepositoryBrowser.this.handleBackspaceKey();
    			}
        	}
        });
		this.tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				ISelection selection = e.getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection structured = (IStructuredSelection)selection;
					if (structured.size() == 1) {
						RepositoryBrowser.this.handleDoubleClick(structured);
					}
				}
			}
		});
        this.tableViewer.refresh();
		this.showResourceLabel();
		this.partListener = new IPartListener2() {
			public void partVisible(IWorkbenchPartReference partRef) {
				this.setViewState(partRef, true);
			}
			public void partHidden(IWorkbenchPartReference partRef) {
				this.setViewState(partRef, false);
			}
			public void partInputChanged(IWorkbenchPartReference partRef) {
			}
			public void partOpened(IWorkbenchPartReference partRef) {
			}
			public void partDeactivated(IWorkbenchPartReference partRef) {
			}
			public void partClosed(IWorkbenchPartReference partRef) {
				if (partRef.getId().equals(RepositoryBrowser.VIEW_ID)) {
					RepositoryBrowser.this.getViewSite().getPage().removePartListener(this);
				}
			}
			public void partBroughtToTop(IWorkbenchPartReference partRef) {
			}
			public void partActivated(IWorkbenchPartReference partRef) {
			}
			protected void setViewState(IWorkbenchPartReference partRef, boolean visible) {
				if (partRef.getId().equals(RepositoryBrowser.VIEW_ID) ||
					partRef.getId().equals(RepositoriesView.VIEW_ID)) {
					if (partRef.getId().equals(RepositoryBrowser.VIEW_ID)) {
						IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
						SVNTeamPreferences.setRepositoryBoolean(store, SVNTeamPreferences.REPOSITORY_SHOW_BROWSER_NAME, visible);
					}
					RepositoryBrowser.this.setViewState(visible);
				}
			}
		};
		
		this.getViewSite().getPage().addPartListener(this.partListener);
	
		//Setting context help
	    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.team.svn.help.repositoryBrowserViewContext");
	}

	protected void connectTo(RepositoryResource inputElement) {
		this.inputElement = inputElement;
		this.repositoryResource = inputElement.getRepositoryResource();
		this.showResourceLabel();
	}
	
	protected void refreshTableView() {
		if (!this.tableViewer.getTable().isDisposed()) {
			boolean hideGrid = false;
			if (this.inputElement instanceof RepositoryFolder) {
				Object []children = ((RepositoryFolder)this.inputElement).peekChildren(null);
				hideGrid = children.length > 0 ? children[0] instanceof RepositoryFictiveNode : false;
			}
			this.tableViewer.getTable().setLinesVisible(!hideGrid);
			this.tableViewer.setInput(this.inputElement);
			if (this.selectedFile != null) {
				this.tableViewer.setSelection(new StructuredSelection(this.selectedFile), true);
			}
			else {
				this.tableViewer.getTable().setSelection(0);
			}
		}
	}

	protected void setViewState(boolean visible) {
		RepositoriesView instance = RepositoriesView.instance();
		if (instance != null) {
			instance.refreshButtonsState();
			RepositoryTreeViewer viewer = instance.getRepositoryTree();
			if (visible) {
				this.selectionChanged(new SelectionChangedEvent(viewer, viewer.getSelection()));
				viewer.addSelectionChangedListener(this);
				viewer.addRefreshListener(this);
			}
			else {
				viewer.removeSelectionChangedListener(this);
				viewer.removeRefreshListener(this);
			}
		}
		if (!visible) {
			this.disconnect();
		}
	}
	
	protected void handleRefresh() {
		if (this.repositoryResource != null) {
		    UIMonitorUtility.doTaskBusyDefault(new RefreshRemoteResourcesOperation(new IRepositoryResource[] {this.repositoryResource}));
		}
	}
	
	protected void handleDeleteKey(IStructuredSelection selection) {
	    Action tmp = new Action() {}; 
	    AbstractSVNTeamAction action = new DeleteAction();
	    action.selectionChanged(tmp, selection);
	    action.setActivePart(tmp, RepositoryBrowser.this);
	    if (tmp.isEnabled()) {
		    action.run(tmp);
	    }
	    else {
		    action = new DiscardRevisionLinksAction();
		    action.selectionChanged(tmp, selection);
		    action.setActivePart(tmp, RepositoryBrowser.this);
		    if (tmp.isEnabled()) {
			    action.run(tmp);
		    }
		    else {
			    action = new DiscardRepositoryLocationAction();
			    action.selectionChanged(tmp, selection);
			    action.setActivePart(tmp, RepositoryBrowser.this);
			    if (tmp.isEnabled()) {
				    action.run(tmp);
			    }
		    }
	    }
	}
	
	protected void handleBackspaceKey() {
		Table table = this.tableViewer.getTable();
		TableItem[] items = table.getItems();
		if (items != null && items.length != 0 && items[0] != null) {
			Object data = items[0].getData();
			if (!(data instanceof RepositoryError || data instanceof RepositoryPending)) {
				this.goUp();
			}
		}
	}
	
	protected void handleDoubleClick(IStructuredSelection selection) {
	    Action tmp = new Action() {};
	    AbstractSVNTeamAction action = new OpenFileAction();
	    action.selectionChanged(tmp, selection);
	    action.setActivePart(tmp, RepositoryBrowser.this);
	    if (tmp.isEnabled()) {
		    action.run(tmp);
	    }
	    else {
			Object node = selection.getFirstElement();
	    	if (node instanceof RepositoryFictiveWorkingDirectory) {
	    		this.goUp();
	    	}
	    	else if (node instanceof IParentTreeNode) {
	    		this.goDown(node);
	    	}
	    }
	}
	
	protected void goUp() {
		RepositoriesView view = RepositoriesView.instance();
		RepositoryResource node = this.inputElement;
		if (view == null || node == null) {
			return;
		}
		
		Object parent = node.getParent();
		if (parent != null) {
			Object oldInput = node;
			if (this.isTopLevelNode(view.getRepositoryTree(), node)) {
				this.goBack(view, node);
			}
			else {
				RepositoryTreeViewer treeViewer = view.getRepositoryTree();
				TreeItem[] items = treeViewer.getIdenticalNodes(node, false);
				if (items != null && items.length != 0 && items[0] != null && items != parent) {
					parent = items[0].getParentItem().getData();
				}
				treeViewer.setExpandedState(parent, true);
				treeViewer.setSelection(new StructuredSelection(parent));
			}
			if (this.inputElement != null) {
				Table table = this.tableViewer.getTable();
				TableItem[] items = table.getItems();
				if (items != null && items.length != 0) {
					for (int i = 0; i < items.length; i++) {
						Object data = items[i].getData();
						if (oldInput.equals(data)) {
							this.tableViewer.setSelection(new StructuredSelection(oldInput), true);
							break;
						}
					}
				}
			}
		}
	}
	
	protected void goBack(RepositoriesView view, Object node) {
		if (!view.canGoBack()) {
			return;
		}
		view.goBack();
		if (this.inputElement != null) {
			view.getRepositoryTree().setExpandedState(this.inputElement, true);
			if (node.equals(this.inputElement)) {
				RepositoryTreeViewer treeViewer = view.getRepositoryTree();
				TreeItem[] items = treeViewer.getIdenticalNodes(this.inputElement, false);
				if (items != null && items.length != 0 && items[0] != null) {
					if (items[0].getParentItem() != null) {
						Object newSelectedElement = items[0].getParentItem().getData();
						if (newSelectedElement != null) {
							treeViewer.setSelection(new StructuredSelection(newSelectedElement));
						}
					}
					else {
						this.goBack(view, node);
					}
				}
			}
		}
	}
	
	protected void goDown(Object node) {
		RepositoryTreeViewer treeViewer;
		RepositoriesView view = RepositoriesView.instance();
		if (view == null || !(node instanceof IResourceTreeNode)) {
			return;
		}
		treeViewer = view.getRepositoryTree();
		treeViewer.setExpandedState((this.rawInputElement == null) ? this.inputElement : this.rawInputElement, true);
		treeViewer.setExpandedState(node, true);
		treeViewer.setSelection(new StructuredSelection(node));
	}
	
	protected boolean isTopLevelNode(RepositoryTreeViewer tree, Object node) {
		TreeItem[] items = tree.getIdenticalNodes(node, false);
		if (items != null && items.length != 0 && items[0] != null) {
			return (items[0].getParentItem() == null);
		}
		return true;
	}

	protected void disconnect() {
		this.repositoryResource = null;
		this.inputElement = null;
		this.showResourceLabel();
		this.tableViewer.setInput(null);
		this.tableViewer.getTable().setLinesVisible(false);
	}

	protected void disconnectView() {
	}

	protected void refreshView() {
	}

	protected boolean needsLinkWithEditorAndSelection() {
		return false;
	}
	
	protected Object getSelectedElement(ISelection selection) {
		Object selectedElement = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structured = (IStructuredSelection)selection;
			if (structured.size() == 1) {
				selectedElement = structured.getFirstElement();
			}
		}
		return selectedElement;
	}

}
