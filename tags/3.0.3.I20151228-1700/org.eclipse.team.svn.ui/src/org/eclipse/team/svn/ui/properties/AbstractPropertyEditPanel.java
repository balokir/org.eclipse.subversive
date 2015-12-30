/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.properties.PredefinedProperty;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.verifier.AbstractFormattedVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.CompositePropertiesVerifier;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.ExistingResourceVerifier;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.PropertyNameVerifier;
import org.eclipse.team.svn.ui.verifier.PropertyVerifier;

/**
 * Abstract panel for editing properties implementation.
 * 
 * @author Alexei Goncharov
 */
public abstract class AbstractPropertyEditPanel extends AbstractDialogPanel {
	
	protected SVNProperty[] source;
	protected HashMap<String, String> alreadyExistent;
	
	protected Combo nameField;
	protected Text valueField;
	protected Text fileField;
	protected Text descriptionField;
	
	protected boolean fileSelected;
	protected String propertyName;
	protected String propertyValue;
	protected String propertyFile;
	
	protected List<PredefinedProperty> predefinedProperties;
	protected HashMap<String, AbstractFormattedVerifier> verifiers;
	
	public AbstractPropertyEditPanel(SVNProperty[] propertyData, String dialogTitle, String dialogDescription) {
		super();
		if (propertyData != null) {
			this.propertyName = propertyData[0].name;
			this.propertyValue = propertyData[0].value;
		}
		this.verifiers = new HashMap<String, AbstractFormattedVerifier>();
		this.dialogTitle = dialogTitle;
		this.dialogDescription = dialogDescription;
		this.source = propertyData;
		this.fileSelected = false;
		this.alreadyExistent = new HashMap<String, String>();
		if (propertyData != null) {
			for (SVNProperty current : propertyData) {
				this.alreadyExistent.put(current.name, current.value);
			}
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
	
	public void setPropertyToEdit(SVNProperty propertyToEdit) {
		if (propertyToEdit != null) {
			this.propertyName = propertyToEdit.name;
			this.propertyValue = propertyToEdit.value;
		}
		else {
			this.propertyName = this.propertyValue = ""; //$NON-NLS-1$
		}
	}
	
	protected void createControlsImpl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText(SVNUIMessages.AbstractPropertyEditPanel_Name);
		
		this.nameField = new Combo(composite, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		this.nameField.setLayoutData(data);
		this.nameField.setVisibleItemCount(10);
		this.nameField.setItems(this.getPropertyNames(this.predefinedProperties));
		this.nameField.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				String selected = AbstractPropertyEditPanel.this.nameField.getItem(AbstractPropertyEditPanel.this.nameField.getSelectionIndex());
				String value = AbstractPropertyEditPanel.this.alreadyExistent.get(selected);
				if (value != null) {
					AbstractPropertyEditPanel.this.valueField.setText(value);
				}
				else {
					PredefinedProperty prop = AbstractPropertyEditPanel.this.getPredefinedProperty(selected);
					if (prop != null) {
						AbstractPropertyEditPanel.this.valueField.setText(prop.value);
					}
				}
				AbstractPropertyEditPanel.this.descriptionField.setText(AbstractPropertyEditPanel.this.getDescriptionText());
			}
			public void widgetDefaultSelected(SelectionEvent e) {				
			}			
		});
		Listener nameFieldListener = new Listener() {
			public void handleEvent(Event event) {
				PredefinedProperty prop = AbstractPropertyEditPanel.this.getPredefinedProperty(AbstractPropertyEditPanel.this.nameField.getText());
				if (prop != null) {
					AbstractPropertyEditPanel.this.valueField.setText(prop.value);	
				}
				AbstractPropertyEditPanel.this.descriptionField.setText(AbstractPropertyEditPanel.this.getDescriptionText());
			}
		};
		this.nameField.addListener(SWT.Selection, nameFieldListener);
		this.nameField.addListener(SWT.Modify, nameFieldListener);
		
