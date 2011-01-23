/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *    Igor Burilo - Bug 245509: Improve extract log
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.remote;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.local.InitExtractLogOperation;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.resource.IRepositoryResourceWithStatusProvider;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Extract selected resources to location (only remote resources)
 * Used from synchronize view ExtractTo incoming action
 * 
 * @author Alexei Goncharov
 */
public class ExtractToOperationRemote extends AbstractActionOperation {
	private InitExtractLogOperation logger;
	private Collection<String> toDelete;
	private IRepositoryResourceProvider deletionsProvider;
	private IRepositoryResourceWithStatusProvider dataProvider;
	private String path;
	private boolean delitionAllowed;
	private HashMap<String, String> exportRoots2Names;
	
	/**
	 * Operation for extracting remote resources to a specified location
	 * 
	 * @param incomingResources - the resources to extract array
	 * @param statusesMap - the map of the incoming statuses associated to remote resource URLs
	 * @param markedForDelition - the collection of the resource URLs to delete (can be empty but must not be null)
	 * @param path - path to extract to
	 * @param exportRoots2Names - resource URL to project name mapping (can be empty but must not be null)
	 * @param delitionAllowed - specifies if deletion allowed if the resource is marked for deletion
	 */
	public ExtractToOperationRemote(IRepositoryResource []incomingResources, Map<String, String> statusesMap, Collection<String> markedForDelition, String path, HashMap<String, String> resource2projectNames, InitExtractLogOperation logger, boolean delitionAllowed) {
		this(new IRepositoryResourceWithStatusProvider.DefaultRepositoryResourceWithStatusProvider(incomingResources, statusesMap),
				markedForDelition,
				path,
				resource2projectNames,
				logger,
				delitionAllowed);
	}
	
	/**
	 * Operation for extracting remote resources to a specified location
	 * 
	 * @param incomingResourcesProvider - incoming resources with statuses to extract provider
	 * @param markedForDelition - the collection of the resource URLs to delete (can be empty but must not be null)
	 * @param path - path to extract to
	 * @param exportRoots2Names - resource URL to project name mapping (can be empty but must not be null)
	 * @param delitionAllowed - specifies if deletion allowed if the resource is marked for deletion
	 */
	public ExtractToOperationRemote(IRepositoryResourceWithStatusProvider incomingResourcesProvider, Collection<String> markedForDelition, String path, HashMap<String, String> exportRoots2Names, InitExtractLogOperation logger, boolean delitionAllowed) {
		super("Operation_ExtractTo", SVNMessages.class); //$NON-NLS-1$
		this.logger = logger;
		this.dataProvider = incomingResourcesProvider;
		this.path = path;
		this.delitionAllowed = delitionAllowed;
		this.toDelete = markedForDelition;
		this.exportRoots2Names = exportRoots2Names;
	}

	public ExtractToOperationRemote(IRepositoryResourceWithStatusProvider incomingResourcesProvider, IRepositoryResourceProvider markedForDelition, String path, HashMap<String, String> exportRoots2Names, InitExtractLogOperation logger, boolean delitionAllowed) {
		super("Operation_ExtractTo", SVNMessages.class); //$NON-NLS-1$
		this.logger = logger;
		this.dataProvider = incomingResourcesProvider;
		this.path = path;
		this.delitionAllowed = delitionAllowed;
		this.deletionsProvider = markedForDelition;
		this.exportRoots2Names = exportRoots2Names;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRepositoryResource []resources = this.dataProvider.getRepositoryResources();
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
		HashMap<String, String> repoFolder2localFolder = new HashMap<String, String>();
		for (IRepositoryResource current : resources) {
			String currentURL = current.getUrl();
			IPath currentPath = SVNUtility.createPathForSVNUrl(currentURL);
			String toOperate = ""; //$NON-NLS-1$
			String rootUrl = null;
			String rootName = null;
			for (String url : this.exportRoots2Names.keySet()) {
				if (SVNUtility.createPathForSVNUrl(url).isPrefixOf(currentPath)) {
					rootUrl = url;
					rootName = this.exportRoots2Names.get(url);
				}
			}
			if (current instanceof IRepositoryContainer) {
				String localPath = "/" + (rootUrl == null ? current.getName() : current.getUrl().substring(rootUrl.lastIndexOf('/') + 1)); //$NON-NLS-1$
				repoFolder2localFolder.put(currentURL, localPath);
				toOperate = localPath;
			}
			else {
				String localFolderPath; 
				String parentFolderURL = currentURL.substring(0, currentURL.lastIndexOf('/'));
				if (!repoFolder2localFolder.containsKey(parentFolderURL))
				{
					localFolderPath = "/" + (rootUrl == null ? "" : parentFolderURL.substring(rootUrl.lastIndexOf('/') + 1)); //$NON-NLS-1$
					repoFolder2localFolder.put(parentFolderURL, localFolderPath);
				}
				else
				{
					localFolderPath = repoFolder2localFolder.get(parentFolderURL); 
				}
				toOperate = "/" + localFolderPath + currentURL.substring(currentURL.lastIndexOf('/')); //$NON-NLS-1$ 
			}
			if (rootUrl != null) {
				String projectRepoName = rootUrl.substring(rootUrl.lastIndexOf("/") + 1); //$NON-NLS-1$
				int idx = toOperate.indexOf(projectRepoName);
				toOperate = toOperate.substring(0, idx) + rootName + toOperate.substring(idx + projectRepoName.length());
			}
			toOperate = this.path + toOperate;
			File operatingDirectory = new File(toOperate);
			String status = this.dataProvider.getStatusesMap().get(currentURL);
			if (status != null)
			{
				this.logger.log(operatingDirectory.getAbsolutePath().substring(this.path.length() + 1), status);
			};
			if (this.toDelete.contains(current.getUrl())) {
				if (operatingDirectory.exists() && this.delitionAllowed)
				{
					FileUtility.deleteRecursive(operatingDirectory);
				}
			}
			else if (current instanceof IRepositoryContainer) {
				monitor.subTask(SVNMessages.format(SVNMessages.Operation_ExtractTo_Folders, new String [] {currentURL}));
				operatingDirectory.mkdirs();
			}
			else {
				monitor.subTask(SVNMessages.format(SVNMessages.Operation_ExtractTo_Folders, new String [] {currentURL}));
				if (operatingDirectory.getParentFile() != null) {
					operatingDirectory.getParentFile().mkdirs();
				}
				monitor.subTask(SVNMessages.format(SVNMessages.Operation_ExtractTo_RemoteFile, new String [] {currentURL}));
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
		if (this.dataProvider.getRepositoryResources() != null && this.dataProvider.getRepositoryResources().length == 0) {
			return 0;
		}
		return 4;
	}
	
}
