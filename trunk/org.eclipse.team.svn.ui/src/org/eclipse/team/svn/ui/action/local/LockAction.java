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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.local.LockOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.ui.action.AbstractRecursiveTeamAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.local.CommitPanel;
import org.eclipse.team.svn.ui.panel.local.LockPanel;

/**
 * Lock action implementation
 * 
 * @author Alexander Gurov
 */
public class LockAction extends AbstractRecursiveTeamAction {

    public LockAction() {
        super();
    }

	public void runImpl(IAction action) {
		IResource []selectedResources = this.getSelectedResources();
		boolean containsFolder = false;
		for (int i = 0; i < selectedResources.length; i++) {
			if (selectedResources[i] instanceof IContainer) {
				containsFolder = true;
				break;
			}
		}
		CommitPanel.CollectPropertiesOperation cop = new CommitPanel.CollectPropertiesOperation(selectedResources);
		ProgressMonitorUtility.doTaskExternal(cop, null);
		LockPanel commentPanel = new LockPanel(!containsFolder, cop.getMinLockSize());
		DefaultDialog dialog = new DefaultDialog(this.getShell(), commentPanel);
		if (dialog.open() == 0) {
		    IResource []resources = this.getSelectedResourcesRecursive(SF_NONLOCKED, commentPanel.isRecursive() ? IResource.DEPTH_INFINITE : IResource.DEPTH_ONE);
		    LockOperation mainOp = new LockOperation(resources, commentPanel.getMessage(), commentPanel.getForce());
		    
			CompositeOperation op = new CompositeOperation(mainOp.getId());
			op.add(mainOp);
			op.add(new RefreshResourcesOperation(resources));
			
			this.runScheduled(op);
		}
	}
	
    public boolean isEnabled() {
        return this.checkForResourcesPresenceRecursive(LockAction.SF_NONLOCKED);
    }

    public static IStateFilter SF_NONLOCKED = new IStateFilter.AbstractStateFilter() {
        protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
            if (resource instanceof IFile && 
                IStateFilter.SF_EXCLUDE_DELETED.accept(resource, state, mask)) {
                if (resource != null) {
                    local = this.takeLocal(local, resource);
                    return local != null && !local.isLocked();
                }
                return true;
            }
            return false;
        }
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return IStateFilter.SF_EXCLUDE_DELETED.accept(resource, state, mask);
		}
    };
    
}
