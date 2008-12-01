/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *    Ken Geis - [patch] fixed bug in validation messages
 *******************************************************************************/

package org.eclipse.team.svn.ui.composite;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.verifier.AbsolutePathVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.IValidationManager;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.ResourceNameVerifier;

/**
 * Composite to choose trunk, branches and tags locations
 *
 * @author Sergiy Logvin
 */
public class RepositoryRootsComposite extends Composite implements IPropertiesPanel {
	protected Button structureCheckBox;
	protected Text trunkRight;
	protected Text branchesRight;
	protected Text tagsRight;
	
	protected boolean createLocation;
	protected String trunkLocation;
	protected String branchesLocation;
	protected String tagsLocation;
	protected boolean structureEnabled;
	protected boolean forceDisableRoots;
	
	protected IValidationManager validationManager;

	public RepositoryRootsComposite(Composite parent, int style, IValidationManager validationManager) {
		super(parent, style);
		this.validationManager = validationManager;
	}

	public void saveChanges() {
		this.structureEnabled = this.structureCheckBox.getSelection();
		
		this.trunkLocation = this.trunkRight.getText();
		this.branchesLocation = this.branchesRight.getText();
		this.tagsLocation = this.tagsRight.getText();
	}

	public void resetChanges() {
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();

		if (this.createLocation) {
			this.trunkLocation = SVNTeamPreferences.getRepositoryString(store, SVNTeamPreferences.REPOSITORY_HEAD_NAME);
			this.branchesLocation = SVNTeamPreferences.getRepositoryString(store, SVNTeamPreferences.REPOSITORY_BRANCHES_NAME);
			this.tagsLocation = SVNTeamPreferences.getRepositoryString(store, SVNTeamPreferences.REPOSITORY_TAGS_NAME);
			this.structureEnabled = !this.forceDisableRoots;
		}
		this.structureCheckBox.setSelection(this.structureEnabled);
		
		this.trunkRight.setText(this.trunkLocation);
		this.branchesRight.setText(this.branchesLocation);
		this.tagsRight.setText(this.tagsLocation);
		
		this.refreshButtons();
	}

	public void cancelChanges() {
		
	}

	public void initialize() {
		GridLayout layout = null;
		GridData data = null;

		layout = new GridLayout();
		layout.marginHeight = 7;
		layout.verticalSpacing = 3;
		this.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		this.setLayoutData(data);

		this.structureCheckBox = new Button(this, SWT.CHECK);
		data = new GridData();
		this.structureCheckBox.setLayoutData(data);
		this.structureCheckBox.setText(SVNUIMessages.RepositoryRootsComposite_EnableDetection);
		this.structureCheckBox.addSelectionListener(new SelectionListener() {			
			public void widgetSelected(SelectionEvent e) {
				boolean enabled = ((Button)e.widget).getSelection();
				RepositoryRootsComposite.this.trunkRight.setEnabled(enabled);
				RepositoryRootsComposite.this.branchesRight.setEnabled(enabled);
				RepositoryRootsComposite.this.tagsRight.setEnabled(enabled);
				RepositoryRootsComposite.this.validationManager.validateContent();
			}
			public void widgetDefaultSelected(SelectionEvent e) {			
			}			
		});

		Group standardLocations = new Group(this, SWT.NONE);
		standardLocations.setText(SVNUIMessages.RepositoryRootsComposite_ResourceNames);
		layout = new GridLayout();
		layout.numColumns = 2;
		standardLocations.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		standardLocations.setLayoutData(data);
		
		this.trunkRight = this.createControl(standardLocations, "RepositoryRootsComposite.Trunk");
		this.branchesRight = this.createControl(standardLocations, "RepositoryRootsComposite.Branches");
		this.tagsRight = this.createControl(standardLocations, "RepositoryRootsComposite.Tags");
	}
	
	public boolean isStructureEnabled() {
		return this.structureEnabled;
	}

	public void setStructureEnabled(boolean structureEnabled) {
		this.structureEnabled = structureEnabled;
	}

	public String getBranchesLocation() {
		return this.branchesLocation;
	}

	public void setBranchesLocation(String branchesLocation) {
		this.branchesLocation = branchesLocation;
	}

	public String getTagsLocation() {
		return this.tagsLocation;
	}

	public void setTagsLocation(String tagsLocation) {
		this.tagsLocation = tagsLocation;
	}

	public String getTrunkLocation() {
		return this.trunkLocation;
	}

	public void setTrunkLocation(String trunkLocation) {
		this.trunkLocation = trunkLocation;
	}
	
	public boolean isCreateLocation() {
		return this.createLocation;
	}

	public void setCreateLocation(boolean createLocation) {
		this.createLocation = createLocation;
	}
	
	public void setForceDisableRoots(boolean force) {
		this.forceDisableRoots = force;
		this.resetChanges();
	}
	
	protected Text createControl(Composite standardLocations, String id) {
		Label label = new Label(standardLocations, SWT.NONE);
		GridData data = new GridData();
		label.setLayoutData(data);
		label.setText(SVNUIMessages.getString(id));
		
		Text field = new Text(standardLocations, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		field.setLayoutData(data);
		String name = SVNUIMessages.getString(id + "_Verifier");
		CompositeVerifier verifier = new CompositeVerifier();
		verifier.add(new ResourceNameVerifier(name, false));
		verifier.add(new NonEmptyFieldVerifier(name));
		verifier.add(new AbsolutePathVerifier(name));
		this.validationManager.attachTo(field, new AbstractVerifierProxy(verifier){
			protected boolean isVerificationEnabled(Control input) {
				return RepositoryRootsComposite.this.structureCheckBox.getSelection();
			}
		});
		
		return field;
	}

	protected void refreshButtons() {
		this.trunkRight.setEnabled(this.structureCheckBox.getSelection());
		this.branchesRight.setEnabled(this.structureCheckBox.getSelection());
		this.tagsRight.setEnabled(this.structureCheckBox.getSelection());
	}

}
