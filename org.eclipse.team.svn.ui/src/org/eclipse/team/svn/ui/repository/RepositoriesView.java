/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.repository;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractSVNTeamAction;
import org.eclipse.team.svn.ui.action.remote.DeleteAction;
import org.eclipse.team.svn.ui.action.remote.OpenFileAction;
import org.eclipse.team.svn.ui.action.remote.OpenFileWithAction;
import org.eclipse.team.svn.ui.action.remote.RefreshAction;
import org.eclipse.team.svn.ui.action.remote.management.DiscardRepositoryLocationAction;
import org.eclipse.team.svn.ui.action.remote.management.DiscardRevisionLinksAction;
import org.eclipse.team.svn.ui.action.remote.management.NewRepositoryLocationAction;
import org.eclipse.team.svn.ui.action.remote.management.RefreshRepositoryLocationAction;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.repository.browser.RepositoryBrowser;
import org.eclipse.team.svn.ui.repository.model.RepositoriesRoot;
import org.eclipse.team.svn.ui.repository.model.RepositoryContentProvider;
import org.eclipse.team.svn.ui.repository.model.RepositoryFile;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

/**
 * Repository view implementation 
 * 
 * @author Alexander Gurov
 */
public class RepositoriesView extends ViewPart {
	public static final String VIEW_ID = RepositoriesView.class.getName();
	
	protected RepositoryTreeViewer repositoryTree;
	protected RepositoriesRoot root;
	protected DrillDownAdapter ddAdapter;
	protected Action showBrowserAction;
	protected IPartListener2 partListener;
	
	public RepositoriesView() {
		super();
	}
	
