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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.BranchTagSelectionComposite;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;

/**
 * Panel for the Replace With Branch/Tag dialog
 * 
 * @author Alexei Goncharov
 */
public class ReplaceBranchTagPanel extends AbstractDialogPanel {
	protected IRepositoryResource baseResource;
	protected int type;
	protected IRepositoryResource[] branchTagResources;
	protected long currentRevision;
	protected String historyKey;
	protected BranchTagSelectionComposite selectionComposite;
	protected Label resultText;
	
	protected IRepositoryResource resourceToReplaceWith;
	
	public ReplaceBranchTagPanel(IRepositoryResource baseResource, long currentRevision, int type, IRepositoryResource[] branchTagResources) {
		super();
		this.baseResource = baseResource;
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
        this.selectionComposite = new BranchTagSelectionComposite(parent, SWT.NONE, this.baseResource, this.historyKey, this, this.type, this.branchTagResources);
        data = new GridData(GridData.FILL_HORIZONTAL);
        this.selectionComposite.setLayoutData(data);
        this.selectionComposite.setCurrentRevision(this.currentRevision);
        
        Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Label label = new Label(parent, SWT.NONE);
        data = new GridData(GridData.FILL_HORIZONTAL);
        label.setLayoutData(data);
        label.setText(SVNUIMessages.ReplaceBranchTagPanel_ResultDescription);
        
        Composite resultComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 2;
		resultComposite.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		resultComposite.setLayoutData(data);		
		resultComposite.setBackground(UIMonitorUtility.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		
		this.resultText = new Label(resultComposite, SWT.SINGLE | SWT.WRAP);
		this.resultText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
		this.resultText.setBackground(UIMonitorUtility.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		
        this.selectionComposite.addUrlModifyListener(new Listener() {
			public void handleEvent(Event event) {
				ReplaceBranchTagPanel.this.setResultLabel();
			}
		});
        this.selectionComposite.addUrlVerifier(new AbstractVerifier() {
			protected String getErrorMessage(Control input) {
				/*
				 * As resourceToReplaceWith may be not yet re-calculated, we do it explicitly here 
				 */
				if (BranchTagSelectionComposite.getResourceToCompareWith(ReplaceBranchTagPanel.this.baseResource, ReplaceBranchTagPanel.this.getSelectedResource()) == null) {
					return SVNUIMessages.ReplaceBranchTagPanel_ConstructResultVerifierError;
				}
				return null;
			}
			protected String getWarningMessage(Control input) {				
				return null;
			}        	
        });
        
		this.setResultLabel();
	}
	
	protected void setResultLabel() {
		String text = ""; //$NON-NLS-1$
		this.resourceToReplaceWith = null;
		
		if (this.getSelectedResource() != null) {
			this.resourceToReplaceWith = BranchTagSelectionComposite.getResourceToCompareWith(this.baseResource, this.getSelectedResource());			
			if (this.resourceToReplaceWith != null) {				
				text = this.resourceToReplaceWith.getUrl();				
			} else {
				text = SVNUIMessages.ReplaceBranchTagPanel_ResultNone;
			}	
		}	
		this.resultText.setText(text);	
	}
		
	private IRepositoryResource getSelectedResource() {
		return this.selectionComposite.getSelectedResource();
	}

	public IRepositoryResource getResourceToReplaceWith() {
		return this.resourceToReplaceWith;
	}
	
	protected void saveChangesImpl() {
		this.selectionComposite.saveChanges();
	}
	
	protected void cancelChangesImpl() {
	}

}