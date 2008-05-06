/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.local;

import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.RepositoryResourceSelectionComposite;
import org.eclipse.team.svn.ui.panel.common.AbstractRepositoryResourceSelectionPanel;

/**
 * Replace With URL panel
 * 
 * @author Alexei Goncharov
 */
public class ReplaceWithUrlPanel extends AbstractRepositoryResourceSelectionPanel {

	public ReplaceWithUrlPanel(IRepositoryResource baseResource, long currentRevision) {
		super(baseResource, currentRevision, SVNTeamUIPlugin.instance().getResource("ReplaceUrlPanel.Title"), SVNTeamUIPlugin.instance().getResource("ReplaceUrlPanel.Selection.Description"), "replaceUrl", false, SVNTeamUIPlugin.instance().getResource("ReplaceUrlPanel.Selection.Title"), SVNTeamUIPlugin.instance().getResource("ReplaceUrlPanel.Description"), RepositoryResourceSelectionComposite.TEXT_BASE);
    	this.defaultMessage = SVNTeamUIPlugin.instance().getResource("ReplaceUrlPanel.Message");
	}
	
}
