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
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.core.synchronize.IMergeSyncInfo;
import org.eclipse.team.svn.ui.compare.PropertyCompareInput;
import org.eclipse.team.svn.ui.compare.ThreeWayPropertyCompareInput;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Property comparison for merge view.
 * 
 * @author Alexei Goncharov
 */
public class MergePropertiesAction extends AbstractSynchronizeModelAction {

	public MergePropertiesAction(String text, ISynchronizePageConfiguration configuration) {
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
				ILocalResource incoming;
				IResourceChange ancestor;
				if (syncInfo instanceof IMergeSyncInfo) {
					//used in Merge view
					incoming = ((IMergeSyncInfo) syncInfo).getRemoteResource();
					ancestor = ((IMergeSyncInfo) syncInfo).getBaseResource();
				} else {
					incoming = syncInfo.getRemoteChangeResource();	
					ancestor = (IResourceChange)syncInfo.getBaseChangeResource();
				}
				if (!(incoming instanceof IResourceChange) || ancestor == null) {
					return false;
				}
				boolean retVal = IStateFilter.SF_EXCLUDE_DELETED.accept(incoming) & IStateFilter.ST_DELETED != incoming.getStatus();				
				return retVal && (IStateFilter.SF_HAS_PROPERTIES_CHANGES.accept(incoming)
						|| IStateFilter.SF_HAS_PROPERTIES_CHANGES.accept(syncInfo.getLocalResource()));
			}
		}
		return false;
	}
	
	protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		IResource resource = this.getSelectedResource();
		AbstractSVNSyncInfo syncInfo = (AbstractSVNSyncInfo)((SyncInfoModelElement)elements[0]).getSyncInfo();
		IResourceChange right;
		IResourceChange ancestor;
		if (syncInfo instanceof IMergeSyncInfo) {
			//used in Merge view
			IMergeSyncInfo mergeSyncInfo = (IMergeSyncInfo) syncInfo;
			right = mergeSyncInfo.getRemoteResource();
			ancestor = mergeSyncInfo.getBaseResource();
		} else {
			right = (IResourceChange)syncInfo.getRemoteChangeResource();
			ancestor = (IResourceChange)syncInfo.getBaseChangeResource();
		}
				
		SVNEntryRevisionReference baseReference = new SVNEntryRevisionReference(ancestor.getOriginator().getUrl(), ancestor.getPegRevision(), SVNRevision.fromNumber(ancestor.getRevision()));
		SVNEntryRevisionReference remoteReference = new SVNEntryRevisionReference(right.getOriginator().getUrl(), right.getPegRevision(), SVNRevision.fromNumber(right.getRevision()));
		PropertyCompareInput input = new ThreeWayPropertyCompareInput(new CompareConfiguration(),
				resource,
				remoteReference,
				baseReference,
				right.getOriginator().getRepositoryLocation(),
				-1);
		CompareUI.openCompareEditor(input);
		return null;
	}

}
