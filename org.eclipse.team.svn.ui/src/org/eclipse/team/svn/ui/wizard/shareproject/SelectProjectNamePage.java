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

package org.eclipse.team.svn.ui.wizard.shareproject;

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
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.verifier.AbsolutePathVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.ResourceNameVerifier;
import org.eclipse.team.svn.ui.wizard.AbstractVerifiedWizardPage;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Select repository folder name for project
 * 
 * @author Alexander Gurov
 */
public class SelectProjectNamePage extends AbstractVerifiedWizardPage {
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

	public SelectProjectNamePage() {
		super(
			SelectProjectNamePage.class.getName(), 
			"", 
			SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif"));
		
		this.layoutType = ShareProjectOperation.LAYOUT_DEFAULT;
	}
	
	public void setProjectsAndLocation(IProject []projects, IRepositoryLocation location) {
		this.multiProject = projects.length > 1;
		this.location = location;
		
		if (this.multiProject) {
			this.selectedName = this.projectName = "";
			this.setTitle(SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.Title.Multi"));
			this.setDescription(SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.Description.Multi"));
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
			this.setTitle(SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.Title.Single"));
			this.setDescription(SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.Description.Single"));
		}
		this.singleLayoutButton.setEnabled(!this.multiProject);
		this.projectNameField.setText(this.projectName);
		this.rootProjectNameField.setText(this.projectName);
		this.showTargetUrl();
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
				return SelectProjectNamePage.this.selectedName;
			}
		};
	}
	
	public String getRootProjectName() {
		return this.rootProjectName;
	}
	
	protected Composite createControlImpl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		
		// GridLayout
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		
		// GridData
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		CompositeVerifier verifier;
		
		this.nameGroup = new Group(composite, SWT.NONE);
		layout = new GridLayout();
		this.nameGroup.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.nameGroup.setLayoutData(data);
		this.nameGroup.setText(SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.NameOnRepository"));
		
		Button useProjectNameButton = new Button(this.nameGroup, SWT.RADIO);
		useProjectNameButton.setLayoutData(this.makeGridData());
		useProjectNameButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SelectProjectNamePage.this.validateContent();
				Button button = (Button) e.widget;
				if (button.getSelection()) {
					SelectProjectNamePage.this.selectedName = SelectProjectNamePage.this.projectName;
					SelectProjectNamePage.this.setPageComplete(true);
				}
				SelectProjectNamePage.this.showTargetUrl();
			}
		});
		useProjectNameButton.setText(SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.UseProjectName")); 
		useProjectNameButton.setSelection(true);
		
		Button useEmptyNameButton = new Button(this.nameGroup, SWT.RADIO);
		useEmptyNameButton.setLayoutData(this.makeGridData());
		useEmptyNameButton.setText(SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.UseEmptyName"));
		useEmptyNameButton.setSelection(false);
		
		final Button useRedefinedNameButton = new Button(this.nameGroup, SWT.RADIO);
		useRedefinedNameButton.setLayoutData(this.makeGridData());
		useRedefinedNameButton.setText(SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.UseSpecifiedName"));
		useRedefinedNameButton.setSelection(false);
		useRedefinedNameButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SelectProjectNamePage.this.validateContent();
				Button button = (Button) e.widget;
				if (button.getSelection()) {
					SelectProjectNamePage.this.selectedName = SelectProjectNamePage.this.projectNameField.getText();
					SelectProjectNamePage.this.projectNameField.setEditable(true);					
				}
				else {
					SelectProjectNamePage.this.projectNameField.setEditable(false);					
				}
				SelectProjectNamePage.this.showTargetUrl();
			}
		});

		this.projectNameField = new Text(this.nameGroup, SWT.SINGLE | SWT.BORDER);
		this.projectNameField.setLayoutData(this.makeGridData());
		this.projectNameField.setEditable(false);
		this.projectNameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				SelectProjectNamePage.this.selectedName = SelectProjectNamePage.this.projectNameField.getText();
				SelectProjectNamePage.this.showTargetUrl();
			}
		});
		verifier = new CompositeVerifier();
		String name = SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.ProjectName");
		verifier.add(new NonEmptyFieldVerifier(name));
		verifier.add(new ResourceNameVerifier(name, true));
		verifier.add(new AbsolutePathVerifier(name));
		this.attachTo(this.projectNameField, new AbstractVerifierProxy(verifier) {
			protected boolean isVerificationEnabled(Control input) {
				return useRedefinedNameButton.getSelection();
			}			
		});
		
		this.layoutGroup = new Group(composite, SWT.NONE);
		layout = new GridLayout();
		this.layoutGroup.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.layoutGroup.setLayoutData(data);
		this.layoutGroup.setText(SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.ProjectLayout"));
		
		this.defaultLayoutButton = new Button(this.layoutGroup, SWT.RADIO);
		data = this.makeGridData();
		this.defaultLayoutButton.setLayoutData(data);
		this.defaultLayoutButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button)e.widget;
				if (button.getSelection()) {
					SelectProjectNamePage.this.layoutType = ShareProjectOperation.LAYOUT_DEFAULT;
					SelectProjectNamePage.this.managementFoldersEnabledButton.setEnabled(false);
				}
				SelectProjectNamePage.this.validateContent();
				SelectProjectNamePage.this.showTargetUrl();
			}
		});
		this.defaultLayoutButton.setText(SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.RepositoryLocationLayout"));
		this.defaultLayoutButton.setSelection(true);
		
		this.singleLayoutButton = new Button(this.layoutGroup, SWT.RADIO);
		data = this.makeGridData();
		this.singleLayoutButton.setLayoutData(data);
		this.singleLayoutButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button) e.widget;
				if (button.getSelection()) {
					SelectProjectNamePage.this.layoutType = ShareProjectOperation.LAYOUT_SINGLE;
					SelectProjectNamePage.this.managementFoldersEnabledButton.setEnabled(true);
				}
				SelectProjectNamePage.this.validateContent();
				SelectProjectNamePage.this.showTargetUrl();
			}
		});
		this.singleLayoutButton.setText(SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.SingleProjectLayout")); 
		
		final Button multipleLayoutButton = new Button(this.layoutGroup, SWT.RADIO);
		data = this.makeGridData();
		multipleLayoutButton.setLayoutData(data);
		multipleLayoutButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button) e.widget;
				if (button.getSelection()) {
					SelectProjectNamePage.this.layoutType = ShareProjectOperation.LAYOUT_MULTIPLE;
					SelectProjectNamePage.this.rootProjectName = SelectProjectNamePage.this.rootProjectNameField.getText();
					SelectProjectNamePage.this.managementFoldersEnabledButton.setEnabled(true);
				}
				SelectProjectNamePage.this.rootProjectNameField.setEditable(button.getSelection());
				SelectProjectNamePage.this.validateContent();
				SelectProjectNamePage.this.showTargetUrl();
			}
		});
		multipleLayoutButton.setText(SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.MultiProjectLayout"));
		
		this.rootProjectNameField = new Text(this.layoutGroup, SWT.SINGLE | SWT.BORDER);
		this.rootProjectNameField.setLayoutData(this.makeGridData());
		this.rootProjectNameField.setEditable(false);
		this.rootProjectNameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				SelectProjectNamePage.this.rootProjectName = SelectProjectNamePage.this.rootProjectNameField.getText();
				SelectProjectNamePage.this.showTargetUrl();
			}
		});
		verifier = new CompositeVerifier();
		name = SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.RootName");
		verifier.add(new NonEmptyFieldVerifier(name));
		verifier.add(new ResourceNameVerifier(name, true));
		verifier.add(new AbsolutePathVerifier(name));
		this.attachTo(this.rootProjectNameField, new AbstractVerifierProxy(verifier) {
			protected boolean isVerificationEnabled(Control input) {
				return multipleLayoutButton.getSelection();
			}			
		});
		
		new Label(this.layoutGroup, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(this.makeGridData());

		this.managementFoldersEnabledButton = new Button(this.layoutGroup, SWT.CHECK);
		this.managementFoldersEnabledButton.setLayoutData(new GridData());
		this.managementFoldersEnabledButton.setEnabled(false);
		this.managementFoldersEnabledButton.setSelection(true);
		this.managementFoldersEnabled = true;
		this.managementFoldersEnabledButton.setText(SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.UseRecommended"));
		this.managementFoldersEnabledButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SelectProjectNamePage.this.managementFoldersEnabled = ((Button)e.widget).getSelection();
				SelectProjectNamePage.this.showTargetUrl();
			}
		});
		this.initializeDialogUnits(parent);
		Label label = new Label(this.layoutGroup, SWT.WRAP);
		data = this.makeGridData();
		data.heightHint = this.convertHeightInCharsToPixels(2);
		label.setLayoutData(data);
		label.setText(SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.Hint"));

		Composite urlComposite = new Composite(this.layoutGroup, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 2;
		urlComposite.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		urlComposite.setLayoutData(data);
		
		label = new Label(urlComposite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setImage(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER).createImage());
		
		this.targetUrlField = new Label(urlComposite, SWT.SINGLE);
		this.targetUrlField.setLayoutData(this.makeGridData());
		this.projectNameField.setEditable(true);
		this.targetUrlField.setBackground(this.projectNameField.getBackground());
		label.setBackground(this.projectNameField.getBackground());
		urlComposite.setBackground(this.projectNameField.getBackground());
		this.projectNameField.setEditable(false);

		useEmptyNameButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SelectProjectNamePage.this.validateContent();
				SelectProjectNamePage.this.selectedName = "";
				Button button = (Button)e.widget;
				if (button.getSelection()) {
					multipleLayoutButton.setEnabled(false);
					if (multipleLayoutButton.getSelection()) {
						multipleLayoutButton.setSelection(false);
						SelectProjectNamePage.this.rootProjectNameField.setEditable(false);
						SelectProjectNamePage.this.managementFoldersEnabledButton.setEnabled(false);
						SelectProjectNamePage.this.defaultLayoutButton.setSelection(true);
					}
				}
				else {
					multipleLayoutButton.setEnabled(true);
				}
				SelectProjectNamePage.this.showTargetUrl();
			}
		});
		
//		Setting context help
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, "org.eclipse.team.svn.help.projectNameContext");
		
		return composite;
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

	protected GridData makeGridData() {
		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		data.horizontalSpan = 1;
		return data;
	}

}
