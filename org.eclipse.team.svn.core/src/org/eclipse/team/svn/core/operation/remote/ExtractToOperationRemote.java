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
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Extract selected resources to location (only remote resources)
 * Used from synchronize view ExtractTo incoming action
 * 
 * @author Alexei Goncharov
 */
public class ExtractToOperationRemote extends AbstractActionOperation {

	private IResource [] incomingResources;
	private HashSet<IResource> toDelete;
	private String path;
	private boolean delitionAllowed;
	
	public ExtractToOperationRemote(IResource [] incomingResources, HashSet<IResource> markedForDelition, String path, boolean delitionAllowed) {
		super(SVNTeamPlugin.instance().getResource("Operation.ExtractTo"));
		this.incomingResources = incomingResources;
		this.path = path;
		this.delitionAllowed = delitionAllowed;
		this.toDelete = markedForDelition;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		HashSet<IResource> operableFolders = new HashSet<IResource>();
		HashSet<IResource> operableFiles = new HashSet<IResource>();
		HashMap<IProject, Integer> firstNeededSegments = new HashMap<IProject, Integer>();
		for (int i = 0; i < this.incomingResources.length; i++) {
			if (SVNRemoteStorage.instance().asRepositoryResource(this.incomingResources[i]) instanceof IRepositoryContainer) {
				operableFolders.add(this.incomingResources[i]);
			}
			else {
				operableFiles.add(this.incomingResources[i]);
			}
			int preSegmentsCount = this.incomingResources[i].getFullPath().segmentCount() - 1;
			Integer firstNeededSegment = firstNeededSegments.get(this.incomingResources[i].getProject());
			if (firstNeededSegment == null || preSegmentsCount < firstNeededSegment) {
				firstNeededSegments.put(this.incomingResources[i].getProject(), preSegmentsCount);
			}
		}
		
		//to report progress
		int processed = 0;

		//folders first - to create all needed
		IResource [] toOperateFurhter = operableFolders.toArray(new IResource[0]);
		for (int i = 0; i < toOperateFurhter.length; i++) {
			ProgressMonitorUtility.progress(monitor, processed++, this.incomingResources.length);
			IPath resourcePath = toOperateFurhter[i].getFullPath();
			IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(toOperateFurhter[i]);
			monitor.subTask(SVNTeamPlugin.instance().getResource("Operation.ExtractTo.Folders", new String [] {remote.getUrl()}));
			String toOperate = "";
			int firstNeededSegment = firstNeededSegments.get(toOperateFurhter[i].getProject());
			for (int j = firstNeededSegment; j < resourcePath.segmentCount(); j++) {
				toOperate = toOperate + "\\" + resourcePath.segment(j);
			}
			File fileToOperate = new File(this.path + toOperate);
			if (this.toDelete.contains(toOperateFurhter[i])) {
				if (fileToOperate.exists() && this.delitionAllowed) {
					fileToOperate.delete();
				}
			}
			else {
				fileToOperate.mkdirs();
			}
		}

		//then files
		toOperateFurhter = operableFiles.toArray(new IResource[0]);
		for (int i = 0; i < toOperateFurhter.length; i++) {
			ProgressMonitorUtility.progress(monitor, processed++, this.incomingResources.length);
			IPath resourcePath = toOperateFurhter[i].getFullPath();
			IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(toOperateFurhter[i]);
			monitor.subTask(SVNTeamPlugin.instance().getResource("Operation.ExtractTo.RemoteFile", new String [] {remote.getUrl()}));
			String pathToOperate = this.path;
			File to = new File(pathToOperate);
			for (int j = 0; j < resourcePath.segmentCount() - 1; j++) {
				pathToOperate = pathToOperate + "\\" + resourcePath.segment(j);
				to = new File(pathToOperate);
				if (!to.exists()) {
					pathToOperate = this.path;
				}
			}
			if (this.toDelete.contains(toOperateFurhter[i])) {
				to = new File(pathToOperate + "\\" + toOperateFurhter[i].getName());
				if (to.exists() && this.delitionAllowed) {
					to.delete();
				}
			}
			else {					
				this.downloadFile(remote, pathToOperate, monitor);
			}
		}
	}
	
	protected void downloadFile(IRepositoryResource remote, String downloadTo, IProgressMonitor monitor) throws Exception {
		FileOutputStream stream = null;
		IRepositoryLocation location = remote.getRepositoryLocation();
		ISVNConnector proxy = location.acquireSVNProxy();
		try {
			try {
				stream = new FileOutputStream(downloadTo + "\\" + remote.getName());
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
