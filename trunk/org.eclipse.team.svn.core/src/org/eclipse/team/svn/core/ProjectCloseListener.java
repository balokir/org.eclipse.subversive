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

package org.eclipse.team.svn.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.resource.events.ProjectStatesChangedEvent;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Project close listener. Clean local status cache.
 * 
 * @author Alexander Gurov
 */
public class ProjectCloseListener implements IResourceChangeListener {
	public void resourceChanged(IResourceChangeEvent event) {
		IProject []projects = new IProject[] {(IProject)event.getResource()};
		SVNRemoteStorage.instance().fireResourceStatesChangedEvent(new ProjectStatesChangedEvent(projects, event.getType() == IResourceChangeEvent.PRE_CLOSE ? ProjectStatesChangedEvent.ST_PRE_CLOSED : ProjectStatesChangedEvent.ST_PRE_DELETED));
		if (RepositoryProvider.getProvider(projects[0], SVNTeamPlugin.NATURE_ID) != null) {
			ProgressMonitorUtility.doTaskScheduled(new RefreshResourcesOperation(projects, IResource.DEPTH_ZERO, RefreshResourcesOperation.REFRESH_CACHE));
		}
	}

}
