/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.action.remote;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.action.AbstractRepositoryModifyWorkspaceAction;
import org.eclipse.team.svn.ui.wizard.CheckoutAsWizard;

/**
 * UI Checkout As action
 * 
 * @author Alexander Gurov
 */
public class CheckoutAsAction extends AbstractRepositoryModifyWorkspaceAction {
	public CheckoutAsAction() {
		super();
	}
	
	public void runImpl(IAction action) {
		IRepositoryResource []selectedResources = this.getSelectedRepositoryResources();
		IRepositoryLocation []locations = this.getSelectedRepositoryLocations();
		if (selectedResources.length == 0) {
			selectedResources = new IRepositoryResource[locations.length];
			for (int i = 0; i < locations.length; i++) {
				selectedResources[i] = locations[i].getRoot();
			}
		}
		
		CheckoutAsWizard checkoutWizard = new CheckoutAsWizard(selectedResources);
		WizardDialog dialog = new WizardDialog(this.getShell(), checkoutWizard);
		dialog.create();
		dialog.getShell().setSize(Math.max(CheckoutAsWizard.SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x), CheckoutAsWizard.SIZING_WIZARD_HEIGHT);
		dialog.open();
	}
	
	public boolean isEnabled() {
		return true;
	}

}
