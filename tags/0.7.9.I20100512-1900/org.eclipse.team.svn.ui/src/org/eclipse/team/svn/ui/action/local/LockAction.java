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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
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
		
		List<LockResource> lockResources = LockAction.getLockResources(filteredResourcesList.toArray(new IResource[0]));
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
	 * @param resourcesToProcess	resources for which lock info should be returned
	 * @return
	 */
	public static List<LockResource> getLockResources(IResource[] resourcesToProcess) {
		List<LockResource> res = new ArrayList<LockResource>();
		for (IResource resource : resourcesToProcess) {
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
			LockResource lr;
			if (local.isLocked()) {
				lr = new LockResource(resource.getName(), null, true, LockStatusEnum.LOCALLY_LOCKED, null, null, FileUtility.getWorkingCopyPath(resource), null);
			} else {
				lr = LockResource.createNotLockedFile(resource.getName(), FileUtility.getWorkingCopyPath(resource));
			}	
			if (resource.getParent() != null) {
				String parentPath = resource.getParent().getFullPath().toString();
				if (parentPath.startsWith("/")) { //$NON-NLS-1$
					parentPath = parentPath.substring(1);
				}
				LockResource directory = LockResource.createDirectory(parentPath);
				directory.addChild(lr);
				res.add(lr);
			}
		}
		return res;
	}
	
    public boolean isEnabled() {
        return this.checkForResourcesPresenceRecursive(IStateFilter.SF_READY_TO_LOCK);
    }
    
}
