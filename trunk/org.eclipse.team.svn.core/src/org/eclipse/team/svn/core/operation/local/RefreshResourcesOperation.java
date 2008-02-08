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

import java.util.HashSet;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNResourceRuleFactory;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.resource.events.ResourceStatesChangedEvent;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Refresh Eclipse IDE workspace resources operation implementation
 * 
 * @author Alexander Gurov
 */
public class RefreshResourcesOperation extends AbstractWorkingCopyOperation {
	public static int REFRESH_CACHE = 0;
	public static int REFRESH_CHANGES = 1;
	public static int REFRESH_ALL = 2;
	
	protected int depth;
	protected int refreshType;
	
	public RefreshResourcesOperation(IResource []resources) {
		this(resources, IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_CHANGES);
	}
	
	public RefreshResourcesOperation(IResource []resources, int depth, int refreshType) {
		super("Operation.RefreshResources", resources);
		this.depth = depth;
		this.refreshType = refreshType;
	}

	public RefreshResourcesOperation(IResourceProvider provider) {
		this(provider, IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_CHANGES);
	}
	
	public RefreshResourcesOperation(IResourceProvider provider, int depth, int refreshType) {
		super("Operation.RefreshResources", provider);
		this.depth = depth;
		this.refreshType = refreshType;
	}

    public ISchedulingRule getSchedulingRule() {
    	ISchedulingRule rule = super.getSchedulingRule();
    	if (rule instanceof IWorkspaceRoot) {
    		return rule;
    	}
    	IResource []resources = this.operableData();
    	HashSet ruleSet = new HashSet();
    	for (int i = 0; i < resources.length; i++) {
			ruleSet.add(SVNResourceRuleFactory.INSTANCE.refreshRule(resources[i]));
    	}
    	return new MultiRule((IResource [])ruleSet.toArray(new IResource[ruleSet.size()]));
    }

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []resources = this.operableData();
		IResource []original = resources;
		if (this.depth == IResource.DEPTH_INFINITE) {
			resources = FileUtility.shrinkChildNodes(resources);
		}
		
		if (this.refreshType != RefreshResourcesOperation.REFRESH_CACHE) {
			// simply refresh workspace resources in order to avoid double-caching of a changed resources
			for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
				ProgressMonitorUtility.setTaskInfo(monitor, this, resources[i].getName());
				final IResource resource = resources[i];
				this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws Exception {
						if (resource instanceof IContainer && RefreshResourcesOperation.this.depth != IResource.DEPTH_INFINITE) {
							RefreshResourcesOperation.this.refreshMetaInfo((IContainer)resource);
						}
						if (!(resource instanceof IProject)) {
							IContainer parent = resource.getParent();
							RefreshResourcesOperation.this.refreshMetaInfo(parent);
							// parent it self cannot be refreshed any case due to validation of scheduling rules...
						}
						RefreshResourcesOperation.this.doRefresh(resource, RefreshResourcesOperation.this.depth);
					}
				}, monitor, resources.length);
			}
		}
		
		if (RefreshResourcesOperation.this.refreshType != RefreshResourcesOperation.REFRESH_CHANGES) {
			SVNRemoteStorage.instance().refreshLocalResources(resources, this.depth);
			
			IResource []roots = FileUtility.getPathNodes(resources);
			SVNRemoteStorage.instance().fireResourceStatesChangedEvent(new ResourceStatesChangedEvent(roots, IResource.DEPTH_ZERO, ResourceStatesChangedEvent.PATH_NODES));
			SVNRemoteStorage.instance().fireResourceStatesChangedEvent(new ResourceStatesChangedEvent(original, this.depth, ResourceStatesChangedEvent.CHANGED_NODES));
		}
	}
	
	protected void refreshMetaInfo(IContainer container) throws Exception {
		IResource metaInfo = container.findMember(SVNUtility.getSVNFolderName());
		if (metaInfo != null) {
			this.doRefresh(metaInfo, IResource.DEPTH_INFINITE);
		}
	}
	
	protected void doRefresh(final IResource resource, final int depth) throws Exception {
    	ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				resource.refreshLocal(depth, monitor);
				FileUtility.findAndMarkSVNInternals(resource, true);
			}
		}, null, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
	}

}
