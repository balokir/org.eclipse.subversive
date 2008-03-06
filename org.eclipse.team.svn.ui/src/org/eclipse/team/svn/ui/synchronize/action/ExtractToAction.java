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

package org.eclipse.team.svn.ui.synchronize.action;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.ExtractToOperationLocal;
import org.eclipse.team.svn.core.operation.remote.ExtractToOperationRemote;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Extract To action for Synchronize View (both incoming and outgoing - 
 * the conflicting resources are ignored)
 * 
 * @author Alexei Goncharov
 */
public class ExtractToAction extends AbstractSynchronizeModelAction {

	public ExtractToAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter.SyncInfoDirectionFilter(new int[] {SyncInfo.INCOMING, SyncInfo.OUTGOING, SyncInfo.CONFLICTING});
	}
	
	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		AbstractSVNSyncInfo [] infos = this.getSVNSyncInfos();
		for (int i = 0; i < infos.length; i++) {
			if (SyncInfo.getDirection(infos[i].getKind()) == SyncInfo.CONFLICTING) {
				return false;
			}
		}
		return true;
	}
	
	protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		IResource []outgoingChanges = this.syncInfoSelector.getSelectedResources(
				new ISyncStateFilter.StateFilterWrapper(new IStateFilter.OrStateFilter(
						new IStateFilter[] {IStateFilter.SF_COMMITABLE, IStateFilter.SF_NEW}), null, true));
		IResource []incomingChanges = this.syncInfoSelector.getSelectedResources(
				new ISyncStateFilter.StateFilterWrapper(null, IStateFilter.SF_ANY_CHANGE, true));
		HashSet<IResource> deletionsOnly = new HashSet<IResource>(Arrays.asList(this.syncInfoSelector.getSelectedResources(
				new ISyncStateFilter.StateFilterWrapper(null, IStateFilter.SF_DELETED, false))));
		HashSet<IRepositoryResource> incomingResourcesToOperate = new HashSet<IRepositoryResource>();
		HashSet<String> markedForDelition = new HashSet<String>();
		HashMap<String, String> resource2project = new HashMap<String, String>();
		for (IResource current : incomingChanges) {
			IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(current);
			if (current instanceof IProject) {
				resource2project.put(remote.getName(), current.getName());
			}
			incomingResourcesToOperate.add(remote);
			if (deletionsOnly.contains(current)) {
				markedForDelition.add(remote.getUrl());
			}
		}
		DirectoryDialog fileDialog = new DirectoryDialog(configuration.getSite().getShell());
		fileDialog.setText(SVNTeamUIPlugin.instance().getResource("ExtractToAction.Select.Title"));
		fileDialog.setMessage(SVNTeamUIPlugin.instance().getResource("ExtractToAction.Select.Description"));
		String path = fileDialog.open();
		if (path == null) {
			return null;
		}
		CompositeOperation op = new CompositeOperation(SVNTeamPlugin.instance().getResource("Operation.ExtractTo"));
		op.add(new ExtractToOperationLocal(outgoingChanges, path, true));
		op.add(new ExtractToOperationRemote(incomingResourcesToOperate.toArray(new IRepositoryResource[incomingResourcesToOperate.size()]), markedForDelition, path, resource2project, true));
		return op;
	}

}
