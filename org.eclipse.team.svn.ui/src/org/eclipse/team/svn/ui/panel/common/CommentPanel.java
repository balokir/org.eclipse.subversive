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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.CommentComposite;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
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
        this.dialogDescription = SVNUIMessages.CommentPanel_Description;
        this.defaultMessage = SVNUIMessages.CommentPanel_Message;
    }
    
    public CommentPanel(String title, BugtraqModel bugtraqModel) {
        this(title);
        this.bugtraqModel = bugtraqModel;
    }

	public String getMessage() {
		return this.appendBugtraqMessage(this.comment.getMessage());
	}
	
	public void setMessage(String message) {
		this.comment.setMessage(message);
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
		
    	if (!SVNTeamPreferences.getCommentTemplatesBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.COMMENT_USE_SHIFT_ENTER_NAME)) {
			// disallow the default SHIFT+Enter combination, it will cause the minor visual changes - 
			//	the enter button won't be 'default' anymore (at least in visual aspect), still it could be hit from the keyboard with the Ctrl+Enter combination
			//	there was one other way around - to override everything related to the button instantiation and then just skip selection event with the Shift modifier, 
			//	but there then will be a lose of focus from the current control which will be severely annoying, so it'll be better without the visual aspects of a "default button" anyway
			//	resetting the defaultButton flag is wrong since selectionEvent listener is not the only thing related to the default buttons
	    	//	and that is why the least invasive method is to reset default button for the commit comment panel only (multiline editors)
			Shell shell = this.parent.getShell();
			if (shell != null) {
				// Attention! It is important to do it twice, if you're interested why - just check the shell.setDefaultButton() code
				shell.setDefaultButton(null);
				shell.setDefaultButton(null);
			}
    	}
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
				bugtraqMessage = bugtraqMessage.replaceAll(BugtraqModel.BUG_ID, addString);
				if (this.bugtraqModel.isAppend()) {
					message += "\n" + bugtraqMessage; //$NON-NLS-1$
				}
				else {
					message = bugtraqMessage + "\n" + message; //$NON-NLS-1$
				}
			}
		}
		return message;
    }

}
