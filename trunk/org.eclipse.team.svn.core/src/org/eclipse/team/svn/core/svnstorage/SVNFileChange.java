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

package org.eclipse.team.svn.core.svnstorage;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.resource.ICommentProvider;
import org.eclipse.team.svn.core.resource.IFileChange;
import org.eclipse.team.svn.core.resource.IRepositoryResource;

/**
 * File change descriptor
 * 
 * @author Alexander Gurov
 */
public class SVNFileChange extends SVNLocalFile implements IFileChange {
    protected Revision pegRevision;
	protected IRepositoryResource originator;
	protected String comment;
	protected ICommentProvider provider;

	public SVNFileChange(IResource resource, long revision, String status, int changeMask, String author, long lastCommitDate, Revision pegRevision, String comment) {
		super(resource, revision, status, changeMask, author, lastCommitDate);
		this.comment = comment;
		this.pegRevision = pegRevision;
	}
	
	public Revision getPegRevision() {
		return this.pegRevision == null ? (this.revision != Revision.INVALID_REVISION_NUMBER ? Revision.fromNumber(this.revision) : Revision.INVALID_REVISION) : this.pegRevision;
	}
	
	public void setPegRevision(Revision pegRevision) {
		this.pegRevision = pegRevision;
	}

	public IRepositoryResource getOriginator() {
		return this.originator;
	}

	public void setOriginator(IRepositoryResource originator) {
		this.originator = originator;
	}

	public synchronized String getComment() {
		if (this.comment == null && this.provider != null) {
			long rev = this.getRevision();
			this.comment = this.provider.getComment(this.getResource(), rev == Revision.INVALID_REVISION_NUMBER ? Revision.INVALID_REVISION : Revision.fromNumber(rev), this.getPegRevision());
			this.provider = null;
		}
		return this.comment;
	}

	public void setCommentProvider(ICommentProvider provider) {
		this.provider = provider;
	}
	
}
