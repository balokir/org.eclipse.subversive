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

import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.ui.synchronize.AbstractSVNSyncInfo;
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

	protected FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter.SyncInfoDirectionFilter(new int[] {SyncInfo.OUTGOING, SyncInfo.CONFLICTING}) {
            public boolean select(SyncInfo info) {
                return super.select(info) && IStateFilter.SF_VERSIONED_FILES.accept(((AbstractSVNSyncInfo)info).getLocalResource());
            }
        };
	}

	protected IActionOperation execute(final FilteredSynchronizeModelOperation operation) {
		operation.getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				org.eclipse.team.svn.ui.action.local.SetKeywordsAction.doSetKeywords(operation.getSelectedResourcesRecursive(IStateFilter.SF_VERSIONED_FILES));
			}
		});
		return null;
	}

}
