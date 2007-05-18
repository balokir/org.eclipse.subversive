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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.operation.local.AbstractWorkingCopyOperation;
import org.eclipse.team.svn.core.operation.local.DetectDeletedProjectsOperation;
import org.eclipse.team.svn.core.operation.local.management.DisconnectOperation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
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
	public ProcessDeletedProjectsOperation(final DetectDeletedProjectsOperation detectOp) {
		super("Operation.ProcessDeletedProjects", new IResourceProvider() {
			public IResource []getResources() {
				return detectOp.getDeleted();
			}
		});
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
				dlg.open();
			}
		});

		this.reportStatus(new DisconnectOperation(toDisconnect, false).run(monitor).getStatus());
	}

	protected class ProjectListPanel extends AbstractDialogPanel {
		protected IProject []resources;
		protected TableViewer tableViewer;
		
		public ProjectListPanel(IProject []input) {
			super(new String[] {IDialogConstants.OK_LABEL});
			
			this.dialogTitle = ProcessDeletedProjectsOperation.this.getOperationResource("ProjectList.Title");
			this.dialogDescription = ProcessDeletedProjectsOperation.this.getOperationResource("ProjectList.Description");
			this.defaultMessage = ProcessDeletedProjectsOperation.this.getOperationResource("ProjectList.Message");
			this.resources = input;
		}
		
	    public void createControls(Composite parent) {
	    	ProjectListComposite composite = new ProjectListComposite(parent, SWT.FILL, this.resources, false);
	    	composite.initialize();
	    }
	    
	    protected void saveChanges() {

	    }

	    protected void cancelChanges() {

	    }
		
	}
	
}
