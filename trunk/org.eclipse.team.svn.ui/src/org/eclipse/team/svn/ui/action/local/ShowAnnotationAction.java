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
import org.eclipse.team.svn.ui.operation.LocalShowAnnotationOperation;

/**
 * Show annotation for local versioned file
 * 
 * @author Alexander Gurov
 */
public class ShowAnnotationAction extends AbstractWorkingCopyAction {

	public ShowAnnotationAction() {
		super();
	}
	
	public void run(IAction action) {
    	IResource resource = this.getSelectedResources(IStateFilter.SF_ONREPOSITORY)[0];
    	this.runBusy(new LocalShowAnnotationOperation(resource, this.getTargetPage()));
	}
	
	protected boolean isEnabled() {
		return 
			this.getSelectedResources().length == 1 && 
			this.checkForResourcesPresence(IStateFilter.SF_ONREPOSITORY);
	}

}
