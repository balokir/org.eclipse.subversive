/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.history;

import java.net.URI;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.history.ITag;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.resource.IRepositoryResource;

/**
 * Remote file revision representation.
 * Wrapper for SVNLogEntry.
 * 
 * @author Alexei Goncharov
 */
public class SVNRemoteFileRevision implements IFileRevision {

	protected SVNLogEntry entry;
	protected IRepositoryResource resource;
	
	public SVNRemoteFileRevision(SVNLogEntry entry, IRepositoryResource resource){
		this.entry = entry;
		this.resource = resource;
	}
	
	public boolean exists() {
		return true;
	}

	public String getAuthor() {
		return this.entry.author;
	}

	public String getComment() {
		return this.entry.message;
	}

	public String getContentIdentifier() {
		return null;
	}

	public String getName() {
		return this.resource.getName();
	}

	public IStorage getStorage(IProgressMonitor monitor){
		return new RepositoryFileEditorInput(this.resource.asRepositoryFile(resource.getUrl(), false)).getStorage();
	}

	public ITag[] getTags() {
		return null;
	}

	public long getTimestamp() {
		return this.entry.date;
	}

	public URI getURI() {
		return URI.create(this.resource.getUrl());
	}

	public boolean isPropertyMissing() {
		return false;
	}

	public IFileRevision withAllProperties(IProgressMonitor monitor){
		return this;
	}
}
