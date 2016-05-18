/*******************************************************************************
 * Copyright (c) 2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.m2e;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.m2e.scm.ScmUrl;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.repository.model.IResourceTreeNode;

/**
 * @author Eugene Kuleshov
 */
public class SvnUrlAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("rawtypes")
	private static final Class[] ADAPTER_TYPES = new Class[] { ScmUrl.class };

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return SvnUrlAdapterFactory.ADAPTER_TYPES;
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Object adaptable, Class adapterType) {
		// IResourceTreeNode is common interface for both UI objects: RepositoryFolder and RepositoryLocation
		if (ScmUrl.class.equals(adapterType) && (adaptable instanceof IResourceTreeNode)) {
			IRepositoryResource repositoryResource = ((IResourceTreeNode)adaptable).getRepositoryResource();

			String scmUrl = SVNScmHandler.SVN_SCM_ID + repositoryResource.getUrl();
			String scmParentUrl = null;

			IRepositoryResource parent = repositoryResource.getParent();
			if (parent != null) {
				scmParentUrl = SVNScmHandler.SVN_SCM_ID + parent.getUrl();
			}

			return new ScmUrl(scmUrl, scmParentUrl);
		}
		return null;
	}

}
