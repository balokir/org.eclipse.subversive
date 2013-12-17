/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
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

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.team.svn.core.resource.IRepositoryBase;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * SVN implementation of IRepositoryBase
 * 
 * @author Alexander Gurov
 */
public abstract class SVNRepositoryBase extends PlatformObject implements IRepositoryBase, Serializable {
	private static final long serialVersionUID = -4325317873020232839L;

	protected String url;

	// serialization conventional constructor
	protected SVNRepositoryBase() {
		super();
	}
	
	public SVNRepositoryBase(String url) {
		super();
		this.url = url;
	}
	
	public String getName() {
		return SVNUtility.createPathForSVNUrl(this.getUrl()).lastSegment();
	}
	
	public String getUrl() {
		return this.url;
	}
	
	public String toString() {
		return this.getUrl();
	}
	
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof IRepositoryBase) {
			return this.getUrl().equals(((IRepositoryBase)obj).getUrl());
		}
		return false;
	}
	
	public int hashCode() {
		return this.getUrl().hashCode();
	}
	
}
