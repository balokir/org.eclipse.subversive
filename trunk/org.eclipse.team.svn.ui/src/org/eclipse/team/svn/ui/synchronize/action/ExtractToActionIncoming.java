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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.internal.ui.synchronize.SynchronizeModelElement;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.ExtractToOperationRemote;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.synchronize.update.UpdateSyncInfo;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Incoming Extract To action for Synchronize View
 * 
 * @author Alexei Goncharov
 */
public class ExtractToActionIncoming extends AbstractSynchronizeModelAction {

	private HashSet<IRepositoryResource> incomingResourcesToOperate;
	private HashSet<IResource> incomingMapedToProject;
	private HashSet<String> markedForDelition;
	
	public ExtractToActionIncoming(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}
	
	protected FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter.SyncInfoDirectionFilter(new int[] {SyncInfo.INCOMING, SyncInfo.CONFLICTING});
	}

	protected IActionOperation execute(final FilteredSynchronizeModelOperation operation) {
		this.incomingResourcesToOperate = new HashSet<IRepositoryResource>();
		this.incomingMapedToProject = new HashSet<IResource>();
		this.markedForDelition = new HashSet<String>();
		ArrayList<IResource> allSelected = new ArrayList<IResource>();
		IResource [] all = operation.getSelectedResourcesRecursive();
		for (IResource current : all) {
			allSelected.add(current);
		}
		IStructuredSelection selection = this.getStructuredSelection();
		for (Iterator it = selection.iterator(); it.hasNext();) {
			Object obj = it.next();
			if (obj instanceof SynchronizeModelElement) {
				this.collectIncomingRecoursively((SynchronizeModelElement)obj);
			}
		}
		IResource [] parentsForIncoming = FileUtility.getParents(this.incomingMapedToProject.toArray(new IResource[0]), false);
		for (IResource current : parentsForIncoming) {
			if (allSelected.contains(current)) {
				this.incomingResourcesToOperate.add(
						SVNRemoteStorage.instance().asRepositoryResource(current));
			}
		}
		final String path[] = {null};
		operation.getShell().getDisplay().syncExec(new Runnable () {
			public void run() {
				DirectoryDialog fileDialog = new DirectoryDialog(operation.getShell());
				fileDialog.setText(SVNTeamUIPlugin.instance().getResource("ExtractToAction.Select.Title"));
				fileDialog.setMessage(SVNTeamUIPlugin.instance().getResource("ExtractToAction.Select.Description"));
				path[0] = fileDialog.open();
			}
		});
		if (path[0] != null) {
			return new ExtractToOperationRemote(incomingResourcesToOperate.toArray(new IRepositoryResource[0]), this.markedForDelition, path[0], true);
		}
		return null;
	}
	
	private void collectIncomingRecoursively(SynchronizeModelElement element) {
		if (element instanceof SyncInfoModelElement) {
		UpdateSyncInfo info = (UpdateSyncInfo)(((SyncInfoModelElement)element).getSyncInfo());
			if (SyncInfo.getDirection(info.getKind()) == SyncInfo.INCOMING
					|| SyncInfo.getDirection(info.getKind()) == SyncInfo.CONFLICTING) {
				IRepositoryResource toAdd = SVNRemoteStorage.instance().asRepositoryResource(info.getLocalResource().getResource());
				if (!this.incomingResourcesToOperate.contains(toAdd)) {
					if (info.getChange(info.getKind()) == SyncInfo.DELETION) {
						this.markedForDelition.add(toAdd.getUrl());
					}
					this.incomingResourcesToOperate.add(toAdd);
					this.incomingMapedToProject.add(info.getLocalResource().getResource());
				}
			}
		}
		IDiffElement [] children = element.getChildren();
		for (IDiffElement diffElement : children) {
			this.collectIncomingRecoursively((SyncInfoModelElement)diffElement);
		}
	}

}
