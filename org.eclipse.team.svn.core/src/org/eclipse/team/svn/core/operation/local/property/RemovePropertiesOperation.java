/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Vladimir Bykov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.local.property;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.local.AbstractWorkingCopyOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Commit operation implementation
 * 
 * @author Vladimir Bykov
 */
public class RemovePropertiesOperation extends AbstractWorkingCopyOperation {
	protected SVNProperty []data;
	protected boolean isRecursive;
	
	public RemovePropertiesOperation(IResource []resources, SVNProperty []data, boolean isRecursive) {
		super("Operation.RemoveProperties", resources);
		this.data = data;
		this.isRecursive = isRecursive; 
	}
	
	public RemovePropertiesOperation(IResourceProvider resourceProvider, SVNProperty []data, boolean isRecursive) {
		super("Operation.RemoveProperties", resourceProvider);
		this.data = data;
		this.isRecursive = isRecursive; 
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []resources = this.operableData();
		
		if (this.isRecursive) {
			resources = FileUtility.shrinkChildNodes(resources);
		}
		
		for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
			final IResource resource = resources[i];
			
			IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(resource);
			final ISVNConnector proxy = location.acquireSVNProxy();
			
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					RemovePropertiesOperation.this.processResource(proxy, resource, monitor);
				}
			}, monitor, resources.length);
			
			location.releaseSVNProxy(proxy);
		}
	}
	
	protected void processResource(final ISVNConnector proxy, IResource resource, IProgressMonitor monitor) {
		ProgressMonitorUtility.setTaskInfo(monitor, this, resource.getFullPath().toString());
		final String wcPath = FileUtility.getWorkingCopyPath(resource);
		
		for (int i = 0; i < this.data.length && !monitor.isCanceled(); i++) {
		    final SVNProperty current = this.data[i];
		    this.protectStep(new IUnprotectedOperation() {
                public void run(IProgressMonitor monitor) throws Exception {
        			proxy.removeProperty(wcPath, current.name, RemovePropertiesOperation.this.isRecursive ? Depth.INFINITY : Depth.EMPTY, null, new SVNProgressMonitor(RemovePropertiesOperation.this, monitor, null));
                }
            }, monitor, this.data.length);
		}
	}
	
}
