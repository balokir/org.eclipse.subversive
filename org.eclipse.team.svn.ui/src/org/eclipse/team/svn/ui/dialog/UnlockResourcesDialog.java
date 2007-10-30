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

package org.eclipse.team.svn.ui.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * Unlock resources dialog
 *
 * @author Sergiy Logvin
 */
public class UnlockResourcesDialog extends MessageDialog {
	
	protected boolean recursive;
	protected boolean enableRecursive;

	public UnlockResourcesDialog(Shell parentShell, boolean enableRecursive) {
		super(parentShell, 
			SVNTeamUIPlugin.instance().getResource("UnlockResourcesDialog.Title"), 
			null, 
			SVNTeamUIPlugin.instance().getResource("UnlockResourcesDialog.Message"),
			MessageDialog.QUESTION, 
			new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}, 
			0);
		this.enableRecursive = enableRecursive;
		this.recursive = false;
	}
	
	public boolean isRecursive() {
		return this.recursive;
	}

	protected Control createCustomArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		if (this.enableRecursive) {
			Button recursive = new Button(composite, SWT.CHECK);
			recursive.setLayoutData(new GridData());
			recursive.setSelection(false);
			recursive.setText(SVNTeamUIPlugin.instance().getResource("UnlockResourcesDialog.Recursively"));
			recursive.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					Button button = (Button)event.widget;
					UnlockResourcesDialog.this.recursive = button.getSelection();
				}
			});
		}
		
		return composite;
	}
	
}
