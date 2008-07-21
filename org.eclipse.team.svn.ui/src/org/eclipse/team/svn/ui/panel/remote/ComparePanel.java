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

package org.eclipse.team.svn.ui.panel.remote;

import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.RepositoryResourceSelectionComposite;
import org.eclipse.team.svn.ui.panel.common.AbstractRepositoryResourceSelectionPanel;

/**
 * Compare operation repository resource selection panel
 * 
 * @author Alexander Gurov
 */
public class ComparePanel extends AbstractRepositoryResourceSelectionPanel {
    public ComparePanel(IRepositoryResource baseResource) {    	
    	super(baseResource, SVNRevision.INVALID_REVISION_NUMBER, SVNTeamUIPlugin.instance().getResource("ComparePanel.Title"), SVNTeamUIPlugin.instance().getResource("ComparePanel.Description"), "compareUrl", SVNTeamUIPlugin.instance().getResource("ComparePanel.Selection.Title"), SVNTeamUIPlugin.instance().getResource("ComparePanel.Selection.Description"), RepositoryResourceSelectionComposite.TEXT_BASE);
    	this.defaultMessage = SVNTeamUIPlugin.instance().getResource("ComparePanel.Message");
    }
    
    public ComparePanel(IRepositoryResource baseResource, long revision) {    	
    	super(baseResource, revision, SVNTeamUIPlugin.instance().getResource("ComparePanel.Title"), SVNTeamUIPlugin.instance().getResource("ComparePanel.Description"), "compareUrl", SVNTeamUIPlugin.instance().getResource("ComparePanel.Selection.Title"), SVNTeamUIPlugin.instance().getResource("ComparePanel.Selection.Description"), RepositoryResourceSelectionComposite.TEXT_BASE);
    	this.defaultMessage = SVNTeamUIPlugin.instance().getResource("ComparePanel.Message");
    }
    
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.remote_compareDialogContext";
	}

}
