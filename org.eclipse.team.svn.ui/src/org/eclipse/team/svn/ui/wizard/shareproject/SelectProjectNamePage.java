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
import org.eclipse.jface.wizard.IWizardPage;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.operation.local.management.ShareProjectOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.RepositoryResourceOnlySelectionComposite;
import org.eclipse.team.svn.ui.verifier.AbsolutePathVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.IValidationManager;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.ResourceNameVerifier;
import org.eclipse.team.svn.ui.verifier.WrapperValidationManagerProxy;
import org.eclipse.team.svn.ui.wizard.AbstractVerifiedWizardPage;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Select repository folder name for project
 * 
 * @author Alexander Gurov
 */
public class SelectProjectNamePage extends AbstractVerifiedWizardPage {
	
	protected boolean isSimpleMode;
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
		 
	protected Composite simpleModeComposite;
	protected Composite advancedModeComposite;
	
	protected RepositoryResourceOnlySelectionComposite resourceSelectionComposite;
	protected Button modeButton;
	
	/*
	 * As we override behavior of validation manager, then 
	 * managers listed below should be used instead of current wizard page class
	 */
	protected IValidationManager simpleModeValidationManager;
	protected IValidationManager advancedModeValidationManager;
	
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
	
	public SelectProjectNamePage() {
		super(
			SelectProjectNamePage.class.getName(), 
			"", 
			SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif"));
		
		this.layoutType = ShareProjectOperation.LAYOUT_DEFAULT;
		this.isSimpleMode = true;				
	}
	
	public IRepositoryLocation getLocation() {
		return this.location;
	}
	
	public boolean isSimpleMode() {
		return this.isSimpleMode;
	}
	
	public void setProjectsAndLocation(IProject []projects, IRepositoryLocation location) {
		this.multiProject = projects.length > 1;
		this.location = location;
						
		IRepositoryResource baseResource = this.location.asRepositoryContainer(this.location.getUrl(), false);			
		this.resourceSelectionComposite.setBaseResource(baseResource);
		this.resourceSelectionComposite.setMatchToBaseResource(true);
		
		/*
		 * Set picker URL as repository location plus project name
		 */
		if (this.isSimpleMode && !this.multiProject) {
			IProject pr = projects[0];
			String url = baseResource + "/" + pr.getName();
			this.resourceSelectionComposite.setUrl(url);
		}
		
		if (this.multiProject) {
			this.selectedName = this.projectName = "";
			this.nameGroup.setVisible(false);
			((GridData)this.nameGroup.getLayoutData()).exclude = true;			
			this.defaultLayoutButton.setSelection(true);
		}
		else {
			this.selectedName = this.projectName = projects[0].getName();
			this.nameGroup.setVisible(true);
			((GridData)this.nameGroup.getLayoutData()).exclude = false;
		}
		this.singleLayoutButton.setEnabled(!this.multiProject);
		this.projectNameField.setText(this.projectName);
		this.rootProjectNameField.setText(this.projectName);
		this.showTargetUrl();
		
		this.changePageTitle();
	}
	
	public boolean isManagementFoldersEnabled() {	
		if (this.isSimpleMode) {
			return false;
		} else {
			return (this.layoutType == ShareProjectOperation.LAYOUT_DEFAULT) ? true : this.managementFoldersEnabled;
		}		
	}
	
	public int getLayoutType() {
		return this.layoutType;
	}
	
	public ShareProjectOperation.IFolderNameMapper getSelectedNames() {
		if (this.isSimpleMode) {				
			return new ShareProjectOperation.IFolderNameMapper() {
				public String getRepositoryFolderName(IProject project) {
					String folderName = null;
					
					String toTrim = SelectProjectNamePage.this.location.getUrl();					
					String selectedUrl = SelectProjectNamePage.this.resourceSelectionComposite.getSelectedResource().getUrl();
					selectedUrl = SVNUtility.normalizeURL(selectedUrl);
					if (selectedUrl.startsWith(toTrim)) {
						folderName = selectedUrl.equals(toTrim) ? "" : selectedUrl.substring(toTrim.length() + 1);																				
						if (SelectProjectNamePage.this.multiProject) {
							folderName += "/" + project.getName();
						}						
					} else {
						throw new RuntimeException("Inconsistent repository location and selected repository url. "
							+ "Selected url: " + selectedUrl + ", repository location: " + toTrim);
					}

					return folderName;
				}					
			};				
		} else {
			return this.multiProject ? null : new ShareProjectOperation.IFolderNameMapper() {
				public String getRepositoryFolderName(IProject project) {
					return SelectProjectNamePage.this.selectedName;
				}
			};
		}
	}
	
	public String getRootProjectName() {
		return this.rootProjectName;
	}
	
	protected Composite createControlImpl(Composite parent) {
		this.initializeDialogUnits(parent);
		
		this.simpleModeValidationManager = new SelectProjectNamePageValidationManager(this, true);
		this.advancedModeValidationManager = new SelectProjectNamePageValidationManager(this, false);
			
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();		
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		//controls area
		this.createSimpleModeControls(composite);
		this.createAdvancedModeControls(composite);
		
		//mode button				
		this.modeButton = new Button(composite, SWT.PUSH);			
		GridData gridData = new GridData();
		int charsCount = Math.max(SVNTeamUIPlugin.instance().getResource("Button.Advanced").length(), SVNTeamUIPlugin.instance().getResource("Button.Simple").length());
		charsCount += 5;
		gridData.widthHint = this.convertWidthInCharsToPixels(charsCount);
		this.modeButton.setLayoutData(gridData);
		this.modeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//change controls area mode
				SelectProjectNamePage.this.isSimpleMode = !SelectProjectNamePage.this.isSimpleMode;
				if (SelectProjectNamePage.this.isSimpleMode) {
					SelectProjectNamePage.this.layoutType = ShareProjectOperation.LAYOUT_DEFAULT;
				}								
				//refresh
				enableControlsArea();
			}
		});
		
		this.enableControlsArea();
		
		//Setting context help
	    PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, "org.eclipse.team.svn.help.projectNameContext");
		
		return composite;
	}	
	
	protected void changePageTitle() {
		if (this.isSimpleMode) {
			this.setTitle(SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.Title.Simple"));
			this.setDescription(SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.Description.Simple"));		
		} else {
			if (this.multiProject) {
				this.setTitle(SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.Title.Multi"));
				this.setDescription(SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.Description.Multi"));			
			}
			else {
				this.setTitle(SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.Title.Single"));
				this.setDescription(SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.Description.Single"));
			}	
		}
	}
	
	protected void enableControlsArea() {	
		this.changePageTitle();
		 
	    if (this.isSimpleMode) {	
	    	this.modeButton.setText(SVNTeamUIPlugin.instance().getResource("Button.Advanced"));	    	
	    	
	        this.simpleModeComposite.setVisible(true);
	        this.advancedModeComposite.setVisible(false);	        
	        
	        ((GridData)this.advancedModeComposite.getLayoutData()).exclude = true;
	        ((GridData)this.simpleModeComposite.getLayoutData()).exclude = false;
	        	        
	        this.simpleModeComposite.getParent().layout();	        
	    } else {	
	    	this.modeButton.setText(SVNTeamUIPlugin.instance().getResource("Button.Simple"));
	    	
	        this.simpleModeComposite.setVisible(false);
	        this.advancedModeComposite.setVisible(true);
	        
	        ((GridData)this.simpleModeComposite.getLayoutData()).exclude = true;
	        ((GridData)this.advancedModeComposite.getLayoutData()).exclude = false;
	        
	        this.advancedModeComposite.getParent().layout();
	    }
	    
	    //update validators
	    this.simpleModeValidationManager.validateContent();
	    this.advancedModeValidationManager.validateContent();	    	   
	}
	
	protected void createSimpleModeControls(Composite parent) {
		this.simpleModeComposite = new Composite(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		this.simpleModeComposite.setLayoutData(data);
		GridLayout layout = new GridLayout();
		this.simpleModeComposite.setLayout(layout);	
				
		IRepositoryResource baseResource = null;
		
		this.resourceSelectionComposite = new RepositoryResourceOnlySelectionComposite(
				this.simpleModeComposite,
				SWT.NONE,
				this.simpleModeValidationManager, 
				"selectProjectNamePage", 
				baseResource,				
				SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.Select.Title"),
				SVNTeamUIPlugin.instance().getResource("SelectProjectNamePage.Select.Description"));				
				
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 550;
		this.resourceSelectionComposite.setLayoutData(data); 
	}
	
	protected void createAdvancedModeControls(Composite parent) {							
		this.advancedModeComposite = new Composite(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		this.advancedModeComposite.setLayoutData(data);
		
		GridLayout layout = new GridLayout();
		this.advancedModeComposite.setLayout(layout);
		
		CompositeVerifier verifier;
		
		this.nameGroup = new Group(this.advancedModeComposite, SWT.NONE);
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
		this.advancedModeValidationManager.attachTo(this.projectNameField, new AbstractVerifierProxy(verifier) {
			protected boolean isVerificationEnabled(Control input) {
				return useRedefinedNameButton.getSelection();
			}			
		});
		
		this.layoutGroup = new Group(this.advancedModeComposite, SWT.NONE);
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
		this.advancedModeValidationManager.attachTo(this.rootProjectNameField, new AbstractVerifierProxy(verifier) {
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

	public IWizardPage getNextPage() {
		this.resourceSelectionComposite.saveHistory();
		return super.getNextPage();
	}
}
