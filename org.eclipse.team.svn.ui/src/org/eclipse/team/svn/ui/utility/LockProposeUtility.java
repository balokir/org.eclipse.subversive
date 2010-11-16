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
import org.eclipse.core.runtime.Status;
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
	public static IStatus proposeLock(final IResource[] resources) {
		List<LockResource> lockResources = LockAction.getLockResources(resources);
		for (Iterator<LockResource> iter = lockResources.iterator(); iter.hasNext(); ) {
			LockResource lockResource = iter.next();
			if (lockResource.getLockStatus() == LockStatusEnum.LOCALLY_LOCKED) {
				iter.remove();
			}
		}
		
		IActionOperation op = LocksComposite.performLockAction(lockResources.toArray(new LockResource[0]), false, UIMonitorUtility.getShell());
		if (op != null) {
			UIMonitorUtility.doTaskBusyDefault(op);
			return op.getStatus();
		}				
		return Status.CANCEL_STATUS;
	}

}
