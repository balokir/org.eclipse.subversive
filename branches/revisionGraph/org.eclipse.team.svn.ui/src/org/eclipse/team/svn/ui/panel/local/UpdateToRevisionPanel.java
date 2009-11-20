/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.local;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.DepthSelectionComposite;
import org.eclipse.team.svn.ui.composite.RevisionComposite;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;

/**
 * Update to revision panel implementation
 * 
 * @author Igor Burilo
 */
public class UpdateToRevisionPanel extends AbstractDialogPanel {

	protected RevisionComposite revisionComposite;
	protected DepthSelectionComposite depthSelector;
	
	protected IRepositoryResource selectedResource;
	
	//output
	protected SVNRevision revision;
	protected int depth;
	
	public UpdateToRevisionPanel(IRepositoryResource selectedResource) {
		this.dialogTitle = SVNUIMessages.UpdateToRevisionPanel_Title;
        this.dialogDescription = SVNUIMessages.UpdateToRevisionPanel_Description;
        this.defaultMessage = SVNUIMessages.UpdateToRevisionPanel_Message;
        
        this.selectedResource = selectedResource;
	}
	
	protected void createControlsImpl(Composite parent) {		
		this.revisionComposite = new RevisionComposite(parent, this, false, new String[]{SVNUIMessages.RevisionComposite_Revision, SVNUIMessages.RevisionComposite_HeadRevision}, SVNRevision.HEAD, false);
		GridLayout layout = new GridLayout();
		GridData data = new GridData(GridData.FILL_HORIZONTAL);		
		this.revisionComposite.setLayout(layout);
		this.revisionComposite.setLayoutData(data);
		this.revisionComposite.setSelectedResource(this.selectedResource);		
		
		this.depthSelector = new DepthSelectionComposite(parent, SWT.NONE);		
		layout = new GridLayout();
		data = new GridData(GridData.FILL_HORIZONTAL);		
		this.depthSelector.setLayout(layout);
		this.depthSelector.setLayoutData(data);
		this.depthSelector.addAndSelectWorkingCopyDepth();
	}
	
	protected void cancelChangesImpl() {
	}	

	protected void saveChangesImpl() {
		this.revision = this.revisionComposite.getSelectedRevision();
		this.depth = this.depthSelector.getDepth();
	}
	
	public SVNRevision getRevision() {
		return this.revision;
	}
	
	public int getDepth() {
		return this.depth;
	}
	
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.updateDialogContext"; //$NON-NLS-1$
	}

}
