/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexey Mikoyan - Initial implementation
 *    Gabor Liptak - Speedup Pattern's usage
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.utility.PatternProvider;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamAutoPropsPreferencePage;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.FileNameTemplateVerifier;
import org.eclipse.team.svn.ui.verifier.MultiLinePropertyVerifier;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;

/**
 * Edit automatic properties panel
 *
 * @author Alexey Mikoyan
 *
 */
public class EditAutoPropertiesPanel extends AbstractDialogPanel {

	protected SVNTeamAutoPropsPreferencePage.AutoProperty property;
	protected Text txtFileName;
	protected Text txtProperties;
	protected String fileName;
	protected String properties;
	
	public EditAutoPropertiesPanel(SVNTeamAutoPropsPreferencePage.AutoProperty property) {
		super();
		this.property = property;
		this.dialogTitle = SVNTeamUIPlugin.instance().getResource(property == null ? "EditAutoPropertiesPanel.Title.Add" : "EditAutoPropertiesPanel.Title.Edit");
		this.dialogDescription = SVNTeamUIPlugin.instance().getResource("EditAutoPropertiesPanel.Description");
		this.defaultMessage = SVNTeamUIPlugin.instance().getResource("EditAutoPropertiesPanel.Message");
	}
	
	public void createControls(Composite parent) {
		GridLayout layout;
		GridData layoutData;
		Label label;
		
		Composite composite = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginBottom = 5;
		composite.setLayout(layout);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(layoutData);
		
		label = new Label(composite, SWT.NONE);
		label.setText(SVNTeamUIPlugin.instance().getResource("EditAutoPropertiesPanel.FileName"));
		
		this.txtFileName = new Text(composite, SWT.BORDER);
		this.txtFileName.setText((this.property == null) ? "" : this.property.fileName);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		this.txtFileName.setLayoutData(layoutData);
		String fieldName = SVNTeamUIPlugin.instance().getResource("EditAutoPropertiesPanel.FileName.Verifier");
		CompositeVerifier verifier = new CompositeVerifier();
		verifier.add(new NonEmptyFieldVerifier(fieldName));
		verifier.add(new AbstractVerifierProxy(new FileNameTemplateVerifier(fieldName)) {
			protected boolean isVerificationEnabled(Control input) {
				return EditAutoPropertiesPanel.this.txtFileName.getText().trim().length() > 0;
			}
		});
		this.attachTo(this.txtFileName, verifier);
		
		Group group = new Group(parent, SWT.NONE);
		group.setText(SVNTeamUIPlugin.instance().getResource("EditAutoPropertiesPanel.Properties"));
		layoutData = new GridData(GridData.FILL_BOTH);
		group.setLayoutData(layoutData);
		layout = new GridLayout();
		group.setLayout(layout);
		
		label = new Label(group, SWT.NONE);
		label.setText(SVNTeamUIPlugin.instance().getResource("EditAutoPropertiesPanel.Properties.Hint"));
		
		this.txtProperties = new Text(group, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		this.txtProperties.setText((this.property == null) ? "" : PatternProvider.replaceAll(this.property.properties, SVNTeamAutoPropsPreferencePage.AUTO_PROPS_PROPS_SEPARATOR, System.getProperty("line.separator")).trim());
		layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.heightHint = DefaultDialog.convertHeightInCharsToPixels(this.txtProperties, 7);
		this.txtProperties.setLayoutData(layoutData);
		this.attachTo(this.txtProperties, new AbstractVerifierProxy(new MultiLinePropertyVerifier(SVNTeamUIPlugin.instance().getResource("EditAutoPropertiesPanel.Properties.Verifier"))) {
			protected boolean isVerificationEnabled(Control input) {
				return EditAutoPropertiesPanel.this.txtProperties.getText().trim().length() > 0;
			}
		});
	}
	
	protected void cancelChanges() {
	}

	protected void saveChanges() {
		this.fileName = this.txtFileName.getText().trim();
		this.properties = PatternProvider.replaceAll(this.txtProperties.getText().trim(), SVNTeamAutoPropsPreferencePage.AUTO_PROPS_PROPS_SEPARATOR, System.getProperty("line.separator"));
		this.properties = PatternProvider.replaceAll(this.properties.trim(), System.getProperty("line.separator"), SVNTeamAutoPropsPreferencePage.AUTO_PROPS_PROPS_SEPARATOR);
		this.properties = PatternProvider.replaceAll(this.properties, SVNTeamAutoPropsPreferencePage.AUTO_PROPS_PROPS_SEPARATOR + "{2,}", SVNTeamAutoPropsPreferencePage.AUTO_PROPS_PROPS_SEPARATOR);
	}
	
	public String getFileName() {
		return this.fileName;
	}
	
	public String getProperties() {
		return this.properties;
	}

}
