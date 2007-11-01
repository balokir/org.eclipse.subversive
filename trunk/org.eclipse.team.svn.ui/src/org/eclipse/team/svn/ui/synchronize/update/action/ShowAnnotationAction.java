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

package org.eclipse.team.svn.ui.synchronize.update.action;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.ILocalFile;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.operation.LocalShowAnnotationOperation;
import org.eclipse.team.svn.ui.operation.RemoteShowAnnotationOperation;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeModelAction;
import org.eclipse.team.svn.ui.synchronize.action.ISyncStateFilter;
import org.eclipse.team.svn.ui.synchronize.update.UpdateSyncInfo;
import org.eclipse.team.svn.ui.synchronize.variant.ResourceVariant;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Show annotation action implementation
 * 
 * @author Alexander Gurov
 */
public class ShowAnnotationAction extends AbstractSynchronizeModelAction {

	public ShowAnnotationAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		if (selection.size() != 1 || !(selection.getFirstElement() instanceof SyncInfoModelElement)) {
		    return false;
		}
		SyncInfoModelElement element = (SyncInfoModelElement)selection.getFirstElement();
		UpdateSyncInfo sync = (UpdateSyncInfo)element.getSyncInfo();
		ILocalResource outgoing = sync.getLocalResource();
		ResourceVariant incoming = (ResourceVariant)sync.getRemote();
		return 
			outgoing instanceof ILocalFile && 
			(ISyncStateFilter.SF_ONREPOSITORY.accept(outgoing.getResource(), outgoing.getStatus(), outgoing.getChangeMask()) ||
			!IStateFilter.SF_NOTEXISTS.accept(incoming.getResource().getResource(), incoming.getStatus(), incoming.getResource().getChangeMask()));
	}
	
	protected IActionOperation execute(final FilteredSynchronizeModelOperation operation) {
		return new AbstractNonLockingOperation("Operation.UShowAnnotation") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				operation.getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
					    IResource resource = operation.getSelectedResource();
						ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
						if (local != null && IStateFilter.SF_ONREPOSITORY.accept(resource, local.getStatus(), local.getChangeMask())) {
							UIMonitorUtility.doTaskBusyDefault(new LocalShowAnnotationOperation(resource, operation.getPart().getSite().getPage()));
						}
						IResourceChange change = ((IResourceChange)((ResourceVariant)operation.getSVNSyncInfo().getRemote()).getResource());
						IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(resource);
						remote.setPegRevision(change.getPegRevision());
						remote.setSelectedRevision(Revision.fromNumber(change.getRevision()));
						UIMonitorUtility.doTaskBusyDefault(new RemoteShowAnnotationOperation(remote, operation.getPart().getSite().getPage()));
					}
				});
			}
		};
	}

}
