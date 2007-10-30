/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.local.management;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.core.IConnectedProjectInformation;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IResourceProvider;

/**
 * Finds projects related to the specified repository location
 * 
 * @author Alexander Gurov
 */
public class FindRelatedProjectsOperation extends AbstractNonLockingOperation implements IResourceProvider {
	protected IRepositoryLocation location;
	protected List resources;
	protected Set exceptProjects;
	
	public FindRelatedProjectsOperation(IRepositoryLocation location) {
		this(location, null);
	}
	
	public FindRelatedProjectsOperation(IRepositoryLocation location, IProject []exceptProjects) {
		super("Operation.FindRelatedProjects");
		this.location = location;
		if (exceptProjects != null) {
			this.exceptProjects = new HashSet(Arrays.asList(exceptProjects));
		}
	}

	public IResource []getResources() {
		return this.resources == null ? new IResource[0] : (IResource [])this.resources.toArray(new IResource[this.resources.size()]);
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		this.resources = new ArrayList();
		IProject []projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length && !monitor.isCanceled(); i++) {
			final IProject current = projects[i];
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					IConnectedProjectInformation provider = (IConnectedProjectInformation)RepositoryProvider.getProvider(current, SVNTeamPlugin.NATURE_ID);
					if (provider != null && 
						(FindRelatedProjectsOperation.this.exceptProjects == null || !FindRelatedProjectsOperation.this.exceptProjects.contains(current)) &&
						provider.getRepositoryLocation() == FindRelatedProjectsOperation.this.location) {
						FindRelatedProjectsOperation.this.resources.add(current);
					}
				}
			}, monitor, projects.length);
		}
	}

}
