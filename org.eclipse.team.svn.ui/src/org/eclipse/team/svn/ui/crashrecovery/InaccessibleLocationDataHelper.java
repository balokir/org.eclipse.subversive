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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.core.IConnectedProjectInformation;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.extension.crashrecovery.ErrorDescription;
import org.eclipse.team.svn.core.extension.crashrecovery.IResolutionHelper;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.wizard.NewRepositoryLocationWizard;

/**
 * Allows user to create new repository location for the shared project if it is required.
 * 
 * @author Alexander Gurov
 */
public class InaccessibleLocationDataHelper implements IResolutionHelper {

	public boolean acquireResolution(ErrorDescription description) {
		if (description.code == ErrorDescription.CANNOT_READ_LOCATION_DATA) {
			final Object []context = (Object [])description.context;
			// resource URL is inaccessible, project should be disconnected
			if (context[1] == null) {
				return false;
			}
			
			final IProject project = (IProject)context[0];
			
			final boolean []solved = new boolean[] {false};
			final IActionOperation []op = new IActionOperation[1];
			final IRepositoryLocation []location = new IRepositoryLocation[1];
			UIMonitorUtility.parallelSyncExec(new Runnable() {
				public void run() {
					MessageDialog dlg = new MessageDialog(
							UIMonitorUtility.getShell(), 
							SVNUIMessages.InaccessibleLocationDataHelper_Dialog_Title, 
							null, 
							SVNUIMessages.format(SVNUIMessages.InaccessibleLocationDataHelper_Dialog_Message, new String[] {project.getName()}), 
							MessageDialog.WARNING, 
							new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}, 
							0);
					solved[0] = dlg.open() == 0;
					if (solved[0]) {
						String locationId = (String)context[2];
						location[0] = 
							locationId == null ? 
							SVNRemoteStorage.instance().newRepositoryLocation() :
							SVNRemoteStorage.instance().newRepositoryLocation(locationId);
						
							location[0].setUrl((String)context[1]);
						
						NewRepositoryLocationWizard wizard = new NewRepositoryLocationWizard(location[0], false);
						WizardDialog dialog = new WizardDialog(UIMonitorUtility.getShell(), wizard);
						solved[0] = dialog.open() == 0;
						if (solved[0]) {
							solved[0] = (op[0] = wizard.getOperationToPerform()) != null;
						}
					}
				}
			});
			
			if (solved[0]) {
				ProgressMonitorUtility.doTaskExternal(op[0], new NullProgressMonitor());
				if (op[0].getExecutionState() == IActionOperation.OK) {
					IRepositoryContainer container = location[0].asRepositoryContainer((String)context[1], true);
					if (container == null) {
						return false;
					}
					IConnectedProjectInformation provider = (IConnectedProjectInformation)RepositoryProvider.getProvider(project, SVNTeamPlugin.NATURE_ID);
					try {
						provider.switchResource(container);
					}
					catch (CoreException ex) {
						// recovery action failed
						return false;
					}
				}
				else {
					return false;
				}
			}
			
			return solved[0];
		}
		return false;
	}

}
