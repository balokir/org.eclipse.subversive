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

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.remote.management.AddRepositoryLocationOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.PathSelectionComposite;
import org.eclipse.team.svn.ui.dialog.NonValidLocationErrorDialog;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Add repository panel
 * 
 * @author Igor Burilo
 */
public class AddRepositoryPanel extends AbstractDialogPanel {

	protected PathSelectionComposite pathSelectionComposite;
	protected Button createRepositoryLocaton;
	protected Button fsfsButton;
	protected Button bdbButton;	
	
	protected IActionOperation operationToPerform;
	
	public AddRepositoryPanel() {	
		super();
		this.dialogTitle = SVNUIMessages.AddRepositoryPage_Title;
		this.dialogDescription = SVNUIMessages.AddRepositoryPage_Description;	
		this.defaultMessage = SVNUIMessages.AddRepositoryPage_Message;		
	}

	protected void createControlsImpl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0; 
		layout.marginWidth = 0; 
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		//path
		this.pathSelectionComposite = new PathSelectionComposite(
			SVNUIMessages.AddRepositoryPage_RepositoryPath_Label,
			SVNUIMessages.AddRepositoryPage_RepositoryPath_Name,
			SVNUIMessages.AddRepositoryPage_DirectoryDialog_Title,
			SVNUIMessages.AddRepositoryPage_DirectoryDialog_Description,
			true,
			composite, 
			this);
		
		//repository type
		Group typeGroup = new Group(composite, SWT.NONE);
		layout = new GridLayout();
		layout.horizontalSpacing = 40;		
		layout.numColumns = 2;
		typeGroup.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		typeGroup.setLayoutData(data);
		typeGroup.setText(SVNUIMessages.AddRepositoryPage_RepositoryType_Group);
		
		this.fsfsButton = new Button(typeGroup, SWT.RADIO);
		data = new GridData();
		this.fsfsButton.setLayoutData(data);
		this.fsfsButton.setText(SVNUIMessages.AddRepositoryPage_FileSystem_Button);		
		this.fsfsButton.setSelection(true);
		this.bdbButton = new Button(typeGroup, SWT.RADIO);
		data = new GridData();
		this.bdbButton.setLayoutData(data);
		this.bdbButton.setText(SVNUIMessages.AddRepositoryPage_BerkeleyDB_Button);
		int features = CoreExtensionsManager.instance().getSVNConnectorFactory().getSupportedFeatures();
		if ((features & ISVNConnectorFactory.OptionalFeatures.CREATE_REPOSITORY_FSFS) == 0)
		{
			this.fsfsButton.setSelection(false);
			this.fsfsButton.setEnabled(false);
			this.bdbButton.setSelection(true);
		}
		if ((features & ISVNConnectorFactory.OptionalFeatures.CREATE_REPOSITORY_BDB) == 0)
		{
			this.bdbButton.setSelection(false);
			this.bdbButton.setEnabled(false);
		}
		
		//create repository location		
		this.createRepositoryLocaton = new Button(composite, SWT.CHECK);
		data = new GridData();
		this.createRepositoryLocaton.setLayoutData(data);
		this.createRepositoryLocaton.setText(SVNUIMessages.AddRepositoryPage_CreateRepositoryLocation_Button);
		this.createRepositoryLocaton.setSelection(true);							     		
	}
	
	public String getRepositoryPath() {
		return this.pathSelectionComposite.getSelectedPath();		
	}
	
	public String getRepositoryType() {
		return this.fsfsButton.getSelection() ? ISVNConnector.REPOSITORY_FSTYPE_FSFS : ISVNConnector.REPOSITORY_FSTYPE_BDB;
	}
	
	public boolean isCreateRepositoryLocation() {
		return this.createRepositoryLocaton.getSelection();
	}
	
	public boolean performFinish() {		
		final String repositoryType = this.getRepositoryType();				
		File repositoryPathFile = new File(this.getRepositoryPath());
		final String repositoryPath =  repositoryPathFile.isAbsolute() ? this.getRepositoryPath() : repositoryPathFile.getAbsolutePath();
		
		String url = "file:///" + repositoryPath; //$NON-NLS-1$
		url = SVNUtility.normalizeURL(url);				
				
		final IRepositoryLocation location = SVNRemoteStorage.instance().newRepositoryLocation();
		SVNUtility.initializeRepositoryLocation(location, url);
		
		AbstractActionOperation mainOp = new AbstractActionOperation("Operation_CreateRepository", SVNUIMessages.class) { //$NON-NLS-1$
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				ISVNConnector proxy = location.acquireSVNProxy();				
				try {					
					StringBuffer msg = new StringBuffer();
					msg.append("svnadmin create").append(" ");  //$NON-NLS-1$ //$NON-NLS-2$
					msg.append("--fs-type ").append(repositoryType).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
					if (ISVNConnector.REPOSITORY_FSTYPE_BDB.equals(repositoryType)) {
						msg.append("--bdb-txn-nosync").append(" "); //$NON-NLS-1$ //$NON-NLS-2$
						msg.append("--bdb-log-keep").append(" ");	 //$NON-NLS-1$ //$NON-NLS-2$
					}					
					msg.append("\"").append(FileUtility.normalizePath(repositoryPath)).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
					msg.append("\n"); //$NON-NLS-1$
					this.writeToConsole(IConsoleStream.LEVEL_CMD, msg.toString());
					proxy.createRepository(repositoryPath, repositoryType, new SVNProgressMonitor(this, monitor, null));
				} finally {
					location.releaseSVNProxy(proxy);
				}
			}			
		};
				
		CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
		op.add(mainOp);
		
		if (this.isCreateRepositoryLocation()) {			
			op.add(new AbstractActionOperation("Operation_AddRepositoryLocation", SVNUIMessages.class) { //$NON-NLS-1$
				protected void runImpl(IProgressMonitor monitor) throws Exception {		
					//validate repository location before adding
					final boolean[] isAddLocation = new boolean[1];
					isAddLocation[0] = true;
					final Exception validationException = SVNUtility.validateRepositoryLocation(location);
					if (validationException != null) {
						UIMonitorUtility.getDisplay().syncExec(new Runnable() {
							public void run() {
								Shell shell = AddRepositoryPanel.this.manager.getShell() != null ? AddRepositoryPanel.this.manager.getShell() : UIMonitorUtility.getShell();
								NonValidLocationErrorDialog dialog = new NonValidLocationErrorDialog(shell, validationException.getMessage());
								if (dialog.open() != Window.OK) {
									isAddLocation[0] = false;
								}															
							}							
						});						
					}			
										
					if (isAddLocation[0]) {						
						AddRepositoryLocationOperation addLocationOperation = new AddRepositoryLocationOperation(location);
						ProgressMonitorUtility.doTaskExternal(addLocationOperation, monitor);						
					}
				}				
			}, new IActionOperation[] {mainOp});																					
		}						
				
		this.operationToPerform = op;
									
		return true;
	}	
	
	public IActionOperation getOperationToPeform() {
		return this.operationToPerform;
	}	

	protected void cancelChangesImpl() {
		this.operationToPerform = null;
	}

	protected void saveChangesImpl() {
		this.performFinish();
	}
		
	public String getHelpId() {		
        return "org.eclipse.team.svn.help.addRepositoryContext"; //$NON-NLS-1$
	}

}
