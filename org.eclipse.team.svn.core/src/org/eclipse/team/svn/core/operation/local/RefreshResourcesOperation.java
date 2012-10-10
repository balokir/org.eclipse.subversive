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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
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
		super("Operation_RefreshResources", SVNMessages.class, resources); //$NON-NLS-1$
		this.depth = depth;
		this.refreshType = refreshType;
	}

	public RefreshResourcesOperation(IResourceProvider provider) {
		this(provider, IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_CHANGES);
	}
	
	public RefreshResourcesOperation(IResourceProvider provider, int depth, int refreshType) {
		super("Operation_RefreshResources", SVNMessages.class, provider); //$NON-NLS-1$
		this.depth = depth;
		this.refreshType = refreshType;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []original = this.operableData();
		final IResource []resources = this.depth == IResource.DEPTH_INFINITE ? FileUtility.shrinkChildNodes(original) : original;
		
		if (this.refreshType != RefreshResourcesOperation.REFRESH_CACHE) {
	    	ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
						ProgressMonitorUtility.setTaskInfo(monitor, RefreshResourcesOperation.this, resources[i].getName());
						final IResource resource = resources[i];
						RefreshResourcesOperation.this.protectStep(new IUnprotectedOperation() {
							public void run(IProgressMonitor monitor) throws Exception {
								if (SVNUtility.isPriorToSVN17()) {
									if (resource.getType() != IResource.FILE && RefreshResourcesOperation.this.depth != IResource.DEPTH_INFINITE) {
										// if depth is set to "infinite", then meta info will be refreshed together with the resource itself
										RefreshResourcesOperation.this.refreshMetaInfo((IContainer)resource, monitor);
									}
									if (resource.getType() != IResource.PROJECT) {
										// refresh parent's meta info if exists
										RefreshResourcesOperation.this.refreshMetaInfo(resource.getParent(), monitor);
									}
								}
//								else {
//									// if the SVN meta info is present inside the project then it is in the project root or higher (we do not consider mixed working copies at the moment)
//									RefreshResourcesOperation.this.refreshMetaInfo(resource.getProject(), monitor);
//								}
								RefreshResourcesOperation.this.doRefresh(resource, RefreshResourcesOperation.this.depth, monitor);
							}
						}, monitor, resources.length);
					}
				}
			}, null, IWorkspace.AVOID_UPDATE, monitor);
		}
		
		// FIXME there could be event doubles when SVN 1.7 is used
		if (RefreshResourcesOperation.this.refreshType != RefreshResourcesOperation.REFRESH_CHANGES || !SVNUtility.isPriorToSVN17()) {
			SVNRemoteStorage.instance().refreshLocalResources(resources, this.depth);
			
			IResource []roots = FileUtility.getPathNodes(resources);
			SVNRemoteStorage.instance().fireResourceStatesChangedEvent(new ResourceStatesChangedEvent(roots, IResource.DEPTH_ZERO, ResourceStatesChangedEvent.PATH_NODES));
			SVNRemoteStorage.instance().fireResourceStatesChangedEvent(new ResourceStatesChangedEvent(original, this.depth, ResourceStatesChangedEvent.CHANGED_NODES));
		}
	}
	
	protected void refreshMetaInfo(IContainer container, IProgressMonitor monitor) throws CoreException {
		IResource metaInfo = container.findMember(SVNUtility.getSVNFolderName());
		if (metaInfo != null) {
			this.doRefresh(metaInfo, IResource.DEPTH_INFINITE, monitor);
		}
	}
	
	protected void doRefresh(final IResource resource, final int depth, IProgressMonitor monitor) throws CoreException {
		resource.refreshLocal(depth, monitor);
		FileUtility.findAndMarkSVNInternals(resource, true);
	}

}
