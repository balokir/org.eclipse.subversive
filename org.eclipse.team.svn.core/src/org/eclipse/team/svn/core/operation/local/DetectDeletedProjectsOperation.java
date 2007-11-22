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

package org.eclipse.team.svn.core.operation.local;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.client.ISVNConnector;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;

/**
 * The operation will detect incoming deletions for projects in update set
 * 
 * @author Alexander Gurov
 */
public class DetectDeletedProjectsOperation extends AbstractWorkingCopyOperation implements IResourceProvider {
	protected ArrayList toUpdate = new ArrayList();
	protected ArrayList toDisconnect = new ArrayList();
	
	public DetectDeletedProjectsOperation(IResource []resources) {
		super("Operation.DetectDeletedProjects", resources);
	}

	public DetectDeletedProjectsOperation(IResourceProvider provider) {
		super("Operation.DetectDeletedProjects", provider);
	}

	public IResource []getResources() {
		return (IResource [])this.toUpdate.toArray(new IResource[this.toUpdate.size()]);
	}
	
	public IProject []getDeleted() {
		return (IProject [])this.toDisconnect.toArray(new IProject[this.toDisconnect.size()]);
	}
	
	public IConsoleStream getConsoleStream() {
		return null;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []resources = this.operableData();
		for (int i = 0; i < resources.length; i++) {
			final IResource current = resources[i];
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					if (current instanceof IProject) {
						DetectDeletedProjectsOperation.this.checkProject(monitor, (IProject)current);
					}
					else {
						DetectDeletedProjectsOperation.this.toUpdate.add(current);
					}
				}
			}, monitor, resources.length);
		}
		for (Iterator it = this.toUpdate.iterator(); it.hasNext(); ) {
			IResource resource = (IResource)it.next();
			if (this.toDisconnect.contains(resource.getProject())) {
				it.remove();
			}
		}
	}

	protected void checkProject(IProgressMonitor monitor, IProject project) throws Exception {
		IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(project);
		IRepositoryLocation location = remote.getRepositoryLocation();
		
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
			if (!remote.exists() && location.getRepositoryRoot().exists()) {
				this.toDisconnect.add(project);	
			}
			else {
				this.toUpdate.add(project);
			}
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}

}
