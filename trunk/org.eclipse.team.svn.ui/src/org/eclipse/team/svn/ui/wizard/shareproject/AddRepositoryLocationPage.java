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

package org.eclipse.team.svn.ui.wizard.shareproject;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.SVNTeamProvider;
import org.eclipse.team.svn.core.client.Info2;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.management.AddRepositoryLocationOperation;
import org.eclipse.team.svn.core.operation.remote.management.SaveRepositoryLocationsOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.ProjectListComposite;
import org.eclipse.team.svn.ui.composite.RepositoryPropertiesTabFolder;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.dialog.NonValidLocationErrorDialog;
import org.eclipse.team.svn.ui.operation.RefreshRepositoryLocationsOperation;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.verifier.AbstractFormattedVerifier;
import org.eclipse.team.svn.ui.wizard.AbstractVerifiedWizardPage;

/**
 * Add repository location wizard page
 * 
 * @author Alexander Gurov
 */
public class AddRepositoryLocationPage extends AbstractVerifiedWizardPage {
	protected RepositoryPropertiesTabFolder propertiesTabFolder;
	protected IActionOperation operationToPerform;
	protected IRepositoryLocation editable;
	protected boolean alreadyConnected;
	protected boolean createNew;
	protected String initialUrl;
	protected String oldUrl;
	protected String oldLabel;
	protected String oldUuid;
		
	public AddRepositoryLocationPage() {
		this(null);
	}
	
