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

package org.eclipse.team.svn.ui.panel.common;

import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;

/**
 * Select branch folder panel
 * 
 * @author Alexander Gurov
 */
public class BranchPanel extends AbstractBranchTagPanel {

	public BranchPanel(IRepositoryRoot branchTo, boolean showStartsWith, Set existingNames, IRepositoryResource[] selectedRemoteResources) {
		this(branchTo, showStartsWith, existingNames, new IResource[0], selectedRemoteResources);
    }
	
	public BranchPanel(IRepositoryRoot branchTo, boolean showStartsWith, Set existingNames, IResource[] resources, IRepositoryResource[] selectedRemoteResources) {
		super(branchTo, showStartsWith, existingNames, "BranchPanel", "branch", resources, selectedRemoteResources);
	}
	
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.branchDialogContext"; //$NON-NLS-1$
    }
}
