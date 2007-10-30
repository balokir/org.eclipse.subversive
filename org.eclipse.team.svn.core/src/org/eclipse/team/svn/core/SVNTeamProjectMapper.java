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

package org.eclipse.team.svn.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Project-to-SVNTeamProvider mapper
 * 
 * @author Alexander Gurov
 */
public class SVNTeamProjectMapper {
	public static void map(final IProject project, IRepositoryResource resource) throws Exception {
		SVNTeamProvider.map(project, resource);
		// initially mark team private members
    	ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			public void run(final IProgressMonitor monitor) throws CoreException {
				FileUtility.findAndMarkSVNInternals(project, true);
			}
		}, null, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
	}

}
