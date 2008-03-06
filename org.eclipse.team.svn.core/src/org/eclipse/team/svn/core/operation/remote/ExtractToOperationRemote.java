/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
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
	private String path;
	private boolean delitionAllowed;
	private HashMap<String, String> resource2projectNames;
	
	/**
	 * Operation for extracting remote resources to a specified location
	 * 
	 * @param incomingResources - the resources to extract array
	 * @param markedForDelition - the collection of the resource URLs to delete (can be empty but must not be null)
	 * @param path - path to extract to
	 * @param resource2projectNames - resource name to project name mapping (can be empty but must not be null)
	 * @param delitionAllowed - specifies if deletion allowed if the resource is marked for deletion
	 */
	public ExtractToOperationRemote(IRepositoryResource []incomingResources, Collection<String> markedForDelition, String path, HashMap<String, String> resource2projectNames, boolean delitionAllowed) {
		super("Operation.ExtractTo", incomingResources);
		this.path = path;
		this.delitionAllowed = delitionAllowed;
		this.toDelete = markedForDelition;
		this.resource2projectNames = resource2projectNames; 
	}
	
	/**
	 * Operation for extracting remote resources to a specified location
	 * 
	 * @param incomingResourcesProvider - incoming resources to extract provider
	 * @param markedForDelition - the collection of the resource URLs to delete (can be empty but must not be null)
	 * @param path - path to extract to
	 * @param resource2projectNames - resource name to project name mapping (can be empty but must not be null)
	 * @param delitionAllowed - specifies if deletion allowed if the resource is marked for deletion
	 */
	public ExtractToOperationRemote(IRepositoryResourceProvider incomingResourcesProvider, Collection<String> markedForDelition, String path, HashMap<String, String> resource2projectNames, boolean delitionAllowed) {
		super("Operation.ExtractTo", incomingResourcesProvider);
		this.path = path;
		this.delitionAllowed = delitionAllowed;
		this.toDelete = markedForDelition;
		this.resource2projectNames = resource2projectNames;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRepositoryResource []resources = this.operableData();
		//to report progress
		int processed = 0;
		SVNUtility.reorder(resources, true);
		String previousPref = null;
		String previousPath = null;
		for (IRepositoryResource current : resources) {
			String currentURL = current.getUrl();
			String toOperate = "";
			if (previousPref == null
					|| !currentURL.startsWith(previousPref)) {
				if (current instanceof IRepositoryContainer) {
					previousPref = current.getUrl();
				}
				previousPath = "/" + current.getName();
				toOperate = this.path + previousPath;
			}
			else {
				toOperate = this.path + previousPath + currentURL.substring(previousPref.length());
			}
			for (String name : this.resource2projectNames.keySet()) {
				if (toOperate.contains(name)) {
					String [] parts = toOperate.split(name);
					toOperate = parts[0] + this.resource2projectNames.get(name);
					for (int i = 1; i < parts.length; i++) {
						toOperate += parts[i];
					}
				}
			}
			File operatingDirectory = new File(toOperate);
			if (toDelete.contains(current.getUrl())) {
				if (operatingDirectory.exists() && this.delitionAllowed)
				{
					FileUtility.deleteRecursive(operatingDirectory);
				}
			}
			else {
				if (current instanceof IRepositoryContainer) {
					monitor.subTask(SVNTeamPlugin.instance().getResource("Operation.ExtractTo.Folders", new String [] {currentURL}));
					operatingDirectory.mkdirs();
				}
				else {
					monitor.subTask(SVNTeamPlugin.instance().getResource("Operation.ExtractTo.Folders", new String [] {currentURL}));
					String parentUrl = current.getParent().getUrl();
					if (previousPref != null) {
						String dirsToMake = this.path + previousPath + parentUrl.substring(previousPref.length());
						for (String name : this.resource2projectNames.keySet()) {
							if (dirsToMake.contains(name)) {
								String [] parts = dirsToMake.split(name);
								dirsToMake = parts[0] + this.resource2projectNames.get(name);
								for (int i = 1; i < parts.length; i++) {
									dirsToMake += parts[i];
								}
							}
						}
						new File(dirsToMake).mkdirs();
					}
					monitor.subTask(SVNTeamPlugin.instance().getResource("Operation.ExtractTo.RemoteFile", new String [] {currentURL}));
					this.downloadFile(current, toOperate, monitor);
				}
			}
			ProgressMonitorUtility.progress(monitor, processed++, resources.length);
		}
	}
	
	protected void downloadFile(IRepositoryResource remote, String downloadPath, IProgressMonitor monitor) throws Exception {
		FileOutputStream stream = null;
		IRepositoryLocation location = remote.getRepositoryLocation();
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
			try {
				stream = new FileOutputStream(downloadPath);
				proxy.streamFileContent(SVNUtility.getEntryRevisionReference(remote), 2048, stream, new SVNProgressMonitor(this, monitor, null));
			}
			finally {
				if (stream != null) {
					try {stream.close();} catch (Exception ex) {}
				}
			}
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}

}
