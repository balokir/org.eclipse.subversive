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
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.ICommentProvider;
import org.eclipse.team.svn.core.resource.IFolderChange;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;

/**
 * Folder change descriptor
 * 
 * @author Alexander Gurov
 */
public class SVNFolderChange extends SVNLocalFolder implements IFolderChange {
    protected SVNRevision pegRevision;
	protected IRepositoryResource originator;
	protected String comment;
	protected ICommentProvider provider;

	public SVNFolderChange(IResource resource, long revision, String status, int changeMask, String author, long lastCommitDate, SVNRevision pegRevision, String comment) {
		super(resource, revision, status, changeMask, author, lastCommitDate);
		this.comment = comment;
		this.pegRevision = pegRevision;
	}
	
	public SVNRevision getPegRevision() {
		return this.pegRevision == null ? (this.revision != SVNRevision.INVALID_REVISION_NUMBER ? SVNRevision.fromNumber(this.revision) : SVNRevision.INVALID_REVISION) : this.pegRevision;
	}
	
	public void setPegRevision(SVNRevision pegRevision) {
		this.pegRevision = pegRevision;
	}

	public ILocalResource []getChildren() {
		return new ILocalResource[0];
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
			this.comment = this.provider.getComment(this.getResource(), rev == SVNRevision.INVALID_REVISION_NUMBER ? SVNRevision.INVALID_REVISION : SVNRevision.fromNumber(rev), this.getPegRevision());
			this.provider = null;
		}
		return this.comment;
	}

	public void setCommentProvider(ICommentProvider provider) {
		this.provider = provider;
	}
	
}
