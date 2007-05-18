/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elena Matokhina - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;

/**
 * Edit comment templates panel
 *
 * @author Elena Matokhina
 */
public class EditCommentTemplatePanel extends AbstractDialogPanel {
	
	protected Text templateText;
	protected String template;
	
	public EditCommentTemplatePanel(String template) {
		super();
		this.dialogTitle = SVNTeamUIPlugin.instance().getResource(template == null ? "EditCommentTemplatePanel.Title.New" : "EditCommentTemplatePanel.Title.edit");
		this.defaultMessage = SVNTeamUIPlugin.instance().getResource("EditCommentTemplatePanel.Message");
		this.dialogDescription = SVNTeamUIPlugin.instance().getResource("EditCommentTemplatePanel.Description");
		this.template = template;
	}
	
	public void createControls(Composite parent) {
		this.templateText = new Text(parent, SWT.BORDER | SWT.MULTI);
		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH);
		data.heightHint = 180;
		this.templateText.setLayoutData(data);
		this.templateText.setText(this.template == null ? "" : this.template);
		this.templateText.selectAll();
		this.attachTo(this.templateText, new NonEmptyFieldVerifier(SVNTeamUIPlugin.instance().getResource("EditCommentTemplatePanel.Tempalte.Verifier")));
	}
	
	public String getTemplate() {
		return this.template;
	}

	protected void saveChanges() {
		this.template = this.templateText.getText().trim();
	}

	protected void cancelChanges() {
		
	}

}
