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

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.DiffViewerSettings;
import org.eclipse.team.svn.core.operation.local.DiffViewerSettings.ExternalProgramParameters;
import org.eclipse.team.svn.core.operation.local.RunExternalCompareOperation.DefaultExternalProgramParametersProvider;
import org.eclipse.team.svn.core.operation.local.RunExternalCompareOperation.DetectExternalCompareOperationHelper;
import org.eclipse.team.svn.core.operation.local.RunExternalCompareOperation.ExternalCompareOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.action.local.CompareWithWorkingCopyAction;
import org.eclipse.team.svn.ui.preferences.SVNTeamDiffViewerPage;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Open in external compare editor action implementation for Synchronize view
 * 
 * @author Igor Burilo
 */
public class OpenInExternalCompareEditorAction extends AbstractSynchronizeModelAction {
	
	protected ExternalProgramParameters externalProgramParams;
	
	public OpenInExternalCompareEditorAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);			
	}

	protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {					
		IActionOperation op = null;
		if (this.externalProgramParams != null) {
			IResource resource = this.getSelectedResource();				
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
			IRepositoryResource remote = local.isCopied() ? SVNUtility.getCopiedFrom(resource) : SVNRemoteStorage.instance().asRepositoryResource(resource);							
			op = new ExternalCompareOperation(local, remote, new DefaultExternalProgramParametersProvider(this.externalProgramParams));				
		}					
		return op;
	}
	
	protected boolean updateSelection(IStructuredSelection selection) {			
		if (super.updateSelection(selection) && selection.size() == 1) {
			IResource resource = this.getSelectedResource();				
			DiffViewerSettings diffSettings = SVNTeamDiffViewerPage.loadDiffViewerSettings();
			DetectExternalCompareOperationHelper detectCompareHelper = new DetectExternalCompareOperationHelper(resource, diffSettings);
			detectCompareHelper.execute(new NullProgressMonitor());
			this.externalProgramParams = detectCompareHelper.getExternalProgramParameters();
			if (this.externalProgramParams != null) {
				return true;
			}				
		}			
		return false;
	}
			
	public FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter() {
			public boolean select(SyncInfo info) {
				return CompareWithWorkingCopyAction.COMPARE_FILTER.accept(((AbstractSVNSyncInfo)info).getLocalResource());
			}
		};
	}		
}
