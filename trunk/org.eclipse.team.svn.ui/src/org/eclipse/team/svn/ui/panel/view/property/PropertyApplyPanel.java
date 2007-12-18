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

package org.eclipse.team.svn.ui.panel.view.property;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.ApplyPropertyMethodComposite;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;

/**
 * Apply property recursively panel
 *
 * @author Sergiy Logvin
 */
public class PropertyApplyPanel extends AbstractDialogPanel {
	
	protected ApplyPropertyMethodComposite applyComposite;
	
	public PropertyApplyPanel(boolean oneProperty) {
		super();
		this.dialogTitle = SVNTeamUIPlugin.instance().getResource(oneProperty ? "PropertyApplyPanel.Title.Single" : "PropertyApplyPanel.Title.Multi");
		this.dialogDescription = SVNTeamUIPlugin.instance().getResource(oneProperty ? "PropertyApplyPanel.Description.Single" : "PropertyApplyPanel.Description.Multi");
		this.defaultMessage = SVNTeamUIPlugin.instance().getResource(oneProperty ? "PropertyApplyPanel.Message.Single" : "PropertyApplyPanel.Message.Multi");
	}
	
	public int getApplyMethod() {
		return this.applyComposite.getApplyMethod();
	}
	
	public boolean useMask() {
		return this.applyComposite.useMask();
	}
	
	public String getFilterMask() {
		return this.applyComposite.getFilterMask();
	}
	
	public void createControlsImpl(Composite parent) {
		this.applyComposite = new ApplyPropertyMethodComposite(parent, SWT.NONE, this, PropertyEditPanel.MIXED_RESOURCES);
	}
	
	protected void cancelChangesImpl() {
	}
	
	protected void saveChangesImpl() {
		this.applyComposite.saveChanges();
	}

}
