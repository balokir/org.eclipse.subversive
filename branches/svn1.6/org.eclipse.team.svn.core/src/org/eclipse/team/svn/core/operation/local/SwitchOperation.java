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

import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.core.IConnectedProjectInformation;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.remote.AbstractRepositoryOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Switch working copy base url operation implementation
 * 
 * @author Alexander Gurov
 */
public class SwitchOperation extends AbstractRepositoryOperation {
	protected IResource []resources;
	protected int depth;

	public SwitchOperation(IResource []resources, IRepositoryResourceProvider destination, int depth) {
		super("Operation_Switch", destination); //$NON-NLS-1$
		this.resources = resources;
		this.depth = depth;
	}

	public SwitchOperation(IResource []resources, IRepositoryResource []destination, int depth) {
		super("Operation_Switch", destination); //$NON-NLS-1$
		this.resources = resources;
		this.depth = depth;
	}
	
	public int getOperationWeight() {
		return 19;
	}
	
	public ISchedulingRule getSchedulingRule() {
    	HashSet<IResource> ruleSet = new HashSet<IResource>();
    	for (int i = 0; i < this.resources.length; i++) {
			ruleSet.add(this.resources[i] instanceof IProject ? this.resources[i] : this.resources[i].getParent());
    	}
    	return new MultiRule(ruleSet.toArray(new IResource[ruleSet.size()]));
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRepositoryResource []destinations = this.operableData();
		for (int i = 0; i < this.resources.length; i++) {
			final IResource resource = this.resources[i];
			final IRepositoryResource destination = destinations[i];
			final IRepositoryLocation location = destination.getRepositoryLocation();
			final ISVNConnector proxy = location.acquireSVNProxy();
			
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					String wcPath = FileUtility.getWorkingCopyPath(resource);
					SwitchOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn switch \"" + destination.getUrl() + "\" \"" + FileUtility.normalizePath(wcPath) + "\" -r " + destination.getSelectedRevision() + SVNUtility.getDepthArg(SwitchOperation.this.depth) + FileUtility.getUsernameParam(location.getUsername()) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					proxy.doSwitch(wcPath, SVNUtility.getEntryRevisionReference(destination), SwitchOperation.this.depth,
							ISVNConnector.Options.NONE, new SVNProgressMonitor(SwitchOperation.this, monitor, null));
					
					if (resource instanceof IProject) {
						IConnectedProjectInformation provider = (IConnectedProjectInformation)RepositoryProvider.getProvider((IProject)resource);
						provider.switchResource(destination);
					}
				}
			}, monitor, this.resources.length);
			
			location.releaseSVNProxy(proxy);		
		}
	}

}
