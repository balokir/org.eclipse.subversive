/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.mapping;

import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.core.subscribers.ChangeSet;

public class SVNChangeSetResourceMapping extends ResourceMapping {

	protected ChangeSet changeSet;
	
	public SVNChangeSetResourceMapping(ChangeSet changeSet ) {
		this.changeSet = changeSet;
	}

	public Object getModelObject() {
		return this.changeSet;
	}

	public String getModelProviderId() {
		return SVNChangeSetModelProvider.ID;
	}

	public IProject[] getProjects() {
		HashSet<IProject> projects = new HashSet<IProject>();
		IResource[] resources = this.changeSet.getResources();
		for (int i = 0; i < resources.length; i++) {
			projects.add(resources[i].getProject());
		}
		return projects.toArray(new IProject[projects.size()]);
	}

	public ResourceTraversal[] getTraversals(ResourceMappingContext context, IProgressMonitor monitor) throws CoreException {
		IResource[] resources = this.changeSet.getResources();
		if (resources.length == 0) {
			return new ResourceTraversal[0];
		}
		return new ResourceTraversal[] {new ResourceTraversal(resources, IResource.DEPTH_ZERO, IResource.NONE)};
	}
}
