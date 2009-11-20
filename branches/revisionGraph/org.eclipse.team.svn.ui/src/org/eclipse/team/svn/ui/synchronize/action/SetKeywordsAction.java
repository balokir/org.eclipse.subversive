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

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Set keywords action implementation for Synchronize view
 * 
 * @author Alexei Goncharov
 */
public class SetKeywordsAction extends AbstractSynchronizeModelAction {
	public SetKeywordsAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected boolean needsToSaveDirtyEditors() {
		return false;
	}
	
	protected FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter() {
            public boolean select(SyncInfo info) {
                return super.select(info) && IStateFilter.SF_VERSIONED_FILES.accept(((AbstractSVNSyncInfo)info).getLocalResource());
            }
        };
	}
	
	protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		org.eclipse.team.svn.ui.action.local.SetKeywordsAction.doSetKeywords(SetKeywordsAction.this.syncInfoSelector.getSelectedResources());
		return null;
	}

}
