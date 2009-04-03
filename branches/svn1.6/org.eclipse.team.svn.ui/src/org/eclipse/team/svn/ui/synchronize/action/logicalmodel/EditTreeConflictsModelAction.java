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

package org.eclipse.team.svn.ui.synchronize.action.logicalmodel;

import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.local.EditTreeConflictsPanel;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeLogicalModelAction;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Edit tree conflicts logical model action implementation for Synchronize view
 * 
 * TODO rework it to merge with 'Edit Conflicts' action
 * 
 * @author Igor Burilo
 */
public class EditTreeConflictsModelAction extends AbstractSynchronizeLogicalModelAction {

	public EditTreeConflictsModelAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected FastSyncInfoFilter getSyncInfoFilter() {		
		return new FastSyncInfoFilter.SyncInfoDirectionFilter(new int[] {SyncInfo.CONFLICTING}) {
			public boolean select(SyncInfo info) {
				//TODO apply only to one resource
				return super.select(info) && IStateFilter.SF_TREE_CONFLICTING.accept(((AbstractSVNSyncInfo)info).getLocalResource());
			}
		};
	}
	
	protected IActionOperation getOperation() {		
		AbstractSVNSyncInfo syncInfo = this.getSelectedSVNSyncInfo();
		ILocalResource local = syncInfo.getLocalResource();		
		EditTreeConflictsPanel editConflictsPanel = new EditTreeConflictsPanel(local);
		DefaultDialog dialog = new DefaultDialog(this.getConfiguration().getSite().getShell(), editConflictsPanel);
		if (dialog.open() == 0) {
			return editConflictsPanel.getOperation();			
		}		
		return null;
	}

}
