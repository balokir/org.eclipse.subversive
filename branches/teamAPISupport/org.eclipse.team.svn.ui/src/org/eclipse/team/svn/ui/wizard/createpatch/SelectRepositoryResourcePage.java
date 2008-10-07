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

package org.eclipse.team.svn.ui.wizard.createpatch;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.RepositoryResourceSelectionComposite;
import org.eclipse.team.svn.ui.wizard.AbstractVerifiedWizardPage;
import org.eclipse.ui.PlatformUI;

/**
 * Select the URL in compare with patch will be created
 * 
 * @author Alexander Gurov
 */
public class SelectRepositoryResourcePage extends AbstractVerifiedWizardPage {
	protected RepositoryResourceSelectionComposite selectComposite;
	protected IRepositoryResource baseResource;
	protected IRepositoryResource selectedResource;

	public SelectRepositoryResourcePage(IRepositoryResource baseResource) {
		super(
			SelectRepositoryResourcePage.class.getName(), 
			SVNTeamUIPlugin.instance().getResource("SelectRepositoryResourcePage.Title"), 
			SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif"));
		this.setDescription(SVNTeamUIPlugin.instance().getResource("SelectRepositoryResourcePage.Description"));
		this.selectedResource = this.baseResource = baseResource;
	}

	public IRepositoryResource getSelectedResource() {
		return this.selectedResource;
	}
	
	protected Composite createControlImpl(Composite parent) {
		GridData data = null;
		GridLayout layout = null;
		
		Composite composite = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 4;
		composite.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		this.selectComposite = new RepositoryResourceSelectionComposite(
				composite, SWT.NONE, this, "patchUrl", this.baseResource, true, 
				SVNTeamUIPlugin.instance().getResource("SelectRepositoryResourcePage.Select.Title"),
				SVNTeamUIPlugin.instance().getResource("SelectRepositoryResourcePage.Select.Description"), RepositoryResourceSelectionComposite.MODE_DEFAULT, RepositoryResourceSelectionComposite.TEXT_BASE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 550;
		this.selectComposite.setLayoutData(data);
		
//		Setting context help
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, "org.eclipse.team.svn.help.patchRemoteContext");
		
		return composite;
	}

	public IWizardPage getNextPage() {
		this.selectedResource = this.selectComposite.getSelectedResource();
		this.selectComposite.saveHistory();
		return super.getNextPage();
	}

}
