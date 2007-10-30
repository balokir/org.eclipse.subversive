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

package org.eclipse.team.svn.ui.action;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Abstract filtered resource selector w/o recursion
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractWorkingCopyAction extends AbstractLocalTeamAction {

	public AbstractWorkingCopyAction() {
		super();
	}

	protected IResource []getSelectedResources(IStateFilter filter) {
		return FileUtility.getResourcesRecursive(this.getSelectedResources(), filter, IResource.DEPTH_ZERO);
	}
	
}
