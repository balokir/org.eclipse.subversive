/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.LogEntry;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Get repository resource LogMessage's opearation implementation
 * 
 * @author Alexander Gurov
 */
public class GetLogMessagesOperation extends AbstractRepositoryOperation {
	protected LogEntry []msg;
	protected boolean stopOnCopy;
	protected Revision selectedRevision;
	protected long limit;
	
	public GetLogMessagesOperation(IRepositoryResourceProvider provider) {
		super("Operation.GetLogMessages", provider);
		this.stopOnCopy = false;
	}
	
	public GetLogMessagesOperation(IRepositoryResource resource) {
		this(resource, false);
	}
	
	public GetLogMessagesOperation(IRepositoryResource resource, boolean stopOnCopy) {
		super("Operation.GetLogMessages", new IRepositoryResource[] {resource});
		this.stopOnCopy = stopOnCopy;
		this.limit = 0;
	}
	
	public boolean getStopOnCopy() {
		return this.stopOnCopy;
	}
	
	public void setStopOnCopy(boolean stopOnCopy) {
		this.stopOnCopy = stopOnCopy;
	}
	
	public long getLimit() {
		return this.limit;
	}
	
	public void setLimit(long limit) {
		this.limit = limit;
	}
	
	public void setSelectedRevision(Revision revision) {
		this.selectedRevision = revision;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRepositoryResource resource = this.operableData()[0];
		if (this.selectedRevision == null) {
			this.selectedRevision = resource.getSelectedRevision();
		}
		IRepositoryLocation location = resource.getRepositoryLocation();
		ISVNClientWrapper proxy = location.acquireSVNProxy();
		try {
//			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn log " + SVNUtility.encodeURL(this.resource.getUrl()) + (this.limit != 0 ? (" --limit " + this.limit) : "") + (this.stopOnCopy ? " --stop-on-copy" : "") + " -r " + this.selectedRevision + ":0 --username \"" + location.getUsername() + "\"\n");
			this.msg = GetLogMessagesOperation.getMessagesImpl(proxy, resource, this.selectedRevision, Revision.fromNumber(0), null, this.limit, this.stopOnCopy, this, monitor);
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}
	
	public LogEntry []getMessages() {
		return this.msg;
	}
	
	public IRepositoryResource getResource() {
		return this.operableData()[0];
	}
	
	public static LogEntry []getMessagesImpl(ISVNClientWrapper proxy, IRepositoryResource resource, Revision from, Revision to, String[] revProps, long limit, boolean stopOnCopy, IActionOperation parent, IProgressMonitor monitor) throws Exception {
		return SVNUtility.logEntries(proxy, SVNUtility.getEntryReference(resource), from, to, stopOnCopy, true, revProps, limit, new SVNProgressMonitor(parent, monitor, null));
	}
	
	protected String getShortErrorMessage(Throwable t) {
		return MessageFormat.format(super.getShortErrorMessage(t), new String[] {this.operableData()[0].getUrl()});
	}
	
}
