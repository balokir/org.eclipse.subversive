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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.verifier.AbstractValidationManagerProxy;
import org.eclipse.team.svn.ui.verifier.IValidationManager;

/**
 * Provide choise for Eclipse's or external compare editors
 * 
 * @author Igor Burilo
 */
public class DiffViewerCompareEditorComposite extends Composite {

	protected Button eclipseEditorButton;
	protected Button externalEditorButton;
	protected boolean isExternalDefaultCompare;
	protected DiffViewerExternalProgramComposite externalComposite;	
	
	protected IValidationManager validationManager;
	
	public DiffViewerCompareEditorComposite(Composite parent, IValidationManager validationManager) {
		super(parent, SWT.NONE);
		this.validationManager = validationManager;
		
		this.createControls();
	}

	public void initializeControls() {		
		this.externalEditorButton.setSelection(this.isExternalDefaultCompare);			
		this.eclipseEditorButton.setSelection(!this.isExternalDefaultCompare);
								
		this.enableExternalCompositeControls();		
	}
	
	protected void createControls() {		
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 5;
		layout.numColumns = 1;
		GridData data = new GridData(GridData.FILL_BOTH);
		this.setLayout(layout);
		this.setLayoutData(data);
		
		Label descriptionLabel = new Label(this, SWT.NONE);
		data = new GridData();
		descriptionLabel.setLayoutData(data);
		descriptionLabel.setText(SVNUIMessages.DiffViewerCompareEditorComposite_Description);		
		
		this.eclipseEditorButton = new Button(this, SWT.RADIO);
		data = new GridData();
		this.eclipseEditorButton.setLayoutData(data);
		this.eclipseEditorButton.setText(SVNUIMessages.DiffViewerCompareEditorComposite_EclipseCompareEditor);		
		
		this.externalEditorButton = new Button(this, SWT.RADIO);
		data = new GridData();
		this.externalEditorButton.setLayoutData(data);
		this.externalEditorButton.setText(SVNUIMessages.DiffViewerCompareEditorComposite_ExternalCompareEditor);
				
		this.externalComposite = new DiffViewerExternalProgramComposite(this, new AbstractValidationManagerProxy(this.validationManager) {
			protected boolean isVerificationEnabled(Control input) {
				return DiffViewerCompareEditorComposite.this.externalEditorButton.getSelection();	
			}					
		});
		data = (GridData) this.externalComposite.getLayoutData();
		data.horizontalIndent = 20;		
	
		//handlers
			
		Listener editorButtonListener = new Listener() {
			public void handleEvent(Event event) {
				DiffViewerCompareEditorComposite.this.isExternalDefaultCompare = DiffViewerCompareEditorComposite.this.externalEditorButton.getSelection();
				
				DiffViewerCompareEditorComposite.this.enableExternalCompositeControls();
				DiffViewerCompareEditorComposite.this.validationManager.validateContent();
			}			
		};
		
		this.eclipseEditorButton.addListener(SWT.Selection, editorButtonListener);
		this.externalEditorButton.addListener(SWT.Selection, editorButtonListener);
	}
	
	protected void enableExternalCompositeControls() {
		this.externalComposite.setEnabled(this.externalEditorButton.getSelection());
	}
	
	public boolean isExternalDefaultCompare() {
		return this.isExternalDefaultCompare;
	}

	public void setExternalDefaultCompare(boolean isExternalDefaultCompare) {
		this.isExternalDefaultCompare = isExternalDefaultCompare;
	}
	
	public void setProgramParameters(String programParameters) {
		this.externalComposite.setProgramParameters(programParameters);
	}
	
	public String getProgramParameters() {
		return this.externalComposite.getProgramParameters();
	}
	
	public void setProgramPath(String programPath) {
		this.externalComposite.setProgramPath(programPath);
	}
	
	public String getProgramPath() {
		return this.externalComposite.getProgramPath();
	}
}
