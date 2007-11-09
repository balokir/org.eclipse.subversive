/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.wizard.checkoutas;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.SVNContainerSelectionGroup;
import org.eclipse.team.svn.ui.wizard.AbstractVerifiedWizardPage;
import org.eclipse.ui.PlatformUI;

/**
 * Checkout as folder into existing project
 * 
 * @author Alexander Gurov
 */
public class CheckoutAsFolderPage extends AbstractVerifiedWizardPage {
	protected IRepositoryResource []repositoryResources;
	protected SVNContainerSelectionGroup group;
	protected IContainer targetFolder;

	public CheckoutAsFolderPage(IRepositoryResource []repositoryResources) {
		super(CheckoutAsFolderPage.class.getName(), 
				SVNTeamUIPlugin.instance().getResource("CheckoutAsFolderPage.Title"), 
				SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif"));
		this.setDescription(SVNTeamUIPlugin.instance().getResource("CheckoutAsFolderPage.Description"));
		this.repositoryResources = repositoryResources;
	}
	
	public IContainer getTargetFolder() {
		return this.targetFolder;
	}

	protected Composite createControlImpl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		this.setControl(composite);
		
    	Listener listener = new Listener() {
            public void handleEvent(Event event) {
            	IPath path = CheckoutAsFolderPage.this.group.getContainerFullPath();
            	CheckoutAsFolderPage.this.targetFolder = (IContainer)ResourcesPlugin.getWorkspace().getRoot().findMember(path);
            	CheckoutAsFolderPage.this.validateContent();
            }
        };
        this.group = new SVNContainerSelectionGroup(composite, listener);
        this.attachTo(this.group, new SVNContainerSelectionGroup.SVNContainerCheckOutSelectionVerifier());
        
//		Setting context help
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, "org.eclipse.team.svn.help.checkoutAsAFolderContext");
        
		return composite;
	}

}
