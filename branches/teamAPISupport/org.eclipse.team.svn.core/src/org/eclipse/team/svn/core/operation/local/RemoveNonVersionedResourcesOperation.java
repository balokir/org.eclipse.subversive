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
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.local.change.IActionOperationProcessor;
import org.eclipse.team.svn.core.operation.local.change.ResourceChange;
import org.eclipse.team.svn.core.operation.local.change.visitors.RemoveNonVersionedVisitor;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Remove all non-versioned resources in subtree. Should be used in "Override And Update" action
 * 
 * @author Alexander Gurov
 */
public class RemoveNonVersionedResourcesOperation extends AbstractWorkingCopyOperation implements IActionOperationProcessor {
	protected boolean addedAlso;
	
    public RemoveNonVersionedResourcesOperation(IResource[] resources, boolean addedAlso) {
        super("Operation.RemoveNonSVN", resources);
		this.addedAlso = addedAlso;
    }

    public RemoveNonVersionedResourcesOperation(IResourceProvider provider, boolean addedAlso) {
        super("Operation.RemoveNonSVN", provider);
		this.addedAlso = addedAlso;
    }

    protected void runImpl(IProgressMonitor monitor) throws Exception {
        IResource []resources = FileUtility.shrinkChildNodes(this.operableData());
        for (int i = 0; i < resources.length; i++) {
            final IResource current = resources[i];
            this.protectStep(new IUnprotectedOperation() {
                public void run(IProgressMonitor monitor) throws Exception {
                    ResourceChange change = ResourceChange.wrapLocalResource(null, SVNRemoteStorage.instance().asLocalResourceAccessible(current), false);
                    if (change != null) {
                    	change.traverse(new RemoveNonVersionedVisitor(RemoveNonVersionedResourcesOperation.this.addedAlso), IResource.DEPTH_INFINITE, RemoveNonVersionedResourcesOperation.this, monitor);
                    }
                }
            }, monitor, resources.length);
        }
    }

    public void doOperation(IActionOperation op, IProgressMonitor monitor) {
	    this.reportStatus(op.run(monitor).getStatus());
    }
    
}
