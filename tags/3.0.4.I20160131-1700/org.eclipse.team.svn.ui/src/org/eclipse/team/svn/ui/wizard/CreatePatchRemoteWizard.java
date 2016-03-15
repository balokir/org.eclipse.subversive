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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.wizard.createpatch.SelectRepositoryResourcePage;

/**
 * Create patch for repository resource
 * 
 * @author Alexander Gurov
 */
public class CreatePatchRemoteWizard extends CreatePatchWizard {
	protected SelectRepositoryResourcePage urlSelectionPage;
	protected IRepositoryResource targetResource;

	public CreatePatchRemoteWizard(IRepositoryResource targetResource) {
		this(targetResource, false);
	}
	
	public CreatePatchRemoteWizard(IRepositoryResource targetResource, boolean showIgnoreAncestry) {
		super(targetResource.getName(), null, showIgnoreAncestry);
		this.targetResource = targetResource;
	}

	public IRepositoryResource getSelectedResource() {
		return this.urlSelectionPage.getSelectedResource();
	}
	
	public boolean canFinish() {
		IWizardPage currentPage = this.getContainer().getCurrentPage();
		if (currentPage instanceof SelectRepositoryResourcePage) {
			return false;
		}		
		return super.canFinish();
	}
	
	public void addPages() {
		this.addPage(this.urlSelectionPage = new SelectRepositoryResourcePage(this.targetResource));
		super.addPages();
	}
	
}
