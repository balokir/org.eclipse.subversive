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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.core.IConnectedProjectInformation;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.extension.crashrecovery.ErrorDescription;
import org.eclipse.team.svn.core.extension.crashrecovery.IResolutionHelper;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.AbstractWorkingCopyOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.management.FindRelatedProjectsOperation;
import org.eclipse.team.svn.core.operation.local.management.RelocateWorkingCopyOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.crashrecovery.relocated.RelocationChoicesPanel;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Allows us to handle correctly project relocation which is performed outside Eclipse IDE
 * 
 * @author Alexander Gurov
 */
public class RelocatedProjectHelper implements IResolutionHelper {

	public boolean acquireResolution(ErrorDescription description) {
		if (description.code == ErrorDescription.PROJECT_IS_RELOCATED_OUTSIDE_PLUGIN) {
			Object []context = (Object [])description.context;
			final IProject project = (IProject)context[0];
			final IRepositoryLocation location = (IRepositoryLocation)context[2];
			final String relocatedTo = (String)context[1];
			
			IRepositoryRoot []roots = SVNUtility.findRoots(relocatedTo, true);
			if (roots.length != 0) {
				IConnectedProjectInformation provider = (IConnectedProjectInformation)RepositoryProvider.getProvider(project, SVNTeamPlugin.NATURE_ID);
				try {
					provider.switchResource(roots[0].asRepositoryContainer(relocatedTo, false));
					return true;
				}
				catch (CoreException ex) {
					//ask user if not successful
				}
			}
			
			final RelocationChoicesPanel panel = new RelocationChoicesPanel(project);
			UIMonitorUtility.parallelSyncExec(new Runnable() {
				public void run() {
					DefaultDialog dialog = new DefaultDialog(UIMonitorUtility.getShell(), panel);
					dialog.open();
				}
			});
			
			if (panel.getRecoveryAction() == RelocationChoicesPanel.DISCONNECT_PROJECT) {
				return false;
			}
			
			if (panel.getRecoveryAction() == RelocationChoicesPanel.RELOCATE_THE_PROJECT_BACK) {
				RelocateWorkingCopyOperation mainOp = new RelocateWorkingCopyOperation(new IResource[] {project}, location);
				CompositeOperation op = new CompositeOperation(mainOp.getId());
				
				op.add(mainOp);
				op.add(new RefreshResourcesOperation(mainOp));
				
				ProgressMonitorUtility.doTaskExternal(op, new NullProgressMonitor());
				
				return mainOp.getExecutionState() == IActionOperation.OK;
			}
			else if (panel.getRecoveryAction() == RelocationChoicesPanel.RELOCATE_REST_OF_PROJECTS) {
				final IRepositoryLocation backup = SVNRemoteStorage.instance().newRepositoryLocation();
				SVNRemoteStorage.instance().copyRepositoryLocation(backup, location);
				
				FindRelatedProjectsOperation scannerOp = new FindRelatedProjectsOperation(location, new IProject[] {project});
				final RelocateWorkingCopyOperation mainOp = new RelocateWorkingCopyOperation(scannerOp, location);
				CompositeOperation op = new CompositeOperation(mainOp.getId());
				
				op.add(scannerOp);
				op.add(new AbstractWorkingCopyOperation("Operation_ChangeRepositoryLocation", new IResource[] {project}) { //$NON-NLS-1$
					protected void runImpl(IProgressMonitor monitor) throws Exception {
						location.setUrl(relocatedTo);
						location.setUrl(location.getRepositoryRootUrl());
						IConnectedProjectInformation provider = (IConnectedProjectInformation)RepositoryProvider.getProvider(project, SVNTeamPlugin.NATURE_ID);
						provider.relocateResource();
					}
				});
				op.add(mainOp);
				op.add(new AbstractWorkingCopyOperation("Operation_CheckRelocationState", new IResource[] {project}) { //$NON-NLS-1$
					protected void runImpl(IProgressMonitor monitor) throws Exception {
						if (mainOp.getExecutionState() != IActionOperation.OK) {
							SVNRemoteStorage.instance().copyRepositoryLocation(location, backup);
							IConnectedProjectInformation provider = (IConnectedProjectInformation)RepositoryProvider.getProvider(project, SVNTeamPlugin.NATURE_ID);
							provider.relocateResource();
						}
					}
				});
				op.add(new RefreshResourcesOperation(mainOp));
				
				ProgressMonitorUtility.doTaskExternal(op, new NullProgressMonitor());
				
				return true;
			}
			else if (panel.getRecoveryAction() == RelocationChoicesPanel.SHARE_WITH_ANOTHER_LOCATION) {
				try {
					IConnectedProjectInformation provider = (IConnectedProjectInformation)RepositoryProvider.getProvider(project, SVNTeamPlugin.NATURE_ID);
					IRepositoryLocation newLocation = SVNRemoteStorage.instance().newRepositoryLocation();
					SVNRemoteStorage.instance().copyRepositoryLocation(newLocation, location);
					newLocation.setUrl(relocatedTo);
					newLocation.setUrl(newLocation.getRepositoryRootUrl());
					SVNRemoteStorage.instance().addRepositoryLocation(newLocation);
					SVNTeamPlugin.instance().setLocationsDirty(true);
					provider.switchResource(newLocation.asRepositoryContainer(relocatedTo, false));
				}
				catch (CoreException ex) {
					return false;
				}
				return true;
			}
		}
		return false;
	}

}
