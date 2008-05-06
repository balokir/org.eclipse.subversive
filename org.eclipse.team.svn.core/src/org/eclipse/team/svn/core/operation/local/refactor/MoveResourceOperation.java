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

package org.eclipse.team.svn.core.operation.local.refactor;

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Move Eclipse resource in the working copy
 * 
 * @author Alexander Gurov
 */
public class MoveResourceOperation extends AbstractActionOperation {
	protected IResource source;
	protected IResource destination;

	public MoveResourceOperation(IResource source, IResource destination) {
		super("Operation.MoveLocal");
		this.source = source;
		this.destination = destination;
	}
	
	public ISchedulingRule getSchedulingRule() {
		return MultiRule.combine(
				this.source instanceof IProject ? this.source : this.source.getParent(), 
				this.destination instanceof IProject ? this.destination : this.destination.getParent());
	}
	
	public boolean isAllowed() {
		IRemoteStorage storage = SVNRemoteStorage.instance();
		IRepositoryLocation locationSource = storage.getRepositoryLocation(this.source);
		IRepositoryLocation locationDestination = storage.getRepositoryLocation(this.destination);
		return 
			locationSource.equals(locationDestination);
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRemoteStorage storage = SVNRemoteStorage.instance();
		IRepositoryLocation location = storage.getRepositoryLocation(this.source);
		
		ISVNConnector proxy = location.acquireSVNProxy();
		
		String srcPath = FileUtility.getWorkingCopyPath(this.source);
		String dstPath = FileUtility.getWorkingCopyPath(this.destination);
		try {
			// FIXME JavaSVN does not provide any statuses in time of extended API move
//			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn move \"" + FileUtility.normalizePath(localSource.getWorkingCopyPath()) + "\" \"" + FileUtility.normalizePath(localDestination.getWorkingCopyPath()) + "\" --force\n");
			proxy.move(new String[] {srcPath}, dstPath, ISVNConnector.Options.FORCE, new SVNProgressMonitor(this, monitor, null));
		}
		finally {
		    location.releaseSVNProxy(proxy);
		}
	}
	
	protected String getShortErrorMessage(Throwable t) {
		return MessageFormat.format(super.getShortErrorMessage(t), new Object[] {this.source.getName(), this.destination.getParent().getFullPath().toString()});
	}

}
