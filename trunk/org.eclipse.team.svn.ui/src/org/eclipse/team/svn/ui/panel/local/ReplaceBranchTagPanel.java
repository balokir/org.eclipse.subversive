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

package org.eclipse.team.svn.ui.panel.local;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.BranchTagSelectionComposite;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;

/**
 * Panel for the Replace With Branch/Tag dialog
 * 
 * @author Alexei Goncharov
 */
public class ReplaceBranchTagPanel extends AbstractDialogPanel {
	protected IRepositoryResource selectedResource;
	protected int type;
	protected long currentRevision;
	protected boolean stopOnCopy;
	protected String historyKey;
	protected BranchTagSelectionComposite selectionComposite;
	
	public ReplaceBranchTagPanel(IRepositoryResource baseResource, long currentRevision, int type, boolean stopOnCopy) {
		super();
		this.selectedResource = baseResource;
		this.type = type;
		this.stopOnCopy = stopOnCopy;
		if (type == BranchTagSelectionComposite.BRANCH_OPERATED) {
			this.dialogTitle = SVNTeamUIPlugin.instance().getResource("Replace.Branch.Title");
			this.dialogDescription = SVNTeamUIPlugin.instance().getResource("Replace.Branch.Description");
			this.defaultMessage = SVNTeamUIPlugin.instance().getResource("Replace.Branch.Message");
			this.historyKey = "branchReplace";
		}
		else {
			this.dialogTitle = SVNTeamUIPlugin.instance().getResource("Replace.Tag.Title");
			this.dialogDescription = SVNTeamUIPlugin.instance().getResource("Replace.Tag.Description");
			this.defaultMessage = SVNTeamUIPlugin.instance().getResource("Replace.Tag.Message");
			this.historyKey = "tagReplace";
		}
	}
	
	protected void createControlsImpl(Composite parent) {
        GridData data = null;
        this.selectionComposite = new BranchTagSelectionComposite(parent, SWT.NONE, this.selectedResource, this.historyKey, this, this.type, this.stopOnCopy);
        data = new GridData(GridData.FILL_HORIZONTAL);
        this.selectionComposite.setLayoutData(data);
        this.selectionComposite.setCurrentRevision(this.currentRevision);
	}
	
	public IRepositoryResource getSelectedResource() {
		return this.selectionComposite.getSelectedResource();
	}

	protected void saveChangesImpl() {
		this.selectionComposite.saveChanges();
	}
	
	protected void cancelChangesImpl() {
	}

}