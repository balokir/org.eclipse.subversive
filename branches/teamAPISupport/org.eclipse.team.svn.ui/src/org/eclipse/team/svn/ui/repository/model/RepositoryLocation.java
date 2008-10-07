/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.repository.model;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.repository.RepositoryTreeViewer;

/**
 * Repository location node representation 
 * 
 * @author Alexander Gurov
 */
public class RepositoryLocation extends RepositoryFictiveNode implements IParentTreeNode, IDataTreeNode, IResourceTreeNode {
	protected IRepositoryLocation location;
	protected Object []children;
	protected RepositoryFolder locationRoot;
	
	public RepositoryLocation(IRepositoryLocation location) {
		this.location = location;
		this.refresh();
	}

    public void setViewer(RepositoryTreeViewer repositoryTree) {
    	this.locationRoot.setViewer(repositoryTree);
    }
	
	public RepositoryResource getResourceWrapper() {
		return this.locationRoot;
	}
	
	public IRepositoryResource getRepositoryResource() {
		return this.location.getRoot();
	}
	
	public void refresh() {
		this.children = null;
		if (this.locationRoot == null || !this.locationRoot.getRepositoryResource().getUrl().equals(this.location.getUrl())) {
			this.locationRoot = new RepositoryFolder(null, this.location.getRoot()) {
				protected RefreshOperation getRefreshOperation(RepositoryTreeViewer viewer) {
					return new RefreshOperation(viewer) {
						protected void runImpl(IProgressMonitor monitor) throws Exception {
							// TODO rework this using cancellation manager in order to make it thread-safe...
							if (this.viewer != null && !this.viewer.getControl().isDisposed()) {
								this.viewer.refresh(RepositoryLocation.this, null, false);
							}
						}
					};
				}
			};
		}
		else {
			this.locationRoot.refresh();
		}
	}
	
	public IRepositoryLocation getRepositoryLocation() {
		return this.location;
	}
	
	public Object getData() {
		return this.location;
	}
	
	public String getLabel(Object o) {
		return this.location.getLabel();
	}

	public boolean hasChildren() {
		return true;
	}

	public Object []getChildren(Object o) {
		if (this.children == null) {
			ArrayList<Object> list = new ArrayList<Object>(Arrays.asList(this.locationRoot.getChildren(o)));
			list.add(new RepositoryRoot(null, this.location.getRepositoryRoot()));
			list.add(new RepositoryRevisions(this.location));
			if (list.get(0) instanceof RepositoryPending) {
				return list.toArray(); 
			}
			this.children = list.toArray();
	    }
	    return this.children;
	}
	
	public ImageDescriptor getImageDescriptor(Object object) {
		return SVNTeamUIPlugin.instance().getImageDescriptor("icons/objects/repository.gif");
	}

	public boolean equals(Object obj) {
		if (obj != null && obj instanceof RepositoryLocation) {
			return ((RepositoryLocation)obj).location.equals(this.location);
		}
		return super.equals(obj);
	}
	
}
