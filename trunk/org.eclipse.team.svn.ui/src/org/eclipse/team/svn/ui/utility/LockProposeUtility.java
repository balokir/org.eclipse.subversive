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

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.ui.action.local.LockAction;
import org.eclipse.team.svn.ui.lock.LockResource;
import org.eclipse.team.svn.ui.lock.LocksComposite;
import org.eclipse.team.svn.ui.lock.LockResource.LockStatusEnum;

/**
 * An utility to propose user to lock the file if it needs lock
 * 
 * @author Sergiy Logvin
 */
public class LockProposeUtility {
	public static boolean proposeLock(final IResource[] resources) {
		final boolean[] success = new boolean[1];		
		List<LockResource> lockResources = LockAction.getLockResources(resources);
		if (lockResources != null) {
			Iterator<LockResource> iter = lockResources.iterator();
			while (iter.hasNext()) {
				LockResource lockResource = iter.next();
				if (lockResource.getLockStatus() == LockStatusEnum.LOCALLY_LOCKED) {
					iter.remove();
				}
			}
			
			IActionOperation op = LocksComposite.performLockAction(lockResources.toArray(new LockResource[0]), false, UIMonitorUtility.getShell());
			if (op != null) {
				UIMonitorUtility.doTaskBusyDefault(op);
				success[0] = op.getStatus().getSeverity() == IStatus.OK;
			}				
		}	
		return success[0];
	}

}
