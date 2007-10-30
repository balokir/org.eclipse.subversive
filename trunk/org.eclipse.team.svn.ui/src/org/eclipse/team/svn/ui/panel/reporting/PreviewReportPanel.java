/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.reporting;

import org.eclipse.team.svn.ui.SVNTeamUIPlugin;


/**
 * Preview any product quality report panel
 * 
 * @author Alexander Gurov
 */
public class PreviewReportPanel extends PreviewPanel {
	public PreviewReportPanel(String description, String report) {
		super(SVNTeamUIPlugin.instance().getResource("PreviewReportPanel.Preview"), description, SVNTeamUIPlugin.instance().getResource("PreviewReportPanel.Message"), report);
	}
	
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.previewReportDialogContext";
	}	
}
