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
import org.eclipse.team.svn.core.operation.local.AddToSVNWithPropertiesOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.ui.action.AbstractRecursiveTeamAction;
import org.eclipse.team.svn.ui.action.QueryResourceAddition;

/**
 * Team services menu "add to SVN" action implementation
 * 
 * @author Alexander Gurov
 */
public class AddToSVNAction extends AbstractRecursiveTeamAction {
	public AddToSVNAction() {
		super();
	}

	public void runImpl(IAction action) {
		IResource [][]resources = new QueryResourceAddition(this, this.getShell()).queryAdditionsSeparated();
		if (resources != null) {
			AddToSVNWithPropertiesOperation mainOp = new AddToSVNWithPropertiesOperation(resources[0], false);
			
			CompositeOperation op = new CompositeOperation(mainOp.getId());
			op.add(mainOp);
			if (resources[1].length > 0) {
				op.add(new AddToSVNWithPropertiesOperation(resources[1], true));
			}
			// refresh recursivelly starting from parents
			op.add(new RefreshResourcesOperation(resources[2]/*, IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL*/));

			this.runScheduled(op);
		}
	}
	
	public boolean isEnabled() {
		return 
			this.checkForResourcesPresence(IStateFilter.SF_NONVERSIONED) ||
			this.checkForResourcesPresenceRecursive(IStateFilter.SF_NEW);
	}

}
