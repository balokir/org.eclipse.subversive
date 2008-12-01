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

package org.eclipse.team.svn.ui.repository.model;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;

/**
 * Referesh in progress node implementation
 * 
 * @author Alexander Gurov
 */
public class RepositoryPending extends RepositoryFictiveNode {
	public static final String PENDING = "RepositoriesView_Model_Pending"; //$NON-NLS-1$
	
	protected RepositoryResource parent;
	
	public RepositoryPending(RepositoryResource parent) {
		this.parent = parent;
	}
	
    public RGB getForeground(Object element) {
    	return this.parent.getForeground(element);
    }
    
	public boolean hasChildren() {
		return false;
	}
	
	public Object[] getChildren(Object o) {
		return null;
	}

	public String getLabel(Object o) {
		return SVNUIMessages.getString(RepositoryPending.PENDING);
	}
	
	public ImageDescriptor getImageDescriptor(Object object) {
		return SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/repositories/browser_pending.gif"); //$NON-NLS-1$
	}

}
