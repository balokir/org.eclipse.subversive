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

package org.eclipse.team.svn.core.operation.remote;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.client.Depth;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Operation organize export local folder to repository
 * 
 * @author Vladimir Bykov
 */
public class ExportOperation extends AbstractRepositoryOperation {
	protected String path;
	
	public ExportOperation(IRepositoryResource resource, String path) {
		super("Operation.ExportRevision", new IRepositoryResource[] {resource});
		this.path = path;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRepositoryResource resource = this.operableData()[0];
		IRepositoryLocation location = resource.getRepositoryLocation();
		ISVNClientWrapper proxy = location.acquireSVNProxy();
		try {
			String path = this.path + "/" + resource.getName();
			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn export \"" + resource.getUrl() + "@" + resource.getPegRevision() + "\" -r " + resource.getSelectedRevision() + " \"" + FileUtility.normalizePath(path) + "\" --force" + FileUtility.getUsernameParam(location.getUsername()) + "\n");
			proxy.doExport(SVNUtility.getEntryReference(resource), 
					path, 
					true, 
					false, 
					Depth.INFINITY, 	// force
					null, 	// ignore externals 
					new SVNProgressMonitor(this, monitor, null));
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}
	
	protected String getShortErrorMessage(Throwable t) {
		return MessageFormat.format(super.getShortErrorMessage(t), new String[] {this.operableData()[0].getUrl()});
	}
	
}
