/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.crashrecovery;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.svn.core.extension.crashrecovery.ErrorDescription;
import org.eclipse.team.svn.core.extension.crashrecovery.IResolutionHelper;
import org.eclipse.team.svn.core.operation.local.UpgradeWorkingCopyOperation;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Handle invalid meta-information problem here
 * 
 * @author Alexander Gurov
 */
public class UpgradeWorkingCopyHelper implements IResolutionHelper {

	public boolean acquireResolution(ErrorDescription description) {
		if (description.code == ErrorDescription.WORKING_COPY_REQUIRES_UPGRADE) {
			final IProject project = (IProject)description.context;
			final boolean []solved = new boolean[] {false};
			UIMonitorUtility.parallelSyncExec(new Runnable() {
				public void run() {
					String title = SVNUIMessages.format(SVNUIMessages.UpgradeWorkingCopyDialog_Title, new String[] {project.getName()});
					MessageDialog dlg = new MessageDialog(UIMonitorUtility.getShell(), title, null, SVNUIMessages.UpgradeWorkingCopyDialog_Message, MessageDialog.QUESTION, new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}, 0);
					if (dlg.open() == 0) {
						UIMonitorUtility.doTaskNowWorkspaceModify(UIMonitorUtility.getShell(), new UpgradeWorkingCopyOperation(new IResource[] {project}), false);
						solved[0] = true;
					}
				}
			});
			return solved[0];
		}
		return false;
	}
	
}
