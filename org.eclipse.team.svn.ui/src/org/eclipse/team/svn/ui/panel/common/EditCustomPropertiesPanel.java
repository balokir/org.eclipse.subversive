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

package org.eclipse.team.svn.ui.panel.common;

import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.SpellcheckedTextProvider;
import org.eclipse.team.svn.ui.extension.factory.PredefinedProperty;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.verifier.AbstractFormattedVerifier;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.PropertyNameVerifier;

/**
 * Panel to be used in a dialog for entering user defined property and description.
 * 
 * @author Alexei Goncharov
 */
public class EditCustomPropertiesPanel extends AbstractDialogPanel {

	protected PredefinedProperty property;
	protected Text propName;
	protected Text propRegexp;
	protected StyledText propDescription;
	
	public EditCustomPropertiesPanel(PredefinedProperty property) {
		super();
		this.property = property;
		this.dialogTitle = property == null ? SVNUIMessages.EditCustomPropertiesPanel_Title_Add : SVNUIMessages.EditAutoPropertiesPanel_Title_Edit;
		this.dialogDescription = SVNUIMessages.EditCustomPropertiesPanel_Description;
		this.defaultMessage = SVNUIMessages.EditCustomPropertiesPanel_Message;
	}
	
	protected void createControlsImpl(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.verticalSpacing = 10;
		parent.setLayout(layout);
		Label propNameLabel = new Label(parent, SWT.NONE);
		propNameLabel.setText(SVNUIMessages.EditCustomPropertiesPanel_PropName);
		this.propName = new Text(parent, SWT.BORDER);
		this.propName.setText((this.property == null) ? "" : this.property.name); //$NON-NLS-1$
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		this.propName.setLayoutData(layoutData);
		
		CompositeVerifier verifier = new CompositeVerifier();
		String name = SVNUIMessages.EditCustomPropertiesPanel_PropName_Verifier;
		verifier.add(new NonEmptyFieldVerifier(name));
		verifier.add(new PropertyNameVerifier(name));
		this.attachTo(this.propName, verifier);
		
		Group optional = new Group(parent, SWT.NONE);
		optional.setText(SVNUIMessages.EditCustomPropertiesPanel_Optional);
		layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 2;
		optional.setLayoutData(layoutData);
		layout = new GridLayout();
		layout.numColumns = 1;
		optional.setLayout(layout);
		
		Label propRegexpLabel = new Label(optional, SWT.NONE);
		propRegexpLabel.setText(SVNUIMessages.EditCustomPropertiesPanel_PropRegExp);
		this.propRegexp = new Text(optional, SWT.BORDER);
		this.propRegexp.setText((this.property == null) ? "" : this.property.validationRegexp); //$NON-NLS-1$
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		this.propRegexp.setLayoutData(layoutData);
		this.attachTo(this.propRegexp, new AbstractFormattedVerifier("EditCustomProperty_Regexp") {			 //$NON-NLS-1$
			protected String getErrorMessageImpl(Control input) {
				try {
					Pattern.compile(this.getText(input));
				}
				catch (Exception ex) {
					return SVNUIMessages.EditCustomPropertiesPanel_Validator_RegExp;
				}
				return null;
			}
			protected String getWarningMessageImpl(Control input) {
				return null;
			}
		});
		
		Label propDescriptionLabel = new Label(optional, SWT.NONE);
		propDescriptionLabel.setText(SVNUIMessages.EditCustomPropertiesPanel_PropDescription);
		layoutData = new GridData();
		propDescriptionLabel.setLayoutData(layoutData);
		layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.heightHint = 80;
		layoutData.widthHint = 0;
        this.propDescription = SpellcheckedTextProvider.getTextWidget(optional, layoutData, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		this.propDescription.setText((this.property == null) ? "" : this.property.description); //$NON-NLS-1$
	}
	
	public PredefinedProperty getProperty() {
		return this.property;
	}
	
	protected void cancelChangesImpl() {
	}

	protected void saveChangesImpl() {
		this.property = new PredefinedProperty(this.propName.getText(), this.propDescription.getText(), "", this.propRegexp.getText()); //$NON-NLS-1$
	}

}
