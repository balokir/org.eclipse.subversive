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

package org.eclipse.team.svn.ui.synchronize.variant;

import org.eclipse.team.core.variants.CachedResourceVariant;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.client.Status;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Abstract resource variant implementation
 * 
 * @author Alexander Gurov
 */
public abstract class ResourceVariant extends CachedResourceVariant {

	protected ILocalResource local;
	
	public ResourceVariant(ILocalResource local) {
		super();
		
		this.local = local;
	}
	
	public ILocalResource getResource() {
		return this.local;
	}

	protected String getCachePath() {
		return this.local.getResource().getFullPath().toString() + ":" + this.getContentIdentifier();
	}

	protected String getCacheId() {
		return IRemoteStorage.class.getName();
	}

	public String getName() {
		return this.local.getName();
	}

	public byte []asBytes() {
		return this.getContentIdentifier().getBytes();
	}

	public String getStatus() {
		return this.local.getStatus();
	}
	
	public String getContentIdentifier() {
	    if (this.local.isCopied()) {
	    	Status st = SVNUtility.getSVNInfoForNotConnected(this.local.getResource());
	    	return st != null ? String.valueOf(st.revisionCopiedFrom) : "unversioned"; 
	    }
	    if (this.isNotOnRepository()) {
	        return "unversioned";
	    }
		return String.valueOf(this.local.getRevision()); 
	}

    protected boolean isNotOnRepository() {
        return this.local.getRevision() == Revision.SVN_INVALID_REVNUM;
    }
    
}
