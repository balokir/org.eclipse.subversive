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

package org.eclipse.team.svn.ui.panel.local;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNUIMessages;
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
	protected IRepositoryResource[] branchTagResources;
	protected long currentRevision;
	protected String historyKey;
	protected BranchTagSelectionComposite selectionComposite;
	
	public ReplaceBranchTagPanel(IRepositoryResource baseResource, long currentRevision, int type, IRepositoryResource[] branchTagResources) {
		super();
		this.selectedResource = baseResource;
		this.type = type;
		this.branchTagResources = branchTagResources;
		if (type == BranchTagSelectionComposite.BRANCH_OPERATED) {
			this.dialogTitle = SVNUIMessages.Replace_Branch_Title;
			this.dialogDescription = SVNUIMessages.Replace_Branch_Description;
			this.defaultMessage = SVNUIMessages.Replace_Branch_Message;
			this.historyKey = "branchReplace"; //$NON-NLS-1$
		}
		else {
			this.dialogTitle = SVNUIMessages.Replace_Tag_Title;
			this.dialogDescription = SVNUIMessages.Replace_Tag_Description;
			this.defaultMessage = SVNUIMessages.Replace_Tag_Message;
			this.historyKey = "tagReplace"; //$NON-NLS-1$
		}
	}
	
	protected void createControlsImpl(Composite parent) {
        GridData data = null;
        this.selectionComposite = new BranchTagSelectionComposite(parent, SWT.NONE, this.selectedResource, this.historyKey, this, this.type, this.branchTagResources);
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