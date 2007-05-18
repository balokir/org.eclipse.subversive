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

package org.eclipse.team.svn.ui.operation;

import java.util.Comparator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Load folder children. Used in asynchronous repository view refresh.
 * 
 * @author Alexander Gurov
 */
public class GetRemoteFolderChildrenOperation extends AbstractNonLockingOperation {
	protected IRepositoryContainer parent;
	protected IRepositoryResource []children;
	protected boolean sortChildren;

	public GetRemoteFolderChildrenOperation(IRepositoryContainer parent) {
		this(parent, true);
	}

	public GetRemoteFolderChildrenOperation(IRepositoryContainer parent, boolean sortChildren) {
		super("Operation.GetRemoteChildren");
		this.parent = parent;
		this.sortChildren = sortChildren;
	}

	public IRepositoryResource[] getChildren() {
		return this.children;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRepositoryResource []tmp = this.parent.getChildren();
		if (this.sortChildren) {
			FileUtility.sort(tmp, new Comparator() {
				public int compare(Object o1, Object o2) {
					IRepositoryResource first = (IRepositoryResource)o1;
					IRepositoryResource second = (IRepositoryResource)o2;
					boolean firstContainer = first instanceof IRepositoryContainer;
					boolean secondContainer = second instanceof IRepositoryContainer;
					if (firstContainer && secondContainer) {
						boolean firstRoot = first instanceof IRepositoryRoot;
						boolean secondRoot = second instanceof IRepositoryRoot;
						return firstRoot == secondRoot ? (firstRoot ? this.compareRoots(((IRepositoryRoot)first).getKind(), ((IRepositoryRoot)second).getKind()) : first.getUrl().compareTo(second.getUrl())) : (firstRoot ? -1 : 1);
					}
					return firstContainer == secondContainer ? first.getUrl().compareTo(second.getUrl()) : (firstContainer ? -1 : 1);
				}
				
				public int compareRoots(int firstKind, int secondKind) {
					return firstKind < secondKind ? -1 : 1;
				}
			});
		}
		this.children = tmp;
	}

}
