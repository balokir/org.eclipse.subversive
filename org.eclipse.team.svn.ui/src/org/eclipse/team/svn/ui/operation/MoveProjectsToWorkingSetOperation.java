/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.operation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.local.AbstractWorkingCopyOperation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;

/**
 * Move projects to working set operation
 * 
 * @author Sergiy Logvin
 */
public class MoveProjectsToWorkingSetOperation extends AbstractWorkingCopyOperation {
	protected String workingSetName;
	
	public MoveProjectsToWorkingSetOperation(IResource []projects, String workingSetName) {
		super("Operation.MoveToWorkingSet", projects);
		this.workingSetName = workingSetName;
	}

	public MoveProjectsToWorkingSetOperation(IResourceProvider provider, String workingSetName) {
		super("Operation.MoveToWorkingSet", provider);
		this.workingSetName = workingSetName;
	}
	
	public int getOperationWeight() {
		return 0;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []projects = this.operableData();
		IWorkingSet wSet = null; 
		IWorkingSetManager workingSetManager = SVNTeamUIPlugin.instance().getWorkbench().getWorkingSetManager();
		IWorkingSet []workingSets = workingSetManager.getWorkingSets();
		for (int i = 0; i < workingSets.length; i++) {
			if (workingSets[i].getName().equals(this.workingSetName)) {
				wSet = workingSets[i];
				List existing = new ArrayList();
				existing.addAll(Arrays.asList(wSet.getElements()));
				existing.addAll(Arrays.asList(projects));
				wSet.setElements((IAdaptable[])existing.toArray(new IAdaptable[existing.size()]));
				break;
			}
		}
		if (wSet == null) {
			wSet = workingSetManager.createWorkingSet(this.workingSetName, projects);
			wSet.setId("org.eclipse.ui.resourceWorkingSetPage");
			workingSetManager.addWorkingSet(wSet);
		}
	}

}
