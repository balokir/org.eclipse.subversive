/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elena Matokhina - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.repository.model;

import org.eclipse.team.svn.core.resource.IRepositoryLocation;

/**
 * Repository locations filter default implementation
 *
 * @author Elena Matokhina
 */
public class RepositoryLocationFilter implements IRepositoryContentFilter {
	protected String url;
	
	public RepositoryLocationFilter(String locationUrl) {
		this.url = locationUrl;
	}
	
	public boolean accept(Object obj) {
		if (obj instanceof RepositoryLocation) {
			IRepositoryLocation location = ((RepositoryLocation)obj).getRepositoryLocation();
			return location.getUrl().equals(this.url);
		}
		return true;
	}

}
