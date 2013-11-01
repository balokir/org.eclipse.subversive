/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.properties.ResourcePropertyEditPanel;
import org.eclipse.team.svn.ui.utility.UserInputHistory;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.IValidationManager;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;

/**
 * Apply property method selection composite
 *
 * @author Sergiy Logvin
 */
public class ApplyPropertyMethodComposite extends Composite {
	protected static final String PROPERTY_FILTER_HISTORY_NAME = "propertyFilter"; //$NON-NLS-1$
	
	protected Button applyToAllResourcesButton;
	protected Button applyToFilesButton;
	protected Button applyToFoldersButton;
	protected Button useFilterButton;
	protected Label filterLabel;
	protected Combo filterMaskText;
	
	protected boolean applyToFiles;
	protected boolean applyToFolders;
	protected boolean useMask;
	protected int resourcesType;
	protected String filterMask;
	protected UserInputHistory propertyFilterHistory;
	protected IValidationManager validationManager;

	public ApplyPropertyMethodComposite(Composite parent, int style, IValidationManager validationManager, int resourcesType) {
		super(parent, style);
		this.validationManager = validationManager;
		this.resourcesType = resourcesType;
		this.createControls();
	}
	
	public String getFilterMask() {
		return this.filterMask;
	}
	
	public boolean useMask() {
		return this.useMask;
	}
	
	public int getApplyMethod() {
		if (this.applyToFiles) {
			return PropertiesComposite.APPLY_TO_FILES;
		}
		else if (this.applyToFolders) {
			return PropertiesComposite.APPLY_TO_FOLDERS;
		}
		return PropertiesComposite.APPLY_TO_ALL;
	}
	
	public void setEnabled(boolean enabled) {
		if (this.resourcesType == ResourcePropertyEditPanel.MIXED_RESOURCES) {
			this.applyToAllResourcesButton.setEnabled(enabled);
			this.applyToFilesButton.setEnabled(enabled);
			this.applyToFoldersButton.setEnabled(enabled);
			this.useFilterButton.setEnabled(enabled);
		}
		this.filterMaskText.setEnabled(enabled && this.useFilterButton.getSelection());
		this.filterLabel.setEnabled(enabled && this.useFilterButton.getSelection());
	}
	
	public void saveChanges() {
		if (this.resourcesType == ResourcePropertyEditPanel.MIXED_RESOURCES) {
			this.applyToFiles = this.applyToFilesButton.getSelection();
			this.applyToFolders = this.applyToFoldersButton.getSelection();
		}
		this.filterMask = this.filterMaskText.getText();
		this.useMask = this.useFilterButton.getSelection() && this.useFilterButton.isEnabled();
		if (this.useMask) {
			this.propertyFilterHistory.addLine(this.filterMask);
		}
	}
	
	protected void createControls() {
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		this.setLayout(layout);
		GridData data = null;
		
		Group group = new Group(this, SWT.NONE);
		layout = new GridLayout();
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		group.setText(SVNUIMessages.ApplyPropertyMethodComposite_ResourcesOptions);
		
		if (this.resourcesType == ResourcePropertyEditPanel.MIXED_RESOURCES) {
			this.applyToAllResourcesButton = new Button(group, SWT.RADIO);
			this.applyToAllResourcesButton.setLayoutData(new GridData());
			this.applyToAllResourcesButton.setText(SVNUIMessages.ApplyPropertyMethodComposite_AllResources);
			this.applyToAllResourcesButton.setSelection(true);
			
			this.applyToFilesButton = new Button(group, SWT.RADIO);
			this.applyToFilesButton.setLayoutData(new GridData());
			this.applyToFilesButton.setText(SVNUIMessages.ApplyPropertyMethodComposite_FilesOnly);
			
			this.applyToFoldersButton = new Button(group, SWT.RADIO);
			this.applyToFoldersButton.setLayoutData(new GridData());
			this.applyToFoldersButton.setText(SVNUIMessages.ApplyPropertyMethodComposite_FoldersOnly);
		}
		
		Composite filter = new Composite(group, SWT.NONE);
		layout = new GridLayout();
		layout.makeColumnsEqualWidth = true;
		layout.numColumns = 2; 
		layout.marginHeight = layout.marginWidth = 0;
		filter.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		filter.setLayoutData(data);
		
		this.useFilterButton = new Button(filter, SWT.CHECK);
		this.useFilterButton.setText(SVNUIMessages.ApplyPropertyMethodComposite_ResourceNameFiltration);
		this.useFilterButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		
		this.useFilterButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				ApplyPropertyMethodComposite.this.setEnabled(true);
				ApplyPropertyMethodComposite.this.validationManager.validateContent();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		Composite subFilter = new Composite(filter, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2; 
		layout.marginHeight = layout.marginWidth = 0;
		subFilter.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		subFilter.setLayoutData(data);
		
		this.filterLabel = new Label(subFilter, SWT.NONE);
		this.filterLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		this.filterLabel.setText(SVNUIMessages.ApplyPropertyMethodComposite_Mask);
		
		this.propertyFilterHistory = new UserInputHistory(ApplyPropertyMethodComposite.PROPERTY_FILTER_HISTORY_NAME);
		
		this.filterMaskText = new Combo(subFilter, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.filterMaskText.setEnabled(this.useFilterButton.getSelection());
		this.filterMaskText.setLayoutData(data);
		this.filterMaskText.setVisibleItemCount(this.propertyFilterHistory.getDepth());
		this.filterMaskText.setItems(this.propertyFilterHistory.getHistory());
		this.validationManager.attachTo(this.filterMaskText, 
				new AbstractVerifierProxy(new NonEmptyFieldVerifier(SVNUIMessages.ApplyPropertyMethodComposite_Mask_Verifier)) {
					protected boolean isVerificationEnabled(Control input) {
						return ApplyPropertyMethodComposite.this.useFilterButton.getSelection() && ApplyPropertyMethodComposite.this.useFilterButton.isEnabled();
					}
		});
		this.filterMaskText.setText("*"); //$NON-NLS-1$
	}

}
