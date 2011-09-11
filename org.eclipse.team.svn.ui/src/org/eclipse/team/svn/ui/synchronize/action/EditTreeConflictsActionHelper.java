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

import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.local.EditTreeConflictsPanel;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Edit tree conflicts action helper implementation for Synchronize view
 * 
 * @author Igor Burilo
 */
public class EditTreeConflictsActionHelper extends AbstractActionHelper {

	public EditTreeConflictsActionHelper(IAction action, ISynchronizePageConfiguration configuration) {
		super(action, configuration);
	}	

	public IActionOperation getOperation() {
		AbstractSVNSyncInfo syncInfo = this.getSelectedSVNSyncInfo();
	    if (syncInfo != null) {
			ILocalResource local = syncInfo.getLocalResource();	
			if (local.hasTreeConflict()) {
				EditTreeConflictsPanel editConflictsPanel = new EditTreeConflictsPanel(local);
				DefaultDialog dialog = new DefaultDialog(this.configuration.getSite().getShell(), editConflictsPanel);
				if (dialog.open() == 0) {
					return editConflictsPanel.getOperation();			
				}		
			}
	    }
		return null;
	}
}
