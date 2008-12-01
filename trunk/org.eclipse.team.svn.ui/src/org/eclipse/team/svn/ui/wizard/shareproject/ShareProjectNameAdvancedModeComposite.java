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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.operation.local.management.ShareProjectOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.extension.impl.ISelectProjectNamePageData;
import org.eclipse.team.svn.ui.verifier.AbsolutePathVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.IValidationManager;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.ResourceNameVerifier;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * @author Igor Burilo
 *
 */
public class ShareProjectNameAdvancedModeComposite extends Composite implements ISelectProjectNamePageData {
	
	protected int layoutType;
	protected String rootProjectName;
	protected String projectName;
	protected String selectedName;
	protected boolean managementFoldersEnabled;
	
	protected Text projectNameField;
	protected Text rootProjectNameField;
	protected Label targetUrlField;
	protected Button managementFoldersEnabledButton;
	protected IRepositoryLocation location;
	protected boolean multiProject;
	protected Button defaultLayoutButton;
	protected Button singleLayoutButton;
	protected Group nameGroup;
	protected Group layoutGroup;
	protected Point savedPosition;
	protected Button multipleLayoutButton;
	protected Button useEmptyNameButton;
	
	protected IValidationManager validationManager;
	
	protected List<Control> controls = new ArrayList<Control>();
	
	public ShareProjectNameAdvancedModeComposite(Composite parent, int style, IValidationManager validationManager) {
		super(parent, style);
		this.validationManager = validationManager;		
		this.layoutType = ShareProjectOperation.LAYOUT_DEFAULT;
		this.createControls();
	}

