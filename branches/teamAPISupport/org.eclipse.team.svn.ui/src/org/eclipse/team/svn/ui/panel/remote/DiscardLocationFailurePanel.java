/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.remote;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.ProjectListComposite;
import org.eclipse.team.svn.ui.panel.ItemListPanel;

/**
 * Discard location failure panel
 * 
 * @author Sergiy Logvin
 */
public class DiscardLocationFailurePanel extends ItemListPanel {
	
	protected IProject []projects;
	
	public DiscardLocationFailurePanel(String []locations, IProject []projects) {
		super(locations, SVNTeamUIPlugin.instance().getImageDescriptor("icons/objects/repository.gif"),
				SVNTeamUIPlugin.instance().getResource(locations.length == 1 ? "DiscardLocationFailurePanel.Title.Single" : "DiscardLocationFailurePanel.Title.Multi"), 
				SVNTeamUIPlugin.instance().getResource(locations.length == 1 ? "DiscardLocationFailurePanel.Description.Single" : "DiscardLocationFailurePanel.Description.Multi"), 
				SVNTeamUIPlugin.instance().getResource(locations.length == 1 ? "DiscardLocationFailurePanel.Message.Single" : "DiscardLocationFailurePanel.Message.Multi"), 
				new String[] {SVNTeamUIPlugin.instance().getResource("DiscardLocationFailurePanel.Disconnect"), SVNTeamUIPlugin.instance().getResource("DiscardLocationFailurePanel.Delete"), IDialogConstants.CANCEL_LABEL});
		this.projects = projects;
	}
	
	public void createControlsImpl(Composite parent) {
		super.createControlsImpl(parent);
		
		GridData data = (GridData)this.table.getLayoutData();
		data.heightHint = 50;
		this.table.setLayoutData(data);
		
		ProjectListComposite composite = new ProjectListComposite(parent, SWT.NONE, this.projects, true);
		composite.initialize();
		
		data = (GridData)composite.getLayoutData();
		data.heightHint = 100;
		composite.setLayoutData(data);
	}

}
