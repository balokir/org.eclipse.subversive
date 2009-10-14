/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.CommentComposite;
import org.eclipse.team.svn.ui.composite.RevisionComposite;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;

/**
 * Panel to type or select one of existent revisions
 * 
 * @author Sergiy Logvin
 */
public class InputRevisionPanel extends AbstractDialogPanel {
	
	protected IRepositoryResource resource;
	protected boolean isEdit;	
	protected String revisionComment;	
	
	protected RevisionComposite revComposite;	
	protected CommentComposite commentComposite;
	
	protected SVNRevision selectedRevision;
		
	public InputRevisionPanel(IRepositoryResource resource, boolean isEdit, String revisionComment) {
		super();
		this.resource = resource;
		this.isEdit = isEdit;
		this.revisionComment = revisionComment;
				
		this.dialogTitle = isEdit ? SVNUIMessages.InputRevisionPanel_EditTitle : SVNUIMessages.InputRevisionPanel_AddTitle;
		this.dialogDescription = this.resource == null ? SVNUIMessages.InputRevisionPanel_SingleDescription : SVNUIMessages.InputRevisionPanel_MultipleDescription;
		this.defaultMessage = this.resource == null ? SVNUIMessages.InputRevisionPanel_SingleMessage : SVNUIMessages.InputRevisionPanel_MultipleMessage;
	}
	
	public SVNRevision getSelectedRevision() {
		return this.selectedRevision;
	}
	
	public String getRevisionComment() {
		return this.revisionComment;
	}
	
	protected void createControlsImpl(Composite parent) {
		if (this.resource != null) {
			this.revComposite = new RevisionComposite(parent, this, false,  new String [] {SVNUIMessages.InputRevisionPanel_Caption_First, SVNUIMessages.InputRevisionPanel_Caption_Second}, SVNRevision.HEAD, false);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			this.revComposite.setLayoutData(data);
			
			this.revComposite.setSelectedResource(this.resource);
			this.revComposite.setRevisionValue(this.resource.getSelectedRevision());
		}		
		
		Group group = new Group(parent, SWT.NULL);
		group.setLayout(new GridLayout());
		GridData data = new GridData(GridData.FILL_BOTH);
		group.setLayoutData(data);
		group.setText(SVNUIMessages.InputRevisionPanel_Comment);
				
		this.commentComposite = new CommentComposite(group, this);
		data = new GridData(GridData.FILL_BOTH);
		this.commentComposite.setLayoutData(data);
				
		if (this.revisionComment != null) {
			this.commentComposite.setMessage(this.revisionComment);
		}
	}
	
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.revisionDialogContext"; //$NON-NLS-1$
	}
	
	protected void saveChangesImpl() {
		if (this.resource != null) {
			this.resource = this.revComposite.getSelectedResource();
			this.selectedRevision = this.revComposite.getSelectedRevision();			
		}		
		this.commentComposite.saveChanges();		
		this.revisionComment = this.commentComposite.getMessage();
	}

	protected void cancelChangesImpl() {
		this.commentComposite.cancelChanges();
	}

}
