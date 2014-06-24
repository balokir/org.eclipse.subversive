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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.IValidationManager;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.ResourcePathVerifier;

/**
 * Path selection composite
 * Contain path label and text input with browse button 
 * Allow to select either file or directory
 * 
 * @author Igor Burilo
 */
public class PathSelectionComposite extends Composite {

	protected IValidationManager validationManager;
			
	protected boolean isDirectorySelection;
	protected String pathLabelName;
	protected String pathFieldName;
	protected String browseDialogTitle;
	protected String browseDialogDescription;
	
	protected Text pathInput;
	protected String selectedPath;
	protected List<Control> controls = new ArrayList<Control>();
	
	public PathSelectionComposite(String pathLabelName, String pathFieldName, String browseDialogTitle, String browseDialogDescription, boolean isDirectorySelection, Composite parent, IValidationManager validationManager) {
		super(parent, SWT.NONE);
		this.isDirectorySelection = isDirectorySelection;
		this.validationManager = validationManager;
		this.pathLabelName = pathLabelName;
		this.pathFieldName = pathFieldName;
		this.browseDialogTitle = browseDialogTitle;
		this.browseDialogDescription = browseDialogDescription;
		
		this.createControls();
	}
	
	public void setSelectedPath(String selectedPath) {
		if (selectedPath != null) {
			this.pathInput.setText(selectedPath);	
		}		
	}
	
	public String getSelectedPath() {
		return this.selectedPath;
	}
	
	protected void createControls() {
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0; 
		layout.marginWidth = 0;			
		layout.numColumns = 3;
		this.setLayout(layout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		this.setLayoutData(data);
		
		Label pathLabel = new Label(this, SWT.NONE);
		data = new GridData();
		pathLabel.setLayoutData(data);
		pathLabel.setText(this.pathLabelName);
		this.controls.add(pathLabel);
		
		this.pathInput = new Text(this, SWT.BORDER | SWT.SINGLE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.pathInput.setLayoutData(data);						
		this.controls.add(this.pathInput);
		
		Button browseButton = new Button(this, SWT.PUSH);			
		browseButton.setText(SVNUIMessages.Button_Browse);
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(browseButton);
		browseButton.setLayoutData(data);	
		this.controls.add(browseButton);
		
		//validation
		String name = this.pathFieldName;
		CompositeVerifier cVerifier = new CompositeVerifier();
		cVerifier.add(new NonEmptyFieldVerifier(name));
		cVerifier.add(new ResourcePathVerifier(name));			
		this.validationManager.attachTo(this.pathInput, cVerifier);
		
		this.pathInput.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				PathSelectionComposite.this.selectedPath = PathSelectionComposite.this.pathInput.getText();
			}			
		});
		
		browseButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (PathSelectionComposite.this.isDirectorySelection) {
					DirectoryDialog dlg = new DirectoryDialog(PathSelectionComposite.this.getShell());
					dlg.setText(PathSelectionComposite.this.browseDialogTitle);
					dlg.setMessage(PathSelectionComposite.this.browseDialogDescription);
					String path = dlg.open();
					if (path != null) {
						PathSelectionComposite.this.pathInput.setText(path);
					}	
				} else {
					FileDialog dlg = new FileDialog(PathSelectionComposite.this.getShell());					
					dlg.setText(PathSelectionComposite.this.browseDialogTitle);					
					String path = dlg.open();
					if (path != null) {
						PathSelectionComposite.this.pathInput.setText(path);
					}	
				}
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
