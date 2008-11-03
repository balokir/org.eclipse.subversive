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

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.ui.operation.ShowConflictEditorOperation;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Edit conflicts action helper implementation for Synchronize view
 * 
 * @author Igor Burilo
 */
public class EditConflictsActionHelper extends AbstractActionHelper {

	public EditConflictsActionHelper(IAction action, ISynchronizePageConfiguration configuration) {
		super(action, configuration);
	}
	
	public FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter.SyncInfoDirectionFilter(new int[] {SyncInfo.CONFLICTING}) {
			public boolean select(SyncInfo info) {
				return super.select(info) && info.getLocal().getType() == IResource.FILE && IStateFilter.SF_CONFLICTING.accept(((AbstractSVNSyncInfo)info).getLocalResource());
			}
		};
	}

	public IActionOperation getOperation() {
		return new ShowConflictEditorOperation(this.getSyncInfoSelector().getSelectedResources(), false);
	}

}
