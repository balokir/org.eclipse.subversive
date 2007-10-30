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

package org.eclipse.team.svn.core.resource.events;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Basic "resources changed event" implementation 
 * 
 * @author Alexander Gurov
 */
public class ResourceStatesChangedEvent {
	public final IResource []resources;
	public final int depth;
	
	private IResource []fullSet;

	public ResourceStatesChangedEvent(IResource []resources, int depth) {
    	// notify in parent to child order
		FileUtility.reorder(this.resources = resources, true);
		this.depth = depth;
		if (this.depth == IResource.DEPTH_ZERO) {
			this.fullSet = this.resources;
		}
	}
	
	public IResource []getResourcesRecursivelly() {
		if (this.fullSet == null) {
			try {
				this.fullSet = ResourceStatesChangedEvent.collectResources(this.resources, this.depth);
				FileUtility.reorder(this.fullSet, true);
			} 
			catch (CoreException e) {
				this.fullSet = this.resources;
			}
		}
		return this.fullSet;
	}
	
	public boolean contains(IResource resource) {
		if (this.containsImpl(resource)) {
			return true;
		}
		if (this.depth != IResource.DEPTH_ZERO) {
			if (this.containsImpl(resource.getParent())) {
				return true;
			}
			if (this.depth != IResource.DEPTH_ONE) {
				IPath path = resource.getFullPath();
				for (int i = 0; i < this.resources.length; i++) {
					if (this.resources[i].getFullPath().isPrefixOf(path)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static IResource []collectResources(IResource []resources, int depth) throws CoreException {
    	if (depth == IResource.DEPTH_ZERO) {
    		return resources;
    	}
    	
		final List fullList = new ArrayList();
		for (int i = 0; i < resources.length; i++) {
    		FileUtility.visitNodes(resources[i], new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {
					if (FileUtility.isSVNInternals(resource)) {
						return false;
					}
					fullList.add(resource);
					return true;
				}
			}, depth);
		}
		return (IResource [])fullList.toArray(new IResource[fullList.size()]);
	}
	
	protected boolean containsImpl(IResource resource) {
		for (int i = 0; i < this.resources.length; i++) {
			if (this.resources[i].equals(resource)) {
				return true;
			}
		}
		return false;
	}
	
}
