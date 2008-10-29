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

package org.eclipse.team.svn.ui.synchronize.action;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.properties.ResourcePropertyEditPanel;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Set property action helper implementation for Synchronize view
 * 
 * @author Igor Burilo
 */
public class SetPropertyActionHelper extends AbstractActionHelper {
	
	public SetPropertyActionHelper(IAction action, ISynchronizePageConfiguration configuration) {
		super(action, configuration);
	}

	public IActionOperation getOperation() {
		IResource [] resources = this.getTreeNodeSelector().getSelectedResourcesRecursive(IStateFilter.SF_VERSIONED);
		ResourcePropertyEditPanel panel = new ResourcePropertyEditPanel(null, resources, true);
		DefaultDialog dialog = new DefaultDialog(configuration.getSite().getShell(), panel);
		if (dialog.open() == Dialog.OK) {
			org.eclipse.team.svn.ui.action.local.SetPropertyAction.doSetProperty(resources, panel, null);
		}
		return null;
	}

}
