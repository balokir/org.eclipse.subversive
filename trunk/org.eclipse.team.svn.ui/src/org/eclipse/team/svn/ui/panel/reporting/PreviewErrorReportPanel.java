/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.reporting;

import org.eclipse.swt.graphics.Point;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * Preview error report panel
 *
 * @author Sergiy Logvin
 */
public class PreviewErrorReportPanel extends PreviewReportPanel {
	public PreviewErrorReportPanel(String report) {
		super(SVNTeamUIPlugin.instance().getResource("PreviewErrorReportPanel.Description"), report);
	}
	
	public Point getPrefferedSizeImpl() {
		return new Point(750, 700);
	}
	
}
