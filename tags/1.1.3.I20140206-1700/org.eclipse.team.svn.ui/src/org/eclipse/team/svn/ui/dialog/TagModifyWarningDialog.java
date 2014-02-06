/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.dialog;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.SVNTeamProvider;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.ui.SVNUIMessages;

/**
 * Tag modification warning dialog
 * 
 * @author Alexei Goncharov
 */
public class TagModifyWarningDialog extends MessageDialog {

	//projects which contain tag modifications
	protected final IProject[] projects;
	
	protected boolean dontAskAnyMore;
	
	public TagModifyWarningDialog(Shell parentShell, IProject[] projects) {
		super(parentShell,
			SVNUIMessages.TagModifyWarningDialog_Title, 
			null, 
			SVNUIMessages.TagModifyWarningDialog_Message,
			MessageDialog.QUESTION, 
			new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}, 
			0);
		this.projects = projects;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.MessageDialog#createCustomArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createCustomArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		final Button dontAskButton = new Button(composite, SWT.CHECK);
		dontAskButton.setLayoutData(new GridData());		
		dontAskButton.setText(SVNUIMessages.TagModifyWarningDialog_CustomText);
		dontAskButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				TagModifyWarningDialog.this.dontAskAnyMore = dontAskButton.getSelection();														
			}
		});		
		dontAskButton.setSelection(this.dontAskAnyMore = false);
		
		return composite;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#close()
	 */
	@Override
	public boolean close() {
		if (this.dontAskAnyMore) {
			for (IProject project : this.projects) {						
				SVNTeamProvider provider = (SVNTeamProvider)RepositoryProvider.getProvider(project, SVNTeamPlugin.NATURE_ID);
				try {
					provider.setVerifyTagOnCommit(!this.dontAskAnyMore);
				} catch (CoreException e) {
					LoggedOperation.reportError(TagModifyWarningDialog.this.getClass().getName(), e);
				}
			}			
		}			
		
		return super.close();
	}
}
