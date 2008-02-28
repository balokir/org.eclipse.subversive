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

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.svn.core.history.ResourceContentStorage;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.history.IRepositoryEditorInput;
import org.eclipse.team.svn.ui.repository.model.IResourceTreeNode;
import org.eclipse.team.svn.ui.repository.model.RepositoryFile;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Remote file editor input implementation
 * 
 * @author Alexander Gurov
 */
public class RepositoryFileEditorInput extends PlatformObject implements IWorkbenchAdapter, IRepositoryEditorInput, IResourceTreeNode {
	private static final Object []NO_CHILDREN = new Object[0];
	protected ResourceContentStorage storage;
    protected RepositoryFile resource;

	public RepositoryFileEditorInput(IRepositoryFile resource) {
		this.resource = new RepositoryFile(null, resource);
		this.storage = new ResourceContentStorage(resource);
	}
	
	public Object []getChildren(Object o) {
		return RepositoryFileEditorInput.NO_CHILDREN;
	}

	public void setViewer(RepositoryTreeViewer repositoryTree) {
		
	}

	public IRepositoryResource getRepositoryResource() {
		return this.resource.getRepositoryResource();
	}
	
	public Object getData() {
		return this.getRepositoryResource();
	}

	public void refresh() {
		this.resource.refresh();
	}

	public Object getParent(Object o) {
		return this.resource.getParent(o);
	}

	public ImageDescriptor getImageDescriptor(Object object) {
		return this.resource.getImageDescriptor(object);
	}

	public String getLabel(Object o) {
		return this.resource.getLabel(o);
	}

	public void fetchContents(IProgressMonitor monitor) {
		this.storage.fetchContents(monitor);
	}
	
	public IStorage getStorage() {
		return this.storage;
	}

	public boolean exists() {
		try {
			return this.resource.getRepositoryResource().exists();
		}
		catch (Exception ex) {
			LoggedOperation.reportError(this.getClass().getName(), ex);
			return false;
		}
	}

	public ImageDescriptor getImageDescriptor() {
		return this.getImageDescriptor(this.resource);
	}

	public String getName() {
		return SVNTeamUIPlugin.instance().getResource("RepositoryFileViewer.Name", new String[] {this.resource.getRepositoryResource().getName(), String.valueOf(this.resource.getRepositoryResource().getSelectedRevision())});
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return this.getName();
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			return this;
		}
		return super.getAdapter(adapter);
	}

    public boolean equals(Object obj) {
        if (obj != null && obj instanceof RepositoryFileEditorInput) {
            return this.resource.equals(((RepositoryFileEditorInput)obj).resource);
        }
        return false;
    }
    
	public IPath getPath() {
		return this.storage.getTemporaryPath();
	}

}
