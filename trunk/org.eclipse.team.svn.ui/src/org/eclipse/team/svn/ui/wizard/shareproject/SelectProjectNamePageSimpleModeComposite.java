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

package org.eclipse.team.svn.ui.wizard.shareproject;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.svn.core.operation.local.management.ShareProjectOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.RepositoryResourceOnlySelectionComposite;
import org.eclipse.team.svn.ui.extension.impl.ISelectProjectNamePageData;
import org.eclipse.team.svn.ui.verifier.IValidationManager;

/**
 * @author Igor Burilo
 *
 */
public class SelectProjectNamePageSimpleModeComposite extends Composite implements ISelectProjectNamePageData {

	protected RepositoryResourceOnlySelectionComposite resourceSelectionComposite;
	
	protected IValidationManager validationManager;
			
	protected IRepositoryLocation location;
	protected boolean multiProject;
	
	protected List<Control> controls = new ArrayList<Control>();
	
	public SelectProjectNamePageSimpleModeComposite(Composite parent, int style, IValidationManager validationManager) {
		super(parent, style);
		this.validationManager = validationManager;
		this.createControls();
	}
	
	public void setProjectsAndLocation(IProject []projects, IRepositoryLocation location, boolean multiProject) {
		this.location = location;
		this.multiProject = multiProject;
		
		/*
		 * Set picker URL as repository location plus project name
		 */
		IRepositoryResource baseResource = location.asRepositoryContainer(location.getUrl(), false);			
		this.resourceSelectionComposite.setBaseResource(baseResource);
		this.resourceSelectionComposite.setMatchToBaseResource(true);
		
		IProject pr = projects[0];
		String url = baseResource.getUrl();
		if (!this.multiProject) {
			url += "/" + pr.getName();	
		}		
		this.resourceSelectionComposite.setUrl(url);
	}
	
	protected void createControls() {
		GridLayout layout = new GridLayout();
		this.setLayout(layout);
		GridData gridData = new GridData();
		this.setLayoutData(gridData);

		IRepositoryResource baseResource = null;
		
		this.resourceSelectionComposite = new RepositoryResourceOnlySelectionComposite(
				this,
				SWT.NONE,
				this.validationManager, 
				"selectProjectNamePage", 
				baseResource,				
				SVNUIMessages.SelectProjectNamePage_Select_Title,
				SVNUIMessages.SelectProjectNamePage_Select_Description);				
				
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 550;
		this.resourceSelectionComposite.setLayoutData(data);
		this.controls.add(this.resourceSelectionComposite);
	}

	public ShareProjectOperation.IFolderNameMapper getSelectedNames() {				
		return new ShareProjectOperation.IFolderNameMapper() {
			public String getRepositoryFolderName(IProject project) {
				String folderName = null;
				
				String toTrim = SelectProjectNamePageSimpleModeComposite.this.location.getUrl();					
				String selectedUrl = SelectProjectNamePageSimpleModeComposite.this.resourceSelectionComposite.getSelectedResource().getUrl();
				selectedUrl = SVNUtility.normalizeURL(selectedUrl);
				if (selectedUrl.startsWith(toTrim)) {
					folderName = selectedUrl.equals(toTrim) ? "" : selectedUrl.substring(toTrim.length() + 1);																				
					if (SelectProjectNamePageSimpleModeComposite.this.multiProject) {
						folderName += "/" + project.getName();
					}						
				} else {
					throw new RuntimeException("Inconsistent repository location and selected repository url. "
						+ "Selected url: " + selectedUrl + ", repository location: " + toTrim);
				}

				return folderName;
			}					
		};						
	}

	public String getRootProjectName() {
		return "";
	}

	public void save() {
		this.resourceSelectionComposite.saveHistory();		
	}
	
	public int getLayoutType() {	
		return ShareProjectOperation.LAYOUT_DEFAULT;
	}
	
	public boolean isManagementFoldersEnabled() {
		return false;
	}

	public void validateContent() {
		this.validationManager.validateContent();
	}

	public IRepositoryLocation getRepositoryLocation() {
		return this.location;
	}
	
	public void setEnabled (boolean enabled) {
		super.setEnabled(enabled);
		if (this.controls != null && !this.controls.isEmpty()) {
			for (Control control : this.controls) {
				control.setEnabled(enabled);
			} 							
		}
	}
	
}
