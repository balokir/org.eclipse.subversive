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

package org.eclipse.team.svn.core.svnstorage;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.client.LogEntry;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.client.Revision.Kind;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.GetLogMessagesOperation;
import org.eclipse.team.svn.core.resource.ICommentProvider;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Default implementation of the ICommentProvider interface
 * 
 * @author Alexander Gurov
 */
public class CommentProvider implements ICommentProvider {
	public String getComment(IResource resource, Revision rev, Revision peg) {
		if (rev.getKind() == Kind.NUMBER && ((Revision.Number)rev).getNumber() == Revision.SVN_INVALID_REVNUM || 
			peg != null && peg.getKind() == Kind.NUMBER && ((Revision.Number)peg).getNumber() == Revision.SVN_INVALID_REVNUM) {
			return null;
		}
		IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(resource);
		if (location == null) {
			return null;
		}
		IRepositoryResource remote = location.getRepositoryRoot();
		remote.setSelectedRevision(rev);
		remote.setPegRevision(peg);
		GetLogMessagesOperation op = new GetLogMessagesOperation(remote);
		op.setLimit(1);
		ProgressMonitorUtility.doTaskExternalDefault(op, new NullProgressMonitor());
		if (op.getExecutionState() == IActionOperation.OK) {
			LogEntry []msgs = op.getMessages();
			if (msgs.length > 0) {
				return msgs[0].message;
			}
		}
		return null;
	}
}
