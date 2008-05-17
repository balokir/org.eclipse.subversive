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

import org.eclipse.team.svn.ui.repository.RepositoryTreeViewer;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchContentProvider;

/**
 * Repository content provider 
 * 
 * @author Alexander Gurov
 */
public class RepositoryContentProvider extends WorkbenchContentProvider {
	protected RepositoryTreeViewer repositoryTree;
	protected IRepositoryContentFilter filter;

	public RepositoryContentProvider(RepositoryTreeViewer repositoryTree) {
		this.repositoryTree = repositoryTree;
	}
	
	public IRepositoryContentFilter getFilter() {
		return this.filter;
	}
	
	public void setFilter(IRepositoryContentFilter filter) {
		this.filter = filter;
	}

	public boolean hasChildren(Object element) {
		IWorkbenchAdapter adapter = this.getAdapter(element);
		if (adapter instanceof IParentTreeNode) {
			return ((IParentTreeNode)adapter).hasChildren();
		}
		return false;
	}
	
	public Object []getChildren(Object parentElement) {
		IWorkbenchAdapter adapter = this.getAdapter(parentElement);
		if (adapter instanceof IParentTreeNode) {
			if (adapter instanceof IResourceTreeNode) {
				((IResourceTreeNode)adapter).setViewer(this.repositoryTree);
			}
			ArrayList<Object> filtered = new ArrayList<Object>();
			Object []children = adapter.getChildren(parentElement);
			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					if (this.filter == null || this.filter.accept(children[i])) {
						if (children[i] instanceof IResourceTreeNode) {
							((IResourceTreeNode)children[i]).setViewer(this.repositoryTree);
						}
						filtered.add(children[i]);
					}
				}
			}
			return filtered.toArray();
		}
		return new Object[0];
	}
	
}
