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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.operation.local.DiffViewerSettings.ExternalProgramParameters;
import org.eclipse.team.svn.core.operation.local.DiffViewerSettings.ResourceSpecificParameterKind;
import org.eclipse.team.svn.core.operation.local.DiffViewerSettings.ResourceSpecificParameters;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.DiffViewerExternalProgramComposite;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;

/**
 * Edit file associations with external compare editor panel
 * 
 * @author Igor Burilo
 */
public class EditFileAssociationsPanel extends AbstractDialogPanel {

	protected ResourceSpecificParameters param;
		
	protected Text extensionText;
	protected DiffViewerExternalProgramComposite diffExternalComposite;
	protected DiffViewerExternalProgramComposite mergeExternalComposite;
	
	public EditFileAssociationsPanel(ResourceSpecificParameters param) {
		this.param = param;
		
		this.dialogTitle = this.param == null ? SVNUIMessages.EditFileAssociationsPanel_AddDialogTitle : SVNUIMessages.EditFileAssociationsPanel_EditDialogTitle;
		this.dialogDescription = SVNUIMessages.EditFileAssociationsPanel_DialogDescription;
		this.defaultMessage = SVNUIMessages.EditFileAssociationsPanel_DialogDefaultMessage;
	}
	
	public ResourceSpecificParameters getResourceSpecificParameters() {
		return this.param;
	}
	
	protected void createControlsImpl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayout(layout);
		composite.setLayoutData(data);
		
		//extension or mime-type
		Label extensionLabel = new Label(composite, SWT.NONE);
		data = new GridData();
		extensionLabel.setLayoutData(data);
		extensionLabel.setText(SVNUIMessages.EditFileAssociationsPanel_ExtensionMimeType_Label);
		
		this.extensionText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		data = new GridData();
		data.widthHint = 100;
		this.extensionText.setLayoutData(data);
		
		this.diffExternalComposite = new DiffViewerExternalProgramComposite(SVNUIMessages.DiffViewerExternalProgramComposite_DiffProgramArguments_Label, composite, this);
		data = new GridData(GridData.FILL_HORIZONTAL);		
		data.horizontalSpan = 2;
		this.diffExternalComposite.setLayoutData(data);			
		
		this.mergeExternalComposite = new DiffViewerExternalProgramComposite(SVNUIMessages.DiffViewerExternalProgramComposite_MergeProgramArguments_Label, composite, this);
		data = new GridData(GridData.FILL_HORIZONTAL);		
		data.horizontalSpan = 2;
		this.mergeExternalComposite.setLayoutData(data);			
		
		this.attachTo(this.extensionText, new NonEmptyFieldVerifier(SVNUIMessages.EditFileAssociationsPanel_ExtensionMimeType_FieldName));
		
		//init value
		if (this.param != null) {
			if (this.param.kind.kindValue != null) {
				this.extensionText.setText(this.param.kind.formatKindValue());	
			}								
			this.diffExternalComposite.setProgramPath(this.param.params.diffProgramPath);
			this.diffExternalComposite.setProgramParameters(this.param.params.diffParamatersString);
			
			this.mergeExternalComposite.setProgramPath(this.param.params.mergeProgramPath);
			this.mergeExternalComposite.setProgramParameters(this.param.params.mergeParamatersString);
		}
	}		
	
	protected void saveChangesImpl() {
		String extensionStr = this.extensionText.getText();
		ResourceSpecificParameterKind kind = ResourceSpecificParameterKind.getKind(extensionStr);
		
		ExternalProgramParameters externalProgramParams = new ExternalProgramParameters(
				this.diffExternalComposite.getProgramPath(),
				this.mergeExternalComposite.getProgramPath(),
				this.diffExternalComposite.getProgramParameters(), 
				this.mergeExternalComposite.getProgramParameters());
				
		
		if (this.param == null) {
			this.param = new ResourceSpecificParameters(kind, externalProgramParams);
			this.param.isEnabled = true;
		} else {
			this.param.kind = kind;
			this.param.params = externalProgramParams;
		}					
	}
	
	protected void cancelChangesImpl() {
		this.param = null;		
	}

}
