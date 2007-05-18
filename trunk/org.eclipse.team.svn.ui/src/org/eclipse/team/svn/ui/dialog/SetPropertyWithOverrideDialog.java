/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elena Matokhina - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.dialog;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * Property ovewrite warning dialog
 *
 * @author Elena Matokhina
 */
public class SetPropertyWithOverrideDialog extends MessageDialog {

	public SetPropertyWithOverrideDialog(Shell parentShell, String existingName) {
		super(parentShell,
			SVNTeamUIPlugin.instance().getResource("SetPropertyWithOverrideDialog.Title"), 
			null, 
			MessageFormat.format(SVNTeamUIPlugin.instance().getResource("SetPropertyWithOverrideDialog.Message"), new String[] {existingName}),
			MessageDialog.QUESTION, 
			new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}, 
			0);
	}
		
}