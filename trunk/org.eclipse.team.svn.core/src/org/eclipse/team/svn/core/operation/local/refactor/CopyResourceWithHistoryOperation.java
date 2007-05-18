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

package org.eclipse.team.svn.core.operation.local.refactor;

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.Revision;
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
 * @author Vladimir Bykov
 */
public class CopyResourceWithHistoryOperation extends AbstractActionOperation {
	protected IResource source;
	protected IResource destination;
	
	public CopyResourceWithHistoryOperation(IResource source, IResource destination) {
		super("Operation.CopyLocalH");
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
		
		return 
			localSource != null &&
			IStateFilter.SF_ONREPOSITORY.accept(this.source, localSource.getStatus(), localSource.getChangeMask()) &&
			locationSource.equals(locationDestination);
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRemoteStorage storage = SVNRemoteStorage.instance();
		IRepositoryLocation location = storage.getRepositoryLocation(this.source);
        ISVNClientWrapper proxy = location.acquireSVNProxy();
        try {
			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn copy \"" + FileUtility.normalizePath(this.source.getLocation().toString()) + "\" \"" + this.destination.getLocation().toString() + "\"\n");
			proxy.copy(this.source.getLocation().toString(), this.destination.getLocation().toString(), null, Revision.WORKING, new SVNProgressMonitor(this, monitor, null));
		}
		finally {
		    location.releaseSVNProxy(proxy);
		}
	}
	
	protected String getShortErrorMessage(Throwable t) {
		return MessageFormat.format(super.getShortErrorMessage(t), new String[] {this.source.getName(), this.destination.toString()});
	}

}
