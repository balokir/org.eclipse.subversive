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
import org.eclipse.team.svn.core.operation.IResourcePropertyProvider;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.local.AbstractWorkingCopyOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Set resource property operation
 * 
 * @author Vladimir Bykov
 */
public class SetPropertiesOperation extends AbstractWorkingCopyOperation {
	protected SVNProperty []propertyData;
	protected boolean isRecursive;
	protected IResourcePropertyProvider propertyProvider;
	
	public SetPropertiesOperation(IResource []resources, String name, byte []data, boolean isRecursive) {
		this(resources, new SVNProperty[] {new SVNProperty(name, new String(data))}, isRecursive);
	}
	
	public SetPropertiesOperation(IResource []resources, SVNProperty []data, boolean isRecursive) {
		super("Operation.SetProperties", resources);
		this.propertyData = data;
		this.isRecursive = isRecursive;
	}
	
	public SetPropertiesOperation(IResourceProvider resourceProvider, SVNProperty []data, boolean isRecursive) {
		super("Operation.SetProperties", resourceProvider);
		this.propertyData = data;
		this.isRecursive = isRecursive;
	}
	
	public SetPropertiesOperation(IResource []resources, IResourcePropertyProvider propertyProvider, boolean isRecursive) {
		super("Operation.SetProperties", resources);
		this.propertyProvider = propertyProvider;
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
					SetPropertiesOperation.this.processResource(proxy, resource, monitor);
				}
			}, monitor, resources.length);

			location.releaseSVNProxy(proxy);
		}
	}
	
	protected void processResource(final ISVNConnector proxy, IResource resource, IProgressMonitor monitor) {
		ProgressMonitorUtility.setTaskInfo(monitor, this, resource.getFullPath().toString());
		final String wcPath = FileUtility.getWorkingCopyPath(resource);
		
		SVNProperty[] properties = this.getOperableProperties();
		
		for (int i = 0; i < properties.length && !monitor.isCanceled(); i++) {
		    final SVNProperty current = properties[i];
		    this.protectStep(new IUnprotectedOperation() {
                public void run(IProgressMonitor monitor) throws Exception {
					proxy.setProperty(wcPath, current.name, current.value, Depth.infinityOrEmpty(SetPropertiesOperation.this.isRecursive), ISVNConnector.Options.NONE, null, new SVNProgressMonitor(SetPropertiesOperation.this, monitor, null));
                }
            }, monitor, properties.length);
		}
	}
	
	protected SVNProperty[] getOperableProperties() {
		return this.propertyData == null ? this.propertyProvider.getProperties() : this.propertyData;
	}
	
}
