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


package org.eclipse.team.svn.core.operation.local;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
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
	private InitExtractLogOperation logger;
	
	/**
	 * Operation for extracting local resources to a location
	 * 
	 * @param outgoingResources - the resources to extract array
	 * @param path - path to extract to
	 * @param delitionAllowed - specifies if deletion allowed if the resource is marked for deletion
	 */
	public ExtractToOperationLocal(IResource [] outgoingResources, String path, boolean delitionAllowed, InitExtractLogOperation logger) {
		super(SVNMessages.Operation_ExtractTo, SVNMessages.class);
		this.outgoingResources = outgoingResources;
		this.path = path;
		this.delitionAllowed = delitionAllowed;
		this.logger = logger;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		FileUtility.reorder(this.outgoingResources, true);
		int processed = 0;
		IPath previousPref = null;
		for (IResource current : this.outgoingResources) {
			IPath currentPath = current.getFullPath();
			String toOperate = ""; //$NON-NLS-1$
			if (previousPref == null || !previousPref.isPrefixOf(currentPath)) {
				toOperate = this.path + "/" + current.getName(); //$NON-NLS-1$
				if (current instanceof IContainer) {
					previousPref = current.getFullPath();
				}
			}
			else {
				int toRemove = previousPref.removeLastSegments(1).toString().length();
				if (toRemove < 2) {
					toRemove = 0;
				}
				toOperate = this.path + (currentPath.toString()).substring(toRemove);
			}
			File operatingDirectory = new File(toOperate);
			ILocalResource localResource = SVNRemoteStorage.instance().asLocalResourceAccessible(current);
			if (!IStateFilter.SF_NOTMODIFIED.accept(localResource)) {
				this.logger.log(operatingDirectory.getAbsolutePath().substring(this.path.length() + 1), localResource.getStatus());
			}
			if (IStateFilter.SF_DELETED.accept(localResource)) {
				if (operatingDirectory.exists() && this.delitionAllowed) {
					FileUtility.deleteRecursive(operatingDirectory);
				}
			}
			else if (current instanceof IContainer) {
				monitor.subTask(SVNMessages.format(SVNMessages.Operation_ExtractTo_Folders, new String [] {FileUtility.getWorkingCopyPath(current)}));
				operatingDirectory.mkdirs();
			}
			else {
				if (previousPref != null) {
					File parent = operatingDirectory.getParentFile();
					if (parent != null) {
						monitor.subTask(SVNMessages.format(SVNMessages.Operation_ExtractTo_Folders, new String [] {FileUtility.getWorkingCopyPath(current)}));
						parent.mkdirs();
						operatingDirectory = parent;
					}
				}
				monitor.subTask(SVNMessages.format(SVNMessages.Operation_ExtractTo_LocalFile, new String [] {FileUtility.getWorkingCopyPath(current)}));
				FileUtility.copyAll(operatingDirectory, new File(FileUtility.getWorkingCopyPath(current)), FileUtility.COPY_OVERRIDE_EXISTING_FILES, null, monitor);
			}
			ProgressMonitorUtility.progress(monitor, processed++, this.outgoingResources.length);
		}
	}
	
	public int getOperationWeight() {
		if (this.outgoingResources.length == 0) {
			return 0;
		}
		return 1;
	}
}
