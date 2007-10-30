/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileModificationValidator;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.utility.LockProposeUtility;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * SVN file modification validator 
 * 
 * @author Sergiy Logvin
 */
public class SVNTeamModificationValidator implements IFileModificationValidator {
	public IStatus validateEdit(IFile[] files, final Object context) {
		if (FileUtility.isConnected(files[0])) {
			final IResource[] needsLockResources = this.getNeedsLockResources(files);
			if (needsLockResources.length > 0) {
				LockProposeUtility.proposeLock(needsLockResources, context == null ? UIMonitorUtility.getShell() : (Shell)context);
			}
		}
		
		return Status.OK_STATUS;
	}
	
	public IStatus validateSave(IFile file) {
		return Status.OK_STATUS;
	}
	
	protected IResource[] getNeedsLockResources(IResource []files) {
		List returnResources = new ArrayList();
		SVNRemoteStorage storage = SVNRemoteStorage.instance();
		IResource[] needsLockResources = FileUtility.getResourcesRecursive(files, IStateFilter.SF_NEEDS_LOCK, IResource.DEPTH_ZERO);
		for (int i = 0; i < needsLockResources.length; i++) {
			ILocalResource local = storage.asLocalResource(needsLockResources[i]);
			if (local != null && !local.isLocked()) {
				returnResources.add(needsLockResources[i]);
			}
		}
		return (IResource[])returnResources.toArray(new IResource[returnResources.size()]);
	}

}
