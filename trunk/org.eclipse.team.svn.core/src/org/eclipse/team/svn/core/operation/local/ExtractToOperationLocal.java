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

import org.eclipse.core.resources.IContainer;
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
	private String path;
	private boolean delitionAllowed;
	
	/**
	 * Operation for extracting local resources to a location
	 * 
	 * @param outgoingResources - the resources to extract array
	 * @param path - path to extract to
	 * @param delitionAllowed - specifies if delition allowed if the resource is marked for delition
	 */
	public ExtractToOperationLocal(IResource [] outgoingResources, String path, boolean delitionAllowed) {
		super(SVNTeamPlugin.instance().getResource("Operation.ExtractTo"));
		this.outgoingResources = outgoingResources;
		this.path = path;
		this.delitionAllowed = delitionAllowed;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
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
				if (current instanceof IContainer) {
					previousPref = current.getFullPath();
				}
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
				if (current instanceof IContainer) {
					monitor.subTask(SVNTeamPlugin.instance().getResource("Operation.ExtractTo.Folders", new String [] {FileUtility.getWorkingCopyPath(current)}));
					operatingDirectory.mkdirs();
				}
				else {
					if (previousPref != null) {
						toOperate = this.path + previousPref + (current.getParent().getFullPath().toString()).substring(previousPref.toString().length());
						monitor.subTask(SVNTeamPlugin.instance().getResource("Operation.ExtractTo.Folders", new String [] {FileUtility.getWorkingCopyPath(current)}));
						operatingDirectory = new File(toOperate);
						operatingDirectory.mkdirs();
					}
					monitor.subTask(SVNTeamPlugin.instance().getResource("Operation.ExtractTo.LocalFile", new String [] {FileUtility.getWorkingCopyPath(current)}));
					FileUtility.copyAll(operatingDirectory, new File(FileUtility.getWorkingCopyPath(current)), FileUtility.COPY_OVERRIDE_EXISTING_FILES, null, monitor);
				}
			}
			ProgressMonitorUtility.progress(monitor, processed++, this.outgoingResources.length);
		}
	}	
}
