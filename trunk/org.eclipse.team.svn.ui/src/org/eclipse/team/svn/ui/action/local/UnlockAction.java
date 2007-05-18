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
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.UnlockOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.action.AbstractRecursiveTeamAction;
import org.eclipse.team.svn.ui.dialog.UnlockResourcesDialog;

/**
 * Unlock action implementation
 * 
 * @author Alexander Gurov
 */
public class UnlockAction extends AbstractRecursiveTeamAction {

    public UnlockAction() {
        super();
    }

	public void run(IAction action) {
		IResource []checkRecursive = this.getSelectedResources();
		boolean recursive = false;
		for (int i = 0; i < checkRecursive.length; i++) {
			if (checkRecursive[i].getType() != IResource.FILE) {
				recursive = true;
				break;
			}
		}
		UnlockResourcesDialog dialog = new UnlockResourcesDialog(this.getShell(), recursive);
		if (dialog.open() == 0) {
			IResource []resources = this.getSelectedResourcesRecursive(UnlockAction.SF_LOCKED, dialog.isRecursive() ? IResource.DEPTH_INFINITE : IResource.DEPTH_ONE);
		    UnlockOperation mainOp = new UnlockOperation(resources);
		    
			CompositeOperation op = new CompositeOperation(mainOp.getId());
			op.add(mainOp);
			op.add(new RefreshResourcesOperation(resources));
			
			this.runScheduled(op);
		}
	}
	
    protected boolean isEnabled() {
        return this.checkForResourcesPresenceRecursive(UnlockAction.SF_LOCKED);
    }

    protected static final IStateFilter SF_LOCKED = new IStateFilter() {
        public boolean accept(IResource resource, String state, int mask) {
            if (IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask)) {
                if (resource != null) {
                    ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
                    return local != null && local.isLocked();
                }
                return true;
            }
            return false;
        }
		public boolean allowsRecursion(IResource resource, String state, int mask) {
			return IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask);
		}
    };
    
}
