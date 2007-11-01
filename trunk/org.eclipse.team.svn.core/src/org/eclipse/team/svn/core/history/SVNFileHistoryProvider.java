/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.history;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.history.IFileHistory;
import org.eclipse.team.core.history.IFileHistoryProvider;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.history.provider.FileHistoryProvider;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.GetLogMessagesOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * SVN file history provider implementation
 * 
 * @author Alexander Gurov
 */
public class SVNFileHistoryProvider extends FileHistoryProvider {

	public IFileHistory getFileHistoryFor(IResource resource, int flags, IProgressMonitor monitor) {
		IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(resource);
		GetLogMessagesOperation logOp = new GetLogMessagesOperation(remote);
		ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
		if ((flags & IFileHistoryProvider.SINGLE_REVISION) != 0) {
			if (local.getRevision() != Revision.SVN_INVALID_REVNUM) {
				remote.setSelectedRevision(Revision.fromNumber(local.getRevision()));
			}
			logOp.setLimit(1);
		}
		else if ((flags & IFileHistoryProvider.SINGLE_LINE_OF_DESCENT) != 0) {
			if (local.getRevision() != Revision.SVN_INVALID_REVNUM) {
				remote.setSelectedRevision(Revision.fromNumber(local.getRevision()));
			}
			logOp.setLimit(2);
		}
		ProgressMonitorUtility.doTaskExternal(logOp, monitor);
		if (logOp.getExecutionState() == IActionOperation.OK && logOp.getMessages() != null) {
			return new SVNResourceHistory(local, remote, logOp.getMessages(), flags == 0);
		}
		return null;
	}
	
	public IFileRevision getWorkspaceFileRevision(IResource resource) {
		ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
		return new SVNLocalResourceRevision(local, Revision.WORKING);
	}
	
	public IFileHistory getFileHistoryFor(IFileStore store, int flags, IProgressMonitor monitor) {
		return null;
	}
	
}
