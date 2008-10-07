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
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.svn.core.operation.local.management.ShareProjectOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.extension.impl.ISelectProjectNamePageData;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.IValidationManager;
import org.eclipse.team.svn.ui.verifier.WrapperValidationManagerProxy;
import org.eclipse.team.svn.ui.wizard.AbstractVerifiedWizardPage;
import org.eclipse.ui.PlatformUI;

/**
 * Select repository folder name for project
 * 
 * @author Alexander Gurov
 */
public class SelectProjectNamePage extends AbstractVerifiedWizardPage {
	
	protected boolean isSimpleMode;
	
	protected Button simpleModeRadionButton;
	protected Button advancedModeRadionButton;
	
	protected SelectProjectNamePageSimpleModeComposite simpleModeComposite;
	protected ShareProjectNameAdvancedModeComposite advancedModeComposite;	
	
	public SelectProjectNamePage() {
		super(
			SelectProjectNamePage.class.getName(), 
			"", 
			SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif"));
		this.isSimpleMode = true;				
	}
	
	protected class SelectProjectNamePageValidationManager extends WrapperValidationManagerProxy {
				
		protected boolean isSimpleValidationManager;
		
		public SelectProjectNamePageValidationManager(IValidationManager validationManager, boolean isSimpleValidationManager) {
			super(validationManager);	
			this.isSimpleValidationManager = isSimpleValidationManager;
		}

		protected AbstractVerifier wrapVerifier(AbstractVerifier verifier) {
			return new AbstractVerifierProxy(verifier) {
				protected boolean isVerificationEnabled(Control input) {
					return SelectProjectNamePage.this.isSimpleMode == SelectProjectNamePageValidationManager.this.isSimpleValidationManager;
				}
			};			
		}					
	}	
	
	/*
	 * Listens to changes on mode buttons
	 */
	protected class ModeListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent e) {
			//change controls area mode
			Button modeButton = (Button) e.widget;
			if (SelectProjectNamePage.this.simpleModeRadionButton == modeButton && SelectProjectNamePage.this.isSimpleMode == false) {
				SelectProjectNamePage.this.isSimpleMode = true;
				enableControlsArea();
			} else if (SelectProjectNamePage.this.advancedModeRadionButton == modeButton && SelectProjectNamePage.this.isSimpleMode == true) {
				SelectProjectNamePage.this.isSimpleMode = false;
				enableControlsArea();
			}
		}		
	}
	
	public IRepositoryLocation getLocation() {
		return this.getActivePageData().getRepositoryLocation();
	}
	
	public boolean isSimpleMode() {
		return this.isSimpleMode;
	}
	
	public void setProjectsAndLocation(IProject []projects, IRepositoryLocation location) {
		boolean multiProject = projects.length > 1;
					
		this.setTitle(SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.Title"));
		this.setDescription(SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.Description"));
		
		this.simpleModeComposite.setProjectsAndLocation(projects, location, multiProject);
		this.advancedModeComposite.setProjectsAndLocation(projects, location, multiProject);		
	}
	
	public boolean isManagementFoldersEnabled() {
		return this.getActivePageData().isManagementFoldersEnabled(); 
	}
	
	public int getLayoutType() {
		return this.getActivePageData().getLayoutType();
	}
	
	public ShareProjectOperation.IFolderNameMapper getSelectedNames() {
		return this.getActivePageData().getSelectedNames();
	}
	
	public String getRootProjectName() {
		return this.getActivePageData().getRootProjectName();
	}
	
	protected Composite createControlImpl(Composite parent) {																	
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		//controls area
		ModeListener modeListener = new ModeListener();
		
		this.simpleModeRadionButton = new Button(composite, SWT.RADIO);
		this.simpleModeRadionButton.setText(SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.SimpleModeButton"));
		this.simpleModeRadionButton.setSelection(true);
		this.simpleModeRadionButton.addSelectionListener(modeListener);
		
		//simple mode controls
		IValidationManager simpleModeValidationManager = new SelectProjectNamePageValidationManager(this, true);
		this.simpleModeComposite = new SelectProjectNamePageSimpleModeComposite(composite, SWT.NONE, simpleModeValidationManager);
		layout = (GridLayout) this.simpleModeComposite.getLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;	
		GridData gridData = (GridData) this.simpleModeComposite.getLayoutData();
		gridData.horizontalAlignment = GridData.FILL;				
		gridData.grabExcessHorizontalSpace = true;
				
		//line separator
		Label label = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.verticalIndent = 5;
		label.setLayoutData(gridData);
		
		this.advancedModeRadionButton = new Button(composite, SWT.RADIO);
		this.advancedModeRadionButton.setText(SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.AdvancedModeButton"));
		layout = new GridLayout();
		gridData = new GridData();
		gridData.verticalIndent = 5;
		this.advancedModeRadionButton.setLayoutData(gridData);
		this.advancedModeRadionButton.addSelectionListener(modeListener);
		
		//advanced mode controls
		IValidationManager advancedModeValidationManager = new SelectProjectNamePageValidationManager(this, false);
		this.advancedModeComposite = new ShareProjectNameAdvancedModeComposite(composite, SWT.NONE, advancedModeValidationManager);
		layout = (GridLayout) this.advancedModeComposite.getLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;	
		gridData = (GridData) this.advancedModeComposite.getLayoutData();
		gridData.horizontalAlignment = GridData.FILL;		
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		
		this.enableControlsArea();
		
		//Setting context help
	    PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, "org.eclipse.team.svn.help.projectNameContext");
		
		return composite;
	}	
	
	protected void enableControlsArea() {	
		if (this.isSimpleMode) {
			this.simpleModeComposite.setEnabled(true);
			this.advancedModeComposite.setEnabled(false);
		} else {
			this.simpleModeComposite.setEnabled(false);
			this.advancedModeComposite.setEnabled(true);
		}
		
	    //update validators
	    this.simpleModeComposite.validateContent();
	    this.advancedModeComposite.validateContent();	    	   
	}

	protected ISelectProjectNamePageData getActivePageData() {
		return this.isSimpleMode ? this.simpleModeComposite : this.advancedModeComposite;
	}
	
	public IWizardPage getNextPage() {
		this.getActivePageData().save();
		return super.getNextPage();
	}
}
