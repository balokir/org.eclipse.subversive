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

package org.eclipse.team.svn.core.operation.remote;

import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.operation.IResourcePropertyProvider;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * This operation allows us to get properties of the concrete revision of the versioned resource
 * 
 * @author Alexander Gurov
 */
public class GetRemotePropertiesOperation extends AbstractRepositoryOperation implements IResourcePropertyProvider {
	protected SVNProperty []properties;

	public GetRemotePropertiesOperation(IRepositoryResource resource) {
		super("Operation.GetRevisionProperties", new IRepositoryResource[] {resource});
	}
	
	public GetRemotePropertiesOperation(IRepositoryResourceProvider provider) {
		super("Operation.GetRevisionProperties", provider);
	}

	public SVNProperty []getProperties() {
		return this.properties;
	}
	
	public boolean isEditAllowed() {
		return false;
	}
	
	public void refresh() {
		
	}

	public IResource getLocal() {
		return null;
	}

	public IRepositoryResource getRemote() {
		return this.operableData()[0];
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRepositoryResource resource = this.operableData()[0];
		this.properties = null;
		IRepositoryLocation location = resource.getRepositoryLocation();
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
//			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn proplist " + url + "@" + resource.getPegRevision() + " --revprop -r " + resource.getSelectedRevision() + " --username \"" + location.getUsername() + "\"\n");
			this.properties = SVNUtility.properties(proxy, SVNUtility.getEntryRevisionReference(resource), new SVNProgressMonitor(this, monitor, null));
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}

	protected String getShortErrorMessage(Throwable t) {
		return MessageFormat.format(super.getShortErrorMessage(t), new Object[] {this.operableData()[0].getUrl()});
	}

}
