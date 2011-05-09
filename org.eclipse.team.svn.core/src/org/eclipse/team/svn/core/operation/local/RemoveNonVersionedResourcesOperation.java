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
import org.eclipse.team.svn.core.operation.local.change.visitors.RemoveNonVersionedVisitor;
import org.eclipse.team.svn.core.resource.IResourceProvider;

/**
 * Remove all non-versioned resources in subtree. Should be used in "Override And Update" action.
 * Legacy code, use new ResourcesTraversalOperation(resources, new RemoveNonVersionedVisitor(true), IResource.DEPTH_INFINITE) instead.
 * 
 * @author Alexander Gurov
 */
public class RemoveNonVersionedResourcesOperation extends ResourcesTraversalOperation {
    public RemoveNonVersionedResourcesOperation(IResource[] resources, boolean addedAlso) {
        super(resources, new RemoveNonVersionedVisitor(addedAlso), IResource.DEPTH_INFINITE);
    }

    public RemoveNonVersionedResourcesOperation(IResourceProvider provider, boolean addedAlso) {
        super(provider, new RemoveNonVersionedVisitor(addedAlso), IResource.DEPTH_INFINITE);
    }

}
