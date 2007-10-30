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

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.misc.ContainerContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.verifier.AbstractFormattedVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.ResourceNameVerifier;
import org.eclipse.team.svn.ui.verifier.ResourcePathVerifier;
import org.eclipse.team.svn.ui.wizard.AbstractVerifiedWizardPage;
import org.eclipse.team.svn.ui.wizard.CreatePatchWizard;

/**
 * Select patch file wizard page
 * 
 * @author Alexander Gurov
 */
public class SelectPatchFilePage extends AbstractVerifiedWizardPage {
	protected Text fileNameField;
	protected Button browseButton;
	protected TreeViewer treeViewer;
	protected Text workspaceFilenameField;
	
	protected String proposedName;
	
	protected String fileName;
	protected int writeMode;

	public SelectPatchFilePage(String proposedName) {
		super(
			SelectPatchFilePage.class.getName(), 
			SVNTeamUIPlugin.instance().getResource("SelectPatchFilePage.Title"), 
			SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif"));
		this.setDescription(SVNTeamUIPlugin.instance().getResource("SelectPatchFilePage.Description"));
		this.proposedName = proposedName;
		this.writeMode = CreatePatchWizard.WRITE_TO_CLIPBOARD;
		try {
			File tmp = File.createTempFile("patch", "tmp");
			tmp.delete();
			SelectPatchFilePage.this.fileName = tmp.getAbsolutePath();
		} 
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getFileName() {
		return this.fileName;
	}
	
	public int getWriteMode() {
		return this.writeMode;
	}

	protected Composite createControlImpl(Composite parent) {
		GridLayout layout = null;
		GridData data = null;
		
		Composite composite = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		composite.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		Button saveToClipboard = new Button(composite, SWT.RADIO);
		saveToClipboard.setText(SVNTeamUIPlugin.instance().getResource("SelectPatchFilePage.SaveToClipboard"));
		data = new GridData(GridData.FILL_HORIZONTAL);
		saveToClipboard.setLayoutData(data);
		saveToClipboard.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Button button = (Button)event.widget;
				if (button.getSelection()) {
					SelectPatchFilePage.this.fileNameField.setEnabled(false);
					SelectPatchFilePage.this.browseButton.setEnabled(false);
					SelectPatchFilePage.this.workspaceFilenameField.setEnabled(false);
					SelectPatchFilePage.this.treeViewer.getTree().setEnabled(false);
					try {
						SelectPatchFilePage.this.fileName = File.createTempFile("patch", ".tmp").getAbsolutePath();
					} 
					catch (IOException e) {
						throw new RuntimeException(e);
					}
					SelectPatchFilePage.this.writeMode = CreatePatchWizard.WRITE_TO_CLIPBOARD;
					SelectPatchFilePage.this.validateContent();
				}
			}
		});
		
