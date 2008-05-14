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

package org.eclipse.team.svn.ui.wizard.checkoutas;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.RecureDepthSelector;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.ResourceNameVerifier;
import org.eclipse.team.svn.ui.wizard.AbstractVerifiedWizardPage;
import org.eclipse.ui.PlatformUI;

/**
 * Checkout method selection page
 * 
 * @author Alexander Gurov
 */
public class CheckoutMethodSelectionPage extends AbstractVerifiedWizardPage {
	protected static final int USE_NEW_PROJECT_WIZARD = 0;
	protected static final int FIND_PROJECTS = 1;
	protected static final int CHECKOUT_AS_PROJECT = 2;
	protected static final int CHECKOUT_AS_FOLDER = 3;
	
	protected String defaultName;
	protected Button selectLocationButton;
	protected Text nameField;
	protected RecureDepthSelector recureDepthSelector;

	protected String projectName;
	protected int checkoutType;
	protected boolean ignoreExternalsSelected;

	public CheckoutMethodSelectionPage(String defaultName, boolean newProjectSelectionEnabled) {
		super(CheckoutMethodSelectionPage.class.getName(), 
			SVNTeamUIPlugin.instance().getResource("CheckoutMethodSelectionPage.Title"), 
			SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif"));
		
		this.setDescription(SVNTeamUIPlugin.instance().getResource("CheckoutMethodSelectionPage.Description"));
		
		this.projectName = this.defaultName = defaultName;
		this.checkoutType = newProjectSelectionEnabled ? CheckoutMethodSelectionPage.USE_NEW_PROJECT_WIZARD : CheckoutMethodSelectionPage.CHECKOUT_AS_PROJECT;

		this.ignoreExternalsSelected = false;
	}
	
	public String getProjectName() {
		return this.projectName;
	}
	
	public boolean isUseNewProjectWizard() {
		return this.checkoutType == CheckoutMethodSelectionPage.USE_NEW_PROJECT_WIZARD;
	}
	
	public boolean isFindProjectsSelected() {
		return this.checkoutType == CheckoutMethodSelectionPage.FIND_PROJECTS;
	}
	
	public boolean isCheckoutAsFolderSelected() {
		return this.checkoutType == CheckoutMethodSelectionPage.CHECKOUT_AS_FOLDER;
	}
	
	public int getRecureDepth() {
		return this.recureDepthSelector.getRescureDepth();
	}
	
	public boolean isIgnoreExternalsSelected() {
		return this.ignoreExternalsSelected;
	}
	
	protected Composite createControlImpl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		this.initializeDialogUnits(composite);
		
		// GridLayout
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		
		// GridData
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		this.setControl(composite);
		
		Label description = new Label(composite, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		data.heightHint = this.convertHeightInCharsToPixels(this.checkoutType == CheckoutMethodSelectionPage.USE_NEW_PROJECT_WIZARD ? 1 : 2);
		description.setLayoutData(data);
		String message = SVNTeamUIPlugin.instance().getResource("CheckoutMethodSelectionPage.HintHead", new String[] {this.defaultName});
		description.setText(message + (this.checkoutType != CheckoutMethodSelectionPage.USE_NEW_PROJECT_WIZARD ?
				" " + SVNTeamUIPlugin.instance().getResource("CheckoutMethodSelectionPage.HintTail") : ""));
		this.projectName = this.defaultName = FileUtility.formatResourceName(this.defaultName);

		Button useNewProjectWizardButton = new Button(composite, SWT.RADIO);
		data = new GridData(GridData.FILL_HORIZONTAL);
		useNewProjectWizardButton.setLayoutData(data);
		useNewProjectWizardButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CheckoutMethodSelectionPage.this.selectionChanged(CheckoutMethodSelectionPage.USE_NEW_PROJECT_WIZARD);
			}
		});
		useNewProjectWizardButton.setText(SVNTeamUIPlugin.instance().getResource("CheckoutMethodSelectionPage.NewWizard")); 
		useNewProjectWizardButton.setSelection(this.checkoutType == CheckoutMethodSelectionPage.USE_NEW_PROJECT_WIZARD);
		useNewProjectWizardButton.setEnabled(this.checkoutType == CheckoutMethodSelectionPage.USE_NEW_PROJECT_WIZARD);
		
		Button findProjectsButton = new Button(composite, SWT.RADIO);
		data = new GridData(GridData.FILL_HORIZONTAL);
		findProjectsButton.setLayoutData(data);
		findProjectsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CheckoutMethodSelectionPage.this.selectionChanged(CheckoutMethodSelectionPage.FIND_PROJECTS);
			}
		});
		findProjectsButton.setText(SVNTeamUIPlugin.instance().getResource("CheckoutMethodSelectionPage.Find")); 
		findProjectsButton.setEnabled(this.checkoutType == CheckoutMethodSelectionPage.USE_NEW_PROJECT_WIZARD ||
				ExtensionsManager.getInstance().getCurrentCheckoutFactory().findProjectsOptionEnabled());
		
		Button checkoutAsFolder = new Button(composite, SWT.RADIO);
		data = new GridData(GridData.FILL_HORIZONTAL);
		checkoutAsFolder.setLayoutData(data);
		checkoutAsFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CheckoutMethodSelectionPage.this.selectionChanged(CheckoutMethodSelectionPage.CHECKOUT_AS_FOLDER);
			}
		});
		checkoutAsFolder.setText(SVNTeamUIPlugin.instance().getResource("CheckoutMethodSelectionPage.Folder"));
		
		this.selectLocationButton = new Button(composite, SWT.RADIO);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.selectLocationButton.setLayoutData(data);
		this.selectLocationButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CheckoutMethodSelectionPage.this.selectionChanged(CheckoutMethodSelectionPage.CHECKOUT_AS_PROJECT);
			}
		});
		this.selectLocationButton.setText(SVNTeamUIPlugin.instance().getResource("CheckoutMethodSelectionPage.Project")); 
		this.selectLocationButton.setSelection(this.checkoutType != CheckoutMethodSelectionPage.USE_NEW_PROJECT_WIZARD);
		
		this.nameField = new Text(composite, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.nameField.setLayoutData(data);
		this.nameField.setText(this.defaultName);
		this.nameField.setEnabled(this.checkoutType != CheckoutMethodSelectionPage.USE_NEW_PROJECT_WIZARD);
		this.nameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String name = CheckoutMethodSelectionPage.this.nameField.getText().trim();
				CheckoutMethodSelectionPage.this.projectName = name;
			}
		});
		String name = SVNTeamUIPlugin.instance().getResource("CheckoutMethodSelectionPage.ProjectName.Verifier");
		CompositeVerifier verifier = new CompositeVerifier();
		verifier.add(new ResourceNameVerifier(name, false));
		verifier.add(new NonEmptyFieldVerifier(name));
		this.attachTo(this.nameField, new AbstractVerifierProxy(verifier) {
			protected boolean isVerificationEnabled(Control input) {
				return CheckoutMethodSelectionPage.this.selectLocationButton.getSelection();
			}
		});
		
		Label separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		separator.setVisible(false);
		
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.recureDepthSelector = new RecureDepthSelector(composite, SWT.NONE);
		this.recureDepthSelector.setLayoutData(data);
			
		Button ignoreExternalsCheckbox = new Button (composite, SWT.CHECK);
		data = new GridData(GridData.FILL_HORIZONTAL);
		ignoreExternalsCheckbox.setLayoutData(data);
		ignoreExternalsCheckbox.setSelection(false);
		ignoreExternalsCheckbox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CheckoutMethodSelectionPage.this.ignoreExternalsSelected = ((Button)e.widget).getSelection();
			}
		});
		ignoreExternalsCheckbox.setText(SVNTeamUIPlugin.instance().getResource("CheckoutMethodSelectionPage.Externals"));
		
//		Setting context help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, "org.eclipse.team.svn.help.checkoutMethodSelectionContext");
		
		return composite;
	}

	protected void selectionChanged(int newSelection) {
		this.checkoutType = newSelection;
		switch (this.checkoutType) {
			case CheckoutMethodSelectionPage.USE_NEW_PROJECT_WIZARD: 
			case CheckoutMethodSelectionPage.FIND_PROJECTS: 
			case CheckoutMethodSelectionPage.CHECKOUT_AS_FOLDER: {
				this.projectName = this.defaultName;
				this.nameField.setEnabled(false);
				break;
			}
			case CheckoutMethodSelectionPage.CHECKOUT_AS_PROJECT: {
				this.projectName = this.nameField.getText().trim();
				this.nameField.setEnabled(true);
				break;
			}
		}
		this.validateContent();
	}
	
}
