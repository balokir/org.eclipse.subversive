/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.local;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.verifier.AbstractFormattedVerifier;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.ResourceNameVerifier;
import org.eclipse.ui.internal.ide.misc.ContainerContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * The panel allows to select where new file will be placed in workspace
 * 
 * @author Alexander Gurov
 */
public class SavePatchInWorkspacePanel extends AbstractDialogPanel {
	protected TreeViewer treeViewer;
	protected Text workspaceFilenameField;
	protected String proposedName;
	
	protected IFile file;

	public SavePatchInWorkspacePanel(String proposedName) {
		super();
		this.dialogTitle = SVNUIMessages.SavePatchInWorkspace_Title;
		this.dialogDescription = SVNUIMessages.SavePatchInWorkspace_Description;
		this.defaultMessage = SVNUIMessages.SavePatchInWorkspace_Message;
		this.proposedName = proposedName;
	}
	
	public IFile getFile() {
		return this.file;
	}
	
    public String getHelpId() {
    	return "org.eclipse.team.svn.help.savePatchInWorkspaceContext"; //$NON-NLS-1$
    }
    
	protected Point getPrefferedSizeImpl() {
		return new Point(640, 300);
	}

	protected void createControlsImpl(Composite parent) {
		GridData data = null;
		GridLayout layout = null;
		
		this.treeViewer = new TreeViewer(parent, SWT.BORDER);
		data = new GridData(GridData.FILL_BOTH);
		this.treeViewer.getTree().setLayoutData(data);
		ContainerContentProvider cp = new ContainerContentProvider();
		cp.showClosedProjects(false);
		this.treeViewer.setContentProvider(cp);
		this.treeViewer.setLabelProvider(new WorkbenchLabelProvider());
		this.treeViewer.setInput(ResourcesPlugin.getWorkspace());
		this.treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				SavePatchInWorkspacePanel.this.validateContent();
			}
		});
		AbstractFormattedVerifier verifier = new AbstractFormattedVerifier(SVNUIMessages.SavePatchInWorkspace_WorkspaceTree) {
			protected String getWarningMessageImpl(Control input) {
				return null;
			}
			protected String getErrorMessageImpl(Control input) {
				IStructuredSelection selection = (IStructuredSelection)SavePatchInWorkspacePanel.this.treeViewer.getSelection();
				return selection != null && !selection.isEmpty() ? null : SVNUIMessages.SavePatchInWorkspace_WorkspaceTree_Verifier_Error;
			}
		};
		this.attachTo(this.treeViewer.getTree(), verifier);

		Composite wsComposite = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		wsComposite.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		wsComposite.setLayoutData(data);
		
		Label description = new Label(wsComposite,SWT.NONE);
		data = new GridData();
		description.setLayoutData(data);
		description.setText(SVNUIMessages.SavePatchInWorkspace_FileName);
	
		this.workspaceFilenameField = new Text(wsComposite, SWT.BORDER | SWT.SINGLE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.workspaceFilenameField.setLayoutData(data);
		CompositeVerifier cVerifier = new CompositeVerifier();
		String name = SVNUIMessages.SavePatchInWorkspace_FileName_Verifier;
		cVerifier.add(new NonEmptyFieldVerifier(name));
		cVerifier.add(new ResourceNameVerifier(name, false));
		cVerifier.add(new AbstractFormattedVerifier(name) {
		    protected String getErrorMessageImpl(Control input) {
		        return null;
		    }
		    protected String getWarningMessageImpl(Control input) {
		    	IFile file = SavePatchInWorkspacePanel.this.makeFile();
		        if (file != null && file.isAccessible()) {
		            return SVNUIMessages.format(SVNUIMessages.SavePatchInWorkspace_FileName_Verifier_Warning, new String[] {AbstractFormattedVerifier.FIELD_NAME});
		        }
		        return null;
		    }
		});
		this.attachTo(this.workspaceFilenameField, cVerifier);
		
		this.workspaceFilenameField.setText(this.proposedName);
	}

	protected void cancelChangesImpl() {

	}

	protected void saveChangesImpl() {
		this.file = this.makeFile();
	}
	
	protected IFile makeFile() {
		String fileName = this.workspaceFilenameField.getText();
		IStructuredSelection selection = (IStructuredSelection)this.treeViewer.getSelection();
		if (selection != null && !selection.isEmpty()) {
			IContainer treeNode = (IContainer)selection.getFirstElement();
			return treeNode.getProject().getFile(treeNode.getFullPath().append(fileName).removeFirstSegments(1));
		}
		return null;
	}

}
