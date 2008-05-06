/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.action.local;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.local.AddToSVNIgnoreOperation;
import org.eclipse.team.svn.core.operation.local.AddToSVNOperation;
import org.eclipse.team.svn.core.operation.local.ClearLocalStatusesOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.action.AbstractNonRecursiveTeamAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.local.AddToSVNPanel;
import org.eclipse.team.svn.ui.panel.local.IgnoreMethodPanel;

/**
 * Team services menu "add to svn::ignore" action implementation
 * 
 * @author Alexander Gurov
 */
public class AddToSVNIgnoreAction extends AbstractNonRecursiveTeamAction {

	public AddToSVNIgnoreAction() {
		super();
	}

	public void runImpl(IAction action) {
		IResource []resources = this.getSelectedResources(AddToSVNIgnoreAction.SF_NEW_AND_PARENT_VERSIONED);

		IResource []operableParents = FileUtility.getOperableParents(resources, IStateFilter.SF_UNVERSIONED);
		if (operableParents.length > 0) {
		    AddToSVNPanel panel = new AddToSVNPanel(operableParents);
			DefaultDialog dialog1 = new DefaultDialog(this.getShell(), panel);
			if (dialog1.open() != 0) {
			    return;
			}
		    operableParents = panel.getSelectedResources();
		}
	    
		IgnoreMethodPanel panel = new IgnoreMethodPanel(resources);
		DefaultDialog dialog = new DefaultDialog(this.getShell(), panel);
		if (dialog.open() == 0) {
			AddToSVNIgnoreOperation mainOp = new AddToSVNIgnoreOperation(resources, panel.getIgnoreType(), panel.getIgnorePattern());
			
			CompositeOperation op = new CompositeOperation(mainOp.getId());
			
			if (operableParents.length > 0) {
				op.add(new AddToSVNOperation(operableParents));
				op.add(new ClearLocalStatusesOperation(operableParents));
			}

			op.add(mainOp);
			HashSet<IResource> tmp = new HashSet<IResource>(Arrays.asList(resources));
			for (int i = 0; i < resources.length; i++) {
				tmp.add(resources[i].getParent());
			}
			IResource []resourcesAndParents = tmp.toArray(new IResource[tmp.size()]);
			op.add(new RefreshResourcesOperation(resourcesAndParents, IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL));

			this.runScheduled(op);
		}
	}

	public boolean isEnabled() {
		return this.checkForResourcesPresence(AddToSVNIgnoreAction.SF_NEW_AND_PARENT_VERSIONED);
	}
	
	public static IStateFilter SF_NEW_AND_PARENT_VERSIONED = new IStateFilter.AbstractStateFilter() {
        protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
            if (state == IStateFilter.ST_NEW) {
            	IContainer parent = resource.getParent();
            	if (parent != null) {
            		ILocalResource localParent = SVNRemoteStorage.instance().asLocalResource(parent);
            		if (localParent != null) {
            			return IStateFilter.SF_VERSIONED.accept(localParent);	
            		}
                }
            }
            return false;
        }
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return state != IStateFilter.ST_IGNORED && state != IStateFilter.ST_OBSTRUCTED && state != IStateFilter.ST_LINKED;
		}
    };

}
