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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.ui.action.AbstractRecursiveTeamAction;
import org.eclipse.team.svn.ui.utility.LockProposeUtility;

/**
 * Unlock action implementation
 * 
 * @author Alexander Gurov
 */
public class UnlockAction extends AbstractRecursiveTeamAction {

    public UnlockAction() {
        super();
    }

	public void runImpl(IAction action) {				
		//get resources which can be unlocked
		List<IResource> filteredResourcesList = new ArrayList<IResource>();		
		IResource[] filteredResources = this.getSelectedResourcesRecursive(IStateFilter.SF_LOCKED);
		for (IResource filteredResource : filteredResources) {
			if (filteredResource.getType() == IResource.FILE && filteredResource.getLocation() != null) {
				filteredResourcesList.add(filteredResource);
			}
		}
		
		IActionOperation op = LockProposeUtility.performUnlockAction(filteredResourcesList.toArray(new IResource[0]), this.getShell());
		if (op != null) {
			this.runScheduled(op);
		}
	}
	
    public boolean isEnabled() {
        return this.checkForResourcesPresenceRecursive(IStateFilter.SF_LOCKED);
    }
    
}
