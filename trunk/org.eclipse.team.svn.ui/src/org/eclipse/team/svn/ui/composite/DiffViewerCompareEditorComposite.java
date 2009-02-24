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
import org.eclipse.team.svn.core.operation.local.DiffViewerSettings.ExternalProgramParameters;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.verifier.AbstractValidationManagerProxy;
import org.eclipse.team.svn.ui.verifier.IValidationManager;

/**
 * Provide choise for Eclipse's or external compare editors
 * 
 * @author Igor Burilo
 */
public class DiffViewerCompareEditorComposite extends Composite {

	protected Button externalEditorButton;
	protected boolean isExternalDefaultCompare;
	protected DiffViewerExternalProgramComposite diffExternalComposite;	
	protected DiffViewerExternalProgramComposite mergeExternalComposite;
	
	protected IValidationManager validationManager;
	
	public DiffViewerCompareEditorComposite(Composite parent, IValidationManager validationManager) {
		super(parent, SWT.NONE);
		this.validationManager = validationManager;
		
		this.createControls();
	}

	public void initializeControls() {		
		this.externalEditorButton.setSelection(this.isExternalDefaultCompare);
								
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
		
		this.externalEditorButton = new Button(this, SWT.CHECK);
		data = new GridData();
		this.externalEditorButton.setLayoutData(data);
		this.externalEditorButton.setText(SVNUIMessages.DiffViewerCompareEditorComposite_ExternalCompareEditor);
				
		this.diffExternalComposite = new DiffViewerExternalProgramComposite(SVNUIMessages.DiffViewerExternalProgramComposite_DiffProgramArguments_Label, this, new AbstractValidationManagerProxy(this.validationManager) {
			protected boolean isVerificationEnabled(Control input) {
				return DiffViewerCompareEditorComposite.this.externalEditorButton.getSelection();				
			}					
		});		
		
		this.mergeExternalComposite = new DiffViewerExternalProgramComposite(SVNUIMessages.DiffViewerExternalProgramComposite_MergeProgramArguments_Label, this, new AbstractValidationManagerProxy(this.validationManager) {
			protected boolean isVerificationEnabled(Control input) {
				//return DiffViewerCompareEditorComposite.this.externalEditorButton.getSelection();
				return false;
			}					
		});	
	
		//handlers
			
		Listener editorButtonListener = new Listener() {
			public void handleEvent(Event event) {
				DiffViewerCompareEditorComposite.this.isExternalDefaultCompare = DiffViewerCompareEditorComposite.this.externalEditorButton.getSelection();
				
				DiffViewerCompareEditorComposite.this.enableExternalCompositeControls();
				DiffViewerCompareEditorComposite.this.validationManager.validateContent();
			}			
		};
				
		this.externalEditorButton.addListener(SWT.Selection, editorButtonListener);
	}
	
	protected void enableExternalCompositeControls() {
		this.diffExternalComposite.setEnabled(this.externalEditorButton.getSelection());
		this.mergeExternalComposite.setEnabled(this.externalEditorButton.getSelection());
	}
	
	public boolean isExternalDefaultCompare() {
		return this.isExternalDefaultCompare;
	}

	public void setExternalDefaultCompare(boolean isExternalDefaultCompare) {
		this.isExternalDefaultCompare = isExternalDefaultCompare;
	}
	
	public void setExternalProgramParameters(ExternalProgramParameters programParams) {
		this.diffExternalComposite.setProgramPath(programParams != null ? programParams.diffProgramPath : null);
		this.diffExternalComposite.setProgramParameters(programParams != null ? programParams.diffParamatersString : null);
		
		this.mergeExternalComposite.setProgramPath(programParams != null ? programParams.mergeProgramPath : null);
		this.mergeExternalComposite.setProgramParameters(programParams != null ? programParams.mergeParamatersString : null);
	}
	
	public ExternalProgramParameters getExternalProgramParameters() {
		ExternalProgramParameters params = new ExternalProgramParameters(
				this.diffExternalComposite.getProgramPath(),
				this.mergeExternalComposite.getProgramPath(),
				this.diffExternalComposite.getProgramParameters(),
				this.mergeExternalComposite.getProgramParameters()
				);
		return params;
	}
}
