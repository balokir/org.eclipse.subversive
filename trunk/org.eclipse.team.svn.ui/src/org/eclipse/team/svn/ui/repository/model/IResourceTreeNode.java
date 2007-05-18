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

package org.eclipse.team.svn.ui.repository.model;

import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.repository.RepositoryTreeViewer;

/**
 * IRepositoryResource based tree node interface
 * 
 * @author Alexander Gurov
 */
public interface IResourceTreeNode extends IDataTreeNode {
	public IRepositoryResource getRepositoryResource();
	public void setViewer(RepositoryTreeViewer repositoryTree);
}
