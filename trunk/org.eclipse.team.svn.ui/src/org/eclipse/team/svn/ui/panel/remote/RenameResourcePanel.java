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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;


/**
 * Rename remote resource panel
 * 
 * @author Alexander Gurov
 */
public class RenameResourcePanel extends AbstractGetResourceNamePanel {
    public RenameResourcePanel(String originalName) {
        super(SVNTeamUIPlugin.instance().getResource("RenameResourcePanel.Title"), true);
        this.dialogDescription = SVNTeamUIPlugin.instance().getResource("RenameResourcePanel.Description");
        this.disallowedName = originalName;
    }

    public void createControls(Composite parent) {
    	super.createControls(parent);
    	
    	this.text.setText(this.disallowedName);
    	this.text.selectAll();
    }
    
}
