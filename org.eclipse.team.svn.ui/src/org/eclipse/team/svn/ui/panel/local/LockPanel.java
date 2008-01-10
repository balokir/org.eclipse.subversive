/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.local;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.CommentComposite;
import org.eclipse.team.svn.ui.panel.common.CommentPanel;

/**
 * Lock options panel
 * 
 * @author Alexander Gurov
 */
public class LockPanel extends CommentPanel {
	protected Button forceButton;
	protected Button recursiveButton;
	
	protected boolean force;
	protected boolean recursive;
	protected boolean isFile;
	protected int minLockSize;
	
	
	public LockPanel(boolean isFile, int minLockSize) {
        super(SVNTeamUIPlugin.instance().getResource("LockPanel.Title"));
        this.isFile = isFile;
        this.minLockSize = minLockSize;
        this.dialogDescription = SVNTeamUIPlugin.instance().getResource("LockPanel.Description.Default");
    }
	
	public LockPanel(boolean isFile, boolean needsLock, int minLockSize) {
		this(isFile, minLockSize);
		if (needsLock) {
			 this.dialogDescription = SVNTeamUIPlugin.instance().getResource("LockPanel.Description.NeedsLock");
			 this.defaultMessage = SVNTeamUIPlugin.instance().getResource("LockPanel.Message.NeedsLock");
		}
	}
	
    public void createControlsImpl(Composite parent) {
    	GridData data = null;
    	GridLayout layout = null;

    	layout = new GridLayout();
		layout.marginHeight = 0;
		parent.setLayout(layout);
		
    	Group group = new Group(parent, SWT.NULL);
		group.setLayout(new GridLayout());
		data = new GridData(GridData.FILL_BOTH);
		group.setLayoutData(data);
		group.setText(SVNTeamUIPlugin.instance().getResource("LockPanel.Comment"));
    	this.comment = new CommentComposite(group, null, this, null, null, this.minLockSize);
		data = new GridData(GridData.FILL_BOTH);
		this.comment.setLayoutData(data);
		
		Composite forcePanel = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		forcePanel.setLayout(layout);
		
		this.forceButton = new Button(forcePanel, SWT.CHECK);
		this.forceButton.setText(SVNTeamUIPlugin.instance().getResource("LockPanel.Force"));
		this.forceButton.setSelection(false);
		
		if (!this.isFile) {
			this.recursiveButton = new Button(forcePanel, SWT.CHECK);
			this.recursiveButton.setText(SVNTeamUIPlugin.instance().getResource("LockPanel.Recursively"));
			this.recursiveButton.setSelection(false);
		}
    }
    
    public String getHelpId() {
    	return "org.eclipse.team.svn.help.lockDialogContext";
    }
    
    public Point getPrefferedSizeImpl() {
    	return new Point(525, SWT.DEFAULT);
    }
    
    public boolean getForce() {
    	return this.force;
    }
    
    public boolean isRecursive() {
    	return this.recursive;
    }
    
    protected void saveChangesImpl() {
    	super.saveChangesImpl();
    	this.force = this.forceButton.getSelection();
    	this.recursive = this.isFile ? false : this.recursiveButton.getSelection();
    }

}
