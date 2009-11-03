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

package org.eclipse.team.svn.ui.action.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.action.AbstractRecursiveTeamAction;
import org.eclipse.team.svn.ui.lock.LockResource;
import org.eclipse.team.svn.ui.lock.LocksComposite;
import org.eclipse.team.svn.ui.lock.LockResource.LockStatusEnum;
import org.eclipse.team.svn.ui.operation.ScanLocksOperation;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Lock action implementation
 * 
 * @author Alexander Gurov
 */
public class LockAction extends AbstractRecursiveTeamAction {

    public LockAction() {
        super();
    }

	public void runImpl(IAction action) {		
		//get resources which can be locked
		List<IResource> filteredResourcesList = new ArrayList<IResource>();		
		IResource[] filteredResources = this.getSelectedResourcesRecursive(IStateFilter.SF_VERSIONED);
		for (IResource filteredResource : filteredResources) {
			if (filteredResource.getType() == IResource.FILE && filteredResource.getLocation() != null) {
				filteredResourcesList.add(filteredResource);
			}
		}
		
		List<LockResource> lockResources = LockAction.getLockResources(this.getSelectedResources(), filteredResourcesList.toArray(new IResource[0]));
		if (lockResources != null) {
			Iterator<LockResource> iter = lockResources.iterator();
			while (iter.hasNext()) {
				LockResource lockResource = iter.next();
				if (lockResource.getLockStatus() == LockStatusEnum.LOCALLY_LOCKED) {
					iter.remove();
				}
			}
			
			IActionOperation op = LocksComposite.performLockAction(lockResources.toArray(new LockResource[0]), false, this.getShell());
			if (op != null) {
				this.runScheduled(op);
			}				
		}													
	}
	
	/**
	 * @param parentResources		resources for which scan locks operation will be called
	 * 								in order to detect lock info, e.g. resource can be locked outside. Indeed,
	 *  							parentResources are parent of resourcesToProcess
	 * @param resourcesToProcess	resources for which lock info should be returned
	 * @return
	 */
	public static List<LockResource> getLockResources(IResource[] parentResources, IResource[] resourcesToProcess) {
		/*
		 * Scan operation can be called only for resources which exist on repository. If resource isn't on repository, 
		 * e.g. lock operation is called at unversioned external folder,
		 * we can't detect outside lock statuses, so we use only local lock info.
		 */		
		parentResources = FileUtility.getResourcesRecursive(parentResources, IStateFilter.SF_ONREPOSITORY, IResource.DEPTH_ZERO);
		
		List<LockResource> processedLockResources = new ArrayList<LockResource>();		
		ScanLocksOperation scanLocksOp = new ScanLocksOperation(parentResources);
		if (!UIMonitorUtility.doTaskNowDefault(scanLocksOp, true).isCancelled() && scanLocksOp.getExecutionState() == IActionOperation.OK) {
			LockResource[] lockResources = scanLocksOp.getLockResources();
			Map<IPath, LockResource> lockResourcesMap = new HashMap<IPath, LockResource>(); 
			for (LockResource lockResource : lockResources) {				
				lockResourcesMap.put(new Path(lockResource.getFullFileSystemPath()), lockResource);
			}
			
			//match IResource with LocalResource			
			for (IResource filteredResource : resourcesToProcess) {
				IPath path = filteredResource.getLocation();				
				LockResource lr = lockResourcesMap.get(path);
				if (lr == null) {
					ILocalResource local = SVNRemoteStorage.instance().asLocalResource(filteredResource);
					if (local.isLocked()) {
						lr = new LockResource(filteredResource.getName(), null, true, LockStatusEnum.LOCALLY_LOCKED, null, null, FileUtility.getWorkingCopyPath(filteredResource), null);						
					} else {
						lr = LockResource.createNotLockedFile(filteredResource.getName(), FileUtility.getWorkingCopyPath(filteredResource));
					}
				}
				String parentPath = filteredResource.getParent().getFullPath().toString();
				if (parentPath.startsWith("/")) { //$NON-NLS-1$
					parentPath = parentPath.substring(1);
				}														
				LockResource directory = LockResource.createDirectory(parentPath);
				directory.addChild(lr);
				
				processedLockResources.add(lr);				 								
			}
		} else {
			return null;
		}
		return processedLockResources;
	}
	
    public boolean isEnabled() {
        return this.checkForResourcesPresenceRecursive(IStateFilter.SF_READY_TO_LOCK);
    }
    
}
