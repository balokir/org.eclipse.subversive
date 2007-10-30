/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Panagiotis Korros - [patch] optimization: reduces memory consumption by 15%
 *******************************************************************************/

package org.eclipse.team.svn.core.svnstorage;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.resource.ILocalResource;

/**
 * Working copy resource represenation
 * 
 * @author Alexander Gurov
 */
public abstract class SVNLocalResource implements ILocalResource {
	protected IResource resource;
	protected long revision;
	protected String status;
	protected int changeMask;
	protected String author;
	protected long lastCommitDate;

	protected SVNLocalResource(IResource resource, long revision, String status, int changeMask, String author, long lastCommitDate) {
		this.resource = resource;
		this.revision = revision;
		this.status = status;
		this.changeMask = changeMask;
		this.author = author != null ? author.intern() : null;
		this.lastCommitDate = lastCommitDate;
	}
	
	public boolean isLocked() {
	    return (this.changeMask & ILocalResource.IS_LOCKED) != 0;
	}
	
	public IResource getResource() {
		return this.resource;
	}

	public String getName() {
		return this.resource.getName();
	}

	public long getRevision() {
		return this.revision;
	}

	public String getStatus() {
		return this.status;
	}
	
	public int getChangeMask() {
		return this.changeMask;
	}
	
	public boolean isCopied() {
		return (this.changeMask & ILocalResource.IS_COPIED) != 0;
	}

	public String getAuthor() {
		return this.author;
	}
	
	public long getLastCommitDate() {
		return this.lastCommitDate;
	}
	
	public String toString() {
		return this.resource.toString();
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof ILocalResource) {
			return this.resource.equals(((ILocalResource)obj).getResource());
		}
		return false;
	}
	
}
