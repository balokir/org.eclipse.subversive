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

package org.eclipse.team.svn.ui.extension.impl;

import org.eclipse.core.resources.IProject;
import org.eclipse.team.svn.core.operation.local.management.ShareProjectOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.ui.extension.factory.IShareProjectFactory;
import org.eclipse.team.svn.ui.wizard.shareproject.SelectProjectNamePage;

/**
 * Default implementation
 * 
 * @author Alexander Gurov
 */
public class DefaultShareProjectFactory implements IShareProjectFactory {

	public DefaultShareProjectFactory() {

	}

	public SelectProjectNamePage getProjectLayoutPage() {
		return new SelectProjectNamePage();
	}

	public ShareProjectOperation getShareProjectOperation(IProject []projects, IRepositoryLocation location, SelectProjectNamePage page, String commitComment) {
		return new ShareProjectOperation(projects, location, page.getSelectedNames(), page.getRootProjectName(), page.getLayoutType(), page.isManagementFoldersEnabled(), commitComment);
	}

	public boolean disallowFinishOnAddRepositoryLocation(IProject []projects) {
		return false;
	}

	public boolean disallowFinishOnCommitComment(IProject []projects) {
		return false;
	}

	public boolean disallowFinishOnAlreadyConnected(IProject []projects) {
		return false;
	}

	public boolean disallowFinishOnSelectRepositoryLocation(IProject []projects) {
		return false;
	}

}
