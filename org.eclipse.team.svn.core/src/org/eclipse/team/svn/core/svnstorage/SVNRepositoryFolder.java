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

import org.eclipse.team.svn.core.client.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryFolder;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;

/**
 * SVN based representation of IRepositoryFolder
 * 
 * @author Alexander Gurov
 */
public class SVNRepositoryFolder extends SVNRepositoryContainer implements IRepositoryFolder {
	private static final long serialVersionUID = -8790962415969490733L;
	
	public SVNRepositoryFolder(IRepositoryLocation location, String url, SVNRevision selectedRevision) {
		super(location, url, selectedRevision);
	}
	
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof IRepositoryFolder)) {
			return false;
		}
		return super.equals(obj);
	}
	
}
