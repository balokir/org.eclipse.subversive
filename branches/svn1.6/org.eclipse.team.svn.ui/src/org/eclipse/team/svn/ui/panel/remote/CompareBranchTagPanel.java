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

package org.eclipse.team.svn.ui.panel.remote;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.BranchTagSelectionComposite;
import org.eclipse.team.svn.ui.composite.DiffFormatComposite;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;

/**
 * Panel for the Compare With Branch/Tag dialog
 * 
 * @author Alexei Goncharov
 */
public class CompareBranchTagPanel extends AbstractDialogPanel {
	protected IRepositoryResource selectedResource;
	protected int type;
	protected IRepositoryResource[] branchTagResources;
	protected long currentRevision;
	protected String historyKey;
	protected BranchTagSelectionComposite selectionComposite;
	protected DiffFormatComposite diffFormatComposite;
	
	public CompareBranchTagPanel(IRepositoryResource baseResource, int type, IRepositoryResource[] branchTagResources) {
		super();
		this.selectedResource = baseResource;
		this.type = type;
		this.branchTagResources = branchTagResources;
		if (type == BranchTagSelectionComposite.BRANCH_OPERATED) {
			this.dialogTitle = SVNUIMessages.Compare_Branch_Title;
			this.dialogDescription = SVNUIMessages.Compare_Branch_Description;
			this.defaultMessage = SVNUIMessages.Compare_Branch_Message;
			this.historyKey = "branchCompare"; //$NON-NLS-1$
		}
		else {
			this.dialogTitle = SVNUIMessages.Compare_Tag_Title;
			this.dialogDescription = SVNUIMessages.Compare_Tag_Description;
			this.defaultMessage = SVNUIMessages.Compare_Tag_Message;
			this.historyKey = "tagCompare"; //$NON-NLS-1$
		}
	}
	
	protected void createControlsImpl(Composite parent) {
        GridData data = null;
        this.selectionComposite = new BranchTagSelectionComposite(parent, SWT.NONE, this.selectedResource, this.historyKey, this, this.type, this.branchTagResources);
        data = new GridData(GridData.FILL_HORIZONTAL);
        this.selectionComposite.setLayoutData(data);
        this.selectionComposite.setCurrentRevision(this.currentRevision);
        
        this.diffFormatComposite = new DiffFormatComposite(parent, this);
	}
	
	public String getDiffFile() {			
		return this.diffFormatComposite.getDiffFile();
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
