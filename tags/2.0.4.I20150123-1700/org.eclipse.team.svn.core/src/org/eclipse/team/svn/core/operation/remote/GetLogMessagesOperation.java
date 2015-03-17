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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.BaseMessages;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.connector.SVNErrorCodes;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.ISVNConnector.Options;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Get repository resource LogMessage's opearation implementation
 * 
 * @author Alexander Gurov
 */
public class GetLogMessagesOperation extends AbstractRepositoryOperation {
	protected SVNLogEntry []msg;
	protected SVNRevision startRevision;
	protected SVNRevision endRevision;
	protected long limit;
	protected boolean isRetryIfMergeInfoNotSupported;
	protected long options;
	
	public GetLogMessagesOperation(IRepositoryResource resource) {
		this(resource, false);
	}
	
	public GetLogMessagesOperation(IRepositoryResource resource, boolean stopOnCopy) {
		this(resource, ISVNConnector.Options.DISCOVER_PATHS | (stopOnCopy ? ISVNConnector.Options.STOP_ON_COPY : ISVNConnector.Options.NONE));
	}
	
	public GetLogMessagesOperation(IRepositoryResource resource, long options) {
		super("Operation_GetLogMessages", SVNMessages.class, new IRepositoryResource[] {resource}); //$NON-NLS-1$
		this.options = options & ISVNConnector.CommandMasks.LIST_HISTORY_LOG;
		this.limit = 0;
		this.endRevision = SVNRevision.fromNumber(0); 
	}
	
	public boolean getIncludeMerged() {
		return (this.options & ISVNConnector.Options.INCLUDE_MERGED_REVISIONS) != 0;
	}
	
	public void setIncludeMerged(boolean includeMerged) {
		this.options &= ~ISVNConnector.Options.INCLUDE_MERGED_REVISIONS;
		this.options |= includeMerged ? ISVNConnector.Options.INCLUDE_MERGED_REVISIONS : ISVNConnector.Options.NONE;
	}
	
	public void setRetryIfMergeInfoNotSupported(boolean isRetryIfMergeInfoNotSupported) {
		this.isRetryIfMergeInfoNotSupported = isRetryIfMergeInfoNotSupported;
	}
	
	public boolean getStopOnCopy() {
		return (this.options & ISVNConnector.Options.STOP_ON_COPY) != 0;
	}
	
	public void setStopOnCopy(boolean stopOnCopy) {
		this.options &= ~ISVNConnector.Options.STOP_ON_COPY;
		this.options |= stopOnCopy ? ISVNConnector.Options.STOP_ON_COPY : ISVNConnector.Options.NONE;
	}
	
	public boolean getDiscoverPaths() {
		return (this.options & ISVNConnector.Options.DISCOVER_PATHS) != 0;
	}

	public void setDiscoverPaths(boolean discoverPaths) {
		this.options &= ~ISVNConnector.Options.DISCOVER_PATHS;
		this.options |= discoverPaths ? ISVNConnector.Options.DISCOVER_PATHS : ISVNConnector.Options.NONE;
	}
	
	public long getLimit() {
		return this.limit;
	}
	
	public void setLimit(long limit) {
		this.limit = limit;
	}
	
	public void setStartRevision(SVNRevision revision) {
		this.startRevision = revision;
	}

	public void setEndRevision(SVNRevision revision) {
		this.endRevision = revision;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRepositoryResource resource = this.operableData()[0];
		if (this.startRevision == null) {
			this.startRevision = resource.getSelectedRevision();
		}
		IRepositoryLocation location = resource.getRepositoryLocation();
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
//			this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn log " + SVNUtility.encodeURL(this.resource.getUrl()) + (this.limit != 0 ? (" --limit " + this.limit) : "") + (this.stopOnCopy ? " --stop-on-copy" : "") + " -r " + this.selectedRevision + ":0 --username \"" + location.getUsername() + "\"\n");
			try {
				this.msg = SVNUtility.logEntries(proxy, SVNUtility.getEntryReference(resource), this.startRevision, this.endRevision, this.options, ISVNConnector.DEFAULT_LOG_ENTRY_PROPS, this.limit, new SVNProgressMonitor(this, monitor, null));
			} catch (SVNConnectorException ex) {
				/*
				 * If SVN server doesn't support merged revisions, then we re-call without this option
				 */
				if (this.isRetryIfMergeInfoNotSupported && 
					ex.getErrorId() == SVNErrorCodes.unsupportedFeature && 
					(this.options & Options.INCLUDE_MERGED_REVISIONS) != 0) {
					this.options &= ~Options.INCLUDE_MERGED_REVISIONS;  
					this.msg = SVNUtility.logEntries(proxy, SVNUtility.getEntryReference(resource), this.startRevision, this.endRevision, this.options, ISVNConnector.DEFAULT_LOG_ENTRY_PROPS, this.limit, new SVNProgressMonitor(this, monitor, null));
				} else {
					throw ex;
				}
			}
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}
	
	public SVNLogEntry []getMessages() {
		return this.msg;
	}
	
	public IRepositoryResource getResource() {
		return this.operableData()[0];
	}
	
	protected String getShortErrorMessage(Throwable t) {
		return BaseMessages.format(super.getShortErrorMessage(t), new Object[] {this.operableData()[0].getUrl()});
	}

}
