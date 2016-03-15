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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.team.svn.ui.wizard.NewRepositoryLocationWizard;

/**
 * New repository location action implementation
 * 
 * @author Alexander Gurov
 */
public class NewRepositoryLocationAction extends TeamAction {

	public NewRepositoryLocationAction() {
		super();
	}
	
	public void run(IAction action) {
		NewRepositoryLocationWizard wizard = new NewRepositoryLocationWizard();
		WizardDialog dialog = new WizardDialog(this.getShell(), wizard);
		dialog.open();
	}

	public boolean isEnabled() {
		return true;
	}

	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		// compatibility with 3.3
	}

}
