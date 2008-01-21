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

package org.eclipse.team.svn.ui.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * Validate location error dialog
 * 
 * @author Alexander Gurov
 */
public class NonValidLocationErrorDialog extends MessageDialog {

	public NonValidLocationErrorDialog(Shell parentShell, String message) {
		super(parentShell, 
			SVNTeamUIPlugin.instance().getResource("NonValidLocationErrorDialog.Title"), 
			null, 
			SVNTeamUIPlugin.instance().getResource("NonValidLocationErrorDialog.Message", new String[] {message == null ? "" : message + "\n\n"}),
			MessageDialog.QUESTION, 
			new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, 
			0);
	}

}
