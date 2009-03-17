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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.IValidationManager;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.ResourcePathVerifier;

/**
 * Contain a flag which determines whether to generate diff file and
 * set path to it
 * 
 * @author Igor Burilo
 */
public class DiffFormatComposite extends Composite {

	protected Button generateUDiffCheckbox;
	protected Text uDiffPath;	
	protected Button browseButton;
	protected String diffFile;
	
	protected IValidationManager validationManager;
	
	public DiffFormatComposite(Composite parent, IValidationManager validationManager) {
		super(parent, SWT.NONE);
		this.validationManager = validationManager;
		this.createControls();
	}

	public String getDiffFile() {
		return this.diffFile;
	}
	
	protected void createControls() {			
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		this.setLayout(layout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		this.setLayoutData(data);
		
		this.generateUDiffCheckbox = new Button(this, SWT.CHECK);			
		this.generateUDiffCheckbox.setText(SVNUIMessages.DiffFormatComposite_GenerateDiffFile_Message);
		data = new GridData();
		this.generateUDiffCheckbox.setLayoutData(data);		
		
		this.uDiffPath = new Text(this, SWT.SINGLE | SWT.BORDER);		
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.uDiffPath.setLayoutData(data);				
				
		this.browseButton = new Button(this, SWT.PUSH);
		this.browseButton.setText(SVNUIMessages.Button_Browse);
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(this.browseButton);
		this.browseButton.setLayoutData(data);
		
		//validation
		String name = SVNUIMessages.DiffFormatComposite_DiffFile_Name;
		CompositeVerifier cVerifier = new CompositeVerifier();
		cVerifier.add(new NonEmptyFieldVerifier(name));
		cVerifier.add(new ResourcePathVerifier(name));			
		this.validationManager.attachTo(this.uDiffPath, new AbstractVerifierProxy(cVerifier) {				
			protected boolean isVerificationEnabled(Control input) {
				return DiffFormatComposite.this.generateUDiffCheckbox.getSelection();
			}				
		});
					
		//event handlers:			
		
		this.generateUDiffCheckbox.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				DiffFormatComposite.this.validationManager.validateContent();					
				DiffFormatComposite.this.setEnablement();			
			}
		});
											
		this.browseButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				FileDialog dlg = new FileDialog(DiffFormatComposite.this.getShell(), SWT.PRIMARY_MODAL | SWT.SAVE);
				dlg.setFilterExtensions(new String[] {"*.diff", "*.*"}); //$NON-NLS-1$ //$NON-NLS-2$
				dlg.setText(SVNUIMessages.DiffFormatComposite_SaveDiffFileAs);
				String file = dlg.open();
				if (file != null) {
					DiffFormatComposite.this.uDiffPath.setText(file);
				}
			}
		});	
		
		this.uDiffPath.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				DiffFormatComposite.this.diffFile = ((Text) e.widget).getText();
			}			
		});
		
		//set init values and run enablement
		this.generateUDiffCheckbox.setSelection(false);
		this.setEnablement();			
	}
	
	protected void setEnablement() {
		boolean enabled = this.generateUDiffCheckbox.getSelection();
		DiffFormatComposite.this.uDiffPath.setEnabled(enabled);
		DiffFormatComposite.this.browseButton.setEnabled(enabled);								
	}
}
