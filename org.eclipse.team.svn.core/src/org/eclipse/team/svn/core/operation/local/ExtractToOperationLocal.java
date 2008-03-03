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

package org.eclipse.team.svn.core.operation.local;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Extract selected resources to location (only local resources)
 * Used from synchronize view ExtractTo outgoing action
 * 
 * @author Alexei Goncharov
 */
public class ExtractToOperationLocal extends AbstractActionOperation {

	private IResource [] outgoingResources;
	private ArrayList<IResource> allResources;
	private String path;
	private boolean delitionAllowed;
	
	/**
	 * Operation for extracting local resources to a location
	 * 
	 * @param outgoingResources - the resources to extract array
	 * @param allSelected - also selected resources (for SynchView only)
	 * @param path - path to extract to
	 * @param delitionAllowed - specifies if delition allowed if the resource is marked for delition
	 */
	public ExtractToOperationLocal(IResource [] outgoingResources, IResource [] allSelected, String path, boolean delitionAllowed) {
		super(SVNTeamPlugin.instance().getResource("Operation.ExtractTo"));
		this.outgoingResources = outgoingResources;
		this.allResources = new ArrayList<IResource>();
		for (int i = 0; i < allSelected.length; i++) {
			this.allResources.add(allSelected[i]);
		}
		this.path = path;
		this.delitionAllowed = delitionAllowed;
	}
	
	/**
	 * Operation for extracting local resources to a location
	 * 
	 * @param outgoingResources - the resources to extract array
	 * @param path - path to extract to
	 * @param delitionAllowed - specifies if delition allowed if the resource is marked for delition
	 */
	public ExtractToOperationLocal(IResource [] outgoingResources, String path, boolean delitionAllowed) {
		this(outgoingResources, new IResource[0], path, delitionAllowed);
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		ArrayList<IResource> operableResources = new ArrayList<IResource>(Arrays.asList(this.outgoingResources));
		IResource [] parents = FileUtility.getParents(this.outgoingResources, false);
		for (int i = 0; i < parents.length; i++) {
			if (this.allResources.contains(parents[i])) {
				operableResources.add(parents[i]);
			}
		}
		this.outgoingResources = operableResources.toArray(new IResource[operableResources.size()]);		
		
		//progressReporter
		int processed = 0;
		
		FileUtility.reorder(this.outgoingResources, true);
		
		IPath previousPref = null;
		for (IResource current : this.outgoingResources) {
			IPath currentPath = current.getFullPath();
			String toOperate = "";
			if (previousPref == null
					|| !previousPref.isPrefixOf(currentPath)) {
				toOperate = this.path + "/" + current.getName();
				previousPref = current.getFullPath();
			}
			else {
				toOperate = this.path + previousPref + (currentPath.toString()).substring(previousPref.toString().length());
			}
			File operatingDirectory = new File(toOperate);
			if (IStateFilter.SF_DELETED.accept(SVNRemoteStorage.instance().asLocalResource(current))) {
				if (operatingDirectory.exists() && this.delitionAllowed) {
					FileUtility.deleteRecursive(operatingDirectory);
				}
			}
			else {
				if (new File(FileUtility.getWorkingCopyPath(current)).isDirectory()) {
					monitor.subTask(SVNTeamPlugin.instance().getResource("Operation.ExtractTo.Folders", new String [] {FileUtility.getWorkingCopyPath(current)}));
					operatingDirectory.mkdirs();
				}
				else {
					toOperate = this.path + previousPref + (current.getParent().getFullPath().toString()).substring(previousPref.toString().length());
					monitor.subTask(SVNTeamPlugin.instance().getResource("Operation.ExtractTo.Folders", new String [] {FileUtility.getWorkingCopyPath(current)}));
					operatingDirectory = new File(toOperate);
					operatingDirectory.mkdirs();
					monitor.subTask(SVNTeamPlugin.instance().getResource("Operation.ExtractTo.LocalFile", new String [] {FileUtility.getWorkingCopyPath(current)}));
					FileUtility.copyAll(operatingDirectory, new File(FileUtility.getWorkingCopyPath(current)), monitor);
				}
			}
			ProgressMonitorUtility.progress(monitor, processed++, this.outgoingResources.length);
		}
	}	
}
