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
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.extension.factory.IReporter;
import org.eclipse.team.svn.ui.operation.UILoggedOperation;
import org.eclipse.team.svn.ui.panel.reporting.MailReportPanel;
import org.eclipse.team.svn.ui.utility.DefaultOperationWrapperFactory;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * The action allows user to send tip for product improvment. 
 * 
 * @author Alexander Gurov
 */
public class SendTipForImprovementAction extends AbstractMainMenuAction {
	public void run(IAction action) {
		String title = SVNTeamUIPlugin.instance().getResource("SendTipForImprovementAction.Panel.Title");
		MailReportPanel panel = new MailReportPanel(title, SVNTeamUIPlugin.instance().getResource("SendTipForImprovementAction.Panel.Description"), SVNTeamUIPlugin.instance().getResource("SendTipForImprovementAction.Panel.Message"), false);
		IReporter reporter = panel.getReporter();
		if (reporter != null && reporter.isCustomEditorSupported() && ExtensionsManager.getInstance().getReportingDescriptors().length == 1) {
			this.runReporter(reporter);
		}
		else {
			DefaultDialog dlg = new DefaultDialog(this.getShell(), panel);
			if (dlg.open() == 0) {
				this.runReporter(panel.getReporter());
			}
		}
	}
	
	protected void runReporter(IReporter reporter) {
		UIMonitorUtility.doTaskNow(this.getShell(), reporter, true, new DefaultOperationWrapperFactory() {
			protected IActionOperation wrappedOperation(IActionOperation operation) {
				return new LoggedOperation(operation);
			}
		});
		if (reporter.getExecutionState() != IActionOperation.OK) {
			UILoggedOperation.showSendingError(reporter.getStatus(), this.getShell(), reporter.getReportingDescriptor(), reporter.buildReport());
		}
	}

}
