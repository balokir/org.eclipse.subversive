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
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;

/**
 * Implementation of IRepositoryRoot
 * 
 * @author Alexander Gurov
 */
public class SVNRepositoryLocationRoot extends SVNRepositoryRootBase {
	private static final long serialVersionUID = 2511682499975444957L;
	
	public SVNRepositoryLocationRoot(IRepositoryLocation location) {
		super(location, location.getUrl(), SVNRevision.HEAD);
	}

	public int getKind() {
		return IRepositoryRoot.KIND_LOCATION_ROOT;
	}
	
}
