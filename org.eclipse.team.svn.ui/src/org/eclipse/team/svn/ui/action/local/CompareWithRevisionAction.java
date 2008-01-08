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
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractWorkingCopyAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.history.HistoryViewImpl;
import org.eclipse.team.svn.ui.operation.CompareResourcesOperation;
import org.eclipse.team.svn.ui.operation.ShowHistoryViewOperation;
import org.eclipse.team.svn.ui.panel.common.InputRevisionPanel;

/**
 * Compare menu "compare with selected revision" action implementation
 * 
 * @author Alexander Gurov
 */
public class CompareWithRevisionAction extends AbstractWorkingCopyAction {

	public CompareWithRevisionAction() {
		super();
	}
	
	public void runImpl(IAction action) {
		IResource local = this.getSelectedResources()[0];
		ILocalResource wcInfo = SVNRemoteStorage.instance().asLocalResource(local);
		if (wcInfo != null) {
			IRepositoryResource ancestor = wcInfo.isCopied() ? SVNUtility.getCopiedFrom(local) : SVNRemoteStorage.instance().asRepositoryResource(local);
			IRepositoryResource remote = SVNUtility.copyOf(ancestor);
			
			InputRevisionPanel panel = new InputRevisionPanel(remote, SVNTeamUIPlugin.instance().getResource("CompareWithRevisionAction.InputRevisionPanel.Title"));
			DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
			if (dialog.open() == 0) {
				remote.setSelectedRevision(panel.getSelectedRevision());
				this.runScheduled(new CompareResourcesOperation(local, ancestor, remote));
				this.runBusy(new ShowHistoryViewOperation(local, HistoryViewImpl.COMPARE_MODE, HistoryViewImpl.COMPARE_MODE));
			}
		}
	}

	public boolean isEnabled() {
		boolean isCompareFoldersAllowed = (CoreExtensionsManager.instance().getSVNConnectorFactory().getSupportedFeatures() & ISVNConnectorFactory.OptionalFeatures.COMPARE_FOLDERS) != 0;
		return 
			this.getSelectedResources().length == 1 && 
			(isCompareFoldersAllowed || this.getSelectedResources()[0].getType() == IResource.FILE) && 
			this.checkForResourcesPresence(CompareWithWorkingCopyAction.COMPARE_FILTER);
	}

}
