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
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.operation.local.CreatePatchOperation;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
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
	
	protected long diffOutputOptions;
	
	protected Button rootSelection;
	protected Button rootProject;
	protected Button rootWorkspace;

	public PatchOptionsPage(boolean localMode) {
		this(localMode, false);
	}
	
	public PatchOptionsPage(boolean localMode, boolean showIgnoreAncestry) {
		super(PatchOptionsPage.class.getName(), 
			SVNUIMessages.PatchOptionsPage_Title, 
			SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif")); //$NON-NLS-1$
		this.setDescription(SVNUIMessages.PatchOptionsPage_Description);
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
	
	public long getDiffOutputOptions() {
		return this.diffOutputOptions;
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
		options.setText(SVNUIMessages.PatchOptionsPage_Options);
		layout = new GridLayout();
		options.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		options.setLayoutData(data);
		
		Button recursiveButton = new Button(options, SWT.CHECK);
		data = new GridData();
		recursiveButton.setLayoutData(data);
		recursiveButton.setText(SVNUIMessages.PatchOptionsPage_Recurse);
		recursiveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PatchOptionsPage.this.recursive = ((Button)e.widget).getSelection();
			}
		});
		recursiveButton.setSelection(this.recursive = true);
		
		Button processBinaryButton = new Button(options, SWT.CHECK);
		data = new GridData();
		processBinaryButton.setLayoutData(data);
		processBinaryButton.setText(SVNUIMessages.PatchOptionsPage_Binary);
		processBinaryButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PatchOptionsPage.this.processBinary = ((Button)e.widget).getSelection();
				PatchOptionsPage.this.validateContent();
			}
		});
		processBinaryButton.setSelection(this.processBinary = false);
		AbstractVerifier verifier = new AbstractFormattedVerifier(SVNUIMessages.PatchOptionsPage_ProcessBinary_Verifier) {
		    protected String getErrorMessageImpl(Control input) {
		        return null;
		    }
		    protected String getWarningMessageImpl(Control input) {
		    	if (((Button)input).getSelection()) {
		            return SVNUIMessages.format(SVNUIMessages.PatchOptionsPage_ProcessBinary_Verifier_Warning, new String[] {AbstractFormattedVerifier.FIELD_NAME});
		        }
		        return null;
		    }
		};
		this.attachTo(processBinaryButton, verifier);
		
		Button processDeletedButton = new Button(options, SWT.CHECK);
		data = new GridData();
		processDeletedButton.setLayoutData(data);
		processDeletedButton.setText(SVNUIMessages.PatchOptionsPage_Deleted);
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
			processUnversionedButton.setText(SVNUIMessages.PatchOptionsPage_New);
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
			showIgnoreAncestryButton.setText(SVNUIMessages.PatchOptionsPage_Ancestry);
			showIgnoreAncestryButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					PatchOptionsPage.this.ignoreAncestry = showIgnoreAncestryButton.getSelection();
				}
			});
			showIgnoreAncestryButton.setSelection(this.ignoreAncestry);
		}
		
		if (CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() >= ISVNConnectorFactory.APICompatibility.SVNAPI_1_8_x) {
			Group outputOptions = new Group(composite, SWT.NONE);
			outputOptions.setText(SVNUIMessages.PatchOptionsPage_DiffOutputOptions);
			layout = new GridLayout();
			outputOptions.setLayout(layout);
			data = new GridData(GridData.FILL_HORIZONTAL);
			outputOptions.setLayoutData(data);
			
			Button ignoreWhitespaceButton = new Button(outputOptions, SWT.CHECK);
			data = new GridData();
			ignoreWhitespaceButton.setLayoutData(data);
			ignoreWhitespaceButton.setText(SVNUIMessages.PatchOptionsPage_IgnoreWhitespace);
			ignoreWhitespaceButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (((Button)e.widget).getSelection()) {
						PatchOptionsPage.this.diffOutputOptions |= ISVNConnector.DiffOptions.IGNORE_WHITESPACE;
					}
					else {
						PatchOptionsPage.this.diffOutputOptions &= ~ISVNConnector.DiffOptions.IGNORE_WHITESPACE;
					}
				}
			});
			ignoreWhitespaceButton.setSelection((this.diffOutputOptions & ISVNConnector.DiffOptions.IGNORE_WHITESPACE) != 0);
			
			Button ignoreSpaceChangeButton = new Button(outputOptions, SWT.CHECK);
			data = new GridData();
			ignoreSpaceChangeButton.setLayoutData(data);
			ignoreSpaceChangeButton.setText(SVNUIMessages.PatchOptionsPage_IgnoreSpaceChange);
			ignoreSpaceChangeButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (((Button)e.widget).getSelection()) {
						PatchOptionsPage.this.diffOutputOptions |= ISVNConnector.DiffOptions.IGNORE_SPACE_CHANGE;
					}
					else {
						PatchOptionsPage.this.diffOutputOptions &= ~ISVNConnector.DiffOptions.IGNORE_SPACE_CHANGE;
					}
				}
			});
			ignoreSpaceChangeButton.setSelection((this.diffOutputOptions & ISVNConnector.DiffOptions.IGNORE_SPACE_CHANGE) != 0);
			
			Button ignoreEOLStyleButton = new Button(outputOptions, SWT.CHECK);
			data = new GridData();
			ignoreEOLStyleButton.setLayoutData(data);
			ignoreEOLStyleButton.setText(SVNUIMessages.PatchOptionsPage_IgnoreEOLStyle);
			ignoreEOLStyleButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (((Button)e.widget).getSelection()) {
						PatchOptionsPage.this.diffOutputOptions |= ISVNConnector.DiffOptions.IGNORE_EOL_STYLE;
					}
					else {
						PatchOptionsPage.this.diffOutputOptions &= ~ISVNConnector.DiffOptions.IGNORE_EOL_STYLE;
					}
				}
			});
			ignoreEOLStyleButton.setSelection((this.diffOutputOptions & ISVNConnector.DiffOptions.IGNORE_EOL_STYLE) != 0);
			
			Button showCFunctionButton = new Button(outputOptions, SWT.CHECK);
			data = new GridData();
			showCFunctionButton.setLayoutData(data);
			showCFunctionButton.setText(SVNUIMessages.PatchOptionsPage_ShowCFunction);
			showCFunctionButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (((Button)e.widget).getSelection()) {
						PatchOptionsPage.this.diffOutputOptions |= ISVNConnector.DiffOptions.SHOW_FUNCTION;
					}
					else {
						PatchOptionsPage.this.diffOutputOptions &= ~ISVNConnector.DiffOptions.SHOW_FUNCTION;
					}
				}
			});
			showCFunctionButton.setSelection((this.diffOutputOptions & ISVNConnector.DiffOptions.SHOW_FUNCTION) != 0);
			
			Button useGITFormatButton = new Button(outputOptions, SWT.CHECK);
			data = new GridData();
			useGITFormatButton.setLayoutData(data);
			useGITFormatButton.setText(SVNUIMessages.PatchOptionsPage_GITFormat);
			useGITFormatButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (((Button)e.widget).getSelection()) {
						PatchOptionsPage.this.diffOutputOptions |= ISVNConnector.DiffOptions.GIT_FORMAT;
					}
					else {
						PatchOptionsPage.this.diffOutputOptions &= ~ISVNConnector.DiffOptions.GIT_FORMAT;
					}
				}
			});
			useGITFormatButton.setSelection((this.diffOutputOptions & ISVNConnector.DiffOptions.GIT_FORMAT) != 0);
		}
		
		if (this.localMode) { 
			Group patchRoot = new Group(composite, SWT.NONE);
			patchRoot.setText(SVNUIMessages.PatchOptionsPage_PatchRoot);
			layout = new GridLayout();
			patchRoot.setLayout(layout);
			data = new GridData(GridData.FILL_HORIZONTAL);
			patchRoot.setLayoutData(data);
			
			this.rootWorkspace = new Button(patchRoot, SWT.RADIO);
			data = new GridData();
			this.rootWorkspace.setLayoutData(data);
			this.rootWorkspace.setText(SVNUIMessages.PatchOptionsPage_PatchRootWorkspace);
			this.rootWorkspace.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					PatchOptionsPage.this.rootPoint = CreatePatchOperation.WORKSPACE;
				}
			});
			
			this.rootProject = new Button(patchRoot, SWT.RADIO);
			data = new GridData();
			this.rootProject.setLayoutData(data);
			this.rootProject.setText(SVNUIMessages.PatchOptionsPage_PatchRootProject);
			this.rootProject.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					PatchOptionsPage.this.rootPoint = CreatePatchOperation.PROJECT;
				}
			});
			
			this.rootSelection = new Button(patchRoot, SWT.RADIO);
			data = new GridData();
			this.rootSelection.setLayoutData(data);
			this.rootSelection.setText(SVNUIMessages.PatchOptionsPage_PatchRootSelection);
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
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, "org.eclipse.team.svn.help.patchOptionsContext"); //$NON-NLS-1$
		
		return composite;
	}

}
