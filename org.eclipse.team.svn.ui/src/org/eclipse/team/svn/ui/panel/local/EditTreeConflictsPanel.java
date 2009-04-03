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

package org.eclipse.team.svn.ui.panel.local;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.team.svn.core.connector.SVNConflictDescriptor.Operation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;

/**
 * Edit tree conflicts panel
 * 
 * TODO add help 
 *  
 * @author Igor Burilo
 */
public class EditTreeConflictsPanel extends AbstractDialogPanel {

	protected ILocalResource local;
	protected EditTreeConflictsHelper helper;
	
	protected IActionOperation operation;
		
	protected Button localResolutionButton;
	protected Button remoteResolutionButton;
	protected Button manualResolutionButton;
	
	public EditTreeConflictsPanel(ILocalResource local) {		
		this.local = local;
		this.helper = new EditTreeConflictsHelper(this.local);
		
		//TODO make correct values
        this.dialogTitle = SVNUIMessages.EditTreeConflictsPanel_Title;
        this.dialogDescription = SVNUIMessages.EditTreeConflictsPanel_Description;
        this.defaultMessage = SVNUIMessages.EditTreeConflictsPanel_DefaultMessage;
	}

	protected void createControlsImpl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		layout.numColumns = 1;
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayout(layout);
		composite.setLayoutData(data);
		
		this.createConflictInfoControls(composite);
		this.createConflictResolutionControls(composite);				
	}
	
	protected void createConflictInfoControls(Composite parent) {		
		Group composite = new Group(parent, SWT.NULL);
		GridLayout layout = new GridLayout();	
		layout.numColumns = 3;
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayout(layout);
		composite.setLayoutData(data);
		composite.setText(SVNUIMessages.EditTreeConflictsPanel_ConlictInfo_Group);
		
		//operation
		Label label = new Label(composite, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText(SVNUIMessages.EditTreeConflictsPanel_Operation_Label);
		
		label = new Label(composite, SWT.NULL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		label.setText(this.helper.getOperationAsString());
		
		//local status
		label = new Label(composite, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText(SVNUIMessages.EditTreeConflictsPanel_LocalStatus_Label);
		
		label = new Label(composite, SWT.NULL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		label.setText(this.helper.getReasonAsString());
		
		//remote action
		label = new Label(composite, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText(SVNUIMessages.EditTreeConflictsPanel_IncomingAction_Label);
		
		label = new Label(composite, SWT.NULL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		label.setText(this.helper.getActionAsString());
		
		//srcLeft
		label = new Label(composite, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText(SVNUIMessages.EditTreeConflictsPanel_StartVersion_Label);
		
		label = new Label(composite, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText(SVNUIMessages.format(SVNUIMessages.EditTreeConflictsPanel_revision, String.valueOf(this.local.getTreeConflictDescriptor().srcLeftVersion.pegRevision)));
		
		if (this.local.getTreeConflictDescriptor().operation == Operation.MERGE) {
			Link leftLink = new Link(composite, SWT.NULL); 
			leftLink.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			leftLink.setText("<a>" + this.helper.getSrcUrl(true) + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$
			leftLink.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {			
					//TODO add impl
				}			
			});
		} else {
			label = new Label(composite, SWT.NULL);
			label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			label.setText(this.helper.getSrcUrl(true));
		}
						
		//srcRight
		label = new Label(composite, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText(SVNUIMessages.EditTreeConflictsPanel_EndRevision_Label);
		
		label = new Label(composite, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText(SVNUIMessages.format(SVNUIMessages.EditTreeConflictsPanel_revision, String.valueOf(this.local.getTreeConflictDescriptor().srcRightVersion.pegRevision)));
		
		Link rightLink = new Link(composite, SWT.NULL); 
		rightLink.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		rightLink.setText("<a>" + this.helper.getSrcUrl(false) + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$
		rightLink.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {			
				//TODO add impl.
			}			
		});
	}	
	
	protected void createConflictResolutionControls(Composite parent) {
		Group composite = new Group(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		//layout.marginHeight = layout.marginWidth = 0;
		layout.numColumns = 1;
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayout(layout);
		composite.setLayoutData(data);
		composite.setText(SVNUIMessages.EditTreeConflictsPanel_Conflict_Resolution_Group);
		
		//tips section
		String tip = this.helper.getTip();
		if (tip != null) {
			Label tipLabel = new Label(composite, SWT.NONE);
			tipLabel.setLayoutData(new GridData());		
			tipLabel.setText(SVNUIMessages.EditTreeConflictsPanel_Tips_Label);
			tipLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));	
			
			Label tipValue = new Label(composite, SWT.WRAP);
			tipValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			tipValue.setText(tip);
		}
		
		this.localResolutionButton = new Button(composite, SWT.RADIO);
		this.localResolutionButton.setLayoutData(new GridData());
		this.localResolutionButton.setText(SVNUIMessages.EditTreeConflictsPanel_ApplyLocalChanges_Resolution);
		
		this.remoteResolutionButton = new Button(composite, SWT.RADIO);
		this.remoteResolutionButton.setLayoutData(new GridData());
		this.remoteResolutionButton.setText(SVNUIMessages.EditTreeConflictsPanel_ApplyIncomigChanges_Resolution);
		
		this.manualResolutionButton = new Button(composite, SWT.RADIO);
		this.manualResolutionButton.setLayoutData(new GridData());
		this.manualResolutionButton.setText(SVNUIMessages.EditTreeConflictsPanel_ManualResolution);
		this.manualResolutionButton.setSelection(true);			
	}					
	
	protected void saveChangesImpl() {
		this.operation = this.helper.getOperation(this.remoteResolutionButton.getSelection(), this.localResolutionButton.getSelection());		
	}		
			
	protected void cancelChangesImpl() {
		this.operation = null;
	}	
	
	public IActionOperation getOperation() {
		return this.operation;
	}	
}
