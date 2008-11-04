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

package org.eclipse.team.svn.ui.synchronize.update.action;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.UnlockOperation;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.ui.dialog.UnlockResourcesDialog;
import org.eclipse.team.svn.ui.synchronize.action.AbstractActionHelper;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Unlock action helper implementation for Synchronize View
 * 
 * @author Igor Burilo
 */
public class UnlockActionHelper extends AbstractActionHelper {
	
	public UnlockActionHelper(IAction action, ISynchronizePageConfiguration configuration) {
		super(action, configuration);
	}
	
	public FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter() {
            public boolean select(SyncInfo info) {
                return super.select(info) && IStateFilter.SF_LOCKED.accept(((AbstractSVNSyncInfo)info).getLocalResource());
            }
        };
	}
	
	public IActionOperation getOperation() {
		IResource [] selectedResources = this.getSyncInfoSelector().getSelectedResources();
		UnlockResourcesDialog dialog = new UnlockResourcesDialog(configuration.getSite().getShell(), false);
		if (dialog.open() == 0) {
		    UnlockOperation mainOp = new UnlockOperation(selectedResources);
			CompositeOperation unlockOp = new CompositeOperation(mainOp.getId());
			unlockOp.add(mainOp);
			unlockOp.add(new RefreshResourcesOperation(selectedResources));
			return unlockOp;
		}
		return null;
	}

}
