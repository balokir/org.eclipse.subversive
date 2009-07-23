/*******************************************************************************
 * Copyright (c) 2009 Tasktop Technologies, Polarion Software and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.discovery.util;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * @author David Green
 * @author Steffen Pingel much of this is copied from TasksUiInternal
 * @author Igor Burilo
 */
public abstract class DiscoveryUiUtil {
	private DiscoveryUiUtil() {
	}

	public static void logAndDisplayStatus(final String title, final IStatus status) {
		logAndDisplayStatus(null, title, status);
	}

	public static void logAndDisplayStatus(Shell shell, final String title, final IStatus status) {
		//TODO is it correct ?
		SVNTeamUIPlugin.instance().getLog().log(status);
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench != null && !workbench.getDisplay().isDisposed()) {
			if (shell == null) {
				shell = UIMonitorUtility.getShell();
			}
			displayStatus(shell, title, status, true);
		}
	}

	public static void displayStatus(final String title, final IStatus status) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench != null && !workbench.getDisplay().isDisposed()) {
			displayStatus(UIMonitorUtility.getShell(), title, status, false);
		} else {
			//TODO is it correct ?
			SVNTeamUIPlugin.instance().getLog().log(status);
		}
	}

	public static void displayStatus(Shell shell, final String title, final IStatus status, boolean showLinkToErrorLog) {
		String message = status.getMessage();
		if (showLinkToErrorLog) {
			message += SVNUIMessages.DiscoveryUi_seeErrorLog;
		}
		switch (status.getSeverity()) {
		case IStatus.CANCEL:
		case IStatus.INFO:
			createDialog(shell, title, message, MessageDialog.INFORMATION).open();
			break;
		case IStatus.WARNING:
			createDialog(shell, title, message, MessageDialog.WARNING).open();
			break;
		case IStatus.ERROR:
		default:
			createDialog(shell, title, message, MessageDialog.ERROR).open();
			break;
		}

	}

	public static MessageDialog createDialog(Shell shell, String title, String message, int type) {
		return new MessageDialog(shell, title, null, message, type, new String[] { IDialogConstants.OK_LABEL }, 0);
	}
}
