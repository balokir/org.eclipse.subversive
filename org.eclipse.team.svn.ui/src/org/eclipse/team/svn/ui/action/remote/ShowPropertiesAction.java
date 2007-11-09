/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Vladimir Bykov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.action.remote;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IResourcePropertyProvider;
import org.eclipse.team.svn.core.operation.remote.GetRemotePropertiesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.operation.ShowPropertiesOperation;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.properties.PropertiesView;
import org.eclipse.ui.PlatformUI;

/**
 * Show properties action imlementation
 * 
 * @author Vladimir Bykov
 */
public class ShowPropertiesAction extends AbstractRepositoryTeamAction {
	
	public ShowPropertiesAction() {
		super();
	}
		
	public void runImpl(IAction action) {
		IRepositoryResource resource = this.getSelectedRepositoryResources()[0];
		IResourcePropertyProvider provider = new GetRemotePropertiesOperation(resource);
		
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		boolean usePropertiesView = SVNTeamPreferences.getPropertiesBoolean(store, SVNTeamPreferences.PROPERTY_USE_VIEW_NAME);
		
		if (usePropertiesView) {
			PropertiesView view = (PropertiesView)this.showView(PropertiesView.VIEW_ID);
			view.setResource(resource, provider, false);
		}
		else {
			ShowPropertiesOperation op = new ShowPropertiesOperation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), resource, provider);
			CompositeOperation composite = new CompositeOperation(op.getId());
			composite.add((IActionOperation)provider);
			composite.add(op, new IActionOperation[] {provider});
			if (!op.isEditorOpened()) {
				this.runScheduled(composite);
			}
		}
	}
	
	public boolean isEnabled() {
		return this.getSelectedRepositoryResources().length == 1;
	}
	
}
