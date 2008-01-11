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
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.action.AbstractWorkingCopyAction;
import org.eclipse.team.svn.ui.history.HistoryViewImpl;
import org.eclipse.team.svn.ui.operation.CompareResourcesOperation;
import org.eclipse.team.svn.ui.operation.CorrectRevisionOperation;
import org.eclipse.team.svn.ui.operation.ShowHistoryViewOperation;

/**
 * Compare menu "compare with latest revision" action implementation
 * 
 * @author Alexander Gurov
 */
public class CompareWithLatestRevisionAction extends AbstractWorkingCopyAction {
	
	public CompareWithLatestRevisionAction() {
		super();
	}
	
	public void runImpl(IAction action) {
		IResource resource = this.getSelectedResources()[0];
		
		ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
		if (local != null) {
			IRepositoryResource remote = local.isCopied() ? SVNUtility.getCopiedFrom(resource) : SVNRemoteStorage.instance().asRepositoryResource(resource);
			remote.setSelectedRevision(SVNRevision.HEAD);
			
			CorrectRevisionOperation correctOp = new CorrectRevisionOperation(null, remote, local.getRevision(), resource);
			
			if (!this.runNow(correctOp, true).isCancelled()) {
				this.runScheduled(new CompareResourcesOperation(local, remote));
				this.runBusy(new ShowHistoryViewOperation(resource, HistoryViewImpl.COMPARE_MODE, HistoryViewImpl.COMPARE_MODE));
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
