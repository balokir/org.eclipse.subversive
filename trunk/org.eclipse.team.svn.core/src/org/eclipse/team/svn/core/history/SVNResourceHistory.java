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

import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.history.provider.FileHistory;
import org.eclipse.team.svn.core.client.SVNLogEntry;
import org.eclipse.team.svn.core.client.SVNRevision;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;

/**
 * IResource-based resource history model object
 * 
 * @author Alexander Gurov
 */
public class SVNResourceHistory extends FileHistory {
	protected static IFileRevision []EMPTY = new IFileRevision[0];
	
	protected ILocalResource local;
	protected IRepositoryResource remote;
	protected SVNLogEntry []msgs;
	protected boolean full;

	public SVNResourceHistory(ILocalResource local, IRepositoryResource remote, SVNLogEntry []msgs, boolean full) {
		this.local = local;
		this.remote = remote;
		this.msgs = msgs;
		this.full = full;
	}

	public IFileRevision []getContributors(IFileRevision revision) {
		int idx = this.getRevisionIdx(revision.getContentIdentifier());
		if (idx != -1 && idx < this.msgs.length - 1) {
			return new IFileRevision[] {new SVNRemoteResourceRevision(this.remote, this.msgs[idx + 1])};
		}
		return SVNResourceHistory.EMPTY;
	}

	public IFileRevision getFileRevision(String id) {
		if (SVNRevision.WORKING.toString().equals(id)) {
			return new SVNLocalResourceRevision(this.local, SVNRevision.WORKING);
		}
		else if (SVNRevision.BASE.toString().equals(id)) {
			return new SVNLocalResourceRevision(this.local, SVNRevision.BASE);
		}
		else if (SVNRevision.HEAD.toString().equals(id)) {
			return this.full ? new SVNRemoteResourceRevision(this.remote, this.msgs[0]) : null;
		}
		else if (SVNRevision.START.toString().equals(id)) {
			return this.full ? new SVNRemoteResourceRevision(this.remote, this.msgs[this.msgs.length - 1]) : null;
		}
		else if (SVNRevision.PREVIOUS.toString().equals(id)) {
			int idx = this.getRevisionIdx(this.local.getRevision());
			return idx != -1 && idx < this.msgs.length - 1 ? new SVNRemoteResourceRevision(this.remote, this.msgs[idx + 1]) : null;
		}
		else if (SVNRevision.COMMITTED.toString().equals(id)) {
			int idx = this.getRevisionIdx(this.local.getRevision());
			return idx != -1 ? new SVNRemoteResourceRevision(this.remote, this.msgs[idx]) : null;
		}
		
		int idx = this.getRevisionIdx(id);
		if (idx != -1) {
			return new SVNRemoteResourceRevision(this.remote, this.msgs[idx]);
		}
		return null;
	}

	public IFileRevision []getFileRevisions() {
		IFileRevision []revs = new IFileRevision[this.msgs.length];
		for (int i = 0; i < this.msgs.length; i++) {
			revs[i] = new SVNRemoteResourceRevision(this.remote, this.msgs[i]);
		}
		return revs;
	}

	public IFileRevision []getTargets(IFileRevision revision) {
		int idx = this.getRevisionIdx(revision.getContentIdentifier());
		if (idx > 0) {
			return new IFileRevision[] {new SVNRemoteResourceRevision(this.remote, this.msgs[idx - 1])};
		}
		return SVNResourceHistory.EMPTY;
	}

	protected int getRevisionIdx(String id) {
		long revision;
		try {
			revision = Long.parseLong(id);
		}
		catch (Exception ex) {
			return -1;
		}
		return this.getRevisionIdx(revision);
	}
	
	protected int getRevisionIdx(long revision) {
		for (int i = 0; i < this.msgs.length; i++) {
			if (this.msgs[i].revision == revision) {
				return i;
			}
		}
		return -1;
	}
	
}
