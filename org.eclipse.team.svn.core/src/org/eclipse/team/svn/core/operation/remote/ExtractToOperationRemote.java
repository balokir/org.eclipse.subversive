/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.remote;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.local.InitExtractLogOperation;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Extract selected resources to location (only remote resources)
 * Used from synchronize view ExtractTo incoming action
 * 
 * @author Alexei Goncharov
 */
public class ExtractToOperationRemote extends AbstractRepositoryOperation {
	private Collection<String> toDelete;
	private IRepositoryResourceProvider deletionsProvider;
	private String path;
	private boolean delitionAllowed;
	private HashMap<String, String> exportRoots2Names;
	
	/**
	 * Operation for extracting remote resources to a specified location
	 * 
	 * @param incomingResources - the resources to extract array
	 * @param markedForDelition - the collection of the resource URLs to delete (can be empty but must not be null)
	 * @param path - path to extract to
	 * @param exportRoots2Names - resource URL to project name mapping (can be empty but must not be null)
	 * @param delitionAllowed - specifies if deletion allowed if the resource is marked for deletion
	 */
	public ExtractToOperationRemote(IRepositoryResource []incomingResources, Collection<String> markedForDelition, String path, HashMap<String, String> resource2projectNames, boolean delitionAllowed) {
		super("Operation.ExtractTo", incomingResources);
		this.path = path;
		this.delitionAllowed = delitionAllowed;
		this.toDelete = markedForDelition;
		this.exportRoots2Names = resource2projectNames; 
	}
	
	/**
	 * Operation for extracting remote resources to a specified location
	 * 
	 * @param incomingResourcesProvider - incoming resources to extract provider
	 * @param markedForDelition - the collection of the resource URLs to delete (can be empty but must not be null)
	 * @param path - path to extract to
	 * @param exportRoots2Names - resource URL to project name mapping (can be empty but must not be null)
	 * @param delitionAllowed - specifies if deletion allowed if the resource is marked for deletion
	 */
	public ExtractToOperationRemote(IRepositoryResourceProvider incomingResourcesProvider, Collection<String> markedForDelition, String path, HashMap<String, String> exportRoots2Names, boolean delitionAllowed) {
		super("Operation.ExtractTo", incomingResourcesProvider);
		this.path = path;
		this.delitionAllowed = delitionAllowed;
		this.toDelete = markedForDelition;
		this.exportRoots2Names = exportRoots2Names;
	}

	public ExtractToOperationRemote(IRepositoryResourceProvider incomingResourcesProvider, IRepositoryResourceProvider markedForDelition, String path, HashMap<String, String> exportRoots2Names, boolean delitionAllowed) {
		super("Operation.ExtractTo", incomingResourcesProvider);
		this.path = path;
		this.delitionAllowed = delitionAllowed;
		this.deletionsProvider = markedForDelition;
		this.exportRoots2Names = exportRoots2Names;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRepositoryResource []resources = this.operableData();
		if (this.deletionsProvider != null) {
			IRepositoryResource []deletions = this.deletionsProvider.getRepositoryResources();
			this.toDelete = new HashSet<String>();
			if (deletions != null) {
				for (IRepositoryResource deletion : deletions) {
					this.toDelete.add(deletion.getUrl());
				}
			}
		}
		int processed = 0;
		SVNUtility.reorder(resources, true);
		String previousPref = null;
		String previousPath = null;
		for (IRepositoryResource current : resources) {
			String currentURL = current.getUrl();
			Path currentPath = new Path(currentURL);
			String toOperate = "";
			String rootUrl = null;
			String rootName = null;
			for (String url : this.exportRoots2Names.keySet()) {
				if (new Path(url).isPrefixOf(currentPath)) {
					rootUrl = url;
					rootName = this.exportRoots2Names.get(url);
				}
			}
			if (previousPref == null || !new Path(previousPref).isPrefixOf(currentPath)) {
				if (current instanceof IRepositoryContainer) {
					previousPref = current.getUrl();
				}
				previousPath = "/" + (rootUrl == null ? current.getName() : current.getUrl().substring(rootUrl.lastIndexOf('/') + 1));
				toOperate = this.path + previousPath;
			}
			else {
				toOperate = this.path + previousPath + currentURL.substring(previousPref.length());
			}
			if (rootUrl != null) {
				String projectRepoName = rootUrl.substring(rootUrl.lastIndexOf("/") + 1);
				String [] parts = toOperate.split(projectRepoName);
				toOperate = parts[0] + rootName;
				for (int i = 1; i < parts.length; i++) {
					toOperate += parts[i];
				}
			}
			File operatingDirectory = new File(toOperate);
			InitExtractLogOperation.logToAll(this.path, operatingDirectory.getAbsolutePath().substring(this.path.length() + 1));
			if (this.toDelete.contains(current.getUrl())) {
				InitExtractLogOperation.logToDeletions(this.path, operatingDirectory.getAbsolutePath().substring(this.path.length() + 1), true);
				if (operatingDirectory.exists() && this.delitionAllowed)
				{
					FileUtility.deleteRecursive(operatingDirectory);
				}
			}
			else if (current instanceof IRepositoryContainer) {
				monitor.subTask(SVNTeamPlugin.instance().getResource("Operation.ExtractTo.Folders", new String [] {currentURL}));
				operatingDirectory.mkdirs();
			}
			else {
				monitor.subTask(SVNTeamPlugin.instance().getResource("Operation.ExtractTo.Folders", new String [] {currentURL}));
				if (operatingDirectory.getParentFile() != null) {
					operatingDirectory.getParentFile().mkdirs();
				}
				monitor.subTask(SVNTeamPlugin.instance().getResource("Operation.ExtractTo.RemoteFile", new String [] {currentURL}));
				this.downloadFile(current, toOperate, monitor);
			}
			ProgressMonitorUtility.progress(monitor, processed++, resources.length);
		}
	}
	
	protected void downloadFile(IRepositoryResource remote, String downloadPath, IProgressMonitor monitor) throws Exception {
		IRepositoryLocation location = remote.getRepositoryLocation();
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
			FileOutputStream stream = new FileOutputStream(downloadPath);
			try {
				proxy.streamFileContent(SVNUtility.getEntryRevisionReference(remote), 2048, stream, new SVNProgressMonitor(this, monitor, null));
			}
			finally {
				try {stream.close();} catch (Exception ex) {}
			}
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}

	public int getOperationWeight() {
		if (this.operableData() != null && this.operableData().length == 0) {
			return 0;
		}
		return 4;
	}
	
}
