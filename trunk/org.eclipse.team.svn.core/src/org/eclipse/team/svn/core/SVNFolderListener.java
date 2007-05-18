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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.client.Status;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.operation.local.management.ReconnectProjectOperation;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * This listener is designed to handle SVN team private members
 * 
 * @author Alexander Gurov
 */
public class SVNFolderListener implements IResourceChangeListener {

	public SVNFolderListener() {

	}

	public void resourceChanged(final IResourceChangeEvent event) {
		try {
	    	ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(final IProgressMonitor monitor) throws CoreException {
					event.getDelta().accept(new IResourceDeltaVisitor() {
						public boolean visit(IResourceDelta delta) throws CoreException {
							IResource resource = delta.getResource();
							
							if (resource.getType() == IResource.ROOT) {
								return true;
							}
							
							if (resource.getType() == IResource.PROJECT && 
								(delta.getKind() == IResourceDelta.ADDED || delta.getKind() == IResourceDelta.CHANGED) &&
								delta.getFlags() == IResourceDelta.OPEN &&
								SVNTeamPlugin.instance().getOptionProvider().isAutomaticProjectShareEnabled() &&
								((IProject)resource).isOpen()) {
								Status info = SVNUtility.getSVNInfoForNotConnected(resource);
								if (info != null && info.url != null) {
									String url = SVNUtility.decodeURL(info.url);
									IRepositoryRoot []roots = SVNUtility.findRoots(url, true);
									if (roots != null && roots.length == 1) {
										ProgressMonitorUtility.doTaskExternalDefault(new ReconnectProjectOperation(new IProject[] {(IProject)resource}, roots[0].getRepositoryLocation()), monitor);
									}
								}
							}
							
							if (SVNFolderListener.this.shouldBeMarked(delta)) {
							    if (resource.isAccessible()) {
									FileUtility.findAndMarkSVNInternals(resource, true);
									resource.refreshLocal(IResource.DEPTH_INFINITE, null);
									resource.getParent().refreshLocal(IResource.DEPTH_ONE, null);
							    }
								return false;
							}
							
							return true;
						}
					});
				}
			}, null, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		}
		catch (CoreException ex) {
		    LoggedOperation.reportError(this.getClass().getName(), ex);
		}
	}

	protected boolean shouldBeMarked(IResourceDelta delta) {
		IResource resource = delta.getResource();
	    if (delta.getKind() == IResourceDelta.ADDED &&
			FileUtility.isSVNInternals(resource)) {
	        return true;
	    }
        return false;
	}
	
}
