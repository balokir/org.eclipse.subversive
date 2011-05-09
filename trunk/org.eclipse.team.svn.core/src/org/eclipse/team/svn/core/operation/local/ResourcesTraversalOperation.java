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

package org.eclipse.team.svn.core.operation.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.local.change.IActionOperationProcessor;
import org.eclipse.team.svn.core.operation.local.change.IResourceChangeVisitor;
import org.eclipse.team.svn.core.operation.local.change.ResourceChange;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Specified resources traversal operation.
 * Usage sample: new ResourcesTraversalOperation(resources, new RemoveNonVersionedVisitor(true), IResource.DEPTH_INFINITE)
 * 
 * @author Alexander Gurov
 */
public class ResourcesTraversalOperation extends AbstractWorkingCopyOperation implements IActionOperationProcessor {
	protected IResourceChangeVisitor visitor;
	protected int depth;
	
	//FIXME externalize operation name, to do so first NLS support should be reworked: SVNMessages.class should be passed transparently.
    public ResourcesTraversalOperation(IResource[] resources, IResourceChangeVisitor visitor, int depth) {
        super("Operation_RemoveNonSVN", SVNMessages.class, resources); //$NON-NLS-1$
		this.visitor = visitor;
		this.depth = depth;
    }

    public ResourcesTraversalOperation(IResourceProvider provider, IResourceChangeVisitor visitor, int depth) {
        super("Operation_RemoveNonSVN", SVNMessages.class, provider); //$NON-NLS-1$
		this.visitor = visitor;
		this.depth = depth;
    }

    protected void runImpl(IProgressMonitor monitor) throws Exception {
        IResource []resources = this.operableData();
        resources = this.depth == IResource.DEPTH_ZERO ? resources : FileUtility.shrinkChildNodes(resources);
        for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
            final IResource current = resources[i];
            this.protectStep(new IUnprotectedOperation() {
                public void run(IProgressMonitor monitor) throws Exception {
                    ResourceChange change = ResourceChange.wrapLocalResource(null, SVNRemoteStorage.instance().asLocalResourceAccessible(current), false);
                    if (change != null) {
                    	change.traverse(ResourcesTraversalOperation.this.visitor, ResourcesTraversalOperation.this.depth, ResourcesTraversalOperation.this, monitor);
                    }
                }
            }, monitor, resources.length);
        }
    }

    public void doOperation(IActionOperation op, IProgressMonitor monitor) {
	    this.reportStatus(op.run(monitor).getStatus());
    }
    
}
