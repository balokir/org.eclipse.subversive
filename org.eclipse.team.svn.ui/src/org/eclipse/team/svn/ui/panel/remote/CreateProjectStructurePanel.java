/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.remote;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;

/**
 * Project structure creation input panel
 * 
 * @author Alexander Gurov
 */
public class CreateProjectStructurePanel extends AbstractGetResourceNamePanel {
	public CreateProjectStructurePanel() {
		super(SVNUIMessages.CreateProjectStructurePanel_Title, true);
        this.dialogDescription = SVNUIMessages.CreateProjectStructurePanel_Description;
	}

    public void createControlsImpl(Composite parent) {
        GridData data = null;
        GridLayout layout = null;

        Composite projectComposite = new Composite(parent, SWT.NONE);
        layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        projectComposite.setLayout(layout);
        data = new GridData(GridData.FILL_HORIZONTAL);        
        projectComposite.setLayoutData(data);
        
        Button monolythicButton = new Button(projectComposite, SWT.RADIO);
        data = new GridData(GridData.FILL_HORIZONTAL);        
        monolythicButton.setLayoutData(data);
        monolythicButton.setText(SVNUIMessages.CreateProjectStructurePanel_Monolythic);
        monolythicButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (((Button)e.widget).getSelection()) {
					CreateProjectStructurePanel.this.resourceName = ""; //$NON-NLS-1$
					CreateProjectStructurePanel.this.text.setEnabled(false);
				}
				CreateProjectStructurePanel.this.validateContent();
			}
		});
        monolythicButton.setSelection(false);
        
        Button otherButton = new Button(projectComposite, SWT.RADIO);
        data = new GridData(GridData.FILL_HORIZONTAL);        
        otherButton.setLayoutData(data);
        otherButton.setText(SVNUIMessages.CreateProjectStructurePanel_SingleOrMulti);
        otherButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (((Button)e.widget).getSelection()) {
					CreateProjectStructurePanel.this.resourceName = CreateProjectStructurePanel.this.text.getText();
					CreateProjectStructurePanel.this.text.setEnabled(true);
				}
				CreateProjectStructurePanel.this.validateContent();
			}
		});
        otherButton.setSelection(true);
        
    	super.createControlsImpl(projectComposite);
    	
    	this.text.setFocus();
    }
    
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.remote_createProjectStructureDialogContext"; //$NON-NLS-1$
	}
    
    protected AbstractVerifier createNonEmptyNameFieldVerifier() {
    	return new AbstractVerifierProxy(super.createNonEmptyNameFieldVerifier()) {
    		protected boolean isVerificationEnabled(Control input) {
    			return CreateProjectStructurePanel.this.text.isEnabled();
    		}
    	};
    }
    
}