	public static MenuManager newMenuInstance(final ISelectionProvider provider) {
		MenuManager menuMgr = new MenuManager();
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(final IMenuManager manager) {
        		MenuManager sub = new MenuManager(SVNTeamUIPlugin.instance().getResource("RepositoriesView.New"), "addMenu");
        		sub.add(new Separator("mainGroup"));
        		sub.add(new Separator("managementGroup"));
        		sub.add(new Separator("repositoryGroup"));
        		sub.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        		Action newRepositoryLocation = new Action(SVNTeamUIPlugin.instance().getResource("RepositoriesView.RepositoryLocation")) {
					public void run() {
						new NewRepositoryLocationAction().run(this);
					}
        		};
        		newRepositoryLocation.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/objects/repository.gif"));
        		sub.add(newRepositoryLocation);
        		manager.add(sub);
        		
				manager.add(new Separator("checkoutGroup"));
                
        		sub = new MenuManager(SVNTeamUIPlugin.instance().getResource("RepositoriesView.OpenWith"), "openWithMenu");
        		sub.add(new Separator("dynamicGroup"));
        		IStructuredSelection selection = (IStructuredSelection)provider.getSelection();
        		if (selection.size() == 1) {
        			Object item = selection.getFirstElement();
        			if (item instanceof RepositoryFile) {
        				String name = ((RepositoryFile)item).getRepositoryResource().getName();
        				IEditorDescriptor []editors = SVNTeamUIPlugin.instance().getWorkbench().getEditorRegistry().getEditors(name);
        				for (int i = 0; i < editors.length; i++) {
        					if (!editors[i].getId().equals(EditorsUI.DEFAULT_TEXT_EDITOR_ID)) {
        						final OpenFileWithAction openAction = new OpenFileWithAction(editors[i].getId(), false);
        		        		Action wrapper = new Action(editors[i].getLabel()) {
        							public void run() {
        								openAction.run(this);
        							}
        		        		};
        						openAction.selectionChanged(wrapper, selection);
        						sub.add(wrapper);
        					}
        				}
        			}
        		}
        		sub.add(new Separator("fixedGroup"));
        		sub.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        		manager.add(sub);
                
                manager.add(new Separator("miscGroup"));
                
        		sub = new MenuManager(SVNTeamUIPlugin.instance().getResource("RepositoriesView.Refactor"), "refactorMenu");
        		sub.add(new Separator("mainGroup"));
        		sub.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        		manager.add(sub);
        		
				manager.add(new Separator("locationGroup"));
				
                manager.add(new Separator("propertiesGroup"));

                manager.add(new Separator("importExportGroup"));
                
				sub = new MenuManager(SVNTeamUIPlugin.instance().getResource("RepositoriesView.CompareWith"), "compareMenu");
        		sub.add(new Separator("mainGroup"));
        		sub.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        		manager.prependToGroup("importExportGroup", sub);
                
                manager.add(new Separator("refreshGroup"));
                
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
            }

        });
        menuMgr.setRemoveAllWhenShown(true);
        return menuMgr;
	}

	public void createPartControl(Composite parent) {
		this.repositoryTree = new RepositoryTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		this.repositoryTree.setContentProvider(new RepositoryContentProvider(this.repositoryTree));
		this.repositoryTree.setLabelProvider(new WorkbenchLabelProvider());
		this.getSite().setSelectionProvider(this.repositoryTree);
		this.repositoryTree.setInput(this.root = new RepositoriesRoot());
		//this.repositoryTree.setSorter(new ViewSorter())
		
		this.ddAdapter = new DrillDownAdapter(this.repositoryTree);
		
		// popup menu
        Tree tree = this.repositoryTree.getTree();
        MenuManager menuMgr = RepositoriesView.newMenuInstance(this.repositoryTree);
        tree.setMenu(menuMgr.createContextMenu(tree));
        this.getSite().registerContextMenu(menuMgr, this.repositoryTree);
        
        // toolbar
        IActionBars actionBars = getViewSite().getActionBars();
        IToolBarManager tbm = actionBars.getToolBarManager();
        this.ddAdapter.addNavigationActions(tbm);
        Action tAction = null;
        tbm.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("SVNView.Refresh.Label")) {
            public void run() {
                if (RepositoriesView.this.repositoryTree.getSelection() instanceof IStructuredSelection) {
                    IStructuredSelection selection = (IStructuredSelection)RepositoriesView.this.repositoryTree.getSelection();
                    RepositoriesView.this.handleRefresh(selection);
                }
            }
        }); 
        tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/refresh.gif"));
        tAction.setToolTipText(SVNTeamUIPlugin.instance().getResource("SVNView.Refresh.ToolTip"));
        
		tbm.add(new Separator("collapseAllGroup"));
		
        tbm.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("RepositoriesView.CollapseAll.Label")) {
			public void run() {
			    RepositoriesView.this.repositoryTree.collapseAll();				
			}
        }); 
        tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/collapseall.gif"));
        tAction.setToolTipText(SVNTeamUIPlugin.instance().getResource("RepositoriesView.CollapseAll.ToolTip"));
        
		tbm.add(new Separator("repositoryGroup"));
        
        tbm.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("RepositoriesView.NewLocation.Label")) {
			public void run() {
				new NewRepositoryLocationAction().run(this);
			}
        }); 
        tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/repositories/new_location.gif"));
        tAction.setToolTipText(SVNTeamUIPlugin.instance().getResource("RepositoriesView.NewLocation.ToolTip"));
        
        tbm.add(this.showBrowserAction = new Action(SVNTeamUIPlugin.instance().getResource("RepositoriesView.ShowBrowser.Label"), Action.AS_CHECK_BOX) {
			public void run() {
				if (this.isChecked()) {
					RepositoriesView.this.showRepositoryBrowser(true);
				}
				else {
					RepositoriesView.this.hideRepositoryBrowser();
				}
			}			
        });        
        this.showBrowserAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/repositories/browser.gif"));
        this.showBrowserAction.setToolTipText(SVNTeamUIPlugin.instance().getResource("RepositoriesView.ShowBrowser.ToolTip"));

        this.repositoryTree.getControl().addKeyListener(new KeyAdapter() {
        	public void keyPressed(KeyEvent event) {
    			if (RepositoriesView.this.repositoryTree.getSelection() instanceof IStructuredSelection) {
        			IStructuredSelection selection = (IStructuredSelection)RepositoriesView.this.repositoryTree.getSelection();
	        		if (event.keyCode == SWT.F5) {
	    				RepositoriesView.this.handleRefresh(selection);
	        		}
	        		else if (event.keyCode == SWT.DEL) {
	        		    RepositoriesView.this.handleDeleteKey(selection);
	        		}
    			}
        	}
        });
        
		this.repositoryTree.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				ISelection selection = e.getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection structured = (IStructuredSelection)selection;
					if (structured.size() == 1) {
						RepositoriesView.this.handleDoubleClick(structured);
					}
				}
			}
		});
		
		this.partListener = new IPartListener2() {
			public void partVisible(IWorkbenchPartReference partRef) {
				if (partRef.getId().equals(RepositoriesView.VIEW_ID)) {
					RepositoriesView.this.refreshRepositoriesImpl(false);
				}				
			}
			public void partHidden(IWorkbenchPartReference partRef) {
			}
			public void partInputChanged(IWorkbenchPartReference partRef) {
			}
			public void partOpened(IWorkbenchPartReference partRef) {
			}
			public void partDeactivated(IWorkbenchPartReference partRef) {
			}
			public void partClosed(IWorkbenchPartReference partRef) {
				if (partRef.getId().equals(RepositoryBrowser.VIEW_ID)) {
					RepositoriesView.this.getViewSite().getPage().removePartListener(this);
				}
			}
			public void partBroughtToTop(IWorkbenchPartReference partRef) {
			}
			public void partActivated(IWorkbenchPartReference partRef) {
			}
		};
		
		this.getViewSite().getPage().addPartListener(this.partListener);
		
		//Setting context help
	    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.team.svn.help.repositoryViewContext");
	}
	
	public void dispose() {
		super.dispose();
		this.getViewSite().getPage().removePartListener(this.partListener);		
	}
	
	public void setFocus() {
		this.repositoryTree.getControl().setFocus();
	}
	
	public static void refresh(Object where) {
		RepositoriesView.refresh(where, null);
	}
	
	public static void refresh(Object where, RepositoryTreeViewer.IRefreshVisitor visitor) {
		RepositoriesView instance = RepositoriesView.instance();
		if (instance != null) {
			instance.repositoryTree.refresh(where, visitor, false);
		}
	}
	
	public static void refreshRepositories(boolean deep) {
		RepositoriesView instance = RepositoriesView.instance();
		if (instance != null) {
			instance.refreshRepositoriesImpl(deep);
		}
	}

	public RepositoryTreeViewer getRepositoryTree() {
		return this.repositoryTree;
	}
	
	public void refreshButtonsState() {
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		boolean isBrowserVisible = SVNTeamPreferences.getRepositoryBoolean(store, SVNTeamPreferences.REPOSITORY_SHOW_BROWSER_NAME);
		this.showBrowserAction.setChecked(isBrowserVisible);
	}
	
	public static RepositoriesView instance() {
		final RepositoriesView []view = new RepositoriesView[1];
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = SVNTeamUIPlugin.instance().getWorkbench().getActiveWorkbenchWindow();
				if (window != null && window.getActivePage() != null) {
					view[0] = (RepositoriesView)window.getActivePage().findView(RepositoriesView.VIEW_ID);
				}
			}
		});
		return view[0];
	}
	
	protected void refreshRepositoriesImpl(boolean deep) {
		if (deep) {
			this.root.refresh();
		}
		else {
			this.root.softRefresh();
		}
		this.repositoryTree.refresh();
	}

	protected void showRepositoryBrowser(final boolean force) {
		final IWorkbenchPage page = this.getSite().getPage();
		UIMonitorUtility.doTaskBusyDefault(new AbstractActionOperation("Operation.ShowBrowser") {
            protected void runImpl(IProgressMonitor monitor) throws Exception {
        		RepositoryBrowser browser = (RepositoryBrowser)page.showView(RepositoryBrowser.VIEW_ID);
    			ISelection selection = RepositoriesView.this.repositoryTree.getSelection();
    			browser.selectionChanged(new SelectionChangedEvent(RepositoriesView.this.repositoryTree, selection));
            }
        });
	}
	
	protected void hideRepositoryBrowser() {
    	IWorkbenchPage page = this.getSite().getPage();
    	IViewPart part = page.findView(RepositoryBrowser.VIEW_ID);
    	if (part != null) {
        	page.hideView(part);
    	}
	}

	protected void handleRefresh(IStructuredSelection selection) {
	    Action tmp = new Action() {};
	    AbstractSVNTeamAction action = null;
	    
	    action = new RefreshAction();
	    action.selectionChanged(tmp, selection);
	    action.setActivePart(tmp, RepositoriesView.this);
	    if (tmp.isEnabled()) {
		    action.run(tmp);
	    }
	    
    	action = new RefreshRepositoryLocationAction();
	    action.selectionChanged(tmp, selection);
	    action.setActivePart(tmp, RepositoriesView.this);
	    if (tmp.isEnabled()) {
		    action.run(tmp);
	    }
	}

	protected void handleDeleteKey(IStructuredSelection selection) {
	    Action tmp = new Action() {}; 
	    AbstractSVNTeamAction action = new DeleteAction();
	    action.selectionChanged(tmp, selection);
	    action.setActivePart(tmp, RepositoriesView.this);
	    if (tmp.isEnabled()) {
		    action.run(tmp);
	    }
	    else {
		    action = new DiscardRevisionLinksAction();
		    action.selectionChanged(tmp, selection);
		    action.setActivePart(tmp, RepositoriesView.this);
		    if (tmp.isEnabled()) {
			    action.run(tmp);
		    }
		    else {
			    action = new DiscardRepositoryLocationAction();
			    action.selectionChanged(tmp, selection);
			    action.setActivePart(tmp, RepositoriesView.this);
			    if (tmp.isEnabled()) {
				    action.run(tmp);
			    }
		    }
	    }
	}
	
	protected void handleDoubleClick(IStructuredSelection selection) {
	    Action tmp = new Action() {};
	    AbstractSVNTeamAction action = new OpenFileAction();
	    action.selectionChanged(tmp, selection);
	    action.setActivePart(tmp, RepositoriesView.this);
	    if (tmp.isEnabled()) {
		    action.run(tmp);
	    }
	}
	
	public boolean canGoBack() {
		return this.ddAdapter.canGoBack();
	}
	
	public void goBack() {
		this.ddAdapter.goBack();
	}

}
