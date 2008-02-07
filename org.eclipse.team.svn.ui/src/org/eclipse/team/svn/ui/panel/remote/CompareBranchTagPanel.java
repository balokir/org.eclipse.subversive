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

package org.eclipse.team.svn.ui.panel.remote;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.BranchTagSelectionComposite;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;

/**
 * Panel for the Compare With Branch/Tag dialog
 * 
 * @author Alexei Goncharov
 */
public class CompareBranchTagPanel extends AbstractDialogPanel {
	protected IRepositoryResource selectedResource;
	protected int type;
	protected long currentRevision;
	protected boolean stopOnCopy;
	protected String historyKey;
	protected BranchTagSelectionComposite selectionComposite;
	
	public CompareBranchTagPanel(IRepositoryResource baseResource, int type, boolean stopOnCopy) {
		super();
		this.selectedResource = baseResource;
		this.type = type;
		this.stopOnCopy = stopOnCopy;
		if (type == BranchTagSelectionComposite.BRANCH_OPERATED) {
			this.dialogTitle = SVNTeamUIPlugin.instance().getResource("Compare.Branch.Title");
			this.dialogDescription = SVNTeamUIPlugin.instance().getResource("Compare.Branch.Description");
			this.defaultMessage = SVNTeamUIPlugin.instance().getResource("Compare.Branch.Message");
			this.historyKey = "branchCompare";
		}
		else {
			this.dialogTitle = SVNTeamUIPlugin.instance().getResource("Compare.Tag.Title");
			this.dialogDescription = SVNTeamUIPlugin.instance().getResource("Compare.Tag.Description");
			this.defaultMessage = SVNTeamUIPlugin.instance().getResource("Compare.Tag.Message");
			this.historyKey = "tagCompare";
		}
	}
	
	protected void createControlsImpl(Composite parent) {
        GridData data = null;
        this.selectionComposite = new BranchTagSelectionComposite(parent, SWT.NONE, this.selectedResource, this.historyKey, this, this.type, this.stopOnCopy);
        data = new GridData(GridData.FILL_HORIZONTAL);
        this.selectionComposite.setLayoutData(data);
        this.selectionComposite.setCurrentRevision(this.currentRevision);
	}
	
	public IRepositoryResource getSelectedResoure() {
		return this.selectionComposite.getSelectedResource();
	}

	protected void saveChangesImpl() {
		this.selectionComposite.saveChanges();
	}
	
	protected void cancelChangesImpl() {
	}

}
