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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.CommentComposite;
import org.eclipse.team.svn.ui.composite.LockResourceSelectionComposite;
import org.eclipse.team.svn.ui.composite.LockResourceSelectionComposite.ILockResourceSelectionChangeListener;
import org.eclipse.team.svn.ui.composite.LockResourceSelectionComposite.LockResourceSelectionChangedEvent;
import org.eclipse.team.svn.ui.lock.LockResource;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;

/**
 * Lock resources panel implementation
 * 
 * @author Igor Burilo
 */
public class LockResourcesPanel extends AbstractDialogPanel {

	protected LockResource[] resources;
	protected boolean hasComment;
	
	protected LockResourceSelectionComposite selectionComposite;
	protected CommentComposite comment;
	protected SashForm sForm;
	protected Button forceButton;
	protected boolean force;	
	
	public LockResourcesPanel(LockResource[] resources, String dialogTitle, String dialogDescription, String defaultMessage) {
		this(resources, false, false, dialogTitle, dialogDescription, defaultMessage);
	}
	
	public LockResourcesPanel(LockResource[] resources, boolean hasComment, boolean force, String dialogTitle, String dialogDescription, String defaultMessage) {
		super(new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL});
		
		this.resources = resources;
		this.hasComment = hasComment;
		this.force = force;
		
		this.dialogTitle = dialogTitle;
		this.dialogDescription = dialogDescription;
		this.defaultMessage = defaultMessage;
	}

	public void createControlsImpl(Composite parent) {
    	GridLayout layout = null;
    	GridData data = null;
    	
    	Composite composite = new Composite(parent, SWT.NONE);
    	layout = new GridLayout();
    	layout.horizontalSpacing = 0;
    	layout.verticalSpacing = 0;
    	layout.marginWidth = 0;
    	layout.marginHeight = 0;
    	composite.setLayout(layout);
    	data = new GridData(GridData.FILL_BOTH);
    	composite.setLayoutData(data);
    	    	
    	if (this.hasComment) {
    		this.sForm = new SashForm(composite, SWT.VERTICAL);
        	layout = new GridLayout();
        	layout.marginHeight = layout.marginWidth = 0;
        	layout.verticalSpacing = 3;
        	this.sForm.setLayout(layout);
        	data = new GridData(GridData.FILL_BOTH);
        	data.heightHint = 400;
        	this.sForm.setLayoutData(data);    		    		        
        	
    		Composite commentParent = new Composite(this.sForm, SWT.NONE);
    		commentParent.setLayoutData(new GridData(GridData.FILL_BOTH));
    		layout = new GridLayout();
    		layout.verticalSpacing = 4;
    		layout.marginHeight = 0;
    		layout.marginWidth = 0;
    		commentParent.setLayout(layout);    		    		
    		
    		Group group = new Group(commentParent, SWT.NULL);
    		group.setLayout(new GridLayout());
    		data = new GridData(GridData.FILL_BOTH);
    		group.setLayoutData(data);
    		group.setText(SVNUIMessages.LockPanel_Comment);
    		
    		this.comment = new CommentComposite(group, this);
    		data = new GridData(GridData.FILL_BOTH);
    		this.comment.setLayoutData(data);
    		
    		this.forceButton = new Button(commentParent, SWT.CHECK);
    		data = new GridData();		
    		this.forceButton.setLayoutData(data);
    		this.forceButton.setText("Steal the locks"); //TODONLS
    		this.forceButton.setSelection(this.force);
    		this.forceButton.addSelectionListener(new SelectionAdapter() {			
    			public void widgetSelected(SelectionEvent e) {
    				LockResourcesPanel.this.force = forceButton.getSelection();
    			}
    		});
    		
        	Label separator = new Label(commentParent, SWT.SEPARATOR | SWT.HORIZONTAL);
    		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    	}    			
    	
    	this.selectionComposite = new LockResourceSelectionComposite(this.sForm != null ? this.sForm :  composite, SWT.NONE, true, true);
    	data = new GridData(GridData.FILL_BOTH);
		this.selectionComposite.setLayoutData(data);
		this.selectionComposite.setInput(this.resources);
		this.selectionComposite.addResourcesSelectionChangedListener(new ILockResourceSelectionChangeListener() {			
			public void resourcesSelectionChanged( LockResourceSelectionChangedEvent event) {
				LockResourcesPanel.this.validateContent();	
			}
		});		
		this.attachTo(this.selectionComposite, new AbstractVerifier() {
			protected String getErrorMessage(Control input) {
				LockResource []selection = LockResourcesPanel.this.getSelectedResources();
				if (selection == null || selection.length == 0) {
					return SVNUIMessages.ResourceSelectionComposite_Verifier_Error;
				}
				return null;
			}
			protected String getWarningMessage(Control input) {
				return null;
			}
		});
		
		if (this.hasComment) {
        	IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
    		int first = SVNTeamPreferences.getDialogInt(store, SVNTeamPreferences.LOCK_DIALOG_WEIGHT_NAME);
    		this.sForm.setWeights(new int[] {first, 100 - first});
		}
	}

	public String getMessage() {
		return this.hasComment ? this.comment.getMessage() : null;
	}
	
	public boolean getForce() {
		return this.hasComment ? this.force : false;
	}
	
	public LockResource[] getSelectedResources() {
		return this.selectionComposite.getSelectedResources();
	}
	
	protected void saveChangesImpl() {		
		if (this.hasComment) {
			this.comment.saveChanges();			
			this.savePreferences();						
		}    	
	}
	
	protected void savePreferences() {
		int[] weights = this.sForm.getWeights();
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		SVNTeamPreferences.setDialogInt(store, SVNTeamPreferences.LOCK_DIALOG_WEIGHT_NAME, weights[0] / 10);
	}
	
	protected void cancelChangesImpl() {
		if (this.hasComment) {
			this.comment.cancelChanges();
			this.savePreferences();		
		}
	}
}
