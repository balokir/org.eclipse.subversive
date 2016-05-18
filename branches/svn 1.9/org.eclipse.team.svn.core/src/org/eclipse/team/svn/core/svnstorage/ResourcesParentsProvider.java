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

package org.eclipse.team.svn.core.svnstorage;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.team.svn.core.resource.IResourceProvider;

public class ResourcesParentsProvider implements IResourceProvider {
	
	protected IResource[] resources;
	protected IResourceProvider provider;
	
	public ResourcesParentsProvider(IResource[] resources) {
		this.resources = resources;
	}
	
	public ResourcesParentsProvider(IResourceProvider provider) {
		this.provider = provider;
	}

	public IResource[] getResources() {
		IResource[] resources = this.operableData();
		if (resources == null) {
			return new IResource[1];
		}
		HashSet resourcesWithParents = new HashSet();
		resourcesWithParents.addAll(Arrays.asList(resources));
		for (int i = 0; i < resources.length; i++) {
			IResource parent = resources[i].getParent();
			if (parent != null && !(parent instanceof IWorkspaceRoot)) {
				resourcesWithParents.add(parent);
			}
		}
		return (IResource[])resourcesWithParents.toArray(new IResource[resourcesWithParents.size()]);
	}
	
	protected IResource[] operableData() {
		return this.resources == null ? this.provider.getResources() : this.resources;
	}

}
