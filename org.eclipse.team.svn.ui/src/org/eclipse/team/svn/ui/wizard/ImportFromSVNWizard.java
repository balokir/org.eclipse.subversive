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

package org.eclipse.team.svn.ui.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.wizard.checkoutas.SelectCheckoutResourcePage;
import org.eclipse.team.svn.ui.wizard.shareproject.AddRepositoryLocationPage;
import org.eclipse.team.svn.ui.wizard.shareproject.SelectRepositoryLocationPage;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Implements "Import from SVN" functionality
 * 
 * @author Alexander Gurov
 */
public class ImportFromSVNWizard extends AbstractSVNWizard implements INewWizard {
	protected SelectRepositoryLocationPage selectLocation;
	protected AddRepositoryLocationPage addLocation;
	protected SelectCheckoutResourcePage selectResource;

	public ImportFromSVNWizard() {
		super();
		this.setWindowTitle(SVNTeamUIPlugin.instance().getResource("ImportFromSVNWizard.Title"));
	}
	
	public void addPages() {
		IRepositoryLocation []locations = SVNRemoteStorage.instance().getRepositoryLocations();
		if (locations.length > 0) {
			this.addPage(this.selectLocation = new SelectRepositoryLocationPage(locations, true));
		}
		this.addPage(this.addLocation = new AddRepositoryLocationPage());
		this.addPage(this.selectResource = new SelectCheckoutResourcePage());
	}

	public IWizardPage getNextPage(IWizardPage page) {
		IWizardPage retVal = null;
	    this.addLocation.setInitialUrl(null);
		if (page instanceof SelectRepositoryLocationPage && 
			this.selectLocation.useExistingLocation()) {
			retVal = super.getNextPage(super.getNextPage(page));
		}
		else {
			retVal = super.getNextPage(page);
		}
		
		if (retVal instanceof SelectCheckoutResourcePage) {
			this.selectResource.setRepositoryLocation(this.selectLocation != null && this.selectLocation.useExistingLocation() ? this.selectLocation.getRepositoryLocation() : this.addLocation.getRepositoryLocation());
		}

		return retVal;
	}
	
	public IWizardPage getPreviousPage(IWizardPage page) {
		if (page instanceof SelectCheckoutResourcePage &&
			this.selectLocation != null && 
			this.selectLocation.useExistingLocation()) {
			return super.getPreviousPage(super.getPreviousPage(page));
		}
		return super.getPreviousPage(page);
	}
	
	public boolean canFinish() {
		IWizardPage currentPage = this.getContainer().getCurrentPage();
		return currentPage instanceof SelectCheckoutResourcePage && this.selectResource.isPageComplete();
	}
	
	public boolean performFinish() {
		IRepositoryResource []resources = this.selectResource.getSelectedResources();
		CheckoutAsWizard checkoutWizard = new CheckoutAsWizard(resources, this.addLocation.getOperationToPeform());
		WizardDialog dialog = new WizardDialog(this.getShell(), checkoutWizard);
		dialog.create();
		dialog.getShell().setSize(Math.max(CheckoutAsWizard.SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x), CheckoutAsWizard.SIZING_WIZARD_HEIGHT);
		return dialog.open() == 0 ? true : false;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {

	}

}
