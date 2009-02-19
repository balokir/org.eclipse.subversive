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

package org.eclipse.team.svn.ui.panel.common;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;

/**
 * Pre-defined variables selection panel for diff viewer's external program 
 * 
 * @author Igor Burilo
 */
public class DiffViewerVariablesPanel extends AbstractDialogPanel {

	protected List variablesList;
	protected Text variableDescription;
	
	protected Map<String, String> variablesMap = new LinkedHashMap<String, String>();
	protected String variable;
	
	public DiffViewerVariablesPanel() {
		this.dialogTitle = SVNUIMessages.DiffViewerVariablesPanel_DialogTitle;
		this.dialogDescription = SVNUIMessages.DiffViewerVariablesPanel_DialogDescription;
		this.defaultMessage = SVNUIMessages.DiffViewerVariablesPanel_DialogDefaultMessage;
					
		//TODO correct descriptions and variables.
		this.variablesMap.put("base", SVNUIMessages.DiffViewerVariablesPanel_BaseVariable_Description); //$NON-NLS-1$
		this.variablesMap.put("mine", SVNUIMessages.DiffViewerVariablesPanel_MineVariable_Description); //$NON-NLS-1$
		this.variablesMap.put("theirs", SVNUIMessages.DiffViewerVariablesPanel_TheirsVariable_Description); //$NON-NLS-1$
		this.variablesMap.put("merged", SVNUIMessages.DiffViewerVariablesPanel_MergedVariable_Description); //$NON-NLS-1$
		this.variablesMap.put("default-doc-program", SVNUIMessages.DiffViewerVariablesPanel_DefaultDocVariable_Description); //$NON-NLS-1$
		this.variablesMap.put("default-xls-program", SVNUIMessages.DiffViewerVariablesPanel_DefaultXLSVariable_Description); //$NON-NLS-1$
		this.variablesMap.put("default-ppt-program", SVNUIMessages.DiffViewerVariablesPanel_DefaultPptVariable_Description); //$NON-NLS-1$
	}
	
	protected void createControlsImpl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		layout.numColumns = 1;
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayout(layout);
		composite.setLayoutData(data);
		
		this.variablesList = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = DefaultDialog.convertHeightInCharsToPixels(this.variablesList, 10);
		this.variablesList.setLayoutData(data);
		
		Label variableDescriptionLabel = new Label(composite, SWT.NONE);
		data = new GridData();
		variableDescriptionLabel.setLayoutData(data);
		variableDescriptionLabel.setText(SVNUIMessages.DiffViewerVariablesPanel_VariableDescriptionLabel);
		
		this.variableDescription = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = DefaultDialog.convertHeightInCharsToPixels(this.variableDescription, 4);
		this.variableDescription.setLayoutData(data);
		this.variableDescription.setBackground(variableDescription.getBackground());
		this.variableDescription.setEditable(false);
		
		//handlers
		
		this.variablesList.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				String variableName = DiffViewerVariablesPanel.this.getSelectedVariable();
				if (variableName != null) {
					//init description
					DiffViewerVariablesPanel.this.variableDescription.setText(DiffViewerVariablesPanel.this.variablesMap.get(variableName));
					
					//run validation
					DiffViewerVariablesPanel.this.validateContent();					
				}					
			}			
		});
		
		this.initializeControls();
	}
	
	protected String getSelectedVariable() {
		String variable = null; 
		String[] selected = DiffViewerVariablesPanel.this.variablesList.getSelection();
		if (selected.length > 0) {
			variable = selected[0];
		}
		return variable;
	}
	
	protected void initializeControls() {
		String firstVariable = null;
		for (String variableName : this.variablesMap.keySet()) {
			this.variablesList.add(variableName);
			if (firstVariable == null) {
				firstVariable = variableName;
			}
		}		
		this.variablesList.select(0);
		this.variableDescription.setText(this.variablesMap.get(firstVariable));
	}
	
	protected void saveChangesImpl() {
		String var = this.getSelectedVariable();
		this.variable = var != null ? ("\"${" + var + "}\"") : ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	protected void cancelChangesImpl() {
		this.variable = null;
	}

	public String getVariable() {
		return this.variable;
	}

}
