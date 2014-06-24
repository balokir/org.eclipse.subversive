/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.svnstorage;

import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRevisionLink;

/**
 * SVN implementation of IRevisionLink
 * 
 * @author Igor Burilo
 */
public class SVNRevisionLink implements IRevisionLink {

	protected IRepositoryResource repositoryResource;	
	protected String comment;
	
	public SVNRevisionLink(IRepositoryResource repositoryResource) {
		this.repositoryResource = repositoryResource;
	}

	public String getComment() {
		return this.comment == null ? "" : this.comment; //$NON-NLS-1$
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public IRepositoryResource getRepositoryResource() {		
		return this.repositoryResource;
	}
	
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof IRevisionLink)) {
			return false;
		}
		IRevisionLink link = (IRevisionLink) obj;		
		return this.repositoryResource.equals(link.getRepositoryResource()) && 
			   this.getComment().equals(link.getComment());
	}
	
	public int hashCode() {
		return this.repositoryResource.hashCode();
	}
	
	public String toString() {
		return this.repositoryResource.toString();
	}		
}
