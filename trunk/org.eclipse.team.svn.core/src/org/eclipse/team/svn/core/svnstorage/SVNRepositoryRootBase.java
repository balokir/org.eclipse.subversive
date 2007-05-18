/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.svnstorage;

import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;

/**
 * Base implementation for IRepositoryRoot children
 * 
 * @author Alexander Gurov
 */
public abstract class SVNRepositoryRootBase extends SVNRepositoryContainer implements IRepositoryRoot {
	private static final long serialVersionUID = -5582315908144905324L;

	protected SVNRepositoryRootBase() {
	}

	public SVNRepositoryRootBase(IRepositoryLocation location, String url, Revision selectedRevision) {
		super(location, url, selectedRevision);
	}


	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof IRepositoryRoot)) {
			return false;
		}
		return ((IRepositoryRoot)obj).getKind() == this.getKind() && super.equals(obj);
	}
	
}
