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

package org.eclipse.team.svn.ui.synchronize.merge.action;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IResourcePropertyProvider;
import org.eclipse.team.svn.core.operation.remote.GetRemotePropertiesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.core.synchronize.IMergeSyncInfo;
import org.eclipse.team.svn.ui.operation.ShowPropertiesOperation;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeModelAction;
import org.eclipse.team.svn.ui.synchronize.action.ShowIncomingPropertiesActionHelper;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.PlatformUI;

/**
 * Show properties action implementation for Merge view.
 * 
 * @author Igor Burilo
 */
public class ShowIncomingPropertiesAction extends AbstractSynchronizeModelAction {

	protected ShowIncomingPropertiesActionHelper actionHelper;
	
	public ShowIncomingPropertiesAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
		this.actionHelper = new ShowIncomingPropertiesActionHelper(this, configuration);
	}

	public ShowIncomingPropertiesAction(String text, ISynchronizePageConfiguration configuration, ISelectionProvider selectionProvider) {
		super(text, configuration, selectionProvider);
		this.actionHelper = new ShowIncomingPropertiesActionHelper(this, configuration);
	}
	
	protected boolean needsToSaveDirtyEditors() {
		return false;
	}
	
	
	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		if (selection.size() == 1 && selection.getFirstElement() instanceof SyncInfoModelElement) {
			ISynchronizeModelElement element = (ISynchronizeModelElement)selection.getFirstElement();
			if (element instanceof SyncInfoModelElement) {
				SyncInfo syncInfo = ((SyncInfoModelElement)selection.getFirstElement()).getSyncInfo();
				if (syncInfo instanceof IMergeSyncInfo) {
					IMergeSyncInfo mergeSyncInfo = (IMergeSyncInfo) syncInfo; 
					IResourceChange change = mergeSyncInfo.getRemoteResource();		
					if (change != null) {
						return IStateFilter.ST_DELETED != change.getStatus();	
					}					
				}
			}
		}
		return false;
	}
	
	
	protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {		
		AbstractSVNSyncInfo syncInfo = this.getSelectedSVNSyncInfo();
		if (syncInfo instanceof IMergeSyncInfo) {
			IResourceChange change = ((IMergeSyncInfo) syncInfo).getRemoteResource();					    
		    IRepositoryResource remote = change.getOriginator();
			IResourcePropertyProvider provider = new GetRemotePropertiesOperation(remote);
			ShowPropertiesOperation op = new ShowPropertiesOperation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), remote, provider);
			return op;
		}				
		return null;
	}

}
