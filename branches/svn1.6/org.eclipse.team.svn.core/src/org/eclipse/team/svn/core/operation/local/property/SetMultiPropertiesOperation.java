/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.local.property;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.local.AbstractWorkingCopyOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Operation to set the properties on multiple resources
 * 
 * @author Sergiy Logvin
 */
public class SetMultiPropertiesOperation extends AbstractWorkingCopyOperation {
	protected IPropertyProvider propertyProvider;
	protected int depth;
	protected IStateFilter filter;
	
	public SetMultiPropertiesOperation(IResourceProvider resourceProvider, IPropertyProvider propertyProvider, IStateFilter filter, int depth) {
		super("Operation_SetMultiProperties", resourceProvider); //$NON-NLS-1$
		this.propertyProvider = propertyProvider;
		this.depth = depth;
		this.filter = filter != null ? filter : IStateFilter.SF_VERSIONED;
	}
	
	public SetMultiPropertiesOperation(IResource[] resources, IPropertyProvider propertyProvider, IStateFilter filter, int depth) {
		super("Operation_SetMultiProperties", resources); //$NON-NLS-1$
		this.propertyProvider = propertyProvider;
		this.depth = depth;
		this.filter = filter != null ? filter : IStateFilter.SF_VERSIONED;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []resources = this.operableData();
		
		for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
			final IResource current = resources[i];
			
			IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(current);
			final ISVNConnector proxy = location.acquireSVNProxy();
			try {
				this.protectStep(new IUnprotectedOperation() {
					public void run(final IProgressMonitor monitor) throws Exception {
						FileUtility.visitNodes(current, new IResourceVisitor() {
							public boolean visit(IResource resource) throws CoreException {
								if (monitor.isCanceled()) {
									return false;
								}
								ILocalResource local = SVNRemoteStorage.instance().asLocalResourceAccessible(resource);
								if (SetMultiPropertiesOperation.this.filter.accept(local)) {
									SVNProperty []properties = SetMultiPropertiesOperation.this.propertyProvider.getProperties(resource);
									if (properties != null) {
										SetMultiPropertiesOperation.this.processResource(proxy, resource, properties, monitor);
									}
								}
								return SetMultiPropertiesOperation.this.filter.allowsRecursion(local);
							}
						}, SetMultiPropertiesOperation.this.depth);
					}
				}, monitor, resources.length);
			}
			finally {
				location.releaseSVNProxy(proxy);
			}
		}
		
	}

	protected void processResource(final ISVNConnector proxy, IResource current, SVNProperty []properties, IProgressMonitor monitor) {
		ProgressMonitorUtility.setTaskInfo(monitor, this, current.getFullPath().toString());
		final String wcPath = FileUtility.getWorkingCopyPath(current);
		for (int i = 0; i < properties.length && !monitor.isCanceled(); i++) {
			final SVNProperty property = properties[i];
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
                	proxy.setProperty(wcPath, property.name, property.value, Depth.EMPTY, ISVNConnector.Options.NONE, null, new SVNProgressMonitor(SetMultiPropertiesOperation.this, monitor, null));
				}
			}, monitor, properties.length);
		}
	}
	
}
