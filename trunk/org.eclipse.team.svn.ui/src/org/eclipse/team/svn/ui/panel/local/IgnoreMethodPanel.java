/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.PatternVerifier;

/**
 * Ignore method selection panel
 * 
 * @author Alexander Gurov
 */
public class IgnoreMethodPanel extends AbstractDialogPanel {
	protected int ignoreType;
	protected String ignorePattern;
	protected IResource []resources;
	protected Button patternButton;
	
	protected Text ignorePatternField;

    public IgnoreMethodPanel(IResource []resources) {
        super();
        if (resources.length == 1) {
            this.dialogTitle = SVNUIMessages.IgnoreMethodPanel_Title_Single;
            this.dialogDescription = SVNUIMessages.IgnoreMethodPanel_Description_Single;
        }
        else {
            this.dialogTitle = SVNUIMessages.IgnoreMethodPanel_Title_Multi;
            this.dialogDescription = SVNUIMessages.IgnoreMethodPanel_Description_Multi;
        }
        this.defaultMessage = SVNUIMessages.IgnoreMethodPanel_Message;
        
		this.ignoreType = IRemoteStorage.IGNORE_NAME;
		this.ignorePattern = null;
		this.resources = resources;
    }

	public int getIgnoreType() {
		return this.ignoreType;
	}
	
	public String getIgnorePattern() {
		return this.ignorePattern;
	}

    public void createControlsImpl(Composite parent) {
        GridData data = null;

		Button nameButton = new Button(parent, SWT.RADIO);
		data = new GridData(GridData.FILL_HORIZONTAL);
		nameButton.setLayoutData(data);
		String text = SVNUIMessages.format(this.resources.length == 1 ? SVNUIMessages.IgnoreMethodPanel_Name_Single : SVNUIMessages.IgnoreMethodPanel_Name_Multi, new String[] {this.resources[0].getName()});
		nameButton.setText(text);
		nameButton.setSelection(true);
		nameButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
			    IgnoreMethodPanel.this.ignoreType = IRemoteStorage.IGNORE_NAME;
			    IgnoreMethodPanel.this.ignorePatternField.setEnabled(false);
			}
		});

		Button extensionButton = new Button(parent, SWT.RADIO);
		data = new GridData(GridData.FILL_HORIZONTAL);
		extensionButton.setLayoutData(data);
		String extension = null;
		for (int i = 0; i < this.resources.length; i++) {
		    if (extension == null) {
			    extension = this.resources[i].getFileExtension();
		    }
		    else {
		        break;
		    }
		}
		text = SVNUIMessages.format(this.resources.length == 1 ? SVNUIMessages.IgnoreMethodPanel_Extension_Single : SVNUIMessages.IgnoreMethodPanel_Extension_Multi, new String[] {extension == null ? "" : extension});
		extensionButton.setText(text);
		extensionButton.setSelection(false);
		extensionButton.setEnabled(extension != null);
		extensionButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
			    IgnoreMethodPanel.this.ignoreType = IRemoteStorage.IGNORE_EXTENSION;
			    IgnoreMethodPanel.this.ignorePatternField.setEnabled(false);
			}
		});
		
		this.patternButton = new Button(parent, SWT.RADIO);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.patternButton.setLayoutData(data);
		text = SVNUIMessages.format(SVNUIMessages.IgnoreMethodPanel_Pattern, new String[] {this.resources[0].getName().substring(1)});
		this.patternButton.setText(text);
		this.patternButton.setSelection(false);
		this.patternButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
			    IgnoreMethodPanel.this.ignoreType = IRemoteStorage.IGNORE_PATTERN;
			    IgnoreMethodPanel.this.ignorePatternField.setEnabled(true);
			}
		});
		
		this.ignorePatternField = new Text(parent, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.ignorePatternField.setLayoutData(data);
		this.ignorePatternField.setEnabled(false);
		this.ignorePatternField.setText(this.resources[0].getName());
		this.attachTo(this.ignorePatternField, new AbstractVerifierProxy(new PatternVerifier(SVNUIMessages.IgnoreMethodPanel_Pattern_Verifier, this.resources)) {
			protected boolean isVerificationEnabled(Control input) {
				return IgnoreMethodPanel.this.patternButton.getSelection();
			}
		});
    }
    
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.addToIgnoreDialogContext";
    }
    
	public Point getPrefferedSizeImpl() {
		return new Point(470, 130);
	}
    
    protected void saveChangesImpl() {
    	this.ignorePattern = this.patternButton.getSelection() ? this.ignorePatternField.getText() : null;
    }

    protected void cancelChangesImpl() {
    }
	
}
