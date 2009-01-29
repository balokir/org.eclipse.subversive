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

package org.eclipse.team.svn.ui.action.remote.management;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.management.FindRelatedProjectsOperation;
import org.eclipse.team.svn.core.operation.local.management.RelocateWorkingCopyOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.action.AbstractRepositoryModifyWorkspaceAction;
import org.eclipse.team.svn.ui.wizard.NewRepositoryLocationWizard;

/**
 * Edit repository location properties action implementation
 * 
 * @author Alexander Gurov
 */
public class EditRepositoryLocationPropertiesAction extends AbstractRepositoryModifyWorkspaceAction {

	public EditRepositoryLocationPropertiesAction() {
		super();
	}
	
	public void runImpl(IAction action) {
		final IRepositoryLocation []locations = this.getSelectedRepositoryLocations();
		String oldRootUrl = locations[0].getRepositoryRootUrl();
		
		final IRepositoryLocation backup = SVNRemoteStorage.instance().newRepositoryLocation();
		SVNRemoteStorage.instance().copyRepositoryLocation(backup, locations[0]);
		
		NewRepositoryLocationWizard wizard = new NewRepositoryLocationWizard(locations[0], false);
		WizardDialog dialog = new WizardDialog(this.getShell(), wizard);
		if (dialog.open() == 0) {
			String newRootUrl = locations[0].getRepositoryRootUrl();
			if (!newRootUrl.equals(oldRootUrl)) {
				FindRelatedProjectsOperation scannerOp = new FindRelatedProjectsOperation(locations[0]);
				final RelocateWorkingCopyOperation mainOp = new RelocateWorkingCopyOperation(scannerOp, locations[0]);
				CompositeOperation op = new CompositeOperation(mainOp.getId());
				op.add(scannerOp);
				op.add(mainOp);
				op.add(new AbstractActionOperation("Operation_CheckRelocationState") { //$NON-NLS-1$
					protected void runImpl(IProgressMonitor monitor) throws Exception {
						if (mainOp.getExecutionState() != IActionOperation.OK) {
							SVNRemoteStorage.instance().copyRepositoryLocation(locations[0], backup);
						}
					}
				});
				op.add(wizard.getOperationToPerform());
				op.add(new RefreshResourcesOperation(mainOp));
				
				this.runScheduled(op); 
			}
			else {
				CompositeOperation op = (CompositeOperation)wizard.getOperationToPerform();
				FindRelatedProjectsOperation findOp = new FindRelatedProjectsOperation(locations[0]);
				op.add(findOp);
				op.add(new RefreshResourcesOperation(findOp, IResource.DEPTH_ZERO, RefreshResourcesOperation.REFRESH_CACHE));
				
				this.runScheduled(op);
			}
		}
	}

	public boolean isEnabled() {
		return this.getSelectedRepositoryLocations().length == 1;
	}

}
