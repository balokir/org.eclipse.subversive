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
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Project-to-SVNTeamProvider mapper
 * 
 * @author Alexander Gurov
 */
public class SVNTeamProjectMapper {
	public static void map(IProject project, IRepositoryResource resource) throws Exception {
		SVNTeamProvider.map(project, resource);
		// initially mark team private members
		FileUtility.findAndMarkSVNInternals(project, true);
	}

}
