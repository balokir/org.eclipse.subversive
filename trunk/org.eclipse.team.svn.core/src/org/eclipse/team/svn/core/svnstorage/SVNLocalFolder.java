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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.local.GetAllResourcesOperation;
import org.eclipse.team.svn.core.resource.ILocalFolder;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Working Copy folder representation
 * 
 * @author Alexander Gurov
 */
public class SVNLocalFolder extends SVNLocalResource implements ILocalFolder {
	public SVNLocalFolder(IResource resource, long revision, long baseRevision, String status, int changeMask, String author, long lastCommitDate) {
		super(resource, revision, baseRevision, status, changeMask, author, lastCommitDate);
	}

	public ILocalResource []getChildren() {
		IContainer root = (IContainer)this.resource;
		List<ILocalResource> members = new ArrayList<ILocalResource>();
		
		GetAllResourcesOperation op = new GetAllResourcesOperation(root);
		ProgressMonitorUtility.doTaskExternalDefault(op, new NullProgressMonitor());
		IResource []resources = op.getChildren();
		for (int i = 0; i < resources.length; i++) {
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resources[i]);
			if (local != null && local.getStatus() != IStateFilter.ST_NOTEXISTS) {
				members.add(local);
			}
		}
		
		return members.toArray(new ILocalResource[members.size()]);
	}

}
