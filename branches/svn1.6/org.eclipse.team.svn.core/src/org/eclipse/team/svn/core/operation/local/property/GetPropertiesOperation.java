/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.local.property;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.BaseMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
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
 * @author Sergiy Logvin
 */
public class GetPropertiesOperation extends AbstractActionOperation implements IResourcePropertyProvider {
	protected SVNProperty []properties;
	protected IResource resource;
	protected SVNRevision revision;
	
	public GetPropertiesOperation(IResource resource) {
		this(resource, SVNRevision.WORKING);
	}
	
	public GetPropertiesOperation(IResource resource, SVNRevision revision) {
		super("Operation_GetProperties"); //$NON-NLS-1$
		this.resource = resource;
		this.revision = revision;
	}
	
	public SVNProperty []getProperties() {
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
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
//			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn proplist \"" + local.getWorkingCopyPath() + "\"\n");
			this.properties = SVNUtility.properties(proxy, new SVNEntryRevisionReference(FileUtility.getWorkingCopyPath(this.resource), null, this.revision), new SVNProgressMonitor(this, monitor, null));
		}
		finally {
		    location.releaseSVNProxy(proxy);
		}
	}
	
	protected String getShortErrorMessage(Throwable t) {
		return BaseMessages.format(super.getShortErrorMessage(t), new Object[] {this.resource.getName()});
	}
	
}
