/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.remote;

import java.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.CommentComposite;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.verifier.AbsolutePathVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractFormattedVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.ResourceNameVerifier;

/**
 * Abstract panel implementation that allows us to get resource name for remote resource
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractGetResourceNamePanel extends AbstractDialogPanel {
	protected String resourceName;
	protected String disallowedName;
	protected Text text;
	protected CommentComposite comment;
	protected boolean allowMultipart;

    public AbstractGetResourceNamePanel(String title, boolean allowMultipart) {
        super();
        this.dialogTitle = title;
        this.allowMultipart = allowMultipart;
        this.defaultMessage = allowMultipart ? SVNUIMessages.AbstractGetResourceNamePanel_Message_MultiPart : SVNUIMessages.AbstractGetResourceNamePanel_Message_Simple;
        this.resourceName = ""; //$NON-NLS-1$
    }

	public String getResourceName() {
		return this.resourceName.trim();
	}

	public String getMessage() {
		return this.comment.getMessage();
	}
    
    public void createControlsImpl(Composite parent) {
        GridData data = null;
        GridLayout layout = null;

        Composite nameComposite = new Composite(parent, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        nameComposite.setLayout(layout);
        data = new GridData(GridData.FILL_HORIZONTAL);        
        nameComposite.setLayoutData(data);
        
        Label nameLabel = new Label(nameComposite, SWT.NONE);
        nameLabel.setText(SVNUIMessages.AbstractGetResourceNamePanel_Name);
		this.text = new Text(nameComposite, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.text.setLayoutData(data);
		this.text.setEditable(true);
		CompositeVerifier verifier = new CompositeVerifier();
		verifier.add(this.createNonEmptyNameFieldVerifier());
		String name = SVNUIMessages.AbstractGetResourceNamePanel_Name_Verifier;
		verifier.add(new ResourceNameVerifier(name, this.allowMultipart));
		verifier.add(new AbsolutePathVerifier(name));
		verifier.add(new AbstractFormattedVerifier(name) {
			private String msg = SVNUIMessages.AbstractGetResourceNamePanel_Name_Verifier_Error;
			
			protected String getErrorMessageImpl(Control input) {
				String text = this.getText(input);
				if (AbstractGetResourceNamePanel.this.disallowedName != null &&
					AbstractGetResourceNamePanel.this.disallowedName.equals(text)) {
					return MessageFormat.format(this.msg, new Object[] {AbstractFormattedVerifier.FIELD_NAME, text});
				}
				return null;
			}
			protected String getWarningMessageImpl(Control input) {
				return null;
			};
		});
		this.attachTo(this.text, verifier);

		Group group = new Group(parent, SWT.NULL);
		group.setLayout(new GridLayout());
		data = new GridData(GridData.FILL_BOTH);
		group.setLayoutData(data);
		group.setText(SVNUIMessages.AbstractGetResourceNamePanel_Comment);
		
        this.comment = new CommentComposite(group, this);
		data = new GridData(GridData.FILL_BOTH);
		this.comment.setLayoutData(data);
    }
    
    public Point getPrefferedSizeImpl() {
    	return new Point(525, SWT.DEFAULT);
    }
    
    public void postInit() {
    	super.postInit();
    	this.comment.postInit(this.manager);
    }
    
    protected void saveChangesImpl() {
        this.resourceName = this.text.getText();
        this.comment.saveChanges();
    }

    protected void cancelChangesImpl() {
    	this.comment.cancelChanges();
    }

    protected AbstractVerifier createNonEmptyNameFieldVerifier() {
    	return new NonEmptyFieldVerifier(SVNUIMessages.AbstractGetResourceNamePanel_Name_Verifier);
    }
    
}
