/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Takashi Okamoto - [patch] Fix bugtraq integration bug (when bug id is empty)
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.CommentComposite;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.properties.bugtraq.BugtraqModel;

/**
 * Operation comment panel
 * 
 * @author Alexander Gurov
 */
public class CommentPanel extends AbstractDialogPanel {
	protected CommentComposite comment;
	protected BugtraqModel bugtraqModel;

    public CommentPanel(String title) {
        super();
        this.dialogTitle = title;
        this.dialogDescription = SVNTeamUIPlugin.instance().getResource("CommentPanel.Description");
        this.defaultMessage = SVNTeamUIPlugin.instance().getResource("CommentPanel.Message");
    }
    
    public CommentPanel(String title, BugtraqModel bugtraqModel) {
        this(title);
        this.bugtraqModel = bugtraqModel;
    }

	public String getMessage() {
		return this.appendBugtraqMessage(this.comment.getMessage());
	}
	
    public void createControlsImpl(Composite parent) {
        this.comment = new CommentComposite(parent, this);
		GridData data = new GridData(GridData.FILL_BOTH);
		this.comment.setLayoutData(data);
    }
    
    protected Point getPrefferedSizeImpl() {
    	return new Point(510, SWT.DEFAULT);
    }
    
    public void postInit() {
    	super.postInit();
    	this.comment.postInit(this.manager);
    }
    
    protected void saveChangesImpl() {
        this.comment.saveChanges();
    }

    protected void cancelChangesImpl() {
    	this.comment.cancelChanges();
    }
    
    protected String appendBugtraqMessage(String message) {
    	String bugtraqMessage;
		if (this.bugtraqModel != null && (bugtraqMessage = this.bugtraqModel.getMessage()) != null) {
			String addString = this.comment.getBugID();
			if (addString != null && addString.trim().length() > 0) {
				bugtraqMessage = bugtraqMessage.replaceFirst(BugtraqModel.BUG_ID, addString);
				if (this.bugtraqModel.isAppend()) {
					message += "\n" + bugtraqMessage;
				}
				else {
					message = bugtraqMessage + "\n" + message;
				}
			}
		}
		return message;
    }

}
