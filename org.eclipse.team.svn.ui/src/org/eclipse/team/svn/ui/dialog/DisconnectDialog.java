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

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * Disconnect from repository confirmation dialog
 * 
 * @author Alexander Gurov
 */
public class DisconnectDialog extends MessageDialog {
	
	protected boolean dropSVNFolders;

	public DisconnectDialog(Shell parentShell, IProject[] projects) {
		super(parentShell, 
			SVNTeamUIPlugin.instance().getResource("DisconnectDialog.Title"), 
			null, 
			MessageFormat.format(SVNTeamUIPlugin.instance().getResource("DisconnectDialog.Message"), new String[] {FileUtility.getNamesListAsString(projects)}),
			MessageDialog.QUESTION, 
			new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}, 
			0);
		
		this.dropSVNFolders = false;
	}
	
	protected Control createCustomArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());

		Button dropSVNFoldersButton = new Button(composite, SWT.RADIO);
		dropSVNFoldersButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button) e.widget;
				DisconnectDialog.this.dropSVNFolders = button.getSelection();
			}
		});
		
		dropSVNFoldersButton.setText(SVNTeamUIPlugin.instance().getResource("DisconnectDialog.Option.dropSVNMeta")); 

		Button leaveSVNFoldersButton = new Button(composite, SWT.RADIO);

		leaveSVNFoldersButton.setText(SVNTeamUIPlugin.instance().getResource("DisconnectDialog.Option.leaveSVNMeta")); 
		
		// set initial state
		dropSVNFoldersButton.setSelection(false);
		leaveSVNFoldersButton.setSelection(true);
		
//		WorkbenchHelp.setHelp(composite, IHelpContextIds.DISCONNECT_ACTION);
		
		return composite;
	}

	public boolean dropSVNFolders() {
		return this.dropSVNFolders;
	}
	
}
