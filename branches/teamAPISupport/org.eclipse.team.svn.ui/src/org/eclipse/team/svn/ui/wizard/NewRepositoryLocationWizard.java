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

package org.eclipse.team.svn.ui.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.wizard.shareproject.AddRepositoryLocationPage;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Repository location registration wizard
 * 
 * @author Alexander Gurov
 */
public class NewRepositoryLocationWizard extends AbstractSVNWizard implements INewWizard {
	protected AddRepositoryLocationPage locationPage;
	protected IRepositoryLocation editable;
	protected boolean performAction;
	protected IRepositoryLocation backup;
	
	public NewRepositoryLocationWizard() {
		this(null, true);
	}
	
	public NewRepositoryLocationWizard(IRepositoryLocation editable, boolean performAction) {
		super();
		this.performAction = performAction;
		this.editable = editable;
		if (this.editable != null) {
			this.setWindowTitle(SVNTeamUIPlugin.instance().getResource("NewRepositoryLocationWizard.Title.Edit"));
			this.backup = SVNRemoteStorage.instance().newRepositoryLocation();
			SVNRemoteStorage.instance().copyRepositoryLocation(this.backup, editable);
		}
		else {
			this.setWindowTitle(SVNTeamUIPlugin.instance().getResource("NewRepositoryLocationWizard.Title.New"));
		}
	}
	
	public void addPages() {
		this.addPage(this.locationPage = new AddRepositoryLocationPage(this.editable));
	}
	
	public IActionOperation getOperationToPerform() {
		return this.locationPage.getOperationToPeform();
	}
	
	public boolean performCancel() {
		if (this.editable != null) {
			SVNRemoteStorage.instance().copyRepositoryLocation(this.editable, this.backup);
		}
		return super.performCancel();
	}

	public boolean performFinish() {
		if (this.locationPage.performFinish()) {
			if (this.performAction) {
				IActionOperation op = this.locationPage.getOperationToPeform();
				if (op != null) {
					UIMonitorUtility.doTaskBusyDefault(op);
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		
	}

}
