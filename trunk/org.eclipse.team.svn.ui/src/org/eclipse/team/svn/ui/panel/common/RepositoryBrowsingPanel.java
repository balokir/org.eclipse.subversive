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

package org.eclipse.team.svn.ui.panel.common;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryBase;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.operation.UILoggedOperation;
import org.eclipse.team.svn.ui.operation.UILoggedOperation.OperationErrorInfo;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.panel.reporting.ErrorCancelPanel;
import org.eclipse.team.svn.ui.repository.RepositoryTreeViewer;
import org.eclipse.team.svn.ui.repository.model.IRepositoryContentFilter;
import org.eclipse.team.svn.ui.repository.model.RepositoryContentProvider;
import org.eclipse.team.svn.ui.repository.model.RepositoryError;
import org.eclipse.team.svn.ui.repository.model.RepositoryFictiveNode;
import org.eclipse.team.svn.ui.repository.model.RepositoryFile;
import org.eclipse.team.svn.ui.repository.model.RepositoryFolder;
import org.eclipse.team.svn.ui.repository.model.RepositoryLocation;
import org.eclipse.team.svn.ui.repository.model.RepositoryPending;
import org.eclipse.team.svn.ui.repository.model.RepositoryResource;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Repository browsing panel
 * 
 * @author Alexander Gurov
 */
public class RepositoryBrowsingPanel extends AbstractDialogPanel {
	protected RepositoryTreeViewer repositoryTree;
	protected IRepositoryBase root;
	protected IRepositoryResource selectedResource;
	protected boolean onlyFolders;
	protected SVNRevision revision;
	protected boolean autoExpandFirstLevel;

    public RepositoryBrowsingPanel(String title, IRepositoryBase root) {
		this(title, root, SVNRevision.HEAD, true);
    }

    public RepositoryBrowsingPanel(String title, IRepositoryBase root, SVNRevision revision) {
		this(title, root, revision, true);
    }

    public RepositoryBrowsingPanel(String title, IRepositoryBase root, SVNRevision revision, boolean onlyFolders) {
        super();
        this.dialogTitle = title;
		this.root = root;
		this.revision = revision;
		this.onlyFolders = onlyFolders;
		this.dialogDescription = SVNTeamUIPlugin.instance().getResource("RepositoryBrowsingPanel.Description");
		this.defaultMessage = SVNTeamUIPlugin.instance().getResource("RepositoryBrowsingPanel.Message");
    }
    
    public void setAutoexpandFirstLevel(boolean autoExpandFirstLevel) {
    	this.autoExpandFirstLevel = autoExpandFirstLevel;
    }

	public IRepositoryResource getSelectedResource() {
		return this.selectedResource;
	}

    public void createControlsImpl(Composite parent) {
		this.repositoryTree = new RepositoryTreeViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE);
		if (this.autoExpandFirstLevel) {
			this.repositoryTree.setAutoExpandLevel(2);
		}
		RepositoryContentProvider provider = new RepositoryContentProvider(this.repositoryTree) {
			public Object []getChildren(Object parentElement) {
				Object []retVal = super.getChildren(parentElement);
				if (parentElement instanceof RepositoryResource && ((RepositoryResource)parentElement).getRepositoryResource() == RepositoryBrowsingPanel.this.root) {
					if (retVal == null || retVal.length == 0) {
						return new Object[] {new RepositoryFictiveNode() {
							public Object[] getChildren(Object o) {
								return null;
							}
							public ImageDescriptor getImageDescriptor(Object object) {
								return null;
							}
							public String getLabel(Object o) {
								return SVNTeamUIPlugin.instance().getResource("RepositoryBrowsingPanel.EmptyLabel");
							}
							
						}};
					}
				}
				return retVal;
			}
		};
		provider.setFilter(new IRepositoryContentFilter() {
			public boolean accept(Object obj) {
				if (RepositoryBrowsingPanel.this.onlyFolders) {
					if (obj instanceof RepositoryResource) {
						RepositoryResource child = (RepositoryResource)obj;
						if (child instanceof RepositoryFile) {
							return false;
						}
						IRepositoryResource remote = child.getRepositoryResource();
						remote.setSelectedRevision(RepositoryBrowsingPanel.this.revision);
						remote.setPegRevision(RepositoryBrowsingPanel.this.revision);
					}
					else if (!(obj instanceof RepositoryPending || obj instanceof RepositoryError)) {
						return false;
					}
				}
				return true;
			}
		});
		this.repositoryTree.setContentProvider(provider);
		this.repositoryTree.setLabelProvider(new WorkbenchLabelProvider());
		this.repositoryTree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (RepositoryBrowsingPanel.this.manager != null) {
					IStructuredSelection selection = (IStructuredSelection)RepositoryBrowsingPanel.this.repositoryTree.getSelection();
					RepositoryBrowsingPanel.this.manager.setButtonEnabled(0, !selection.isEmpty() && selection.getFirstElement() instanceof RepositoryResource);
				}
			}
		});
		if (this.root instanceof IRepositoryLocation) {
			this.repositoryTree.setInput(new RepositoryLocation((IRepositoryLocation)this.root));
		}
		else {
			RepositoryResource resource = RepositoryFolder.wrapChild(null, (IRepositoryResource)this.root);
			resource.setViewer(this.repositoryTree);
			this.repositoryTree.setInput(resource);
		}
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 185;
		this.repositoryTree.getTree().setLayoutData(data);
		
		MenuManager menuMgr = new MenuManager();
        Menu menu = menuMgr.createContextMenu(this.repositoryTree.getTree());
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
            	final IStructuredSelection tSelection = (IStructuredSelection)RepositoryBrowsingPanel.this.repositoryTree.getSelection();
        		if (tSelection.getFirstElement() instanceof RepositoryError) {
	            	manager.add(new Action(SVNTeamUIPlugin.instance().getResource("RepositoryBrowsingPanel.Details")) {
						public void run() {
							RepositoryError error = (RepositoryError)tSelection.getFirstElement();
							OperationErrorInfo errorInfo =  UILoggedOperation.formatMessage(error.getErrorStatus(), true);
							ErrorCancelPanel panel = new ErrorCancelPanel(SVNTeamUIPlugin.instance().getResource("RepositoryBrowsingPanel.Details.Title"), errorInfo.numberOfErrors, errorInfo.simpleMessage, errorInfo.advancedMessage, false, null);
							DefaultDialog dialog = new DefaultDialog(UIMonitorUtility.getDisplay().getActiveShell(), panel);
							dialog.open();
						}
	        		});
        		}
            }
        });
        menuMgr.setRemoveAllWhenShown(true);
        this.repositoryTree.getTree().setMenu(menu);
    }
    
    protected void saveChangesImpl() {
		IStructuredSelection selection = (IStructuredSelection)this.repositoryTree.getSelection();
		if (selection != null && !selection.isEmpty()) {
			this.selectedResource = ((RepositoryResource)selection.getFirstElement()).getRepositoryResource();
		}
    }

    protected void cancelChangesImpl() {
    }

}
