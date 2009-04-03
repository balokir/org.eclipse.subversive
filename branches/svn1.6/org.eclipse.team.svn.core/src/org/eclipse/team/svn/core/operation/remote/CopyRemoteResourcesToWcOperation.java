/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.remote;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryReference;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Copy remote resource to working copy operation 
 * 
 * @author Igor Burilo
 */
public class CopyRemoteResourcesToWcOperation extends AbstractActionOperation {

	protected SVNEntryReference entry;
	protected boolean isFolder;
	protected IResource resource;
	
	public CopyRemoteResourcesToWcOperation(SVNEntryReference entry, boolean isFolder, IResource resource) {
		super("Operation_CopyRemoteToWC"); //$NON-NLS-1$
		this.entry = entry;
		this.isFolder = isFolder;
		this.resource = resource;
	}
	protected void runImpl(IProgressMonitor monitor) throws Exception {															
		IRepositoryResource repositoryResource = SVNUtility.asRepositoryResource(this.entry.path, this.isFolder);
		IRepositoryLocation location = repositoryResource.getRepositoryLocation();			
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn copy \"" + this.entry.path + "@" + this.entry.pegRevision + "\" \"" + FileUtility.getWorkingCopyPath(this.resource) + "\"\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			proxy.copy(new String[]{this.entry.path}, FileUtility.getWorkingCopyPath(this.resource), this.entry.pegRevision, new SVNProgressMonitor(this, monitor, null));			
		} finally {
			location.releaseSVNProxy(proxy);
		}
	}
	
	public ISchedulingRule getSchedulingRule() {
		if (this.resource.exists()) {
			return this.resource instanceof IProject ? this.resource : this.resource.getParent();	
		} else {
			return null;
		}			
	}
}
