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

package org.eclipse.team.svn.ui.action.local.management;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.action.IAction;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.local.AddRepositoryPanel;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * New repository action implementation
 * 
 * @author Igor Burilo
 */
public class NewRepositoryAction extends TeamAction {

	public void run(IAction action) {
		AddRepositoryPanel addRepositoryPanel = new AddRepositoryPanel();
		DefaultDialog dialog = new DefaultDialog(this.getShell(), addRepositoryPanel);
		if (dialog.open() == 0) {
			IActionOperation op = addRepositoryPanel.getOperationToPeform();
			if (op != null) {				
				UIMonitorUtility.doTaskScheduledActive(op);						
			}						
		}
	}
	
	public boolean isEnabled() {
		return true;
	}
	
	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {		
		// compatibility with 3.3
	}
	
	public static boolean checkEnablement() {
		return (CoreExtensionsManager.instance().getSVNConnectorFactory().getSupportedFeatures() & ISVNConnectorFactory.OptionalFeatures.CREATE_REPOSITORY) != 0;
	}

}
