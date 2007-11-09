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
import org.eclipse.team.svn.ui.action.AbstractWorkingCopyAction;
import org.eclipse.team.svn.ui.operation.ShowUpdateViewOperation;
import org.eclipse.team.ui.synchronize.WorkingSetScope;
import org.eclipse.ui.IWorkingSet;

/**
 * Synchronize workspace resources action
 * 
 * @author Alexander Gurov
 */
public class SynchronizeAction extends AbstractWorkingCopyAction {

	public SynchronizeAction() {
		super();
	}

	public void runImpl(IAction action) {
		IWorkingSet []sets = this.getSelectedWorkingSets();
		ShowUpdateViewOperation op;
		if (sets != null && sets.length > 0) {
			op = new ShowUpdateViewOperation(new WorkingSetScope(sets), this.getTargetPart());
		}
		else {
			IResource []resources = this.getSelectedResources(IStateFilter.SF_VALID);
			op = new ShowUpdateViewOperation(resources, this.getTargetPart());
		}

		this.runScheduled(op);
	}
	
	protected IWorkingSet []getSelectedWorkingSets() {
		return (IWorkingSet [])this.getSelectedResources(IWorkingSet.class);
	}
	
	public boolean isEnabled() {
		return this.checkForResourcesPresence(IStateFilter.SF_VALID);
	}

}
