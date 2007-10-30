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
 * Discard location confirmation dialog
 * 
 * @author Alexander Gurov
 */
public class DiscardConfirmationDialog extends MessageDialog {
	public static final int MSG_LOCATION = 0;
	public static final int MSG_LINK = 1;
	public static final int MSG_RESOURCE = 2;
	
	protected static final String [][] MESSAGES = new String[][] {
		new String[] {"DiscardConfirmationDialog.Location.Title", "DiscardConfirmationDialog.Location.Message"},	
		new String[] {"DiscardConfirmationDialog.RevisionLink.Title", "DiscardConfirmationDialog.RevisionLink.Message"},	
		new String[] {"DiscardConfirmationDialog.Resource.Title", "DiscardConfirmationDialog.Resource.Message"}
	};

	public DiscardConfirmationDialog(Shell parentShell, boolean oneLocation, int msgSelector) {
		super(parentShell, 
			SVNTeamUIPlugin.instance().getResource(DiscardConfirmationDialog.MESSAGES[msgSelector][0] + (oneLocation ? ".Single" : ".Multi")), 
			null, 
			SVNTeamUIPlugin.instance().getResource(DiscardConfirmationDialog.MESSAGES[msgSelector][1] + (oneLocation ? ".Single" : ".Multi")),
			MessageDialog.QUESTION, 
			new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}, 
			0);
	}

}
