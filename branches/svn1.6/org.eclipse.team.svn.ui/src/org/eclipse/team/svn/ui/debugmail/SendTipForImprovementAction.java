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
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.extension.factory.IReporter;
import org.eclipse.team.svn.ui.operation.UILoggedOperation;
import org.eclipse.team.svn.ui.panel.reporting.MailReportPanel;

/**
 * The action allows user to send tip for product improvment. 
 * 
 * @author Alexander Gurov
 */
public class SendTipForImprovementAction extends AbstractMainMenuAction {
	public void run(IAction action) {
		String title = SVNUIMessages.SendTipForImprovementAction_Panel_Title;
		MailReportPanel panel = new MailReportPanel(title, SVNUIMessages.SendTipForImprovementAction_Panel_Description, SVNUIMessages.SendTipForImprovementAction_Panel_Message, false);
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