/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.action.local.management;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.ui.action.AbstractLocalTeamAction;
import org.eclipse.team.svn.ui.wizard.ShareProjectWizard;

/**
 * Share multiple projects at once
 * 
 * @author Alexander Gurov
 */
public class ShareProjectsAction extends AbstractLocalTeamAction {
	public ShareProjectsAction() {
		super();
	}
	
	public void runImpl(IAction action) {
		IProject []projects = this.getProjectsToShare();
		
		ShareProjectWizard wizard = new ShareProjectWizard();
		wizard.init(projects);
		WizardDialog dialog = new WizardDialog(this.getShell(), wizard);
		dialog.setPageSize(500, 400);
		dialog.open();
	}

	protected IProject []getProjectsToShare() {
		HashSet<IProject> projects = new HashSet<IProject>(Arrays.asList(this.getSelectedProjects()));
		for (Iterator<IProject> it = projects.iterator(); it.hasNext(); ) {
			IProject project = it.next();
			if (!project.isAccessible() || RepositoryProvider.getProvider(project) != null) {
				it.remove();
			}
		}
		return projects.toArray(new IProject[projects.size()]);
	}
	
	public boolean isEnabled() {
		IProject []projects = this.getSelectedProjects();
		for (int i = 0; i < projects.length; i++) {
			if (projects[i].isAccessible() && RepositoryProvider.getProvider(projects[i]) == null) {
				return true;
			}
		}
		return false;
	}

}
