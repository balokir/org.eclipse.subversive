/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.crashrecovery.relocated;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;

/**
 * Allows user to select how plug-in should process project relocation. There are two choices:
 * 1) Change repository location URL and relocate all related projects if it is required
 * 2) Run 'Share Project' wizard in order to select a new repository location for the project
 * 
 * @author Alexander Gurov
 */
public class RelocationChoicesPanel extends AbstractDialogPanel {
	public static final int DISCONNECT_PROJECT = -1;
	public static final int RELOCATE_THE_PROJECT_BACK = 0;
	public static final int RELOCATE_REST_OF_PROJECTS = 1;
	public static final int SHARE_WITH_ANOTHER_LOCATION = 2;
	
	protected int recoveryAction;

	public RelocationChoicesPanel(IProject project) {
		super();
		this.dialogTitle = SVNTeamUIPlugin.instance().getResource("RelocationChoicesPanel.Title");
		this.dialogDescription = SVNTeamUIPlugin.instance().getResource("RelocationChoicesPanel.Description", new String[] {project.getName()});
		this.defaultMessage = SVNTeamUIPlugin.instance().getResource("RelocationChoicesPanel.Message");
	}
	
	public int getRecoveryAction() {
		return this.recoveryAction;
	}

	protected void createControlsImpl(Composite parent) {
		GridLayout layout = null;
		GridData data = null;
		
		Composite composite = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = layout.marginWidth = 0;
		composite.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		Button relocateOthers = new Button(composite, SWT.RADIO);
		data = new GridData(GridData.FILL_HORIZONTAL);
		relocateOthers.setLayoutData(data);
		relocateOthers.setText(SVNTeamUIPlugin.instance().getResource("RelocationChoicesPanel.ChangeLocation"));
		relocateOthers.setSelection(true);
		this.recoveryAction = RelocationChoicesPanel.RELOCATE_REST_OF_PROJECTS;
		relocateOthers.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (((Button)e.widget).getSelection()) {
					RelocationChoicesPanel.this.recoveryAction = RelocationChoicesPanel.RELOCATE_REST_OF_PROJECTS;
				}
			}
		});
		
		Button relocateBack = new Button(composite, SWT.RADIO);
		data = new GridData(GridData.FILL_HORIZONTAL);
		relocateBack.setLayoutData(data);
		relocateBack.setText(SVNTeamUIPlugin.instance().getResource("RelocationChoicesPanel.RelocateBack"));
		relocateBack.setSelection(false);
		relocateBack.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (((Button)e.widget).getSelection()) {
					RelocationChoicesPanel.this.recoveryAction = RelocationChoicesPanel.RELOCATE_THE_PROJECT_BACK;
				}
			}
		});
		
		Button runShareWizard = new Button(composite, SWT.RADIO);
		data = new GridData(GridData.FILL_HORIZONTAL);
		runShareWizard.setLayoutData(data);
		runShareWizard.setText(SVNTeamUIPlugin.instance().getResource("RelocationChoicesPanel.CreateLocation"));
		runShareWizard.setSelection(false);
		runShareWizard.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (((Button)e.widget).getSelection()) {
					RelocationChoicesPanel.this.recoveryAction = RelocationChoicesPanel.SHARE_WITH_ANOTHER_LOCATION;
				}
			}
		});
	}
	
	protected void cancelChangesImpl() {
		this.recoveryAction = RelocationChoicesPanel.DISCONNECT_PROJECT;
	}

	protected void saveChangesImpl() {
	}

}
