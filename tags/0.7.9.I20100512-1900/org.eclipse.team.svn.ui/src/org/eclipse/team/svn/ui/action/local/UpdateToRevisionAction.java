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

package org.eclipse.team.svn.ui.action.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.action.AbstractRecursiveTeamAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.local.UpdateToRevisionPanel;
import org.eclipse.team.svn.ui.utility.UnacceptableOperationNotificator;

/**
 * Team services menu update to revision action implementation
 * Allows to specify revision and depth for update
 * 
 * @author Igor Burilo
 */
public class UpdateToRevisionAction extends AbstractRecursiveTeamAction {
	
	public void runImpl(IAction action) {		
		IResource []resources = UnacceptableOperationNotificator.shrinkResourcesWithNotOnRespositoryParents(this.getShell(), this.getSelectedResources(IStateFilter.SF_ONREPOSITORY));
		if (resources == null || resources.length == 0) {
			return;
		}
		
		if (this.checkForResourcesPresenceRecursive(IStateFilter.SF_REVERTABLE)) {
			IResource []missing = this.getSelectedResourcesRecursive(UpdateAction.SF_MISSING_RESOURCES);
			if (missing.length > 0 && !UpdateAction.updateMissing(this.getShell(), missing)) {
				return;
			}
		}
		SVNRevision revision = SVNRevision.HEAD;		
		int depth = ISVNConnector.Depth.INFINITY;
		
		//get revision and depth
		IResource resourceForRevisionSelection;
		if (resources.length > 1) {
			//at first try to shrink
			IResource[] shrinkedResources = FileUtility.shrinkChildNodes(resources);
			resourceForRevisionSelection = shrinkedResources.length > 1 ?  shrinkedResources[0].getProject() : shrinkedResources[0];
		} else {
			resourceForRevisionSelection = resources[0];
		}
		IRepositoryResource repositoryResource = SVNRemoteStorage.instance().asRepositoryResource(resourceForRevisionSelection);		
		UpdateToRevisionPanel panel = new UpdateToRevisionPanel(repositoryResource);
		DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
		if (dialog.open() == 0) {
			revision = panel.getRevision();
			depth = panel.getDepth();
			
			this.runScheduled(UpdateAction.getUpdateOperation(resources, revision, depth));
		}				
	}
	
	public boolean isEnabled() {
		return this.checkForResourcesPresence(IStateFilter.SF_ONREPOSITORY);
	}
	
	protected boolean needsToSaveDirtyEditors() {
		return true;
	}

}
