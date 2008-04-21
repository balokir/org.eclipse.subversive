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
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.local.JavaHLMergeOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.SaveProjectMetaOperation;
import org.eclipse.team.svn.core.operation.remote.LocateResourceURLInHistoryOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.ResourcesParentsProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractNonRecursiveTeamAction;
import org.eclipse.team.svn.ui.dialog.AdvancedDialog;
import org.eclipse.team.svn.ui.dialog.OperationErrorDialog;
import org.eclipse.team.svn.ui.operation.ShowMergeViewOperation;
import org.eclipse.team.svn.ui.panel.local.MergePanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;

/**
 * Merge action implementation
 * 
 * @author Alexander Gurov
 */
public class MergeAction extends AbstractNonRecursiveTeamAction {

	public MergeAction() {
		super();
	}

	public void runImpl(IAction action) {
	    IResource []resources = this.getSelectedResources(IStateFilter.SF_EXCLUDE_DELETED);
		
		if (!OperationErrorDialog.isAcceptableAtOnce(resources, SVNTeamUIPlugin.instance().getResource("MergeAction.MergeError"), this.getShell())) {
			return;
		}
	    
		IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(resources[0]);
	    ILocalResource localTo = SVNRemoteStorage.instance().asLocalResource(resources[0]);
	    if (localTo == null) {
	    	return;
	    }
		long revision = localTo.getRevision();
		
		if (resources.length > 1) {
			revision = SVNRevision.INVALID_REVISION_NUMBER;
			remote = remote.getRoot();
		}

		MergePanel panel = new MergePanel(resources, remote, revision);
	    AdvancedDialog dialog = new AdvancedDialog(this.getShell(), panel);
	    if (dialog.open() == 0) {
	    	LocateResourceURLInHistoryOperation locateFirst = new LocateResourceURLInHistoryOperation(panel.getFirstSelection(), true);
	    	LocateResourceURLInHistoryOperation locateSecond = new LocateResourceURLInHistoryOperation(panel.getSecondSelection(), true);
	    	if (SVNTeamPreferences.getMergeBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.MERGE_USE_JAVAHL_NAME)) {
		    	JavaHLMergeOperation mainOp = new JavaHLMergeOperation(resources, locateFirst, locateSecond, false, panel.getIgnoreAncestry());
		    	CompositeOperation op = new CompositeOperation(mainOp.getId());
		    	op.add(locateFirst);
		    	op.add(locateSecond);
	    		SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(resources);
	    		op.add(saveOp);
		    	op.add(mainOp);
	    		op.add(new RestoreProjectMetaOperation(saveOp));
		    	op.add(new RefreshResourcesOperation(new ResourcesParentsProvider(resources)));
		    	this.runScheduled(op);
	    	}
	    	else {
	    		ShowMergeViewOperation mainOp = new ShowMergeViewOperation(resources, locateFirst, locateSecond, panel.getIgnoreAncestry(), this.getTargetPart());
		    	CompositeOperation op = new CompositeOperation(mainOp.getId());
		    	op.add(locateFirst);
		    	op.add(locateSecond);
		    	op.add(mainOp);
	    		this.runScheduled(op);
	    	}
	    }
	}

	public boolean isEnabled() {
		return this.checkForResourcesPresence(IStateFilter.SF_EXCLUDE_DELETED);
	}

	protected boolean needsToSaveDirtyEditors() {
		return true;
	}
	
}
