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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.core.IConnectedProjectInformation;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNConflictDetectionProgressMonitor;
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
public class SwitchOperation extends AbstractRepositoryOperation implements IUnresolvedConflictDetector {
	protected IResource []resources;
	protected int depth;
	protected long options;
	protected UnresolvedConflictDetectorHelper conflictDetectorHelper; 

	public SwitchOperation(IResource []resources, IRepositoryResourceProvider destination, int depth, boolean isStickyDepth, boolean ignoreExternals) {
		this(resources, destination, depth, 
			(ignoreExternals ? ISVNConnector.Options.IGNORE_EXTERNALS : ISVNConnector.Options.NONE) | 
			(isStickyDepth ? ISVNConnector.Options.DEPTH_IS_STICKY : ISVNConnector.Options.NONE));
	}

	public SwitchOperation(IResource []resources, IRepositoryResource []destination, int depth, boolean isStickyDepth, boolean ignoreExternals) {
		this(resources, destination, depth, 
			(ignoreExternals ? ISVNConnector.Options.IGNORE_EXTERNALS : ISVNConnector.Options.NONE) | 
			(isStickyDepth ? ISVNConnector.Options.DEPTH_IS_STICKY : ISVNConnector.Options.NONE));
	}
	
	public SwitchOperation(IResource []resources, IRepositoryResourceProvider destination, int depth, long options) {
		super("Operation_Switch", SVNMessages.class, destination); //$NON-NLS-1$
		this.resources = resources;
		this.depth = depth;
		this.options = options & ISVNConnector.CommandMasks.SWITCH;
		this.conflictDetectorHelper = new UnresolvedConflictDetectorHelper();
	}

	public SwitchOperation(IResource []resources, IRepositoryResource []destination, int depth, long options) {
		super("Operation_Switch", SVNMessages.class, destination); //$NON-NLS-1$
		this.resources = resources;
		this.depth = depth;
		this.options = options & ISVNConnector.CommandMasks.SWITCH;
		this.conflictDetectorHelper = new UnresolvedConflictDetectorHelper();
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
					SwitchOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn switch \"" + destination.getUrl() + "\" \"" + FileUtility.normalizePath(wcPath) + "\" -r " + destination.getSelectedRevision() + SVNUtility.getIgnoreExternalsArg(SwitchOperation.this.options) + SVNUtility.getDepthArg(SwitchOperation.this.depth, SwitchOperation.this.options) + FileUtility.getUsernameParam(location.getUsername()) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					proxy.switchTo(wcPath, SVNUtility.getEntryRevisionReference(destination), SwitchOperation.this.depth,
							SwitchOperation.this.options, new ConflictDetectionProgressMonitor(SwitchOperation.this, monitor, null));
					
					if (resource instanceof IProject) {
						IConnectedProjectInformation provider = (IConnectedProjectInformation)RepositoryProvider.getProvider((IProject)resource);
						provider.switchResource(destination);
					}
				}
			}, monitor, this.resources.length);
			
			location.releaseSVNProxy(proxy);		
		}
	}

    public void setUnresolvedConflict(boolean hasUnresolvedConflict) {
		this.conflictDetectorHelper.setUnresolvedConflict(hasUnresolvedConflict);
	}	
    
    public boolean hasUnresolvedConflicts() {
        return this.conflictDetectorHelper.hasUnresolvedConflicts();
    }
    
    public String getMessage() {
    	return this.conflictDetectorHelper.getMessage();
    }
    
    public IResource []getUnprocessed() {
		return this.conflictDetectorHelper.getUnprocessed();
    }

	public IResource []getProcessed() {
		return this.conflictDetectorHelper.getProcessed();
	}
	
	protected void defineInitialResourceSet(IResource []resources) {
		this.conflictDetectorHelper.defineInitialResourceSet(resources);
	}
	
	public void addUnprocessed(IResource unprocessed) {
		this.conflictDetectorHelper.addUnprocessed(unprocessed);
	}

	public void setConflictMessage(String message) {
		this.conflictDetectorHelper.setConflictMessage(message);		
	}
	
	public void removeProcessed(IResource resource) {
		this.conflictDetectorHelper.removeProcessed(resource);
	}
	
	protected class ConflictDetectionProgressMonitor extends SVNConflictDetectionProgressMonitor {
		public ConflictDetectionProgressMonitor(IActionOperation parent, IProgressMonitor monitor, IPath root) {
			super(parent, monitor, root);
		}
		protected void processConflict(ItemState state) {
			SwitchOperation.this.setUnresolvedConflict(true);
		}
	}
}
