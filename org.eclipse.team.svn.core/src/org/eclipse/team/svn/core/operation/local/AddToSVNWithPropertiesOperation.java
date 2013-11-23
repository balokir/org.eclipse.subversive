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

package org.eclipse.team.svn.core.operation.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.core.Team;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNProperty.BuiltIn;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Set content properties operation
 * 
 * @author Sergiy Logvin
 */
public class AddToSVNWithPropertiesOperation extends AddToSVNOperation {
	protected static final String BINARY_FILE = "application/octet-stream"; //$NON-NLS-1$
	protected static final String TEXT_FILE = "text/plain"; //$NON-NLS-1$
	
	protected boolean doMarkTextFiles;
	
	public AddToSVNWithPropertiesOperation(IResource[] resources) {
		this(resources, false);
	}
	
	public AddToSVNWithPropertiesOperation(IResource[] resources, boolean isRecursive) {
		super(resources, isRecursive);
		this.doMarkTextFiles = CoreExtensionsManager.instance().getOptionProvider().isTextMIMETypeRequired();
	}
	
	public AddToSVNWithPropertiesOperation(IResourceProvider provider, boolean isRecursive) {
		super(provider, isRecursive);
		this.doMarkTextFiles = CoreExtensionsManager.instance().getOptionProvider().isTextMIMETypeRequired();
	}

	protected void doAdd(IResource current, final ISVNConnector proxy, final IProgressMonitor monitor) throws Exception {
		super.doAdd(current, proxy, monitor);
		
		if (!this.isRecursive) {
			this.processResource(current, proxy, monitor);
		}
		else {
			FileUtility.visitNodes(current, new IResourceVisitor() {

				public boolean visit(IResource resource) throws CoreException {
					if (monitor.isCanceled()) {
						return false;
					}
					
					try {
						AddToSVNWithPropertiesOperation.this.processResource(resource, proxy, monitor);
					}
					catch (SVNConnectorException cwe) {
						AddToSVNWithPropertiesOperation.this.reportStatus(IStatus.ERROR, null, cwe);
						return false;
					}
					return true;
				}
				
			}, IResource.DEPTH_INFINITE);
		}
	}
	
	protected void processResource(IResource resource, ISVNConnector proxy, IProgressMonitor monitor) throws SVNConnectorException {
		String path = FileUtility.getWorkingCopyPath(resource);
		SVNProperty[] properties = CoreExtensionsManager.instance().getOptionProvider().getAutomaticProperties(resource.getName());
		for (int pCount = 0; pCount < properties.length; pCount++) {
			proxy.setPropertyLocal(new String[] {path}, new SVNProperty(properties[pCount].name, properties[pCount].value), SVNDepth.EMPTY, ISVNConnector.Options.NONE, null, new SVNProgressMonitor(this, monitor, null));
		}
		if (resource.getType() == IResource.FILE) {
			this.processFile(resource, proxy, monitor);
		}
	}
	
	protected void processFile(IResource resource, ISVNConnector proxy, IProgressMonitor monitor) throws SVNConnectorException {
		String path = FileUtility.getWorkingCopyPath(resource);
		int type = FileUtility.getMIMEType(resource);
		SVNProperty data = proxy.getProperty(new SVNEntryRevisionReference(path), BuiltIn.MIME_TYPE, null, new SVNProgressMonitor(this, monitor, null));
		if (data == null) {
			if (type == Team.BINARY) {
				proxy.setPropertyLocal(new String[] {path}, new SVNProperty(BuiltIn.MIME_TYPE, AddToSVNWithPropertiesOperation.BINARY_FILE), SVNDepth.EMPTY, ISVNConnector.Options.NONE, null, new SVNProgressMonitor(this, monitor, null));
			}
			else if (this.doMarkTextFiles && type == Team.TEXT) {
				proxy.setPropertyLocal(new String[] {path}, new SVNProperty(BuiltIn.MIME_TYPE, AddToSVNWithPropertiesOperation.TEXT_FILE), SVNDepth.EMPTY, ISVNConnector.Options.NONE, null, new SVNProgressMonitor(this, monitor, null));
			}
		}
	}
	
}
