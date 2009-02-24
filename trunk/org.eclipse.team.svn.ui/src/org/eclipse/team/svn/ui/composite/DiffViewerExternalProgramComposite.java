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

package org.eclipse.team.svn.ui.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.common.DiffViewerVariablesPanel;
import org.eclipse.team.svn.ui.verifier.IValidationManager;

/**
 * Allow to set external program path and arguments
 * 
 * @author Igor Burilo
 */
public class DiffViewerExternalProgramComposite extends Composite {

	protected IValidationManager validationManager;
	protected String groupLabel;
	
	protected List<Control> controls = new ArrayList<Control>();
	protected PathSelectionComposite pathComposite;
	protected Text parametersText;
	protected String programParameters;
	
	public DiffViewerExternalProgramComposite(String groupLabel, Composite parent, IValidationManager validationManager) {
		super(parent, SWT.NONE);
		this.groupLabel = groupLabel;
		this.validationManager = validationManager;
		this.createControls();			
	}
	
	public void setProgramPath(String programPath) {
		if (programPath != null) {
			this.pathComposite.setSelectedPath(programPath);	
		}		
	}
	
	public String getProgramPath() {
		return this.pathComposite.getSelectedPath();
	}

	public String getProgramParameters() {
		return this.programParameters;
	}
	
	public void setProgramParameters(String programParameters) {
		if (programParameters != null) {
			this.parametersText.setText(programParameters);	
		}				
	}		
	
	protected void createControls() {
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		this.setLayout(layout);
		this.setLayoutData(data);
		
		Group parametersGroup = new Group(this, SWT.NULL);
		layout = new GridLayout();
		layout.numColumns = 1;
		data = new GridData(GridData.FILL_HORIZONTAL);
		parametersGroup.setLayout(layout);
		parametersGroup.setLayoutData(data);
		parametersGroup.setText(this.groupLabel);	
		
		//program path			
		this.pathComposite = new PathSelectionComposite(
			SVNUIMessages.DiffViewerExternalProgramComposite_Path_LabelName,
			SVNUIMessages.DiffViewerExternalProgramComposite_Path_FieldName,
			SVNUIMessages.DiffViewerExternalProgramComposite_Path_BrowseDialogTitle,
			null,
			false,
			parametersGroup,
			this.validationManager);
		this.controls.add(this.pathComposite);						
		
		//parameters							
		this.parametersText = new Text(parametersGroup, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);
		data = new GridData(GridData.FILL_BOTH);		
		data.heightHint = DefaultDialog.convertHeightInCharsToPixels(this.parametersText, 5);
		this.parametersText.setLayoutData(data);
		this.controls.add(this.parametersText);
		
		Button variablesButton = new Button(parametersGroup, SWT.PUSH);
		variablesButton.setText(SVNUIMessages.DiffViewerExternalProgramComposite_Variables_Button);
		data = new GridData();
		data.horizontalAlignment = SWT.RIGHT;
		data.widthHint = DefaultDialog.computeButtonWidth(variablesButton);
		variablesButton.setLayoutData(data);			
		this.controls.add(variablesButton);
		
		//handlers
		
		variablesButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				DiffViewerVariablesPanel panel = new DiffViewerVariablesPanel();
				DefaultDialog dlg = new DefaultDialog(DiffViewerExternalProgramComposite.this.getShell(), panel);
				if (dlg.open() == 0) {
					String variable = panel.getVariable();
					DiffViewerExternalProgramComposite.this.parametersText.insert(variable);					
				}
			}				
		});
		
		this.parametersText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				DiffViewerExternalProgramComposite.this.programParameters = ((Text) e.widget).getText();
			}				
		});
	}
			
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		for (Control control : this.controls) {
			control.setEnabled(enabled);
		}
	}
}
