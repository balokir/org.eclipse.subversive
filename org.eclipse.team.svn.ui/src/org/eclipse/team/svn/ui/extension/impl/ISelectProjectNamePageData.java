/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.extension.impl;

import org.eclipse.core.resources.IProject;
import org.eclipse.team.svn.core.operation.local.management.ShareProjectOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;

/**
 * @author Igor Burilo
 *
 */
public interface ISelectProjectNamePageData {

	void setProjectsAndLocation(IProject []projects, IRepositoryLocation location, boolean multiProject);
	
	ShareProjectOperation.IFolderNameMapper getSelectedNames();
	
	String getRootProjectName();
	
	int getLayoutType();
	
	boolean isManagementFoldersEnabled();
	
	void validateContent();
	
	IRepositoryLocation getRepositoryLocation();
	
	void save();
}
