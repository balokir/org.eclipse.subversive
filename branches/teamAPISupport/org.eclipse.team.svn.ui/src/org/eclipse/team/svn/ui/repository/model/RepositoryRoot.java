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

package org.eclipse.team.svn.ui.repository.model;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * Repository root node representation 
 * 
 * @author Alexander Gurov
 */
public class RepositoryRoot extends RepositoryFolder {
	protected static String ROOT_NAME;
	
	public RepositoryRoot(RepositoryResource parent, IRepositoryResource resource) {
		super(parent, resource);
		RepositoryRoot.ROOT_NAME = SVNTeamUIPlugin.instance().getResource("RepositoriesView.Model.Root");
		this.relatesToLocation = Boolean.FALSE;
	}

	public String getLabel() {
		return this.label == null && ((IRepositoryRoot)this.resource).getKind() == IRepositoryRoot.KIND_ROOT ? RepositoryRoot.ROOT_NAME : super.getLabel();
	}

	protected ImageDescriptor getImageDescriptorImpl() {
		return this.isExternals() ? super.getImageDescriptorImpl() : (((IRepositoryRoot)this.resource).getKind() == IRepositoryRoot.KIND_ROOT ? SVNTeamUIPlugin.instance().getImageDescriptor("icons/objects/repository-root.gif") : SVNTeamUIPlugin.instance().getImageDescriptor("icons/objects/root.gif"));
	}

}
