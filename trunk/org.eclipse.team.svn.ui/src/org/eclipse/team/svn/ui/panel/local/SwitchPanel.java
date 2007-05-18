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

import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.panel.common.AbstractRepositoryResourceSelectionPanel;

/**
 * Switch panel implementation
 * 
 * @author Alexander Gurov
 */
public class SwitchPanel extends AbstractRepositoryResourceSelectionPanel {
    public SwitchPanel(IRepositoryResource baseResource, long currentRevision) {    	
    	super(baseResource, currentRevision, SVNTeamUIPlugin.instance().getResource("SwitchPanel.Title"), SVNTeamUIPlugin.instance().getResource("SwitchPanel.Description"), "SwitchPanel.URL_HISTORY_NAME", false, SVNTeamUIPlugin.instance().getResource("SwitchPanel.Selection.Title"), SVNTeamUIPlugin.instance().getResource("SwitchPanel.Selection.Description"));
    	this.defaultMessage = SVNTeamUIPlugin.instance().getResource("SwitchPanel.Message");
    }

}
