/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.utility;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.local.LockOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.UnlockOperation;
import org.eclipse.team.svn.core.operation.remote.BreakLockOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.lock.LockResource;
import org.eclipse.team.svn.ui.lock.LockResourcesPanel;
import org.eclipse.team.svn.ui.operation.RefreshRemoteResourcesOperation;

/**
 * An utility to propose user to lock the file if it needs lock
 * 
 * @author Sergiy Logvin
 */
public class LockProposeUtility {
	public static IStatus proposeLock(final IResource[] resources, Shell shell) {
		CompositeOperation op = LockProposeUtility.performLockAction(resources, false, shell);
		if (op != null) {
			UIMonitorUtility.doTaskBusyWorkspaceModify(op);
			return op.getStatus();
		}				
		return Status.CANCEL_STATUS;
	}

	public static IResource[] asResources(LockResource[] lockResources) {
		List<IResource> res = new ArrayList<IResource>();
		if (lockResources != null) {
			for (int i = 0; i < lockResources.length; i ++) {
				Object ob = lockResources[i].getAdapter(IResource.class);
				if (ob != null) {
					res.add((IResource)ob);
				}
			}
		}
		return res.toArray(new IResource[res.size()]);
	}

	public static IRepositoryResource[] asRepositoryResources(LockResource[] lockResources) {
		List<IRepositoryResource> res = new ArrayList<IRepositoryResource>();
		if (lockResources != null) {
			for (int i = 0; i < lockResources.length; i ++) {
				Object ob = lockResources[i].getAdapter(IRepositoryResource.class);
				if (ob != null) {
					res.add((IRepositoryResource)ob);
				}
			}
		}
		return res.toArray(new IRepositoryResource[res.size()]);
	}

	public static CompositeOperation performLockAction(IResource[] resourcesToProcess, boolean forceLock, Shell shell) {
		LockResource []lockResources = LockResource.getLockResources(resourcesToProcess, false);
		return LockProposeUtility.performLockAction(lockResources, forceLock, shell);
	}

	public static CompositeOperation performLockAction(LockResource[] lockResources, boolean forceLock, Shell shell) {
		LockResourcesPanel panel = new LockResourcesPanel(lockResources, true, forceLock, SVNUIMessages.LocksComposite_LockTitle, SVNUIMessages.LocksComposite_LockDescription, SVNUIMessages.LocksComposite_LockDefaultMessage);
		DefaultDialog dlg = new DefaultDialog(shell, panel);
		if (dlg.open() == 0) {			
			IResource[] resources = asResources(panel.getSelectedResources());
							
			LockOperation mainOp = new LockOperation(resources, panel.getMessage(), panel.getForce());			    
			CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
			op.add(mainOp);
			op.add(new RefreshResourcesOperation(resources));
			return op;
		}
		return null;
	}

	public static CompositeOperation performUnlockAction(IResource[] resourcesToProcess, Shell shell) {
		LockResource []lockResources = LockResource.getLockResources(resourcesToProcess, true);
		return LockProposeUtility.performUnlockAction(lockResources, shell);
	}

	public static CompositeOperation performUnlockAction(LockResource[] lockResources, Shell shell) {
		LockResourcesPanel unlockPanel = new LockResourcesPanel(lockResources, SVNUIMessages.LocksComposite_UnlockTitle, SVNUIMessages.LocksComposite_UnlockDescription, SVNUIMessages.LocksComposite_UnlockDefaultMessage);
		DefaultDialog dlg = new DefaultDialog(shell, unlockPanel);
		if (dlg.open() == 0) {								
			IResource[] resources = asResources(unlockPanel.getSelectedResources());
			UnlockOperation mainOp = new UnlockOperation(resources);							    
			CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
			op.add(mainOp);
			op.add(new RefreshResourcesOperation(resources));
			return op;							
		}		
		return null;
	}

	public static CompositeOperation performBreakLockAction(LockResource[] lockResources, Shell shell) {
		LockResourcesPanel panel = new LockResourcesPanel(lockResources, SVNUIMessages.LocksComposite_BreakLockTitle, SVNUIMessages.LocksComposite_BreakLockDescription, SVNUIMessages.LocksComposite_BreakLockDefaultMessage);
		DefaultDialog dlg = new DefaultDialog(shell, panel);
		if (dlg.open() == 0) {			
			IRepositoryResource[] reposResources = asRepositoryResources(panel.getSelectedResources());
			BreakLockOperation mainOp = new BreakLockOperation(reposResources);
			CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
			op.add(mainOp);
			op.add(new RefreshRemoteResourcesOperation(reposResources));
			return op;			
		}
		return null;
	}

}
