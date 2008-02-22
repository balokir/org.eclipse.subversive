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

package org.eclipse.team.svn.ui.utility;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.LockOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.local.CommitPanel;
import org.eclipse.team.svn.ui.panel.local.LockPanel;

/**
 * An utility to propose user to lock the file if it needs lock
 * 
 * @author Sergiy Logvin
 */
public class LockProposeUtility {
	public static boolean proposeLock(final IResource[] resources) {
		final boolean []success = new boolean[1];
		CommitPanel.CollectPropertiesOperation op = new CommitPanel.CollectPropertiesOperation(resources);
		ProgressMonitorUtility.doTaskExternal(op, null);
		final LockPanel panel = new LockPanel(true, true, op.getMinLockSize());
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				DefaultDialog dialog = new DefaultDialog(UIMonitorUtility.getShell(), panel);
				if (dialog.open() == 0) {
					LockOperation mainOp = new LockOperation(resources, panel.getMessage(), panel.getForce());
					CompositeOperation op = new CompositeOperation(mainOp.getId());
					op.add(mainOp);
					op.add(new RefreshResourcesOperation(resources), new IActionOperation[] {mainOp});
					UIMonitorUtility.doTaskBusyDefault(op);
					success[0] = mainOp.getStatus().getSeverity() == IStatus.OK;
				}
			}
		});
		
		return success[0];
	}

}
