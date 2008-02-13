/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.remote;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.CommentComposite;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.verifier.ExistingResourceVerifier;

/**
 * Import Panel
 * 
 * @author Sergiy Logvin
 */
public class ImportPanel extends AbstractDialogPanel {
	protected Text locationField;
	protected String location;
	protected Button recursiveButton;
	protected boolean isRecursive;
	protected CommentComposite comment;
	
	public ImportPanel(String importToUrl) {
		super();
		this.dialogTitle = SVNTeamUIPlugin.instance().getResource("ImportPanel.Title");
		this.dialogDescription = SVNTeamUIPlugin.instance().getResource("ImportPanel.Description");
		this.defaultMessage = SVNTeamUIPlugin.instance().getResource("ImportPanel.Message", new String[] {importToUrl});
		this.isRecursive = true;
    }
	
	public void createControlsImpl(Composite parent) {
		GridLayout layout = null;
		GridData data = null;
		
		Group group = new Group(parent, SWT.NULL);
		layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(data);
		group.setText(SVNTeamUIPlugin.instance().getResource("ImportPanel.Folder"));
		
		this.locationField = new Text(group,  SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.locationField.setLayoutData(data);
		this.attachTo(this.locationField, new ExistingResourceVerifier(SVNTeamUIPlugin.instance().getResource("ImportPanel.Folder.Verifier"), false));
		
		Button browseButton = new Button(group, SWT.PUSH);
		browseButton.setText(SVNTeamUIPlugin.instance().getResource("Button.Browse"));
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(browseButton);
		browseButton.setLayoutData(data);
		browseButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				DirectoryDialog fileDialog = new DirectoryDialog(ImportPanel.this.manager.getShell());
				fileDialog.setText(SVNTeamUIPlugin.instance().getResource("ImportPanel.ImportFolder"));
				fileDialog.setMessage(SVNTeamUIPlugin.instance().getResource("ImportPanel.ImportFolder.Msg"));
				String path = fileDialog.open();
				if (path != null) {
					ImportPanel.this.locationField.setText(path);
				}
			}
		});
		
		this.recursiveButton = new Button(group, SWT.CHECK);
		this.recursiveButton.setText(SVNTeamUIPlugin.instance().getResource("ImportPanel.Recursively"));
		this.recursiveButton.setSelection(true);
		
		group = new Group(parent, SWT.NULL);
		group.setLayout(new GridLayout());
		data = new GridData(GridData.FILL_BOTH);
		group.setLayoutData(data);
		group.setText(SVNTeamUIPlugin.instance().getResource("ImportPanel.Comment"));
		
		this.comment = new CommentComposite(group, this);
		data = new GridData(GridData.FILL_BOTH);
		this.comment.setLayoutData(data);
    }
	
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.remote_importDialogContext";
	}
	
    public Point getPrefferedSizeImpl() {
    	return new Point(525, SWT.DEFAULT);
    }
    
	public void postInit() {
		super.postInit();
		this.comment.postInit(this.manager);
	}
		
	protected void saveChangesImpl() {
		this.location = this.locationField.getText();
		this.comment.saveChanges();
		this.isRecursive = this.recursiveButton.getSelection();
	}

    protected void cancelChangesImpl() {
    	this.comment.cancelChanges();
    }
    
    public String getLocation() {
    	return this.location;
    }
    
    public String getMessage() {
    	return this.comment.getMessage();
    }
    
    public boolean isRecursive() {
    	return this.isRecursive;
    }
    
}