		final Button saveOnFileSystem = new Button(composite, SWT.RADIO);
		saveOnFileSystem.setText(SVNTeamUIPlugin.instance().getResource("SelectPatchFilePage.SaveInFS"));
		data = new GridData(GridData.FILL_HORIZONTAL);
		saveOnFileSystem.setLayoutData(data);
		saveOnFileSystem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Button button = (Button)event.widget;
				if (button.getSelection()) {
					SelectPatchFilePage.this.fileNameField.setEnabled(true);
					SelectPatchFilePage.this.browseButton.setEnabled(true);
					SelectPatchFilePage.this.workspaceFilenameField.setEnabled(false);
					SelectPatchFilePage.this.treeViewer.getTree().setEnabled(false);
					SelectPatchFilePage.this.fileName = SelectPatchFilePage.this.fileNameField.getText();
					SelectPatchFilePage.this.writeMode = CreatePatchWizard.WRITE_TO_EXTERNAL_FILE;
					SelectPatchFilePage.this.validateContent();
				}
			}
		});

		Composite fsComposite = new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		fsComposite.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		fsComposite.setLayoutData(data);
		
		this.fileNameField = new Text(fsComposite, SWT.BORDER | SWT.SINGLE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.fileNameField.setLayoutData(data);
		this.fileNameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				SelectPatchFilePage.this.fileName = SelectPatchFilePage.this.fileNameField.getText();
			}
		});
		CompositeVerifier cVerifier = new CompositeVerifier();
		String name = SVNTeamUIPlugin.instance().getResource("SelectPatchFilePage.SaveInFS.Verifier");
		cVerifier.add(new NonEmptyFieldVerifier(name));
		cVerifier.add(new ResourcePathVerifier(name));
		cVerifier.add(new AbstractFormattedVerifier(name) {
		    protected String getErrorMessageImpl(Control input) {
		        return null;
		    }
		    protected String getWarningMessageImpl(Control input) {
		        String text = this.getText(input);
		        if (new File(text).exists()) {
		        	String message = SVNTeamUIPlugin.instance().getResource("SelectPatchFilePage.SaveInFS.Verifier.Warning");
		            return MessageFormat.format(message, new String[] {AbstractFormattedVerifier.FIELD_NAME});
		        }
		        return null;
		    }
		});
		this.attachTo(this.fileNameField, new AbstractVerifierProxy(cVerifier) {
			protected boolean isVerificationEnabled(Control input) {
				return saveOnFileSystem.getSelection();
			}
		});

		this.browseButton = new Button(fsComposite, SWT.PUSH);
		this.browseButton.setText(SVNTeamUIPlugin.instance().getResource("Button.Browse"));
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(this.browseButton);
		this.browseButton.setLayoutData(data);
		this.browseButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				FileDialog dlg = new FileDialog(SelectPatchFilePage.this.getShell(), SWT.PRIMARY_MODAL | SWT.SAVE);
				dlg.setText(SVNTeamUIPlugin.instance().getResource("SelectPatchFilePage.SavePatchAs"));
				dlg.setFileName(SelectPatchFilePage.this.proposedName + ".patch");
				dlg.setFilterExtensions(new String[] {"patch", "*.*"});
				String file = dlg.open();
				if (file != null) {
					SelectPatchFilePage.this.fileName = file;
					SelectPatchFilePage.this.fileNameField.setText(file);
					SelectPatchFilePage.this.validateContent();
				}			
			}
		});			
		
		final Button saveInWorkspace = new Button(composite, SWT.RADIO);
		data = new GridData(GridData.FILL_HORIZONTAL);
		saveInWorkspace.setLayoutData(data);
		saveInWorkspace.setText(SVNTeamUIPlugin.instance().getResource("SelectPatchFilePage.SaveInWS"));
		saveInWorkspace.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Button button = (Button)event.widget;
				if (button.getSelection()) {
					SelectPatchFilePage.this.fileNameField.setEnabled(false);
					SelectPatchFilePage.this.browseButton.setEnabled(false);
					SelectPatchFilePage.this.workspaceFilenameField.setEnabled(true);
					SelectPatchFilePage.this.treeViewer.getTree().setEnabled(true);
					SelectPatchFilePage.this.fileName = SelectPatchFilePage.this.getComposedFileName();
					SelectPatchFilePage.this.writeMode = CreatePatchWizard.WRITE_TO_WORKSPACE_FILE;
					SelectPatchFilePage.this.validateContent();
				}
			}
		});
		
		Label description = new Label(composite, SWT.LEFT);
		data = new GridData(GridData.FILL_HORIZONTAL);
		description.setLayoutData(data);
		description.setText(SVNTeamUIPlugin.instance().getResource("SelectPatchFilePage.SaveInWS.Hint"));

		this.treeViewer = new TreeViewer(composite, SWT.BORDER);
		data = new GridData(GridData.FILL_BOTH);
		this.treeViewer.getTree().setLayoutData(data);
		ContainerContentProvider cp = new ContainerContentProvider();
		cp.showClosedProjects(false);
		this.treeViewer.setContentProvider(cp);
		this.treeViewer.setLabelProvider(new WorkbenchLabelProvider());
		this.treeViewer.setInput(ResourcesPlugin.getWorkspace());
		this.treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				SelectPatchFilePage.this.fileName = SelectPatchFilePage.this.getComposedFileName();
				SelectPatchFilePage.this.validateContent();
			}
		});
		AbstractFormattedVerifier verifier = new AbstractFormattedVerifier(SVNTeamUIPlugin.instance().getResource("SelectPatchFilePage.WorkspaceTree")) {
			protected String getWarningMessageImpl(Control input) {
				return null;
			}
			protected String getErrorMessageImpl(Control input) {
				IStructuredSelection selection = (IStructuredSelection)SelectPatchFilePage.this.treeViewer.getSelection();
				return selection != null && !selection.isEmpty() ? null : SVNTeamUIPlugin.instance().getResource("SelectPatchFilePage.WorkspaceTree.Verifier.Error");
			}
		};
		this.attachTo(this.treeViewer.getTree(), new AbstractVerifierProxy(verifier) {
			protected boolean isVerificationEnabled(Control input) {
				return saveInWorkspace.getSelection();
			}
		});

		Composite wsComposite = new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		wsComposite.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		wsComposite.setLayoutData(data);
		
		description = new Label(wsComposite,SWT.NONE);
		data = new GridData();
		description.setLayoutData(data);
		description.setText(SVNTeamUIPlugin.instance().getResource("SelectPatchFilePage.FileName"));
	
		this.workspaceFilenameField = new Text(wsComposite, SWT.BORDER | SWT.SINGLE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.workspaceFilenameField.setLayoutData(data);
		this.workspaceFilenameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				SelectPatchFilePage.this.fileName = SelectPatchFilePage.this.getComposedFileName();
			}
		});
		cVerifier = new CompositeVerifier();
		name = SVNTeamUIPlugin.instance().getResource("SelectPatchFilePage.FileName.Verifier");
		cVerifier.add(new NonEmptyFieldVerifier(name));
		cVerifier.add(new ResourceNameVerifier(name, false));
		cVerifier.add(new AbstractFormattedVerifier(name) {
		    protected String getErrorMessageImpl(Control input) {
		        return null;
		    }
		    protected String getWarningMessageImpl(Control input) {
		    	IResource file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(SelectPatchFilePage.this.getComposedFileName()));
		        if (file != null && file.exists()) {
		        	String message = SVNTeamUIPlugin.instance().getResource("SelectPatchFilePage.FileName.Verifier.Warning");
		            return MessageFormat.format(message, new String[] {AbstractFormattedVerifier.FIELD_NAME});
		        }
		        return null;
		    }
		});
		this.attachTo(this.workspaceFilenameField, new AbstractVerifierProxy(cVerifier) {
			protected boolean isVerificationEnabled(Control input) {
				return saveInWorkspace.getSelection();
			}
		});
		
		this.fileNameField.setEnabled(false);
		this.browseButton.setEnabled(false);
		this.workspaceFilenameField.setEnabled(false);
		this.treeViewer.getTree().setEnabled(false);
		saveToClipboard.setSelection(true);
		
//		Setting context help
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, "org.eclipse.team.svn.help.patchFileContext");
		
		return composite;
	}

	protected String getComposedFileName() {
		String firstPart = "";
		IStructuredSelection selection = (IStructuredSelection)this.treeViewer.getSelection();
		if (selection != null && !selection.isEmpty()) {
			IResource treeNode = (IResource)selection.getFirstElement();
			firstPart = FileUtility.getWorkingCopyPath(treeNode);
		}
		return firstPart + "/" + this.workspaceFilenameField.getText();
	}
	
}
