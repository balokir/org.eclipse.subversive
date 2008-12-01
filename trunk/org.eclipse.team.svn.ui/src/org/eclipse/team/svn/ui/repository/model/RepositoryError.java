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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Unaccessible node representation 
 * 
 * @author Alexander Gurov
 */
public class RepositoryError extends RepositoryFictiveNode {
	public static final String ERROR_MSG = "RepositoriesView_Model_Error";
	
	protected IStatus errorStatus;
	
	public RepositoryError(IStatus errorStatus) {
		this.errorStatus = errorStatus;
	}
	
	public IStatus getErrorStatus() {
		return this.errorStatus;
	}
	
	public boolean hasChildren() {
		return false;
	}
	
	public Object[] getChildren(Object o) {
		return null;
	}

	public String getLabel(Object o) {
		return SVNUIMessages.getString(RepositoryError.ERROR_MSG);
	}

	public ImageDescriptor getImageDescriptor(Object o) {
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_ERROR_TSK);
	}

}
