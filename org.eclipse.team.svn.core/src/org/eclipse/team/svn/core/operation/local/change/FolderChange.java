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

package org.eclipse.team.svn.core.operation.local.change;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.resource.ILocalFolder;
import org.eclipse.team.svn.core.resource.ILocalResource;

/**
 * Local folder change store
 * 
 * @author Alexander Gurov
 */
public class FolderChange extends ResourceChange {
    protected ResourceChange []children;

    public FolderChange(ResourceChange parent, ILocalFolder local, boolean needsTemporary) throws Exception {
        super(parent, local, needsTemporary);
        if (needsTemporary) {
            this.tmp.mkdir();
        }
        ILocalResource []tmpChildren = local.getChildren();
        this.children = new ResourceChange[tmpChildren.length];
        try {
            for (int i = 0; i < tmpChildren.length; i++) {
                this.children[i] = ResourceChange.wrapLocalResource(this, tmpChildren[i], needsTemporary);
            }
        }
        catch (Exception ex) {
    		this.disposeChangeModel(new NullProgressMonitor());
    		throw ex;
        }
    }

    public ResourceChange []getChildren() {
        return this.children;
    }

    protected void preTraverse(IResourceChangeVisitor visitor, int depth, IActionOperationProcessor processor, IProgressMonitor monitor) throws Exception {
    	if (depth != IResource.DEPTH_ZERO) {
    		int nextDepth = depth == IResource.DEPTH_ONE ? IResource.DEPTH_ZERO : IResource.DEPTH_INFINITE;
        	for (int i = 0; i < this.children.length && !monitor.isCanceled(); i++) {
        		this.children[i].preTraverse(visitor, nextDepth, processor, monitor);
        	}
    	}
    	visitor.preVisit(this, processor, monitor);
    }
    
    protected void postTraverse(IResourceChangeVisitor visitor, int depth, IActionOperationProcessor processor, IProgressMonitor monitor) throws Exception {
    	visitor.postVisit(this, processor, monitor);
    	if (depth != IResource.DEPTH_ZERO) {
    		int nextDepth = depth == IResource.DEPTH_ONE ? IResource.DEPTH_ZERO : IResource.DEPTH_INFINITE;
        	for (int i = 0; i < this.children.length && !monitor.isCanceled(); i++) {
        		this.children[i].postTraverse(visitor, nextDepth, processor, monitor);
        	}
    	}
    }
    
}
