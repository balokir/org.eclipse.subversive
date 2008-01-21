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

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * Add resources panel implementation
 * 
 * @author Alexander Gurov
 */
public class AddToSVNPanel extends AbstractResourceSelectionPanel {

    public AddToSVNPanel(IResource []resources) {
    	this(resources, null);
    }
    
    public AddToSVNPanel(IResource []resources, IResource []userSelectedResources) {
        super(resources, userSelectedResources, new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL});
        this.dialogTitle = SVNTeamUIPlugin.instance().getResource("AddToSVNPanel.Title");
        this.dialogDescription = SVNTeamUIPlugin.instance().getResource("AddToSVNPanel.Description");
        if (resources.length == 1) {
        	this.defaultMessage = SVNTeamUIPlugin.instance().getResource("AddToSVNPanel.Message.Single");
        }
        else {
        	this.defaultMessage = SVNTeamUIPlugin.instance().getResource("AddToSVNPanel.Message.Multi", new String[] {String.valueOf(resources.length)});
        }
    }
    
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.addToVCDialogContext";
    }

}
