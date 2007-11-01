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

import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.PropertyData;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.core.operation.IResourcePropertyProvider;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Commit operation implementation
 * 
 * @author Vladimir Bykov
 */
public class GetPropertiesOperation extends AbstractNonLockingOperation implements IResourcePropertyProvider {
	protected PropertyData []properties;
	protected IResource resource;
	protected Revision revision;
	
	public GetPropertiesOperation(IResource resource) {
		this(resource, Revision.WORKING);
	}
	
	public GetPropertiesOperation(IResource resource, Revision revision) {
		super("Operation.GetProperties");
		this.resource = resource;
		this.revision = revision;
	}
	
	public PropertyData []getProperties() {
		return this.properties;
	}
	
	public boolean isEditAllowed() {
		return true;
	}
	
	public void refresh() {
		this.run(new NullProgressMonitor());
	}

	public IResource getLocal() {
		return this.resource;
	}

	public IRepositoryResource getRemote() {
		return SVNRemoteStorage.instance().asRepositoryResource(this.resource);
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		this.properties = null;
		IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(this.resource);
		ISVNClientWrapper proxy = location.acquireSVNProxy();
		try {
//			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn proplist \"" + local.getWorkingCopyPath() + "\"\n");
			this.properties = SVNUtility.properties(proxy, FileUtility.getWorkingCopyPath(this.resource), this.revision, null, new SVNProgressMonitor(this, monitor, null));
		}
		finally {
		    location.releaseSVNProxy(proxy);
		}
	}
	
	protected String getShortErrorMessage(Throwable t) {
		return MessageFormat.format(super.getShortErrorMessage(t), new String[] {this.resource.getName()});
	}
	
}
