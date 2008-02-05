/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.view.property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.ApplyPropertyMethodComposite;
import org.eclipse.team.svn.ui.composite.PropertiesComposite;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.extension.factory.PredefinedProperty;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.preferences.SVNTeamPropsPreferencePage;
import org.eclipse.team.svn.ui.verifier.AbstractFormattedVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.CompositePropertiesVerifier;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.ExistingResourceVerifier;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.PropertyNameVerifier;
import org.eclipse.team.svn.ui.verifier.PropertyVerifier;

/**
 * Edit property panel
 * 
 * @author Sergiy Logvin
 */
public class PropertyEditPanel extends AbstractDialogPanel {
	public static int SINGLE_FILE = 0;
	public static int MULTIPLE_FILES = 1;
	public static int MIXED_RESOURCES = 2;
		
	protected Combo nameField;
	protected Text valueField;
	protected Text fileField;
	protected Label descriptionField;
	protected Button recursiveButton;
	protected ApplyPropertyMethodComposite applyComposite;
	protected SVNProperty source;
	protected int resourcesType;
	
	protected String propertyName;
	protected String propertyValue;
	protected String propertyFile;
	protected boolean fileSelected;
	protected boolean recursiveSelected;
	protected boolean applyToAll;
	protected boolean applyToFiles;
	protected boolean applyToFolders;
	protected List predefinedProperties;
	protected HashMap<String, String> predefinedPropertiesRegexps;
	protected HashMap<String, AbstractFormattedVerifier> verifiers;
	protected IResource []selectedResources;
	protected SVNTeamPropsPreferencePage.CustomProperty [] customProps;
	protected boolean strict;

	public PropertyEditPanel(SVNProperty data, IResource []selectedResources, boolean strict) {
		super();
		this.strict = strict;
		this.dialogTitle = SVNTeamUIPlugin.instance().getResource(data != null ? "PropertyEditPanel.Title.Edit" : "PropertyEditPanel.Title.Add");	
		this.fileSelected = false;
		this.source = data;
		this.selectedResources = selectedResources;
		this.resourcesType = this.computeResourcesType();
		this.predefinedProperties = ExtensionsManager.getInstance().getPredefinedPropertySet().getPredefinedProperties(this.selectedResources);
		this.predefinedPropertiesRegexps = ExtensionsManager.getInstance().getPredefinedPropertySet().getPredefinedPropertiesRegexps(this.selectedResources);
		this.customProps = SVNTeamPropsPreferencePage.loadCustomProperties(SVNTeamPreferences.getCustomPropertiesList(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.CUSTOM_PROPERTIES_LIST_NAME));
		this.createVerifiersMap();
		this.dialogDescription = SVNTeamUIPlugin.instance().getResource("PropertyEditPanel.Description");
	}
	
	public boolean isStrict() {
		return this.strict;
	}
	
	private void createVerifiersMap() {
		this.verifiers = new HashMap<String, AbstractFormattedVerifier>();
		String [] properties = {};
		properties = this.predefinedPropertiesRegexps.keySet().toArray(properties);
		for (int i = 0; i <  properties.length; i++) {
			this.verifiers.put(properties[i], new PropertyVerifier("EditPropertiesInputField", this.predefinedPropertiesRegexps.get(properties[i]), properties[i]));
		}
		for (int i = 0; i < this.customProps.length; i++) {
			this.verifiers.put(this.customProps[i].propName, new PropertyVerifier("EditPropertiesInputField", this.customProps[i].regExp.equals("") ? null : this.customProps[i].regExp, this.customProps[i].propName));
		}
	}
	
	public boolean isFileSelected() {
		return this.fileSelected;
	}
	
	public String getPropertyFile() {
		return this.propertyFile;
	}
	
	public String getPropertyName() {
		return this.propertyName;
	}
	
	public String getPropertyValue() {
		return this.propertyValue;
	}
	
	public boolean isRecursiveSelected() {
		return this.recursiveSelected;
	}
	
	public int getApplyMethod() {
		return this.applyComposite == null ? PropertiesComposite.APPLY_TO_ALL : this.applyComposite.getApplyMethod();
	}
	
	public String getFilterMask() {
		return this.applyComposite == null ? "" : this.applyComposite.getFilterMask();
	}
	
	public boolean useMask() {
		return this.applyComposite == null ? false : this.applyComposite.useMask();
	}
		
