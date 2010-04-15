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

package org.eclipse.team.svn.core.operation.local.refactor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.svn.core.BaseMessages;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Copies versioned resource with the history preserved  
 * 
 * @author Sergiy Logvin
 */
public class CopyResourceWithHistoryOperation extends AbstractActionOperation {
	protected IResource source;
	protected IResource destination;
	
	public CopyResourceWithHistoryOperation(IResource source, IResource destination) {
		super("Operation_CopyLocalH", SVNMessages.class); //$NON-NLS-1$
		this.source = source;
		this.destination = destination;
	}
	
	public ISchedulingRule getSchedulingRule() {
		return this.destination instanceof IProject ? this.destination : this.destination.getParent();
	}
	
	public boolean isAllowed() {
		IRemoteStorage storage = SVNRemoteStorage.instance();
		IRepositoryLocation locationSource = storage.getRepositoryLocation(this.source);
		IRepositoryLocation locationDestination = storage.getRepositoryLocation(this.destination);
		ILocalResource localSource =  storage.asLocalResource(this.source);
		
		return IStateFilter.SF_ONREPOSITORY.accept(localSource) && locationSource.equals(locationDestination);
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRemoteStorage storage = SVNRemoteStorage.instance();
		IRepositoryLocation location = storage.getRepositoryLocation(this.source);
        ISVNConnector proxy = location.acquireSVNProxy();
        try {
			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn copy \"" + FileUtility.normalizePath(FileUtility.getWorkingCopyPath(this.source)) + "\" \"" + FileUtility.getWorkingCopyPath(this.destination) + "\"\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			proxy.copy(new String[] {FileUtility.getWorkingCopyPath(this.source)}, FileUtility.getWorkingCopyPath(this.destination), SVNRevision.WORKING, new SVNProgressMonitor(this, monitor, null));
		}
		finally {
		    location.releaseSVNProxy(proxy);
		}
	}
	
	protected String getShortErrorMessage(Throwable t) {
		return BaseMessages.format(super.getShortErrorMessage(t), new Object[] {this.source.getName(), this.destination.toString()});
	}

}
