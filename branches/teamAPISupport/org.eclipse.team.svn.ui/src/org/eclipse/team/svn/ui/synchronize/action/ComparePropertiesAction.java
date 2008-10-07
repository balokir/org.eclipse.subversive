/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize.action;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.compare.PropertyCompareInput;
import org.eclipse.team.svn.ui.compare.ThreeWayPropertyCompareInput;
import org.eclipse.team.svn.ui.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.ui.synchronize.variant.RemoteResourceVariant;
import org.eclipse.team.svn.ui.synchronize.variant.ResourceVariant;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Compare properties action for Synchronize View
 * 
 * @author Alexei Goncharov
 */
public class ComparePropertiesAction extends AbstractSynchronizeModelAction {

	public ComparePropertiesAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}
	
	protected boolean needsToSaveDirtyEditors() {
		return false;
	}
	
	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		if (selection.size() == 1 && selection.getFirstElement() instanceof SyncInfoModelElement) {
			ISynchronizeModelElement element = (ISynchronizeModelElement)selection.getFirstElement();
			if (element instanceof SyncInfoModelElement) {
				AbstractSVNSyncInfo syncInfo = (AbstractSVNSyncInfo)((SyncInfoModelElement)selection.getFirstElement()).getSyncInfo();
				ILocalResource incoming = ((ResourceVariant)syncInfo.getRemote()).getResource();
				boolean retVal = IStateFilter.SF_EXCLUDE_DELETED.accept(incoming);
				if (incoming instanceof IResourceChange) {
					retVal &= IStateFilter.ST_DELETED != incoming.getStatus();
				}
				return retVal && ((incoming.getChangeMask() & ILocalResource.PROP_MODIFIED) != 0
						|| (syncInfo.getLocalResource().getChangeMask() & ILocalResource.PROP_MODIFIED) != 0
						|| incoming.getResource() instanceof IContainer);
			}
		}
		return false;
	}
	
	protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		IResource resource = this.getSelectedResource();
		ILocalResource baseResource = SVNRemoteStorage.instance().asLocalResource(resource);
		IRepositoryResource remote =  SVNRemoteStorage.instance().asRepositoryResource(resource);
	    SVNEntryRevisionReference baseReference = new SVNEntryRevisionReference(FileUtility.getWorkingCopyPath(resource), null, SVNRevision.BASE);
	    SVNEntryRevisionReference remoteReference = baseReference;
	    ILocalResource change = ((RemoteResourceVariant)this.getSelectedSVNSyncInfo().getRemote()).getResource();
	    if (change instanceof IResourceChange) {
	    	remote = ((IResourceChange)change).getOriginator();
	    	remoteReference = new SVNEntryRevisionReference(remote.getUrl(), remote.getPegRevision(), SVNRevision.fromNumber(((IResourceChange)change).getRevision()));
	    }
		PropertyCompareInput input = new ThreeWayPropertyCompareInput(new CompareConfiguration(),
				resource,
				remoteReference,
				baseReference,
				remote.getRepositoryLocation(),
				baseResource.getRevision());
		CompareUI.openCompareEditor(input);
		return null;
	}

}
