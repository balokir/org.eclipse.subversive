/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Kris De Volder  - https://bugs.eclipse.org/bugs/show_bug.cgi?id=361831
 *******************************************************************************/

package org.eclipse.team.svn.core;

import org.eclipse.core.resources.IContainer;
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
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.core.connector.SVNChangeStatus;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.options.IOptionProvider;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.operation.local.management.ReconnectProjectOperation;
import org.eclipse.team.svn.core.operation.remote.management.AddRepositoryLocationOperation;
import org.eclipse.team.svn.core.operation.remote.management.SaveRepositoryLocationsOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
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
							
						    if (!resource.isAccessible()) {
								return false;
						    }
						    
							if (resource.getType() == IResource.ROOT) {
								return true;
							}
							
							if (resource.getType() == IResource.PROJECT && delta.getKind() == IResourceDelta.ADDED && delta.getFlags() == IResourceDelta.OPEN &&
								SVNTeamPlugin.instance().getOptionProvider().is(IOptionProvider.AUTOMATIC_PROJECT_SHARE_ENABLED) && ((IProject)resource).isOpen()) {
								
								/*
								 * If project is already connected, then don't reconnect it again. 
								 * Project may be connected and we can get such event if we just checked out project.
								 * Also this may cause problem if there are several locations with the same url, see bug 298862.  
								 */
								if (RepositoryProvider.getProvider((IProject) resource, SVNTeamPlugin.NATURE_ID) != null) {
									return false;
								}

								SVNChangeStatus info = SVNUtility.getSVNInfoForNotConnected(resource);
								if (info != null && info.url != null) {
									String url = SVNUtility.decodeURL(info.url);
									IRepositoryRoot []roots = SVNUtility.findRoots(url, true);
									IRepositoryLocation location = null;
									if (roots.length == 0) {
										String rootNode = "/" + CoreExtensionsManager.instance().getOptionProvider().getString(IOptionProvider.DEFAULT_TRUNK_NAME); //$NON-NLS-1$
										int idx = url.lastIndexOf(rootNode);
										if (idx == -1 || !url.endsWith(rootNode) && url.charAt(idx + rootNode.length()) != '/') {
											rootNode = "/" + CoreExtensionsManager.instance().getOptionProvider().getString(IOptionProvider.DEFAULT_BRANCHES_NAME); //$NON-NLS-1$
											idx = url.lastIndexOf(rootNode);
											if (idx == -1 || !url.endsWith(rootNode) && url.charAt(idx + rootNode.length()) != '/') {
												rootNode = "/" + CoreExtensionsManager.instance().getOptionProvider().getString(IOptionProvider.DEFAULT_TAGS_NAME); //$NON-NLS-1$
												idx = url.lastIndexOf(rootNode);
												if (idx != -1 && !url.endsWith(rootNode) && url.charAt(idx + rootNode.length()) != '/') {
													idx = -1;
												}
											}
										}
										if (idx != -1) {
											url = url.substring(0, idx);
										}
										location = SVNRemoteStorage.instance().newRepositoryLocation();
										SVNUtility.initializeRepositoryLocation(location, url);
									}
									else {
										location = roots[0].getRepositoryLocation();
									}
									ReconnectProjectOperation mainOp = new ReconnectProjectOperation(new IProject[] {(IProject)resource}, location);
									CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
									if (roots.length == 0) {
										// important! location doubles when it is added asynchronously and several projects for the same location are imported
										//	that is why we're doing it here and now
										op.add(new AddRepositoryLocationOperation(location));
										op.add(new SaveRepositoryLocationsOperation());
									}
									op.add(mainOp);
									// do not use asynchronous execution, this is PRE_BUILD event, so we shouldn't defer marking resources as team-private
									ProgressMonitorUtility.doTaskExternal(op, monitor);
									return false;
								}
							}
							
							if (delta.getKind() == IResourceDelta.ADDED && (resource instanceof IContainer) && 
								!resource.isTeamPrivateMember() && FileUtility.isSVNInternals(resource)) {
								FileUtility.findAndMarkSVNInternals(resource, true);
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

}
