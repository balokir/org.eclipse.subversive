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

import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;

/**
 * SVN based representation of IRepositoryRoot
 * 
 * @author Alexander Gurov
 */
public class SVNRepositoryBranches extends SVNRepositoryRootBase {
	private static final long serialVersionUID = 6550266382831998834L;

	public SVNRepositoryBranches(IRepositoryLocation location, String url, SVNRevision selectedRevision) {
		super(location, url, selectedRevision);
	}
	
	public int getKind() {
		return IRepositoryRoot.KIND_BRANCHES;
	}
	
}
