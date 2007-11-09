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

package org.eclipse.team.svn.core.operation.local.management;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.SVNTeamProjectMapper;
import org.eclipse.team.svn.core.client.SVNEntryStatus;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.UnreportableException;
import org.eclipse.team.svn.core.operation.local.AbstractWorkingCopyOperation;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Reconnect project that has SVN metainformation
 * 
 * @author Alexander Gurov
 */
public class ReconnectProjectOperation extends AbstractWorkingCopyOperation {
	protected IRepositoryLocation location;

	public ReconnectProjectOperation(IProject []projects, IRepositoryLocation location) {
		super("Operation.Reconnect", projects);
		this.location = location;
	}
	
	public ReconnectProjectOperation(IResourceProvider provider, IRepositoryLocation location) {
		super("Operation.Reconnect", provider);
		this.location = location;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []resources = this.operableData();
		
		for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
			final IProject project = (IProject)resources[i];
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					SVNEntryStatus st = SVNUtility.getSVNInfoForNotConnected(project);
					if (st == null) {
						throw new UnreportableException(SVNTeamPlugin.instance().getResource("Error.NonSVNPath"));
					}
					IRepositoryContainer remote = ReconnectProjectOperation.this.location.asRepositoryContainer(SVNUtility.decodeURL(st.url), false);
					SVNTeamProjectMapper.map(project, remote);
				}
			}, monitor, resources.length);
		}
	}

}
