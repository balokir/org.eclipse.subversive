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

import org.eclipse.team.svn.core.client.ClientWrapperException;
import org.eclipse.team.svn.core.client.Depth;
import org.eclipse.team.svn.core.client.DirEntry;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.operation.SVNNullProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * SVN based representation of IRepositoryFile
 * 
 * @author Alexander Gurov
 */
public class SVNRepositoryFile extends SVNRepositoryResource implements IRepositoryFile {
	private static final long serialVersionUID = 6042328067024796901L;
	
	public SVNRepositoryFile(IRepositoryLocation location, String url, Revision selectedRevision) {
		super(location, url, selectedRevision);
	}
	
	protected void getRevisionImpl(ISVNClientWrapper proxy) throws ClientWrapperException {
		DirEntry []entries = proxy.list(SVNUtility.encodeURL(this.getUrl()), this.getSelectedRevision(), this.getPegRevision(), Depth.empty, DirEntry.Fields.all, true, new SVNNullProgressMonitor());
		if (entries != null && entries.length > 0) {
			this.lastRevision = (Revision.Number)Revision.getInstance(entries[0].lastChangedRevision);
			this.setInfo(new IRepositoryResource.Info(entries[0].lock, entries[0].size, entries[0].lastAuthor, entries[0].lastChanged, entries[0].hasProps));
		}
	}
	
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof IRepositoryFile)) {
			return false;
		}
		return super.equals(obj);
	}
	
}
