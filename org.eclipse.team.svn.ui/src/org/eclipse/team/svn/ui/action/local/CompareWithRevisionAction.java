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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.client.ISVNClient;
import org.eclipse.team.svn.core.client.SVNEntryStatus;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNClientFactory;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
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
		IRemoteStorage storage = SVNRemoteStorage.instance();
		IResource left = this.getSelectedResources()[0];
		IRepositoryResource right = null;
		
		ILocalResource localLeft = storage.asLocalResource(left);
		if (localLeft == null) {
			return;
		}
		if (localLeft.isCopied()) {
			IRepositoryLocation location = storage.getRepositoryLocation(left);
			ISVNClient proxy = location.acquireSVNProxy();
			try {
				SVNEntryStatus status = SVNUtility.getSVNInfoForNotConnected(left);
				if (status == null) {
					return;
				}
				right = left instanceof IFile ? (IRepositoryResource)location.asRepositoryFile(status.urlCopiedFrom, false) : location.asRepositoryContainer(status.urlCopiedFrom, false);
			}
			finally {
				location.releaseSVNProxy(proxy);
			}
		}
		else {
			right = storage.asRepositoryResource(left);
		}
		InputRevisionPanel panel = new InputRevisionPanel(right, SVNTeamUIPlugin.instance().getResource("CompareWithRevisionAction.InputRevisionPanel.Title"));
		DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
		if (dialog.open() == 0) {
			this.runScheduled(new CompareResourcesOperation(left, panel.getSelectedRevision(), right.getPegRevision()));
			if (!localLeft.isCopied()) {
				this.runBusy(new ShowHistoryViewOperation(left, HistoryViewImpl.COMPARE_MODE, HistoryViewImpl.COMPARE_MODE));
			}
		}
	}

	public boolean isEnabled() {
		boolean isCompareFoldersAllowed = (CoreExtensionsManager.instance().getSVNClientWrapperFactory().getSupportedFeatures() & ISVNClientFactory.OptionalFeatures.COMPARE_FOLDERS) != 0;
		return 
			this.getSelectedResources().length == 1 && 
			(isCompareFoldersAllowed || this.getSelectedResources()[0].getType() == IResource.FILE) && 
			this.checkForResourcesPresence(CompareWithWorkingCopyAction.COMPARE_FILTER);
	}

}
