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

package org.eclipse.team.svn.ui.panel.remote;

import org.eclipse.team.svn.ui.SVNTeamUIPlugin;


/**
 * Create remote folder panel
 * 
 * @author Alexander Gurov
 */
public class CreateFolderPanel extends AbstractGetResourceNamePanel {

    public CreateFolderPanel() {
        super(SVNTeamUIPlugin.instance().getResource("CreateFolderPanel.Title"), true);
        this.dialogDescription = SVNTeamUIPlugin.instance().getResource("CreateFolderPanel.Description");
    }
    
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.remote_createFolderDialogContext";
	}
    
}
