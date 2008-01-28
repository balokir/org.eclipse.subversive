/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.wizard.createpatch;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.team.svn.core.operation.local.CreatePatchOperation;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.verifier.AbstractFormattedVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;
import org.eclipse.team.svn.ui.wizard.AbstractVerifiedWizardPage;
import org.eclipse.ui.PlatformUI;

/**
 * Create patch options page
 * 
 * @author Alexander Gurov
 */
public class PatchOptionsPage extends AbstractVerifiedWizardPage {
	protected boolean recursive;
	protected boolean ignoreDeleted;
	protected boolean processBinary;
	protected boolean processUnversioned;
	protected boolean localMode;
	protected boolean showIgnoreAncestry;
	protected boolean ignoreAncestry;
	protected int rootPoint;
	protected boolean multiSelect;
	
	protected Button rootSelection;
	protected Button rootProject;
	protected Button rootWorkspace;

	public PatchOptionsPage(boolean localMode) {
		this(localMode, false);
	}
	
	public PatchOptionsPage(boolean localMode, boolean showIgnoreAncestry) {
		super(PatchOptionsPage.class.getName(), 
			SVNTeamUIPlugin.instance().getResource("PatchOptionsPage.Title"), 
			SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif"));
		this.setDescription(SVNTeamUIPlugin.instance().getResource("PatchOptionsPage.Description"));
		this.localMode = localMode;
		this.showIgnoreAncestry = showIgnoreAncestry;
		this.ignoreAncestry = true;
	}

	public void setMultiSelect(boolean multiSelect) {
		if (this.localMode && this.multiSelect != multiSelect) {
			this.rootWorkspace.setSelection(multiSelect);
			this.rootProject.setSelection(!multiSelect);
			this.rootProject.setEnabled(!multiSelect);
			this.rootSelection.setSelection(false);
			this.rootSelection.setEnabled(!multiSelect);
		}
		this.rootPoint = multiSelect ? CreatePatchOperation.WORKSPACE : CreatePatchOperation.PROJECT;
		this.multiSelect = multiSelect;
	}
	
	public int getRootPoint() {
		return this.rootPoint;
	}
	
	public boolean isIgnoreDeleted() {
		return this.ignoreDeleted;
	}

	public boolean isProcessBinary() {
		return this.processBinary;
	}

	public boolean isProcessUnversioned() {
		return this.processUnversioned;
	}

	public boolean isRecursive() {
		return this.recursive;
	}
	
	public boolean isIgnoreAncestry() {
		return this.showIgnoreAncestry ? this.ignoreAncestry : true;
	}

	protected Composite createControlImpl(Composite parent) {
		GridLayout layout = null;
		GridData data = null;
		
		Composite composite = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 4;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		Group options = new Group(composite, SWT.NONE);
		options.setText(SVNTeamUIPlugin.instance().getResource("PatchOptionsPage.Options"));
		layout = new GridLayout();
		options.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		options.setLayoutData(data);
		
		Button recursiveButton = new Button(options, SWT.CHECK);
		data = new GridData();
		recursiveButton.setLayoutData(data);
		recursiveButton.setText(SVNTeamUIPlugin.instance().getResource("PatchOptionsPage.Recurse"));
		recursiveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PatchOptionsPage.this.recursive = ((Button)e.widget).getSelection();
			}
		});
		recursiveButton.setSelection(this.recursive = true);
		
		Button processBinaryButton = new Button(options, SWT.CHECK);
		data = new GridData();
		processBinaryButton.setLayoutData(data);
		processBinaryButton.setText(SVNTeamUIPlugin.instance().getResource("PatchOptionsPage.Binary"));
		processBinaryButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PatchOptionsPage.this.processBinary = ((Button)e.widget).getSelection();
				PatchOptionsPage.this.validateContent();
			}
		});
		processBinaryButton.setSelection(this.processBinary = false);
		AbstractVerifier verifier = new AbstractFormattedVerifier(SVNTeamUIPlugin.instance().getResource("PatchOptionsPage.ProcessBinary.Verifier")) {
		    protected String getErrorMessageImpl(Control input) {
		        return null;
		    }
		    protected String getWarningMessageImpl(Control input) {
		    	if (((Button)input).getSelection()) {
		            return SVNTeamUIPlugin.instance().getResource("PatchOptionsPage.ProcessBinary.Verifier.Warning", new String[] {AbstractFormattedVerifier.FIELD_NAME});
		        }
		        return null;
		    }
		};
		this.attachTo(processBinaryButton, verifier);
		
		Button processDeletedButton = new Button(options, SWT.CHECK);
		data = new GridData();
		processDeletedButton.setLayoutData(data);
		processDeletedButton.setText(SVNTeamUIPlugin.instance().getResource("PatchOptionsPage.Deleted"));
		processDeletedButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PatchOptionsPage.this.ignoreDeleted = !((Button)e.widget).getSelection();
			}
		});
		processDeletedButton.setSelection(!(this.ignoreDeleted = false));

		if (this.localMode) {
			Button processUnversionedButton = new Button(options, SWT.CHECK);
			data = new GridData();
			processUnversionedButton.setLayoutData(data);
			processUnversionedButton.setText(SVNTeamUIPlugin.instance().getResource("PatchOptionsPage.New"));
			processUnversionedButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					PatchOptionsPage.this.processUnversioned = ((Button)e.widget).getSelection();
				}
			});
			processUnversionedButton.setSelection(this.processUnversioned = true);
		}
		
		if (this.showIgnoreAncestry) {
			final Button showIgnoreAncestryButton = new Button(options, SWT.CHECK);
			data = new GridData();
			showIgnoreAncestryButton.setLayoutData(data);
			showIgnoreAncestryButton.setText(SVNTeamUIPlugin.instance().getResource("PatchOptionsPage.Ancestry"));
			showIgnoreAncestryButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					PatchOptionsPage.this.ignoreAncestry = showIgnoreAncestryButton.getSelection();
				}
			});
			showIgnoreAncestryButton.setSelection(this.ignoreAncestry);
		}
		
		if (this.localMode) { 
			Group patchRoot = new Group(composite, SWT.NONE);
			patchRoot.setText(SVNTeamUIPlugin.instance().getResource("PatchOptionsPage.PatchRoot"));
			layout = new GridLayout();
			patchRoot.setLayout(layout);
			data = new GridData(GridData.FILL_HORIZONTAL);
			patchRoot.setLayoutData(data);
			
			this.rootWorkspace = new Button(patchRoot, SWT.RADIO);
			data = new GridData();
			this.rootWorkspace.setLayoutData(data);
			this.rootWorkspace.setText(SVNTeamUIPlugin.instance().getResource("PatchOptionsPage.PatchRootWorkspace"));
			this.rootWorkspace.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					PatchOptionsPage.this.rootPoint = CreatePatchOperation.WORKSPACE;
				}
			});
			
			this.rootProject = new Button(patchRoot, SWT.RADIO);
			data = new GridData();
			this.rootProject.setLayoutData(data);
			this.rootProject.setText(SVNTeamUIPlugin.instance().getResource("PatchOptionsPage.PatchRootProject"));
			this.rootProject.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					PatchOptionsPage.this.rootPoint = CreatePatchOperation.PROJECT;
				}
			});
			
			this.rootSelection = new Button(patchRoot, SWT.RADIO);
			data = new GridData();
			this.rootSelection.setLayoutData(data);
			this.rootSelection.setText(SVNTeamUIPlugin.instance().getResource("PatchOptionsPage.PatchRootSelection"));
			this.rootSelection.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					PatchOptionsPage.this.rootPoint = CreatePatchOperation.SELECTION;
				}
			});
			this.rootWorkspace.setSelection(this.multiSelect);
			this.rootProject.setSelection(!this.multiSelect);
			this.rootProject.setEnabled(!this.multiSelect);
			this.rootSelection.setSelection(false);
			this.rootSelection.setEnabled(!this.multiSelect);
		}
		
//		Setting context help
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, "org.eclipse.team.svn.help.patchOptionsContext");
		
		return composite;
	}

}
