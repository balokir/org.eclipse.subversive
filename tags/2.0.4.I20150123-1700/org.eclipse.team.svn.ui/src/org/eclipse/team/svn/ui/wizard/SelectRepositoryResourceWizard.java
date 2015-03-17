/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.wizard;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.wizard.selectresource.SelectResourceLocationPage;
import org.eclipse.team.svn.ui.wizard.selectresource.SelectSimpleRepositoryLocationPage;


/**
 * Select repository resource wizard
 * It allows to select resource through registered repositories:
 * at first select repository, then resource inside repository
 * 
 * @author Igor Burilo
 */
public class SelectRepositoryResourceWizard extends AbstractSVNWizard {

	protected SelectSimpleRepositoryLocationPage selectLocationPage;
	protected SelectResourceLocationPage selectResourcePage;
	
	protected IRepositoryLocation location;
	protected boolean showFolders;
	
	protected IRepositoryResource selectedResource;
		
	public SelectRepositoryResourceWizard(boolean showFolders) {
		this(showFolders, null);
	}
	
	public SelectRepositoryResourceWizard(boolean showFolders, IRepositoryLocation location) {
		this.showFolders = showFolders;
		this.location = location;
		this.setWindowTitle(SVNUIMessages.SelectRepositoryResourceWizard_Title);
	}
	
	@Override
	public void addPages() {	
		if (this.location == null) {
			IRepositoryLocation []locations = SVNRemoteStorage.instance().getRepositoryLocations();
			this.addPage(this.selectLocationPage = new SelectSimpleRepositoryLocationPage(locations));
			
			this.addPage(this.selectResourcePage = new SelectResourceLocationPage(this.showFolders, null));
		} else {
			this.addPage(this.selectResourcePage = new SelectResourceLocationPage(this.showFolders, this.location));
		}
	}
	
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page instanceof SelectSimpleRepositoryLocationPage) {			
			this.selectResourcePage.setModelRoot(this.selectLocationPage.getRepositoryLocation());			
			return this.selectResourcePage;
		}
		return null;
	}
	
	@Override
	public boolean canFinish() {
		IWizardPage currentPage = this.getContainer().getCurrentPage();
		if (currentPage instanceof SelectResourceLocationPage) {
			return this.selectResourcePage.getRepositoryResource() != null;			
		}
		return false;
	}
	
	public boolean performFinish() {
		this.selectedResource = this.selectResourcePage.getRepositoryResource();
		return true;
	}	 
	
	public IRepositoryResource getSelectedResource() {
		return this.selectedResource;
	}

}
