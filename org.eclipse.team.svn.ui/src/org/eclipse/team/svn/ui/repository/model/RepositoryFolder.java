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

package org.eclipse.team.svn.ui.repository.model;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.ui.operation.GetRemoteFolderChildrenOperation;
import org.eclipse.team.svn.ui.utility.DefaultOperationWrapperFactory;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Repository folder node representation 
 * 
 * @author Alexander Gurov
 */
public class RepositoryFolder extends RepositoryResource implements IParentTreeNode {
	protected GetRemoteFolderChildrenOperation childrenOp;
	
	public RepositoryFolder(RepositoryResource parent, IRepositoryResource resource) {
		super(parent, resource);
	}

	public void refresh() {
		super.refresh();
		this.childrenOp = null;
	}
	
	public boolean hasChildren() {
		return true;
	}
	
	public Object []getChildren(Object o) {
		final IRepositoryContainer container = (IRepositoryContainer)this.resource;
		
		if (this.childrenOp != null) {
			Object []retVal = RepositoryFolder.wrapChildren(this, this.childrenOp.getChildren(), this.childrenOp);
			return retVal == null ? new Object[] {this.childrenOp.getExecutionState() != IActionOperation.ERROR ? (Object)new RepositoryPending(this) : new RepositoryError(this.childrenOp.getStatus())} : retVal;
		}
		else {
			this.childrenOp = new GetRemoteFolderChildrenOperation(container);
			
			if (!((IRepositoryContainer)this.resource).isChildrenCached()) {
				CompositeOperation op = new CompositeOperation(this.childrenOp.getId());
				op.add(this.childrenOp);
				op.add(this.getRefreshOperation(this.getViewer()));

				UIMonitorUtility.doTaskScheduled(op, new DefaultOperationWrapperFactory() {
	                public IActionOperation getLogged(IActionOperation operation) {
	            		return new LoggedOperation(operation);
	                }
	            });
				
				return new Object[] {new RepositoryPending(this)};
			}
			
			UIMonitorUtility.doTaskBusyDefault(this.childrenOp);
			
			return RepositoryFolder.wrapChildren(this, this.childrenOp.getChildren(), this.childrenOp);
		}
	}
	
	public Object []peekChildren(Object o) {
		if (this.childrenOp == null) {
			return this.getChildren(o);
		}
		else  {
			Object []retVal = RepositoryFolder.wrapChildren(this, this.childrenOp.getChildren(), this.childrenOp);
			return retVal == null ? new Object[] {this.childrenOp.getExecutionState() != IActionOperation.ERROR ? (Object)new RepositoryPending(this) : new RepositoryError(this.childrenOp.getStatus())} : retVal;
		}
	}
	
	public static RepositoryResource []wrapChildren(RepositoryResource parent, IRepositoryResource []resources, GetRemoteFolderChildrenOperation childrenOp) {
		if (resources == null) {
			return null;
		}
		RepositoryResource []wrappers = new RepositoryResource[resources.length];
		for (int i = 0; i < resources.length; i++) {
			wrappers[i] = RepositoryFolder.wrapChild(parent, resources[i]);
			if (childrenOp != null) {
				String externalsName = childrenOp.getExternalsName(resources[i]);
				if (externalsName != null) {
					wrappers[i].setLabel(externalsName);
					wrappers[i].setExternals(true);
				}
			}
		}
		return wrappers;
	}
	
	public static RepositoryResource wrapChild(RepositoryResource parent, IRepositoryResource resource) {
		if (resource instanceof IRepositoryRoot) {
			IRepositoryRoot tmp = (IRepositoryRoot)resource;
			switch (tmp.getKind()) {
				case IRepositoryRoot.KIND_TRUNK: {
					return new RepositoryTrunk(parent, tmp);
				}
				case IRepositoryRoot.KIND_BRANCHES: {
					return new RepositoryBranches(parent, tmp);
				}
				case IRepositoryRoot.KIND_TAGS: {
					return new RepositoryTags(parent, tmp);
				}
				default: {
					return new RepositoryRoot(parent, tmp);
				}
			}
		}
		else {
			return resource instanceof IRepositoryFile ? (RepositoryResource)new RepositoryFile(parent, resource) : new RepositoryFolder(parent, resource);
		}
	}
	
	protected ImageDescriptor getImageDescriptorImpl() {
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
	}
	
}
