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

package org.eclipse.team.svn.ui.action.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IResourcePropertyProvider;
import org.eclipse.team.svn.core.operation.local.property.GetPropertiesOperation;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractWorkingCopyAction;
import org.eclipse.team.svn.ui.operation.ShowPropertiesOperation;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.properties.PropertiesView;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * Team services menu "edit resource properties" action implementation
 * 
 * @author Alexander Gurov
 */
public class EditPropertiesAction extends AbstractWorkingCopyAction {
    
	public EditPropertiesAction() {
		super();
	}
	
	public void runImpl(IAction action) {
		IResource []resources = this.getSelectedResources(IStateFilter.SF_EXCLUDE_PREREPLACED_AND_DELETED);
		
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
		
		IResourcePropertyProvider provider = new GetPropertiesOperation(resources[0]);
		
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		boolean usePropertiesView = SVNTeamPreferences.getPropertiesBoolean(store, SVNTeamPreferences.PROPERTY_USE_VIEW_NAME);
		
		if (usePropertiesView) {
			PropertiesView view = (PropertiesView)this.showView(PropertiesView.VIEW_ID);
			view.setResource(resources[0], provider, false);
		}
		else {
			ShowPropertiesOperation op = new ShowPropertiesOperation(page, resources[0], provider);
			CompositeOperation composite = new CompositeOperation(op.getId());
			composite.add((IActionOperation)provider);
			composite.add(op, new IActionOperation[] {provider});
			if (usePropertiesView || !op.isEditorOpened()) {
				this.runScheduled(composite);
			}
		}
	}

	public boolean isEnabled() {
		return 
			this.getSelectedResources().length == 1 &&
			this.checkForResourcesPresence(IStateFilter.SF_EXCLUDE_PREREPLACED_AND_DELETED);
	}

}
