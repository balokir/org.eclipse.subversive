/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.debugmail;

import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.extension.factory.IReporter;
import org.eclipse.team.svn.ui.operation.UILoggedOperation;
import org.eclipse.team.svn.ui.panel.reporting.MailReportPanel;

/**
 * The action allows user to send report about product bug. 
 * 
 * @author Alexander Gurov
 */
public class SendBugReportAction extends AbstractMainMenuAction {
	public void run(IAction action) {
		String title = SVNTeamUIPlugin.instance().getResource("SendBugReportAction.Panel.Title");
		MailReportPanel panel = new MailReportPanel(title, SVNTeamUIPlugin.instance().getResource("SendBugReportAction.Panel.Description"), SVNTeamUIPlugin.instance().getResource("SendBugReportAction.Panel.Message"), true);
		IReporter reporter = panel.getReporter();
		if (reporter != null && reporter.isCustomEditorSupported() && ExtensionsManager.getInstance().getReportingDescriptors().length == 1) {
			UILoggedOperation.sendReport(reporter);
		}
		else {
			DefaultDialog dlg = new DefaultDialog(this.getShell(), panel);
			if (dlg.open() == 0) {
				UILoggedOperation.sendReport(panel.getReporter());
			}
		}
	}

}