		Composite descriptionComposite = new Composite(composite, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		descriptionComposite.setLayoutData(data);
		GridLayout descriptionLayout = new GridLayout();
		descriptionLayout.horizontalSpacing = 5;
		descriptionLayout.numColumns = 2;
		descriptionComposite.setLayout(descriptionLayout);

		Label bulb = new Label(descriptionComposite, SWT.NONE);
		bulb.setImage(SVNTeamUIPlugin.instance().getImageDescriptor("icons/dialogs/bulb.png").createImage()); //$NON-NLS-1$
		data = new GridData();
		data.verticalAlignment = SWT.TOP;
		bulb.setLayoutData(data);

		this.descriptionField = new Text(descriptionComposite, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.verticalAlignment = SWT.TOP;
		data.grabExcessVerticalSpace = true;
		data.heightHint = 100;
		this.descriptionField.setLayoutData(data);
		this.descriptionField.setText(this.getDescriptionText());
		this.descriptionField.setBackground(this.nameField.getBackground());
		descriptionComposite.setBackground(this.nameField.getBackground());
		bulb.setBackground(this.nameField.getBackground());
		this.descriptionField.setEditable(false);		
		
		CompositeVerifier verifier = new CompositeVerifier();
		String name = SVNUIMessages.AbstractPropertyEditPanel_Name_Verifier;
		verifier.add(new NonEmptyFieldVerifier(name));
		verifier.add(new PropertyNameVerifier(name));
		this.attachTo(this.nameField, verifier);
		
		final Button editManual = new Button(composite, SWT.RADIO);
		
		this.valueField = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		CompositePropertiesVerifier valueVerifier = new CompositePropertiesVerifier(this.nameField, this.verifiers);
		this.attachTo(this.valueField, new AbstractVerifierProxy(valueVerifier) {
			protected boolean isVerificationEnabled(Control input) {			
				return editManual.getSelection();
			}			
		});

		data = new GridData();
		editManual.setLayoutData(data);
		editManual.setText(SVNUIMessages.AbstractPropertyEditPanel_EnterValue);
		editManual.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Button button = (Button)event.widget;
				AbstractPropertyEditPanel.this.valueField.setEnabled(button.getSelection());
				AbstractPropertyEditPanel.this.fileSelected = false;
				AbstractPropertyEditPanel.this.validateContent();
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
		browse.setText(SVNUIMessages.Button_Browse);
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(browse);
		browse.setLayoutData(data);
		browse.setEnabled(false);
		browse.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				FileDialog fileDialog = new FileDialog(AbstractPropertyEditPanel.this.manager.getShell(),SWT.OPEN);
				String res = fileDialog.open();
				if (res != null) {
					AbstractPropertyEditPanel.this.fileField.setText(res);
					AbstractPropertyEditPanel.this.validateContent();
				}
			}
		});

		data = new GridData();
		loadFromFile.setLayoutData(data);
		loadFromFile.setText(SVNUIMessages.AbstractPropertyEditPanel_LoadValue);
		loadFromFile.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Button button = (Button)event.widget;
				AbstractPropertyEditPanel.this.fileField.setEnabled(button.getSelection());
				browse.setEnabled(button.getSelection());
				AbstractPropertyEditPanel.this.fileSelected = true;
				AbstractPropertyEditPanel.this.validateContent();
			}
		});
		
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.fileField.setLayoutData(data);
		this.fileField.setEnabled(false);
		this.attachTo(this.fileField, new AbstractVerifierProxy(new ExistingResourceVerifier(SVNUIMessages.AbstractPropertyEditPanel_File_Verifier, true)) {
			protected boolean isVerificationEnabled(Control input) {
				return loadFromFile.getSelection();
			}
		});
		if (this.source != null && this.source.length > 0) {
			this.nameField.setText(this.propertyName);
			this.valueField.setText(this.propertyValue);
		}
		this.nameField.setFocus();
	}
	
	/**
	 * 
	 * @return a list of predefined properties. Must not be null.
	 */
	protected List<PredefinedProperty> getPredefinedProperties() {
		ArrayList<PredefinedProperty> properties = new ArrayList<PredefinedProperty>();
		for (PredefinedProperty property : CoreExtensionsManager.instance().getPredefinedPropertySet().getPredefinedProperties()) {
			if (this.isPropertyAccepted(property)) {
				properties.add(property);
			}
		}
		return properties;
	}

	protected void fillVerifiersMap() {
		this.predefinedProperties = this.getPredefinedProperties();
		IRepositoryResource base = this.getRepostioryResource();
		for (PredefinedProperty current : this.predefinedProperties) {
			this.verifiers.put(current.name, new PropertyVerifier("EditPropertiesInputField", "".equals(current.validationRegexp) ? null : current.validationRegexp, current.name, base)); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	protected abstract boolean isPropertyAccepted(PredefinedProperty property);
	protected abstract IRepositoryResource getRepostioryResource();
	
	protected String[] getPropertyNames(List<PredefinedProperty> predefinedProperties) {
		List<String> names = new ArrayList<String>();
		for (Iterator<PredefinedProperty> it = predefinedProperties.iterator(); it.hasNext(); ) {
			names.add(it.next().name);
		}
		return names.toArray(new String[names.size()]);
	}
	
	protected String getDescriptionText() {
		String propName = this.nameField.getText();
		PredefinedProperty prop = this.getPredefinedProperty(propName);
		if (prop != null) {
			return (prop.description != null && prop.description.trim().length() > 0) ? prop.description : SVNUIMessages.AbstractPropertyEditPanel_NoDescription;			
		}
		return SVNUIMessages.AbstractPropertyEditPanel_UserDefined;
	}
	
	protected PredefinedProperty getPredefinedProperty(String name) {
		int idx = this.predefinedProperties.indexOf(new PredefinedProperty(name));
		if (idx >= 0) {
			return this.predefinedProperties.get(idx);
		}
		return null;
	}
	
	protected void saveChangesImpl() {
		this.propertyName = this.nameField.getText();
		this.propertyValue = this.valueField.getText();
		this.propertyFile = this.fileField.getText();
	}
	
	protected void cancelChangesImpl() {		
	}
	
	public String getDefaultMessage() {
		return SVNUIMessages.AbstractPropertyEditPanel_DefaultMessage;
	}	
}
