/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elena Matokhina (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.wizard.shareproject;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.svn.core.operation.local.management.ShareProjectOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.CommentComposite;
import org.eclipse.team.svn.ui.wizard.AbstractVerifiedWizardPage;

/**
 * Page to edit commit comment
 * 
 * @author Elena Matokhina
 */
public class EditCommentPage extends AbstractVerifiedWizardPage {
	protected String commitComment;
	protected IRepositoryLocation location;
	protected CommentComposite commentComposite;
	protected IResourceProvider provider;
	protected boolean showCommitDialog;

	public EditCommentPage(IResourceProvider provider) {
		super(EditCommentPage.class.getName(), 
				SVNTeamUIPlugin.instance().getResource("EditCommentPage.Title"), 
				SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif"));
		this.setDescription(SVNTeamUIPlugin.instance().getResource("EditCommentPage.Description"));
		this.provider = provider;
	}
	
	public boolean isShowCommitDialog() {
		return this.showCommitDialog;
	}
	
	public String getCommitComment() {
		this.commentComposite.saveChanges();
		return this.commentComposite.getMessage();
	}
	
	public void setSelectedRepositoryLocation(IRepositoryLocation location) {
		this.location = location;
	}
	
	public void setDefaultCommitMessage() {
		this.commitComment = "";
		IProject []projects = this.getProjects();
		for (int i = 0; i < projects.length; i++) {
			String commentPart = ShareProjectOperation.getDefaultComment(projects[i], this.location.getRoot());
			this.commitComment += this.commitComment.length() == 0 ? commentPart : ("\n" + commentPart);
		}
		this.commentComposite.setMessage(this.commitComment);
	}
	
	protected IProject []getProjects() {
		return (IProject [])this.provider.getResources();
	}

	protected Composite createControlImpl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		this.commentComposite = new CommentComposite(composite, this.commitComment, this, null, null);
		GridData data = new GridData(GridData.FILL_BOTH);
		this.commentComposite.setLayoutData(data);
		
		Label separator = new Label (composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		separator.setVisible(false);
		
		Button showComment = new Button(composite, SWT.CHECK);
		showComment.setText(SVNTeamUIPlugin.instance().getResource("EditCommentPage.LaunchCommit"));
		showComment.setSelection(this.showCommitDialog = true);
		showComment.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				EditCommentPage.this.showCommitDialog = ((Button)e.widget).getSelection();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		return composite;
	}

}
