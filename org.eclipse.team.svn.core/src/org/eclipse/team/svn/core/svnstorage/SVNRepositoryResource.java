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

import java.io.Serializable;

import org.eclipse.team.svn.core.client.ClientWrapperCancelException;
import org.eclipse.team.svn.core.client.ClientWrapperException;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.resource.ISVNStorage;

/**
 * SVN based representation of IRepositoryResource
 * 
 * @author Alexander Gurov
 */
public abstract class SVNRepositoryResource extends SVNRepositoryBase implements IRepositoryResource, Serializable {
	private static final long serialVersionUID = 8854704746872311777L;

	private transient Revision selectedRevision;	// should be managed using setters and getters regarding to "transient" modifier
	private transient Revision pegRevision;			// revision where we found this item
	protected transient Revision.Number lastRevision;
	protected transient IRepositoryLocation location;
	protected transient IRepositoryRoot root;
	protected transient IRepositoryResource.Info info;

	// serialization conventional constructor
	protected SVNRepositoryResource() {
		super();
	}
	
	public SVNRepositoryResource(IRepositoryLocation location, String url, Revision selectedRevision) {
		super(url);
		this.location = location;
		this.selectedRevision = selectedRevision;
	}
	
	public void setInfo(IRepositoryResource.Info info) {
		this.info = info;
	}
	
	public Info getInfo() {
		return this.info;
	}
	
	public Revision getPegRevision() {
		return this.pegRevision == null ? Revision.HEAD : this.pegRevision;
	}
	
	public void setPegRevision(Revision pegRevision) {
		this.pegRevision = pegRevision;
	}
	
	public Revision getSelectedRevision() {
		if (this.selectedRevision == null) {
			this.selectedRevision = Revision.HEAD;
		}
		return this.selectedRevision;
	}
	
	public void setSelectedRevision(Revision revision) {
		this.selectedRevision = revision;
	}

	public boolean isInfoCached() {
		return this.lastRevision != null;
	}
	
	public synchronized void refresh() {
		this.lastRevision = null;
	}
	
	public void setRevision(long revisionNumber) {
		this.lastRevision = new Revision.Number(revisionNumber);
	}
	
	public synchronized long getRevision() throws ClientWrapperException {
		if (this.lastRevision == null) {
			this.lastRevision = new ISVNStorage.InvalidRevision();
			ISVNClientWrapper proxy = this.getRepositoryLocation().acquireSVNProxy();
			try {
				this.getRevisionImpl(proxy);
			}
			finally {
			    this.getRepositoryLocation().releaseSVNProxy(proxy);
			}
		}
		return this.lastRevision.getNumber();
	}
	
	public boolean exists() throws ClientWrapperException {
		try {
			return this.getRevision() != Revision.SVN_INVALID_REVNUM;
		}
		catch (ClientWrapperException ex) {
			//FIXME uncomment this when the WI is resolved ("Unknown node kind" exception instead of "Path not found" (PLC-1008)) 
//			if (ex instanceof ClientExceptionEx) {
//				if (((ClientExceptionEx)ex).getErrorMessage().getErrorCode().equals(SVNErrorCode.RA_DAV_PATH_NOT_FOUND)) {
//					return false;
//				}
//			}
//			throw ex;
			if (ex instanceof ClientWrapperCancelException) {
				throw ex;
			}
			return false;
		}
	}

	public IRepositoryResource getParent() {
		String parentUrl = this.getUrl();
		int idx = parentUrl.lastIndexOf('/');
		if (idx != -1) {
			parentUrl = parentUrl.substring(0, idx);
		}
		return this.asRepositoryContainer(parentUrl, true);
	}
	
	public synchronized IRepositoryResource getRoot() {
		if (this.root == null) {
			IRepositoryResource parent = this;
			while (!(parent instanceof IRepositoryRoot)) {
				parent = parent.getParent();
			}
			this.root = (IRepositoryRoot)parent;
		}
		return this.root;
	}
	
	public IRepositoryLocation getRepositoryLocation() {
		return this.location;
	}
	
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof IRepositoryResource)) {
			return false;
		}
		IRepositoryResource other = (IRepositoryResource)obj;
		return 
			super.equals(obj) && 
			this.getSelectedRevision().equals(other.getSelectedRevision()) && 
			this.getPegRevision().equals(other.getPegRevision());
	}
	
	
	public IRepositoryContainer asRepositoryContainer(String url, boolean allowsNull) {
		IRepositoryContainer retVal = this.getRepositoryLocation().asRepositoryContainer(url.indexOf('/') != -1 ? url : (this.getUrl() + "/" + url), allowsNull);
		if (retVal == null) {
			return null;
		}
		retVal.setPegRevision(this.getPegRevision());
		retVal.setSelectedRevision(this.getSelectedRevision());
		return retVal;
	}
	
	public IRepositoryFile asRepositoryFile(String url, boolean allowsNull) {
		IRepositoryFile retVal = this.getRepositoryLocation().asRepositoryFile(url.indexOf('/') != -1 ? url : (this.getUrl() + "/" + url), allowsNull);
		if (retVal == null) {
			return null;
		}
		retVal.setPegRevision(this.getPegRevision());
		retVal.setSelectedRevision(this.getSelectedRevision());
		return retVal;
	}
	
	protected abstract void getRevisionImpl(ISVNClientWrapper proxy) throws ClientWrapperException;
	
}
