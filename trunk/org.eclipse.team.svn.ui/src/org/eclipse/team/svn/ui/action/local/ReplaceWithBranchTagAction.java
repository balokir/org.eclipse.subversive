/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.action.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractWorkingCopyAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.local.ReplaceBranchTagPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;

/**
 * Replace with branch/tag action implementation
 * 
 * @author Alexei Goncharov
 */
public class ReplaceWithBranchTagAction extends AbstractWorkingCopyAction {

	protected int type;
	
	public ReplaceWithBranchTagAction(int type) {
		super();
		this.type = type;
	}
	
	public boolean isEnabled() {
		if (this.getSelectedResources().length == 1 && this.checkForResourcesPresence(CompareWithWorkingCopyAction.COMPARE_FILTER)) {
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(this.getSelectedResources()[0]);
			IRepositoryResource remote = local.isCopied() ? SVNUtility.getCopiedFrom(this.getSelectedResources()[0]) : SVNRemoteStorage.instance().asRepositoryResource(this.getSelectedResources()[0]);		
			boolean isCompareFoldersAllowed = (CoreExtensionsManager.instance().getSVNConnectorFactory().getSupportedFeatures() & ISVNConnectorFactory.OptionalFeatures.COMPARE_FOLDERS) != 0;
			boolean recommendedLayoutUsed = 
				SVNTeamPreferences.getRepositoryBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_NAME) &&
				remote.getRepositoryLocation().isStructureEnabled();
			return 
				isCompareFoldersAllowed &&
				recommendedLayoutUsed &&
				this.getSelectedResources()[0].getType() == IResource.PROJECT;
		}
		return false;
	}

	public void runImpl(IAction action) {
		IResource resource = this.getSelectedResources()[0];
		ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
		IRepositoryResource remote = local.isCopied() ? SVNUtility.getCopiedFrom(resource) : SVNRemoteStorage.instance().asRepositoryResource(resource);
		ReplaceBranchTagPanel panel = new ReplaceBranchTagPanel(remote, local.getRevision(), type, true);
		DefaultDialog dlg = new DefaultDialog(this.getShell(), panel);
		if (dlg.open() == 0){
			//TODO implementation
		}
	}

}