	public void createControlsImpl (Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText(SVNTeamUIPlugin.instance().getResource("PropertyEditPanel.Name"));
		
		this.nameField = new Combo(composite, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		this.nameField.setLayoutData(data);
		this.nameField.setVisibleItemCount(10);
		this.nameField.setItems(this.getPropertyNames(this.predefinedProperties, this.customProps));
		this.nameField.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				String selected = PropertyEditPanel.this.nameField.getItem(PropertyEditPanel.this.nameField.getSelectionIndex());
				PredefinedProperty prop = PropertyEditPanel.this.getPredefinedProperty(selected);
				if (prop != null) {
					PropertyEditPanel.this.valueField.setText(prop.value);
				}
				PropertyEditPanel.this.descriptionField.setText(PropertyEditPanel.this.getDescriptionText());
			}
			public void widgetDefaultSelected(SelectionEvent e) {				
			}			
		});
		this.nameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				PredefinedProperty prop = PropertyEditPanel.this.getPredefinedProperty(PropertyEditPanel.this.nameField.getText());
				if (prop != null) {
					PropertyEditPanel.this.valueField.setText(prop.value);	
				}
				PropertyEditPanel.this.descriptionField.setText(PropertyEditPanel.this.getDescriptionText());
			}
		});
		
		Composite descriptionComposite = new Composite(composite, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		descriptionComposite.setLayoutData(data);
		GridLayout descriptionLayout = new GridLayout();
		descriptionLayout.horizontalSpacing = 5;
		descriptionLayout.numColumns = 2;
		descriptionComposite.setLayout(descriptionLayout);

		Label bulb = new Label(descriptionComposite, SWT.NONE);
		bulb.setImage(SVNTeamUIPlugin.instance().getImageDescriptor("icons/dialogs/bulb.png").createImage());
		data = new GridData();
		data.verticalAlignment = SWT.TOP;
		bulb.setLayoutData(data);
		
		this.descriptionField = new Label(descriptionComposite, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.verticalAlignment = SWT.TOP;
		data.grabExcessVerticalSpace = true;
		data.heightHint = 50;
		this.descriptionField.setLayoutData(data);
		this.descriptionField.setText(this.getDescriptionText());
		this.descriptionField.setBackground(this.nameField.getBackground());
		descriptionComposite.setBackground(this.nameField.getBackground());
		bulb.setBackground(this.nameField.getBackground());
		
		CompositeVerifier verifier = new CompositeVerifier();
		String name = SVNTeamUIPlugin.instance().getResource("PropertyEditPanel.Name.Verifier");
		verifier.add(new NonEmptyFieldVerifier(name));
		verifier.add(new PropertyNameVerifier(name));
		this.attachTo(this.nameField, verifier);
		
		Button editManual = new Button(composite, SWT.RADIO);
		
		this.valueField = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		CompositePropertiesVerifier valueVerifier = new CompositePropertiesVerifier(this.nameField, this.verifiers);
		this.attachTo(valueField, valueVerifier);

		data = new GridData();
		editManual.setLayoutData(data);
		editManual.setText(SVNTeamUIPlugin.instance().getResource("PropertyEditPanel.EnterValue"));
		editManual.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Button button = (Button)event.widget;
				PropertyEditPanel.this.valueField.setEnabled(button.getSelection());
				PropertyEditPanel.this.fileSelected = false;
				PropertyEditPanel.this.validateContent();
			}
		});
		editManual.setSelection(true);

		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 100;
		this.valueField.setLayoutData(data);
		this.valueField.selectAll();
		this.valueField.setEnabled(true);
		
		final Button loadFromFile = new Button(composite, SWT.RADIO);
		
		Composite subComposite = new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		subComposite.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		subComposite.setLayoutData(data);
		
		this.fileField = new Text(subComposite, SWT.SINGLE | SWT.BORDER);
		final Button browse = new Button(subComposite, SWT.PUSH);
		browse.setText(SVNTeamUIPlugin.instance().getResource("Button.Browse"));
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(browse);
		browse.setLayoutData(data);
		browse.setEnabled(false);
		browse.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				FileDialog fileDialog = new FileDialog(PropertyEditPanel.this.manager.getShell(),SWT.OPEN);
				String res = fileDialog.open();
				if (res != null) {
					PropertyEditPanel.this.fileField.setText(res);
					PropertyEditPanel.this.validateContent();
				}
			}
		});

		data = new GridData();
		loadFromFile.setLayoutData(data);
		loadFromFile.setText(SVNTeamUIPlugin.instance().getResource("PropertyEditPanel.LoadValue"));
		loadFromFile.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Button button = (Button)event.widget;
				PropertyEditPanel.this.fileField.setEnabled(button.getSelection());
				browse.setEnabled(button.getSelection());
				PropertyEditPanel.this.fileSelected = true;
				PropertyEditPanel.this.validateContent();
			}
		});
		
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.fileField.setLayoutData(data);
		this.fileField.setEnabled(false);
		this.attachTo(this.fileField, new AbstractVerifierProxy(new ExistingResourceVerifier(SVNTeamUIPlugin.instance().getResource("PropertyEditPanel.File.Verifier"), true)) {
			protected boolean isVerificationEnabled(Control input) {
				return loadFromFile.getSelection();
			}
		});
		if (this.resourcesType != PropertyEditPanel.SINGLE_FILE) {
			if (this.resourcesType == PropertyEditPanel.MIXED_RESOURCES && !this.strict) {
				this.recursiveButton = new Button(subComposite, SWT.CHECK);
				this.recursiveButton.setText(SVNTeamUIPlugin.instance().getResource("PropertyEditPanel.Recursively"));
			
				this.recursiveButton.addSelectionListener(new SelectionListener() {
					public void widgetSelected(SelectionEvent e) {
						PropertyEditPanel.this.refreshControlsEnablement();
						PropertyEditPanel.this.validateContent();
					}
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});
			}
			this.applyComposite = new ApplyPropertyMethodComposite(composite, SWT.NONE, this, this.resourcesType);
			this.applyComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		
		if (this.source != null) {
			this.nameField.setText(this.source.name);
			this.valueField.setText(this.source.value);
		}		
		this.nameField.setFocus();
		if (this.resourcesType == PropertyEditPanel.MIXED_RESOURCES && !this.strict) {
			this.refreshControlsEnablement();
		}
	}
	
    public String getHelpId() {
    	return "org.eclipse.team.svn.help.setPropsDialogContext";
    }

	public String getDefaultMessage() {
		return SVNTeamUIPlugin.instance().getResource("PropertyEditPanel.DefaultMessage");
	}
	
	protected String getDescriptionText() {
		String propName = this.nameField.getText();
		PredefinedProperty prop = this.getPredefinedProperty(propName);
		if (prop != null) {
			return (prop.description != null && prop.description.trim().length() > 0) ? prop.description : SVNTeamUIPlugin.instance().getResource("PropertyEditPanel.NoDescription");			
		}
		SVNTeamPropsPreferencePage.CustomProperty customProp = this.getCustomProperty(propName);
		if (customProp != null && !customProp.descriprion.equals("")) {
			return customProp.descriprion;
		}
		return SVNTeamUIPlugin.instance().getResource("PropertyEditPanel.UserDefined");
	}
	
	protected int computeResourcesType() {
		boolean singleResource = this.selectedResources.length == 1;
		boolean allFiles = true;
		for (int i = 0; i < this.selectedResources.length; i++) {
			if (!(this.selectedResources[i] instanceof IFile)) {
				allFiles = false;
				break;
			}
		}
		if (allFiles) {
			return singleResource ? PropertyEditPanel.SINGLE_FILE : PropertyEditPanel.MULTIPLE_FILES; 
		}
		return PropertyEditPanel.MIXED_RESOURCES;
	}
	
	protected void saveChangesImpl() {
		this.propertyName = this.nameField.getText();
		this.propertyValue = this.valueField.getText();
		this.propertyFile = this.fileField.getText();
		if (this.resourcesType != PropertyEditPanel.SINGLE_FILE) {
			if (this.resourcesType == PropertyEditPanel.MIXED_RESOURCES && !this.strict) {
				this.recursiveSelected = this.recursiveButton.getSelection();
			}
			if (this.recursiveSelected || this.resourcesType == PropertyEditPanel.MULTIPLE_FILES) {
				this.applyComposite.saveChanges();
			}
		}
	}

	protected void cancelChangesImpl() {		
	}
	
	protected void refreshControlsEnablement() {
		this.applyComposite.setEnabled(this.recursiveButton.getSelection());
	}
	
	protected String[] getPropertyNames(List predefinedProperties, SVNTeamPropsPreferencePage.CustomProperty [] customProperties) {
		List names = new ArrayList();
		for (Iterator it = predefinedProperties.iterator(); it.hasNext(); ) {
			names.add(((PredefinedProperty) it.next()).name);
		}
		names.add(SVNTeamUIPlugin.instance().getResource("PropertyEditPanel.custom_description"));
		for (int i = 0; i < customProperties.length; i++) {
			names.add(customProperties[i].propName);
		}
		if (this.customProps.length == 0) {
			names.add("    " + SVNTeamUIPlugin.instance().getResource("PropertyEditPanel.custom_hint"));
		}
		String[] propertyNames = (String[]) names.toArray(new String[names.size()]);
		return propertyNames;
	}
	
	protected PredefinedProperty getPredefinedProperty(String name) {
		int idx = this.predefinedProperties.indexOf(new PredefinedProperty(name));
		if (idx >= 0) {
			return (PredefinedProperty)this.predefinedProperties.get(idx);
		}
		return null;
	}
	
	protected SVNTeamPropsPreferencePage.CustomProperty getCustomProperty(String name) {
		for (int i = 0; i < this.customProps.length; i++) {
			if (this.customProps[i].propName.equals(name)) {
				return this.customProps[i];
			}
		}
		return null;
	}
	
	protected Point getPrefferedSizeImpl() {
        return new Point(590, SWT.DEFAULT);
    }	
}
