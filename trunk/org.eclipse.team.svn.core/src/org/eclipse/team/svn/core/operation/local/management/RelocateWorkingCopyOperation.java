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

package org.eclipse.team.svn.core.operation.local.management;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.core.BaseMessages;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.SVNTeamProvider;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNChangeStatus;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNNullProgressMonitor;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.local.AbstractWorkingCopyOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * This operation relocate any projects associated with the selected repository 
 * 
 * @author Alexander Gurov
 */
public class RelocateWorkingCopyOperation extends AbstractWorkingCopyOperation implements IResourceProvider {
	protected IRepositoryLocation location;
	protected List<IProject> resources;

	public RelocateWorkingCopyOperation(IResource []resources, IRepositoryLocation location) {
		super("Operation_RelocateResources", SVNMessages.class, resources); //$NON-NLS-1$
		this.location = location;
	}
	
	public RelocateWorkingCopyOperation(IResourceProvider provider, IRepositoryLocation location) {
		super("Operation_RelocateResources", SVNMessages.class, provider); //$NON-NLS-1$
		this.location = location;
	}

	public IResource []getResources() {
		return this.resources == null ? new IResource[0] : this.resources.toArray(new IResource[this.resources.size()]);
	}
	
	public ISchedulingRule getSchedulingRule() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		this.resources = new ArrayList<IProject>();
		IResource []projects = this.operableData();
		if (projects.length == 0) {
			return;
		}
		final ISVNConnector proxy = this.location.acquireSVNProxy();
		
		try {
			final IRepositoryResource []children = this.location.getRepositoryRoot().getChildren();
			final HashSet<String> processedPaths = new HashSet<String>(); // handle nested projects
			
			for (int i = 0; i < projects.length && !monitor.isCanceled(); i++) {
				final IProject current = (IProject)projects[i];
				this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws Exception {
						IPath fsLocation = FileUtility.getResourcePath(current);
						if (fsLocation != FileUtility.getAlwaysIgnoredPath()) {
							String path = RelocateWorkingCopyOperation.this.getWCRootPath(fsLocation.toString());
							if (!processedPaths.contains(path)) {
								SVNChangeStatus []stats = SVNUtility.status(proxy, path, SVNDepth.EMPTY, ISVNConnector.Options.INCLUDE_UNCHANGED, new SVNNullProgressMonitor());
								if (stats.length > 0 && stats[0].url != null) {
									String url = SVNUtility.decodeURL(stats[0].url);
									String newURL = RelocateWorkingCopyOperation.this.remapURL(url, children);
									if (!url.equals(newURL)) {
										RelocateWorkingCopyOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn switch --relocate \"" + newURL + "\" \"" + FileUtility.normalizePath(path) + "\"" + FileUtility.getUsernameParam(RelocateWorkingCopyOperation.this.location.getUsername()) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
										proxy.relocate(url, newURL, path, SVNDepth.INFINITY, new SVNProgressMonitor(RelocateWorkingCopyOperation.this, monitor, null));
									}
								}
								processedPaths.add(path);
							}
							SVNTeamProvider provider = (SVNTeamProvider)RepositoryProvider.getProvider(current, SVNTeamPlugin.NATURE_ID);
							provider.relocateResource();
							RelocateWorkingCopyOperation.this.resources.add(current);
						}
					}
				}, monitor, projects.length);
			}
		}
		finally {
		    this.location.releaseSVNProxy(proxy);
		}
	}
	
	public String remapURL(String oldUrl, IRepositoryResource []rootChildren) {
		for (int i = 0; i < rootChildren.length; i++) {
			String childName = rootChildren[i].getName();
			int idx = oldUrl.indexOf(childName);
			if (idx > 0 && oldUrl.charAt(idx - 1) == '/' && (oldUrl.endsWith(childName) || oldUrl.charAt(idx + childName.length()) == '/')) {
				return rootChildren[i].getUrl() + oldUrl.substring(idx + childName.length()); 
			}
		}
		return null;
	}
	
	protected String getWCRootPath(String path) {
		File wcDB = FileUtility.findWCDB(new File(path));
		if (wcDB != null) {
			return wcDB.getParentFile().getParent();
		}
		return path;
	}
	
	protected String getShortErrorMessage(Throwable t) {
		return BaseMessages.format(super.getShortErrorMessage(t), new Object[] {this.location.getUrl()});
	}

}
