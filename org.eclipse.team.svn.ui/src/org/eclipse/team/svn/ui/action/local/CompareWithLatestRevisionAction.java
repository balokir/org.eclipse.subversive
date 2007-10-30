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
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
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
		IResource left = this.getSelectedResources(CompareWithWorkingCopyAction.COMPARE_FILTER)[0];
		
		IRepositoryResource resource = SVNRemoteStorage.instance().asRepositoryResource(left);
		resource.setSelectedRevision(Revision.HEAD);
		
		ILocalResource local = SVNRemoteStorage.instance().asLocalResource(left);
		if (local != null) {
			CorrectRevisionOperation correctOp = new CorrectRevisionOperation(null, resource, local.getRevision(), left);
			
			if (!this.runNow(correctOp, true).isCancelled()) {
				this.runScheduled(new CompareResourcesOperation(left, resource.getSelectedRevision(), resource.getPegRevision()));
				this.runBusy(new ShowHistoryViewOperation(resource, HistoryViewImpl.COMPARE_MODE, HistoryViewImpl.COMPARE_MODE));
			}
		}
	}

	public boolean isEnabled() {
		return
			this.getSelectedResources().length == 1 && 
			this.checkForResourcesPresence(CompareWithWorkingCopyAction.COMPARE_FILTER);
	}

}
