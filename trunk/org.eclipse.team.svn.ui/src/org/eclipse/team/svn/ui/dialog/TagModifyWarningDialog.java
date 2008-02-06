/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * Tag modification warning dialog
 * 
 * @author Alexei Goncharov
 */
public class TagModifyWarningDialog extends MessageDialog {

	public TagModifyWarningDialog(Shell parentShell) {
		super(parentShell,
			SVNTeamUIPlugin.instance().getResource("TagModifyWarningDialog.Title"), 
			null, 
			SVNTeamUIPlugin.instance().getResource("TagModifyWarningDialog.Message"),
			MessageDialog.QUESTION, 
			new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}, 
			0);
	}

}