	public AddRepositoryLocationPage(IRepositoryLocation editable) {
		super(AddRepositoryLocationPage.class.getName(), 
			SVNTeamUIPlugin.instance().getResource("AddRepositoryLocationPage.Title"), 
			SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif"));
		
		this.setDescription(SVNTeamUIPlugin.instance().getResource("AddRepositoryLocationPage.Description"));
		this.editable = editable;
		if (editable != null) {
			this.oldUrl = editable.getUrl();
			this.oldLabel = editable.getLabel();
			this.oldUuid = null;
		}
		this.alreadyConnected = false;
		this.createNew = true;
	}

	protected Composite createControlImpl(Composite parent) {
		this.propertiesTabFolder = new RepositoryPropertiesTabFolder(parent, SWT.NONE, this, this.editable);
		this.propertiesTabFolder.initialize();
		this.propertiesTabFolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		this.propertiesTabFolder.resetChanges();
		
		return this.propertiesTabFolder;
	}
	
	public void setInitialUrl(String initialUrl) {
		this.initialUrl = initialUrl;
	    if (this.alreadyConnected = initialUrl != null) {
	    	this.createNew = initialUrl.trim().length() == 0;
	    	this.getRepositoryLocation().setUrl(initialUrl);
		    this.propertiesTabFolder.resetChanges();
	    }
	}
	
	public void setForceDisableRoots(boolean force) {
		this.propertiesTabFolder.setForceDisableRoots(force, this.initialUrl == null || this.initialUrl.length() == 0 ? null : new AbstractFormattedVerifier(SVNTeamUIPlugin.instance().getResource("AddRepositoryLocationPage.RootURL")) {
		    protected String getErrorMessageImpl(Control input) {
				String url = this.getText(input);
				if (!new Path(url).isPrefixOf(new Path(SVNUtility.decodeURL(AddRepositoryLocationPage.this.initialUrl)))) {
					String message = SVNTeamUIPlugin.instance().getResource("AddRepositoryLocationPage.FixedURL.Verifier.Error");
					return MessageFormat.format(message, new String[] {AbstractFormattedVerifier.FIELD_NAME, AddRepositoryLocationPage.this.initialUrl});
				}
				return null;
			}
		    protected String getWarningMessageImpl(Control input) {
				return null;
			}
		});
	}

	public IRepositoryLocation getRepositoryLocation() {
		return this.propertiesTabFolder.getRepositoryLocation();
	}
	
	public boolean canFlipToNextPage() {
		return (!this.alreadyConnected || this.createNew) && this.isPageComplete();
	}
	
	public IWizardPage getNextPage() {
		return this.performFinish() ? super.getNextPage() : this;
	}
	
	public IWizardPage getPreviousPage() {
		this.performCancel();
		return super.getPreviousPage();
	}
	
	public void performCancel() {
		this.operationToPerform = null;
	}
	
	public boolean performFinish() {
		String newUrl = this.propertiesTabFolder.getLocationUrl();
		ProjectListPanel panel = null;
		ArrayList connectedProjects = new ArrayList();
		IProject []projectsArray = null;
		if (this.editable != null && !newUrl.equals(this.oldUrl) && this.oldUuid == null) {
			IProject []projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			for (int i = 0; i < projects.length; i++) {
				RepositoryProvider tmp = RepositoryProvider.getProvider(projects[i]);
				if (tmp != null && SVNTeamPlugin.NATURE_ID.equals(tmp.getID())) {
					SVNTeamProvider provider = (SVNTeamProvider)tmp;
					if (AddRepositoryLocationPage.this.editable.equals(provider.getRepositoryLocation())) {
						connectedProjects.add(projects[i]);
					}
				}
			}
			if (connectedProjects.size() > 0) {
				projectsArray = (IProject [])connectedProjects.toArray(new IProject[connectedProjects.size()]);
				Info2 info = this.getLocationInfo(this.editable);
				this.oldUuid = info == null ? null : info.reposUUID;
//				if (info == null) {
//					panel = new ProjectListPanel(projectsArray, false);
//				}
//				else {
//					this.oldUuid = info.getUuid();
//				}
			}
		}
		this.propertiesTabFolder.saveChanges();	
		
		if (this.propertiesTabFolder.isStructureEnabled()) {
			IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();

			if (newUrl.endsWith(SVNTeamPreferences.getRepositoryString(store, SVNTeamPreferences.REPOSITORY_HEAD_NAME)) ||
					newUrl.endsWith(SVNTeamPreferences.getRepositoryString(store, SVNTeamPreferences.REPOSITORY_BRANCHES_NAME)) ||
					newUrl.endsWith(SVNTeamPreferences.getRepositoryString(store, SVNTeamPreferences.REPOSITORY_TAGS_NAME))) {
				final int []result = new int[1];
				final MessageDialog dialog = new MessageDialog(this.getShell(), 
														SVNTeamUIPlugin.instance().getResource("AddRepositoryLocationPage.Normalize.Title"),
														null,
														SVNTeamUIPlugin.instance().getResource("AddRepositoryLocationPage.Normalize.Message"),
														MessageDialog.WARNING,
														new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL},
														0);
				UIMonitorUtility.getDisplay().syncExec(new Runnable() {
					public void run() {
						result[0] = dialog.open();
					}
				});
				if (result[0] == IDialogConstants.OK_ID) {
					IRepositoryLocation location = this.editable == null ? this.getRepositoryLocation() : this.editable;
					boolean useCustomLabel = false;
					useCustomLabel = !location.getUrl().equals(location.getLabel());
					newUrl = (new Path(newUrl)).removeLastSegments(1).toString();
					location.setUrl(newUrl);
					if (!useCustomLabel) {
						location.setLabel(newUrl);
					}
					location.reconfigure();
				}
			}
		}
		
		if (connectedProjects.size() > 0) {
			if (panel == null) {
				this.editable.reconfigure();
				Info2 newInfo = this.getLocationInfo(this.editable);
				if (newInfo == null) {
					panel = new ProjectListPanel(projectsArray, false);
				}
				else if (this.oldUuid != null && !this.oldUuid.equals(newInfo.reposUUID)) {
					panel = new ProjectListPanel(projectsArray, true);
				}
			}
			if (panel != null) {
				this.editable.setUrl(this.oldUrl);
				this.editable.setLabel(this.oldLabel);
				this.editable.reconfigure();
				new DefaultDialog(this.getShell(), panel).open();
			}
		}
		
		if (this.propertiesTabFolder.isValidateOnFinishRequested() && panel == null) {
			final Exception []problem = new Exception[1];
			UIMonitorUtility.doTaskNowDefault(this.getShell(), new AbstractNonLockingOperation("Operation.ValidateLocation") {
				protected void runImpl(IProgressMonitor monitor) throws Exception {
					problem[0] = SVNUtility.validateRepositoryLocation(AddRepositoryLocationPage.this.propertiesTabFolder.getRepositoryLocation());
				}
			}, false);
			if (problem[0] != null) {
				NonValidLocationErrorDialog dialog = new NonValidLocationErrorDialog(this.getShell(), problem[0].getMessage());
				if (dialog.open() != 0)
				{
					return false;
				}
			}
		}
		
		boolean shouldntBeAdded = this.editable == null ? false : (SVNRemoteStorage.instance().getRepositoryLocation(this.editable.getId()) != null);

		AbstractActionOperation mainOp = 
			shouldntBeAdded ?
			new AbstractNonLockingOperation("Operation.CommitLocationChanges") {
				protected void runImpl(IProgressMonitor monitor) throws Exception {
					AddRepositoryLocationPage.this.editable.reconfigure();
				}
			} :
			(AbstractActionOperation)new AddRepositoryLocationOperation(this.getRepositoryLocation());
		
		CompositeOperation op = new CompositeOperation(mainOp.getId());
		
		op.add(mainOp);
		op.add(new SaveRepositoryLocationsOperation());
		op.add(shouldntBeAdded ? new RefreshRepositoryLocationsOperation(new IRepositoryLocation[] {this.editable}, true) : new RefreshRepositoryLocationsOperation(false));
		
		this.operationToPerform = op;
		
		return true;
	}
	
	public IActionOperation getOperationToPeform() {
		return this.operationToPerform;
	}
	
	protected static class ProjectListPanel extends AbstractDialogPanel {
		protected IProject []resources;
		protected TableViewer tableViewer;
				
		public ProjectListPanel(IProject []input, boolean differentUuid) {
			super(new String[] {IDialogConstants.OK_LABEL});
			
			this.dialogTitle = SVNTeamUIPlugin.instance().getResource("AddRepositoryLocationPage.ProjectList.Title");
			this.dialogDescription = SVNTeamUIPlugin.instance().getResource("AddRepositoryLocationPage.ProjectList.Description");
			this.defaultMessage = SVNTeamUIPlugin.instance().getResource(differentUuid ? "AddRepositoryLocationPage.ProjectList.Message1" : "AddRepositoryLocationPage.ProjectList.Message2");
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
	
	protected Info2 getLocationInfo(IRepositoryLocation location) {
		try {
			return SVNUtility.getLocationInfo(location);
		}
		catch (Exception ex) {
			return null;
		}
	}
	
}