	protected void createControls() {
		GridLayout layout = new GridLayout();
	
		this.setLayout(layout);
		GridData gridData = new GridData();
		this.setLayoutData(gridData);
		
		CompositeVerifier verifier;
		
		this.nameGroup = new Group(this, SWT.NONE);
		layout = new GridLayout();
		this.nameGroup.setLayout(layout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		this.nameGroup.setLayoutData(data);
		this.nameGroup.setText(SVNUIMessages.SelectProjectNamePage_NameOnRepository);
		this.controls.add(this.nameGroup);
		
		Button useProjectNameButton = new Button(this.nameGroup, SWT.RADIO);
		useProjectNameButton.setLayoutData(this.makeGridData());
		useProjectNameButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ShareProjectNameAdvancedModeComposite.this.validationManager.validateContent();
				Button button = (Button) e.widget;
				if (button.getSelection()) {
					ShareProjectNameAdvancedModeComposite.this.selectedName = ShareProjectNameAdvancedModeComposite.this.projectName;
					//ShareProjectNameAdvancedModeComposite.this.setPageComplete(true);
				}
				ShareProjectNameAdvancedModeComposite.this.showTargetUrl();
			}
		});
		useProjectNameButton.setText(SVNUIMessages.SelectProjectNamePage_UseProjectName); 
		useProjectNameButton.setSelection(true);
		this.controls.add(useProjectNameButton);
		
		this.useEmptyNameButton = new Button(this.nameGroup, SWT.RADIO);
		this.useEmptyNameButton.setLayoutData(this.makeGridData());
		this.useEmptyNameButton.setText(SVNUIMessages.SelectProjectNamePage_UseEmptyName);
		this.useEmptyNameButton.setSelection(false);
		this.controls.add(this.useEmptyNameButton);
		
		final Button useRedefinedNameButton = new Button(this.nameGroup, SWT.RADIO);
		useRedefinedNameButton.setLayoutData(this.makeGridData());
		useRedefinedNameButton.setText(SVNUIMessages.SelectProjectNamePage_UseSpecifiedName);
		useRedefinedNameButton.setSelection(false);
		useRedefinedNameButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ShareProjectNameAdvancedModeComposite.this.validationManager.validateContent();
				Button button = (Button) e.widget;
				if (button.getSelection()) {
					ShareProjectNameAdvancedModeComposite.this.selectedName = ShareProjectNameAdvancedModeComposite.this.projectNameField.getText();
					ShareProjectNameAdvancedModeComposite.this.projectNameField.setEditable(true);					
				}
				else {
					ShareProjectNameAdvancedModeComposite.this.projectNameField.setEditable(false);					
				}
				ShareProjectNameAdvancedModeComposite.this.showTargetUrl();
			}
		});
		this.controls.add(useRedefinedNameButton);
		
		this.projectNameField = new Text(this.nameGroup, SWT.SINGLE | SWT.BORDER);
		this.projectNameField.setLayoutData(this.makeGridData());
		this.projectNameField.setEditable(false);
		this.projectNameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				ShareProjectNameAdvancedModeComposite.this.selectedName = ShareProjectNameAdvancedModeComposite.this.projectNameField.getText();
				ShareProjectNameAdvancedModeComposite.this.showTargetUrl();
			}
		});
		verifier = new CompositeVerifier();
		String name = SVNUIMessages.SelectProjectNamePage_ProjectName;
		verifier.add(new NonEmptyFieldVerifier(name));
		verifier.add(new ResourceNameVerifier(name, true));
		verifier.add(new AbsolutePathVerifier(name));
		this.validationManager.attachTo(this.projectNameField, new AbstractVerifierProxy(verifier) {
			protected boolean isVerificationEnabled(Control input) {
				return useRedefinedNameButton.getSelection();
			}			
		});
		this.controls.add(this.projectNameField);
		
		this.layoutGroup = new Group(this, SWT.NONE);
		layout = new GridLayout();
		this.layoutGroup.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.layoutGroup.setLayoutData(data);
		this.layoutGroup.setText(SVNUIMessages.SelectProjectNamePage_ProjectLayout);
		this.controls.add(this.layoutGroup);
		
		this.defaultLayoutButton = new Button(this.layoutGroup, SWT.RADIO);
		data = this.makeGridData();
		this.defaultLayoutButton.setLayoutData(data);
		this.defaultLayoutButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button)e.widget;
				if (button.getSelection()) {
					ShareProjectNameAdvancedModeComposite.this.layoutType = ShareProjectOperation.LAYOUT_DEFAULT;					
				}
				ShareProjectNameAdvancedModeComposite.this.validationManager.validateContent();
				ShareProjectNameAdvancedModeComposite.this.showTargetUrl();
				ShareProjectNameAdvancedModeComposite.this.enableManagementFoldersEnabledButton();
			}
		});
		this.defaultLayoutButton.setText(SVNUIMessages.SelectProjectNamePage_RepositoryLocationLayout);
		this.defaultLayoutButton.setSelection(true);
		this.controls.add(this.defaultLayoutButton);
		
		this.singleLayoutButton = new Button(this.layoutGroup, SWT.RADIO);
		data = this.makeGridData();
		this.singleLayoutButton.setLayoutData(data);
		this.singleLayoutButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button) e.widget;
				if (button.getSelection()) {
					ShareProjectNameAdvancedModeComposite.this.layoutType = ShareProjectOperation.LAYOUT_SINGLE;					
				}
				ShareProjectNameAdvancedModeComposite.this.validationManager.validateContent();
				ShareProjectNameAdvancedModeComposite.this.showTargetUrl();
				ShareProjectNameAdvancedModeComposite.this.enableManagementFoldersEnabledButton();
			}
		});
		this.singleLayoutButton.setText(SVNUIMessages.SelectProjectNamePage_SingleProjectLayout); 
		this.controls.add(this.singleLayoutButton);
		
		this.multipleLayoutButton = new Button(this.layoutGroup, SWT.RADIO);
		data = this.makeGridData();
		this.multipleLayoutButton.setLayoutData(data);
		this.multipleLayoutButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button) e.widget;
				if (button.getSelection()) {
					ShareProjectNameAdvancedModeComposite.this.layoutType = ShareProjectOperation.LAYOUT_MULTIPLE;
					ShareProjectNameAdvancedModeComposite.this.rootProjectName = ShareProjectNameAdvancedModeComposite.this.rootProjectNameField.getText();					
				}
				ShareProjectNameAdvancedModeComposite.this.rootProjectNameField.setEditable(button.getSelection());
				ShareProjectNameAdvancedModeComposite.this.validationManager.validateContent();
				ShareProjectNameAdvancedModeComposite.this.showTargetUrl();
				ShareProjectNameAdvancedModeComposite.this.enableManagementFoldersEnabledButton();
			}
		});
		this.multipleLayoutButton.setText(SVNUIMessages.SelectProjectNamePage_MultiProjectLayout);
		this.controls.add(this.multipleLayoutButton);
		
		this.rootProjectNameField = new Text(this.layoutGroup, SWT.SINGLE | SWT.BORDER);
		this.rootProjectNameField.setLayoutData(this.makeGridData());
		this.rootProjectNameField.setEditable(false);
		this.rootProjectNameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				ShareProjectNameAdvancedModeComposite.this.rootProjectName = ShareProjectNameAdvancedModeComposite.this.rootProjectNameField.getText();
				ShareProjectNameAdvancedModeComposite.this.showTargetUrl();
			}
		});
		verifier = new CompositeVerifier();
		name = SVNUIMessages.SelectProjectNamePage_RootName;
		verifier.add(new NonEmptyFieldVerifier(name));
		verifier.add(new ResourceNameVerifier(name, true));
		verifier.add(new AbsolutePathVerifier(name));
		this.validationManager.attachTo(this.rootProjectNameField, new AbstractVerifierProxy(verifier) {
			protected boolean isVerificationEnabled(Control input) {
				return ShareProjectNameAdvancedModeComposite.this.multipleLayoutButton.getSelection();
			}			
		});
		this.controls.add(this.rootProjectNameField);
		
		Label label = new Label(this.layoutGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(this.makeGridData());
		this.controls.add(label);
		
		this.managementFoldersEnabledButton = new Button(this.layoutGroup, SWT.CHECK);
		this.managementFoldersEnabledButton.setLayoutData(new GridData());
		this.managementFoldersEnabledButton.setSelection(true);
		this.managementFoldersEnabled = true;
		this.managementFoldersEnabledButton.setText(SVNUIMessages.SelectProjectNamePage_UseRecommended);
		this.managementFoldersEnabledButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ShareProjectNameAdvancedModeComposite.this.managementFoldersEnabled = ((Button)e.widget).getSelection();
				ShareProjectNameAdvancedModeComposite.this.showTargetUrl();
			}
		});		
		this.controls.add(this.managementFoldersEnabledButton);
				
		label = new Label(this.layoutGroup, SWT.WRAP);
		data = this.makeGridData();
		data.heightHint = DefaultDialog.convertHeightInCharsToPixels(this, 2);
		//this.convertHeightInCharsToPixels(2);
		label.setLayoutData(data);
		label.setText(SVNUIMessages.SelectProjectNamePage_Hint);
		this.controls.add(label);
				
		Composite urlComposite = new Composite(this.layoutGroup, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 2;
		urlComposite.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		urlComposite.setLayoutData(data);
		this.controls.add(urlComposite);
		
		label = new Label(urlComposite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setImage(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER).createImage());
		this.controls.add(label);
		
		this.targetUrlField = new Label(urlComposite, SWT.SINGLE);
		this.targetUrlField.setLayoutData(this.makeGridData());
		this.projectNameField.setEditable(true);
		this.targetUrlField.setBackground(this.projectNameField.getBackground());
		this.controls.add(this.targetUrlField);
		
		label.setBackground(this.projectNameField.getBackground());
		urlComposite.setBackground(this.projectNameField.getBackground());
		this.projectNameField.setEditable(false);

		this.useEmptyNameButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ShareProjectNameAdvancedModeComposite.this.validationManager.validateContent();
				ShareProjectNameAdvancedModeComposite.this.selectedName = "";
				Button button = (Button)e.widget;				
				
				if (button.getSelection()) {				
					if (ShareProjectNameAdvancedModeComposite.this.multipleLayoutButton.getSelection()) {
						ShareProjectNameAdvancedModeComposite.this.multipleLayoutButton.setSelection(false);
						ShareProjectNameAdvancedModeComposite.this.rootProjectNameField.setEditable(false);						
						ShareProjectNameAdvancedModeComposite.this.defaultLayoutButton.setSelection(true);
					}
				}

				ShareProjectNameAdvancedModeComposite.this.showTargetUrl();
				ShareProjectNameAdvancedModeComposite.this.enableMultipleLayoutButton();
				ShareProjectNameAdvancedModeComposite.this.enableManagementFoldersEnabledButton();
			}
		});
	}
	
	public void setProjectsAndLocation(IProject []projects, IRepositoryLocation location, boolean multiProject) {
		this.multiProject = multiProject;
		this.location = location;
		
		if (this.multiProject) {
			this.selectedName = this.projectName = "";
			this.savedPosition = this.layoutGroup.getLocation();
			this.layoutGroup.setLocation(this.nameGroup.getLocation());
			this.nameGroup.setVisible(false);
			this.defaultLayoutButton.setSelection(true);
		}
		else {
			this.selectedName = this.projectName = projects[0].getName();
			if (this.savedPosition != null) {
				this.nameGroup.setVisible(true);
				this.layoutGroup.setLocation(this.savedPosition);
				this.savedPosition = null;
			}
		}
									
		this.projectNameField.setText(this.projectName);
		this.rootProjectNameField.setText(this.projectName);
		this.showTargetUrl();
	}
	
	protected GridData makeGridData() {
		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		data.horizontalSpan = 1;
		return data;
	}
	
	protected void showTargetUrl() {
		String targetUrl = ShareProjectOperation.getTargetUrl(this.location, this.layoutType, this.selectedName, this.rootProjectName, this.isManagementFoldersEnabled());
		this.showTargetUrlImpl(targetUrl);
	}
	
	protected void showTargetUrlImpl(String targetUrl) {
		if (this.targetUrlField != null) {
			this.targetUrlField.setText(targetUrl);
		}
	}
	
	public boolean isManagementFoldersEnabled() {			
		return (this.layoutType == ShareProjectOperation.LAYOUT_DEFAULT) ? true : this.managementFoldersEnabled;	
	}
	
	public int getLayoutType() {
		return this.layoutType;
	}
	
	public ShareProjectOperation.IFolderNameMapper getSelectedNames() {		
		return this.multiProject ? null : new ShareProjectOperation.IFolderNameMapper() {
			public String getRepositoryFolderName(IProject project) {
				return ShareProjectNameAdvancedModeComposite.this.selectedName;
			}
		};		
	}
		
	public String getRootProjectName() {
		return this.rootProjectName;
	}
	
	public boolean isMultiProject() {
		return this.multiProject;
	}

	public void save() {
		//do nothing
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
				if (enabled) {
					/*
					 * Handle special cases for enabling controls as
					 * there are dependencies among controls for enabling/disabling 
					 */
					if (control == this.multipleLayoutButton) {						
						this.enableMultipleLayoutButton();
					} else if (control == this.managementFoldersEnabledButton) {
						this.enableManagementFoldersEnabledButton();
					} else if (control == this.singleLayoutButton) {
						this.enableSingleLayoutButton();
					} else {						
						control.setEnabled(enabled);
					}
				} else {
					control.setEnabled(enabled);
				}
			} 							
		}
	}

	protected void enableMultipleLayoutButton() {
		this.multipleLayoutButton.setEnabled(!this.useEmptyNameButton.getSelection());		
	}

	protected void enableSingleLayoutButton() {
		this.singleLayoutButton.setEnabled(!this.multiProject);
	}	
	
	protected void enableManagementFoldersEnabledButton() {
		if (this.defaultLayoutButton.getSelection()) {
			this.managementFoldersEnabledButton.setEnabled(false);			
		} else if (this.singleLayoutButton.getSelection()) {
			this.managementFoldersEnabledButton.setEnabled(true);			
		} else if (this.multipleLayoutButton.getSelection()) {
			this.managementFoldersEnabledButton.setEnabled(true);	
		} 
		
		if (this.useEmptyNameButton.getSelection() && this.multipleLayoutButton.getSelection()) {
			this.managementFoldersEnabledButton.setEnabled(false);	
		}	
	}
}
