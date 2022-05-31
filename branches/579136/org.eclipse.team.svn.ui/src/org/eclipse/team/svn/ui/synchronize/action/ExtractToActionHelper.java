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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.ExtractToOperationLocal;
import org.eclipse.team.svn.core.operation.local.FiniExtractLogOperation;
import org.eclipse.team.svn.core.operation.local.InitExtractLogOperation;
import org.eclipse.team.svn.core.operation.remote.ExtractToOperationRemote;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Extract To action helper for Synchronize View (both incoming and outgoing - 
 * the conflicting resources are ignored)
 * 
 * @author Igor Burilo
 */
public class ExtractToActionHelper extends AbstractActionHelper {

	public ExtractToActionHelper(IAction action, ISynchronizePageConfiguration configuration) {
		super(action, configuration);
	}

	public FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter.SyncInfoDirectionFilter(new int[] {SyncInfo.INCOMING, SyncInfo.OUTGOING, SyncInfo.CONFLICTING});
	}
	
	public IActionOperation getOperation() {
		DirectoryDialog fileDialog = new DirectoryDialog(configuration.getSite().getShell());
		fileDialog.setText(SVNUIMessages.ExtractToAction_Select_Title);
		fileDialog.setMessage(SVNUIMessages.ExtractToAction_Select_Description);
		String path = fileDialog.open();
		if (path == null) {
			return null;
		}
		
		IResource []outgoingChanges = this.getSyncInfoSelector().getSelectedResources(
				new ISyncStateFilter.StateFilterWrapper(new IStateFilter.OrStateFilter(
						new IStateFilter[] {IStateFilter.SF_COMMITABLE, IStateFilter.SF_NEW}), null, true));
		HashSet<IResource> outgoingResources = new HashSet<IResource>(Arrays.asList(outgoingChanges));
		for (IResource current : outgoingChanges) {
			outgoingResources.add(current.getProject());
		}
		outgoingChanges = outgoingResources.toArray(new IResource[outgoingResources.size()]);
		IResource []incomingChanges = this.getSyncInfoSelector().getSelectedResources(
				new ISyncStateFilter.StateFilterWrapper(null, IStateFilter.SF_ANY_CHANGE, true));
		HashSet<IResource> incomingWithProjects = new HashSet<IResource>(Arrays.asList(incomingChanges));
		for (IResource current : incomingChanges) {
			incomingWithProjects.add(current.getProject());
		}
		incomingChanges = incomingWithProjects.toArray(new IResource[incomingWithProjects.size()]);
		HashSet<IResource> deletionsOnly = new HashSet<IResource>(Arrays.asList(this.getSyncInfoSelector().getSelectedResources(
				new ISyncStateFilter.StateFilterWrapper(null, IStateFilter.SF_DELETED, false))));
		HashSet<IRepositoryResource> incomingResourcesToOperate = new HashSet<IRepositoryResource>();
		HashSet<String> markedForDelition = new HashSet<String>();
		HashMap<String, String> resource2project = new HashMap<String, String>();
		HashMap<String, String> url2status = new HashMap<String, String>();
		for (IResource current : incomingChanges) {
			IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(current);
			IRepositoryResource projectRemote = SVNRemoteStorage.instance().asRepositoryResource(current.getProject());
			if (current instanceof IProject) {
				resource2project.put(remote.getUrl(), current.getName());
			}
			else if (!SVNUtility.createPathForSVNUrl(projectRemote.getUrl()).isPrefixOf(SVNUtility.createPathForSVNUrl(remote.getUrl()))) {
				//external reference
				resource2project.put(remote.getUrl(), current.getFullPath().toString().substring(1));
			}
			incomingResourcesToOperate.add(remote);			
			AbstractSVNSyncInfo[] syncInfos = this.getSVNSyncInfos();
			for (AbstractSVNSyncInfo info : syncInfos) {
				if (SyncInfo.getDirection(info.getKind()) == SyncInfo.INCOMING) {
					IResourceChange change = (IResourceChange)info.getRemoteChangeResource();
					if (remote.getUrl().equals(change.getOriginator().getUrl())) {
						url2status.put(remote.getUrl(), change.getStatus());
					}
				}
			}
			if (deletionsOnly.contains(current)) {
				markedForDelition.add(remote.getUrl());
			}
		}
		CompositeOperation op = new CompositeOperation("Operation_ExtractTo", SVNMessages.class); //$NON-NLS-1$
		InitExtractLogOperation logger = new InitExtractLogOperation(path);
		op.add(logger);
		op.add(new ExtractToOperationLocal(outgoingChanges, path, true, logger));
		op.add(new ExtractToOperationRemote(incomingResourcesToOperate.toArray(new IRepositoryResource[incomingResourcesToOperate.size()]), url2status, markedForDelition, path, resource2project, logger, true));
		op.add(new FiniExtractLogOperation(logger));
		return op;
	}

}
