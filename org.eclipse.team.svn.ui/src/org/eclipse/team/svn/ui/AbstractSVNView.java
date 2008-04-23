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

package org.eclipse.team.svn.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.events.IResourceStatesListener;
import org.eclipse.team.svn.core.resource.events.ResourceStatesChangedEvent;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.repository.RepositoryFileEditorInput;
import org.eclipse.team.svn.ui.repository.model.IResourceTreeNode;
import org.eclipse.team.ui.synchronize.SyncInfoCompareInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;

/**
 * Abstract SVN view implementation
 * 
 * @author Sergiy Logvin
 */
public abstract class AbstractSVNView extends ViewPart implements IResourceStatesListener {
	
	protected IResource wcResource;
	protected IRepositoryResource repositoryResource;
	protected String viewDescription;
	protected Object lastSelectedElement;
	protected boolean isLinkWithEditorEnabled;
	
	protected IPartListener partListener = new IPartListener() {
		public void partActivated(IWorkbenchPart part) {
			if (part instanceof IEditorPart) {
				AbstractSVNView.this.editorActivated((IEditorPart) part);
			}
		}

		public void partBroughtToTop(IWorkbenchPart part) {
			if (part == AbstractSVNView.this) {
				AbstractSVNView.this.editorActivated(AbstractSVNView.this.getViewSite().getPage().getActiveEditor());
			}
		}

		public void partOpened(IWorkbenchPart part) {
			if (part == AbstractSVNView.this) {
				AbstractSVNView.this.editorActivated(AbstractSVNView.this.getViewSite().getPage().getActiveEditor());
			}
		}

		public void partClosed(IWorkbenchPart part) {
		}

		public void partDeactivated(IWorkbenchPart part) {
		}
	};
	
	protected IPartListener2 partListener2 = new IPartListener2() {
		public void partActivated(IWorkbenchPartReference ref) {
		}
		public void partBroughtToTop(IWorkbenchPartReference ref) {
		}
		public void partClosed(IWorkbenchPartReference ref) {
		}
		public void partDeactivated(IWorkbenchPartReference ref) {
		}
		public void partOpened(IWorkbenchPartReference ref) {
		}
		public void partHidden(IWorkbenchPartReference ref) {
		}
		public void partVisible(IWorkbenchPartReference ref) {
			if (ref.getPart(true) == AbstractSVNView.this) {
				AbstractSVNView.this.editorActivated(
						AbstractSVNView.this.getViewSite().getPage().getActiveEditor());
			}
		}
		public void partInputChanged(IWorkbenchPartReference ref) {
		}
	};
	
	protected ISelectionListener selectionListener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structSelection = (IStructuredSelection) selection;
				AbstractSVNView.this.lastSelectedElement = structSelection.getFirstElement();
				
				if (!AbstractSVNView.this.isLinkWithEditorEnabled || !AbstractSVNView.this.isPageVisible()) {
					return;
				}
				
				if (AbstractSVNView.this.lastSelectedElement != null) {
					
					if (AbstractSVNView.this.lastSelectedElement instanceof IResource) {
						final IResource resource = (IResource)AbstractSVNView.this.lastSelectedElement;
						AbstractSVNView.this.updateViewInput(resource);
						AbstractSVNView.this.lastSelectedElement = null;
					}
					else if (AbstractSVNView.this.lastSelectedElement instanceof IAdaptable) {
						if (AbstractSVNView.this.lastSelectedElement instanceof IResourceTreeNode) {
							IResourceTreeNode remote = (IResourceTreeNode)AbstractSVNView.this.lastSelectedElement;
							AbstractSVNView.this.updateViewInput(remote.getRepositoryResource());
						} else {
							Object adapted = ((IAdaptable) AbstractSVNView.this.lastSelectedElement).getAdapter(IResource.class);
							if (adapted instanceof IResource) {
								AbstractSVNView.this.updateViewInput((IResource)adapted);
							}
						}
					}
				}
			}
		}
	};
	
	public AbstractSVNView(String viewDescription) {
		super();
		this.viewDescription = viewDescription;
		SVNRemoteStorage.instance().addResourceStatesListener(ResourceStatesChangedEvent.class, this);
	}
	
	public void createPartControl(Composite parent) {
		if (this.needsLinkWithEditorAndSelection()) {
			this.getSite().getPage().addPartListener(this.partListener);
			this.getSite().getPage().addPartListener(this.partListener2);
			this.getSite().getPage().addSelectionListener(this.selectionListener);
		}
	}
	
	public void resourcesStateChanged(ResourceStatesChangedEvent event) {
		if (this.wcResource == null) {
			return;
		}
		if (event.contains(this.wcResource) || event.contains(this.wcResource.getProject())) {
			if (!this.wcResource.exists() || !FileUtility.isConnected(this.wcResource)) {
				this.disconnectView();
			}
			else {
				ILocalResource local = SVNRemoteStorage.instance().asLocalResource(this.wcResource);
				if (local == null || IStateFilter.SF_UNVERSIONED.accept(local)) {
					this.disconnectView();
				}
			}
		    this.refreshView();
		}
	}
	
	protected void showResourceLabel() {
		String resourceName;
		if (this.wcResource != null) {
		    String path = this.wcResource.getFullPath().toString();
		    if (path.startsWith("/")) {
		    	path = path.substring(1);
		    }
			resourceName = path;
		}
		else if (this.repositoryResource != null) {
			resourceName = this.repositoryResource.getUrl();
		}
		else {
			resourceName = SVNTeamUIPlugin.instance().getResource("SVNView.ResourceNotSelected");
		}
		this.setContentDescription(resourceName);
	}
	
	protected boolean isPageVisible() {
		return this.getViewSite().getPage().isPartVisible(this);
	}
	
	public void dispose() {
		if (this.needsLinkWithEditorAndSelection()) {
			this.getSite().getPage().removePartListener(this.partListener);
			this.getSite().getPage().removePartListener(this.partListener2);
			this.getSite().getPage().removeSelectionListener(this.selectionListener);
		}
		SVNRemoteStorage.instance().removeResourceStatesListener(ResourceStatesChangedEvent.class, this);
		super.dispose();
	}
	
	protected void editorActivated(IEditorPart editor) {
		if (editor != null && !this.isPageVisible()) {
			this.lastSelectedElement = editor;
		}
		
		if (editor == null || !this.isLinkWithEditorEnabled || !this.isPageVisible()) {
			return;
		}
		IEditorInput input = editor.getEditorInput();

		if (input != null) {
			if (input instanceof IFileEditorInput || input instanceof SyncInfoCompareInput) {
				Object adapter = input.getAdapter(IFile.class);
				if (adapter instanceof IFile) {
					this.updateViewInput((IFile)adapter);
				}
			}
			else if (input instanceof RepositoryFileEditorInput) {
				this.updateViewInput(((RepositoryFileEditorInput)input).getRepositoryResource());
			}
		}
	}
	
	protected void updateViewInput(IResource resource) {
	}
	protected void updateViewInput(IRepositoryResource resource) {
	}
	
	protected abstract boolean needsLinkWithEditorAndSelection();
	protected abstract void disconnectView();
	protected abstract void refreshView();
}
