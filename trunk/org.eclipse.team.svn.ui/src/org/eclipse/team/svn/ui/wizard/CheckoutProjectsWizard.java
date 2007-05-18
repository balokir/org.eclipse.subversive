/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elena Matokhina - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.wizard;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.remote.CheckoutAction;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.wizard.checkoutas.CheckoutAsFolderPage;
import org.eclipse.team.svn.ui.wizard.checkoutas.ProjectLocationSelectionPage;
import org.eclipse.team.svn.ui.wizard.checkoutas.ProjectsSelectionPage;

/**
 * Checkout projects wizard
 *
 * @author Elena Matokhina
 */
public class CheckoutProjectsWizard extends AbstractSVNWizard {
	
	protected IRepositoryResource []projects;
	protected ProjectsSelectionPage projectsSelectionPage;
	protected ProjectLocationSelectionPage locationSelectionPage;
	protected CheckoutAsFolderPage selectFolderPage;
	protected HashMap name2resources;
	protected boolean respectHierarchy;
	
	public CheckoutProjectsWizard(IRepositoryResource []projects, HashMap name2resources) {
		super();
		this.setWindowTitle(SVNTeamUIPlugin.instance().getResource("CheckoutProjectsWizard.Title"));
		this.projects = projects;
		this.name2resources = name2resources;
	}
	
	public String getLocation() {
		return this.locationSelectionPage.getLocation();
	}
	
	public boolean isCheckoutAsFoldersSelected() {
		return this.projectsSelectionPage.isCheckoutAsFoldersSelected();
	}
	
	public IContainer getTargetFolder() {
		return this.selectFolderPage.getTargetFolder();
	}

	public void addPages() {
		this.addPage(this.projectsSelectionPage = new ProjectsSelectionPage());
		this.locationSelectionPage = new ProjectLocationSelectionPage(true, this.projectsSelectionPage);
		this.locationSelectionPage.setTitle(SVNTeamUIPlugin.instance().getResource("CheckoutProjectsWizard.SelectLocation.Title"));
		this.addPage(this.selectFolderPage = new CheckoutAsFolderPage(this.projects));
		this.addPage(this.locationSelectionPage);	
	}

	public IWizardPage getNextPage(IWizardPage page) {
		if (page == this.selectFolderPage) {
			return null;
		}
		if (page == this.projectsSelectionPage && !this.projectsSelectionPage.isCheckoutAsFoldersSelected()) {
			return super.getNextPage(super.getNextPage(page));
		}
		return super.getNextPage(page);
	}
	
	public void postInit() {
		IStructuredContentProvider contentProvider = new IStructuredContentProvider() {
			public Object []getElements(Object inputElement) {
				return (Object [])inputElement;
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		};
		HashMap resource2name = CheckoutAction.getResources2Names(this.name2resources);
		ITableLabelProvider labelProvider = ExtensionsManager.getInstance().getCurrentCheckoutFactory().getLabelProvider(resource2name);
		this.projectsSelectionPage.postInit(this.locationSelectionPage, (IRepositoryResource[])resource2name.keySet().toArray(new IRepositoryResource[resource2name.keySet().size()]), labelProvider, contentProvider);
	}
	
	public boolean performFinish() {
		return true;
	}
	
	public boolean isRespectHierarchy() {
		return this.projectsSelectionPage.isRespectHierarchy();
	}
	
	public List getResultSelections() {
		return this.projectsSelectionPage.getSelectedProjects();
	}
	
}
