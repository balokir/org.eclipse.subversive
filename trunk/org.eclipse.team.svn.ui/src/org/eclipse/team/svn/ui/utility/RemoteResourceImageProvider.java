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

package org.eclipse.team.svn.ui.utility;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * Image provider for the remote resources
 *
 * @author Elena Matokhina
 */
public class RemoteResourceImageProvider {
	
	public static ImageDescriptor getDescriptor(IAdaptable resource) {
		ImageDescriptor iDescr = null;
		if (resource instanceof IRepositoryRoot) {
			IRepositoryRoot root = (IRepositoryRoot)resource;
			String iconName = "icons/objects/root.gif";
			switch (root.getKind()) {
				case IRepositoryRoot.KIND_TRUNK: {
					iconName = "icons/objects/head.gif";
					break;
				}
				case IRepositoryRoot.KIND_BRANCHES: {
					iconName = "icons/objects/branches.gif";
					break;
				}
				case IRepositoryRoot.KIND_TAGS: {
					iconName = "icons/objects/tags.gif";
					break;
				}
			}
			iDescr = SVNTeamUIPlugin.instance().getImageDescriptor(iconName);
		}
		else {
			if (resource == null) {
				iDescr = PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor("");
			} else {
				IWorkbenchAdapter adapter = (IWorkbenchAdapter)resource.getAdapter(IWorkbenchAdapter.class);
				if (adapter == null) {
					return null;
				}
				iDescr = adapter.getImageDescriptor(resource);
			}
		}
		return iDescr;
	}
}
