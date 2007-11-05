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

package org.eclipse.team.svn.ui.operation;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.local.AbstractWorkingCopyOperation;
import org.eclipse.team.svn.core.operation.local.DetectDeletedProjectsOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.management.DisconnectOperation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.ProjectListComposite;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * The operation process incoming deletions for projects
 * 
 * @author Alexander Gurov
 */
public class ProcessDeletedProjectsOperation extends AbstractWorkingCopyOperation {
	
	protected boolean toDeleteSVNmeta;
	protected boolean toDisconnect;
	
	public ProcessDeletedProjectsOperation(final DetectDeletedProjectsOperation detectOp) {
		super("Operation.ProcessDeletedProjects", new IResourceProvider() {
			public IResource []getResources() {
				return detectOp.getDeleted();
			}
		});
		toDeleteSVNmeta = true;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		final IProject []toDisconnect = (IProject [])this.operableData();
		if (toDisconnect == null || toDisconnect.length == 0) {
			return;
		}
				
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				ProjectListPanel panel = new ProjectListPanel(toDisconnect);
				DefaultDialog dlg = new DefaultDialog(UIMonitorUtility.getShell(), panel);
				if (dlg.open() == DefaultDialog.OK) {
					ProcessDeletedProjectsOperation.this.toDisconnect = true;
				}
				else {
					ProcessDeletedProjectsOperation.this.toDisconnect = false;
				}
			}
		});
		
		if (!this.toDisconnect) {
			return;
		}

		DisconnectOperation mainOp = new DisconnectOperation(toDisconnect, this.toDeleteSVNmeta);
		CompositeOperation op = new CompositeOperation(mainOp.getId());
		op.add(mainOp);
		op.add(new RefreshResourcesOperation(toDisconnect, IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_CACHE));
		this.reportStatus(op.run(monitor).getStatus());
	}

	protected class ProjectListPanel extends AbstractDialogPanel {
		protected IProject []resources;
		protected TableViewer tableViewer;
		protected Button deleteSVNMetaButton;
		
		public ProjectListPanel(IProject []input) {
			super(new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL});
			
			this.dialogTitle = ProcessDeletedProjectsOperation.this.getOperationResource("ProjectList.Title");
			this.dialogDescription = ProcessDeletedProjectsOperation.this.getOperationResource("ProjectList.Description");
			this.defaultMessage = ProcessDeletedProjectsOperation.this.getOperationResource("ProjectList.Message");
			this.resources = input;
		}
		
	    public void createControls(Composite parent) {
	    	ProjectListComposite composite = new ProjectListComposite(parent, SWT.FILL, this.resources, false);
	    	composite.initialize();
	    	this.deleteSVNMetaButton = new Button(parent, SWT.CHECK);
	    	this.deleteSVNMetaButton.setText(SVNTeamUIPlugin.instance().getResource("DisconnectDialog.Option.dropSVNMeta"));
	    	this.deleteSVNMetaButton.setSelection(true);
	    	this.deleteSVNMetaButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					ProcessDeletedProjectsOperation.this.toDeleteSVNmeta = ProjectListPanel.this.deleteSVNMetaButton.getSelection();
				}
			});
	    }
	    
	    protected void saveChanges() {

	    }

	    protected void cancelChanges() {

	    }
		
	}
	
}
