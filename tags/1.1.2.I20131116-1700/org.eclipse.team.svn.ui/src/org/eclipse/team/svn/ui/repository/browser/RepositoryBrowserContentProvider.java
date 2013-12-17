/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.repository.browser;

import org.eclipse.team.svn.ui.repository.model.IParentTreeNode;
import org.eclipse.team.svn.ui.repository.model.RepositoryError;
import org.eclipse.team.svn.ui.repository.model.RepositoryFictiveWorkingDirectory;
import org.eclipse.team.svn.ui.repository.model.RepositoryFolder;
import org.eclipse.team.svn.ui.repository.model.RepositoryPending;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Repository browser content provider
 *
 * @author Sergiy Logvin
 */
public class RepositoryBrowserContentProvider extends BaseWorkbenchContentProvider {
	public boolean hasChildren(Object element) {
		IWorkbenchAdapter adapter = this.getAdapter(element);
		if (adapter instanceof IParentTreeNode) {
			return ((IParentTreeNode)adapter).hasChildren();
		}				
		return false;
	}
	
	public Object[] getChildren(Object parentElement) {
		IWorkbenchAdapter adapter = this.getAdapter(parentElement);
		
		if (adapter instanceof RepositoryFolder) {
			RepositoryFolder folder = (RepositoryFolder)adapter;
			Object[] children = folder.peekChildren(parentElement);
			if (folder.getParent() == null) {
				return children;
			}
			if (children.length != 0 && children[0] != null &&
				(children[0] instanceof RepositoryError || children[0] instanceof RepositoryPending)) {
				return children;
			}
			Object[] childrenWithParent = new Object[children.length + 1];
			childrenWithParent[0] = new RepositoryFictiveWorkingDirectory(parentElement);
			System.arraycopy(children, 0, childrenWithParent, 1, children.length);
			return childrenWithParent;
		}
		return super.getChildren(parentElement);
	}
	
}
