/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *    Rene Link - [patch] NPE in Interactive Merge UI
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.common;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.resource.IRepositoryBase;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.RepositoryTreeComposite;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.repository.model.IParentTreeNode;
import org.eclipse.team.svn.ui.repository.model.IResourceTreeNode;
import org.eclipse.team.svn.ui.repository.model.RepositoryFictiveNode;
import org.eclipse.team.svn.ui.repository.model.RepositoryFile;
import org.eclipse.team.svn.ui.repository.model.RepositoryFolder;
import org.eclipse.team.svn.ui.repository.model.RepositoryLocationFilter;
import org.eclipse.team.svn.ui.repository.model.RepositoryResource;
import org.eclipse.team.svn.ui.repository.model.RepositoryRevisions;

/**
 * Repository tree panel
 *
 * @author Sergiy Logvin
 */
public class RepositoryTreePanel extends AbstractDialogPanel {
	protected RepositoryTreeComposite repositoryTree;
	protected IRepositoryBase root;
	protected IRepositoryResource selectedResource;
	protected IRepositoryResource []selectedResources;
	protected boolean allowSourcesInTree;
	protected boolean allowFiles;
	protected boolean autoExpandFirstLevel;
	protected boolean showRevisionLinks;
	
	public RepositoryTreePanel(String title, IRepositoryResource[] resources, boolean allowSourcesInTree, boolean showRevisionLinks) {
		this(title,
			SVNUIMessages.RepositoryTreePanel_Description, 
			AbstractDialogPanel.makeToBeOperatedMessage(resources), 
			resources, 
			allowSourcesInTree,
			showRevisionLinks);
	}
	
	public RepositoryTreePanel(String title, String description, String message, IRepositoryResource[] resources, boolean allowSourcesInTree, boolean showRevisionLinks) {
		this(title, description, message, resources, allowSourcesInTree, null, showRevisionLinks);
	}
	
	public RepositoryTreePanel(String title, String description, String message, IRepositoryResource[] resources, boolean allowSourcesInTree, IRepositoryBase root, boolean showRevisionLinks) {
		super();
		this.dialogTitle = title;
		this.dialogDescription = description;
		this.defaultMessage = message;
		this.selectedResources = resources;
		this.allowSourcesInTree = allowSourcesInTree;
		this.allowFiles = false;
		this.root = root;		
		this.showRevisionLinks = showRevisionLinks;
	}
	
	public void setAutoExpandFirstLevel(boolean autoExpandFirstLevel) {
		this.autoExpandFirstLevel = autoExpandFirstLevel;
	}
	
	public boolean isAllowFiles() {
		return this.allowFiles;
	}

	public void setAllowFiles(boolean allowFiles) {
		this.allowFiles = allowFiles;
	}
	
	public IRepositoryResource getSelectedResource() {
		return this.selectedResource;
	}
	
	public void createControlsImpl(Composite parent) {
		if (this.root != null) {
			this.repositoryTree = new RepositoryTreeComposite(parent, SWT.BORDER, false, this.root);
		}
		else if (this.selectedResources.length > 0) {
			this.repositoryTree = new RepositoryTreeComposite(parent, SWT.BORDER, false, new ProjectRoot(this.selectedResources[0], this.showRevisionLinks));
		}
		else {
			this.repositoryTree = new RepositoryTreeComposite(parent, SWT.BORDER);
		}
		this.repositoryTree.setAutoExpandFirstLevel(this.autoExpandFirstLevel);
        if (this.repositoryTree.getRepositoryTreeViewer().getInput() instanceof ProjectRoot) {
        	ProjectRoot root = (ProjectRoot)this.repositoryTree.getRepositoryTreeViewer().getInput();
			this.repositoryTree.getRepositoryTreeViewer().setExpandedElements(new Object[] {root.getChildren(null)[0]});
        }
		if (this.root == null && this.selectedResources.length > 0) {
			String url = this.selectedResources[0].getRepositoryLocation().getRepositoryRootUrl();
			this.repositoryTree.setFilter(new RepositoryLocationFilter(url) {
				public boolean accept(Object obj) {
					if (obj instanceof RepositoryFile && !RepositoryTreePanel.this.allowFiles || 
						!RepositoryTreePanel.this.showRevisionLinks && obj instanceof RepositoryRevisions || 
						obj instanceof RepositoryFolder && RepositoryTreePanel.this.isSource(((RepositoryFolder)obj).getRepositoryResource())) {
						return false;
					}
					return super.accept(obj);
				}
			});
		}
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 185;
		this.repositoryTree.setLayoutData(data);
		this.repositoryTree.getRepositoryTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (RepositoryTreePanel.this.manager != null) {
					IStructuredSelection selection = (IStructuredSelection)RepositoryTreePanel.this.repositoryTree.getRepositoryTreeViewer().getSelection();
					RepositoryTreePanel.this.manager.setButtonEnabled(0, !selection.isEmpty() && selection.getFirstElement() instanceof IResourceTreeNode);
				}
			}
		});
	}
	
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.copyMoveToDialogContext"; //$NON-NLS-1$
	}
	
	protected void saveChangesImpl() {
		IStructuredSelection selection = (IStructuredSelection)this.repositoryTree.getRepositoryTreeViewer().getSelection();
		if (!selection.isEmpty() && selection.getFirstElement() instanceof RepositoryResource) {
			this.selectedResource = ((IResourceTreeNode)selection.getFirstElement()).getRepositoryResource();
		}
	}
	
	protected void cancelChangesImpl() {
	}
	
	protected boolean isSource(IRepositoryResource resource) {
		if (!this.allowSourcesInTree) {
			for (int i = 0; i < this.selectedResources.length; i++) {
				if (resource.equals(this.selectedResources[i])) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static class ProjectRoot extends RepositoryFictiveNode implements IParentTreeNode {
		protected Object []children;
		
		public ProjectRoot(IRepositoryResource resource, boolean showRevisionLinks) {
			IRepositoryResource projectRoot = SVNUtility.getTrunkLocation(resource);
			if (((IRepositoryRoot)projectRoot).getKind() == IRepositoryRoot.KIND_TRUNK) {
				projectRoot = projectRoot.getParent();
			}	
			this.children = new Object[showRevisionLinks ? 3 : 2];
			this.children[0] = RepositoryFolder.wrapChild(null, projectRoot);
			this.children[1] = RepositoryFolder.wrapChild(null, resource.getRepositoryLocation().getRepositoryRoot());
			if (showRevisionLinks) {
				this.children[2] = new RepositoryRevisions(resource.getRepositoryLocation());
			}
		}

		public boolean hasChildren() {
			return true;
		}

		public Object[] getChildren(Object o) {
			return this.children;
		}

		public ImageDescriptor getImageDescriptor(Object object) {
			return null;
		}

		public String getLabel(Object o) {
			return null;
		}
		
	}
	
}
