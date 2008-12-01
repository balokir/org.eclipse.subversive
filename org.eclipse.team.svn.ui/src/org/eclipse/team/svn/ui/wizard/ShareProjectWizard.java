/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Alessandro Nistico - [patch] Change Set's implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.wizard;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.local.NotifyProjectStatesChangedOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.management.IShareProjectPrompt;
import org.eclipse.team.svn.core.operation.local.management.ReconnectProjectOperation;
import org.eclipse.team.svn.core.operation.local.management.ShareProjectOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.resource.events.ProjectStatesChangedEvent;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.action.IResourceSelector;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.extension.factory.ICommitDialog;
import org.eclipse.team.svn.ui.extension.factory.IShareProjectFactory;
import org.eclipse.team.svn.ui.panel.local.CommitPanel;
import org.eclipse.team.svn.ui.synchronize.SVNChangeSetCapability;
import org.eclipse.team.svn.ui.utility.CommitActionUtility;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.wizard.shareproject.AddRepositoryLocationPage;
import org.eclipse.team.svn.ui.wizard.shareproject.AlreadyConnectedPage;
import org.eclipse.team.svn.ui.wizard.shareproject.EditCommentPage;
import org.eclipse.team.svn.ui.wizard.shareproject.IShareProjectWrapper;
import org.eclipse.team.svn.ui.wizard.shareproject.SelectProjectNamePage;
import org.eclipse.team.svn.ui.wizard.shareproject.SelectProjectsGroupPage;
import org.eclipse.team.svn.ui.wizard.shareproject.SelectRepositoryLocationPage;
import org.eclipse.team.ui.IConfigurationWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Share project wizard main class
 * 
 * @author Alexander Gurov
 */
public class ShareProjectWizard extends AbstractSVNWizard implements IConfigurationWizard {
	protected IProject []allProjects;
	protected IResourceProvider projects;
	protected SelectRepositoryLocationPage selectLocation;
	protected SelectProjectNamePage selectName;
	protected AddRepositoryLocationPage addLocation;
	protected AlreadyConnectedPage connectedPage;
	protected EditCommentPage commentPage;
	
	public ShareProjectWizard() {
		super();
		this.setWindowTitle(SVNUIMessages.ShareProjectWizard_Title_Single);
	}
	
	public void addPages() {
		this.projects = new SelectProjectsGroupPage(this.allProjects);
		
		this.addPage((SelectProjectsGroupPage)this.projects);
		
		this.addPage(this.connectedPage = new AlreadyConnectedPage());
		
		IRepositoryLocation []locations = SVNRemoteStorage.instance().getRepositoryLocations();
		if (locations.length > 0) {
			this.addPage(this.selectLocation = new SelectRepositoryLocationPage(locations));
		}
		this.addPage(this.addLocation = new AddRepositoryLocationPage());
		this.addPage(this.selectName = ExtensionsManager.getInstance().getCurrentShareProjectFactory().getProjectLayoutPage());
		this.addPage(this.commentPage = new EditCommentPage(this.projects));
	}
	
	public void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);
	}
	
	public IWizardPage getStartingPage() {
		if (!this.isGroupSelectionRequired()) {
			return this.getNextPage(super.getStartingPage());
		}
		return super.getStartingPage();
	}
	
	public IWizardPage getNextPage(IWizardPage page) {
		IWizardPage retVal = null;
	    this.addLocation.setInitialUrl(null);
		if (page == this.projects && !this.alreadyConnected()) {
			retVal = super.getNextPage(super.getNextPage(page));
		}
		else if (page instanceof SelectRepositoryLocationPage && 
			this.selectLocation.useExistingLocation()) {
			retVal = super.getNextPage(super.getNextPage(page));
		}
		else if (page instanceof AlreadyConnectedPage) {
			if (this.connectedPage.createUsingProjectSettings()) {
				this.addLocation.setInitialUrl(this.connectedPage.getResourceUrl());
				this.addLocation.setForceDisableRoots(true);
				retVal = this.selectLocation == null ? super.getNextPage(page) : super.getNextPage(super.getNextPage(page));
			}
			else {
				this.addLocation.setInitialUrl("");
				this.addLocation.setForceDisableRoots(false);
//				this.addLocation.setMessage(AddRepositoryLocationPage.DEFAULT_MESSAGE);
				retVal = super.getNextPage(page);
			}
		}
		else {
			retVal = super.getNextPage(page);
		}
		
		if (retVal == this.selectName) {
			this.selectName.setProjectsAndLocation(this.getProjects(), this.selectLocation != null && this.selectLocation.useExistingLocation() ? this.selectLocation.getRepositoryLocation() : this.addLocation.getRepositoryLocation());
		}
		else if (retVal == this.commentPage) {
			this.commentPage.setSelectedRepositoryLocation(this.selectLocation != null && this.selectLocation.useExistingLocation() ? this.selectLocation.getRepositoryLocation() : this.addLocation.getRepositoryLocation());
			this.commentPage.setDefaultCommitMessage();
		}
		else if (retVal == this.connectedPage && this.alreadyConnected()) {
			this.connectedPage.setProjects(this.getProjects());
		}
		
		return retVal;
	}
	
	public IWizardPage getPreviousPage(IWizardPage page) {
		IWizardPage retVal = null;
		if (super.getPreviousPage(page) == this.connectedPage && !this.alreadyConnected()) {
			retVal = super.getPreviousPage(super.getPreviousPage(page));
		}
		else if (page instanceof SelectProjectNamePage &&
			this.selectLocation != null && 
			this.selectLocation.useExistingLocation()) {
			retVal = super.getPreviousPage(super.getPreviousPage(page));
		}
		else if (page instanceof AddRepositoryLocationPage &&
			this.connectedPage != null && 
			this.connectedPage.createUsingProjectSettings()) {
			retVal = super.getPreviousPage(super.getPreviousPage(page));
		}
		else {
			retVal = super.getPreviousPage(page);
		}
		if (retVal == this.projects && !this.isGroupSelectionRequired()) {
			return null;
		}
		return retVal;
	}
	
	public boolean canFinish() {
		IWizardPage currentPage = this.getContainer().getCurrentPage();
		IShareProjectFactory factory = ExtensionsManager.getInstance().getCurrentShareProjectFactory();
		if (currentPage instanceof SelectProjectsGroupPage) {
			return false;
		}
		if (currentPage instanceof AlreadyConnectedPage) {
			return ((AlreadyConnectedPage)currentPage).useProjectSettings() && currentPage.isPageComplete() && !factory.disallowFinishOnAlreadyConnected(this.getProjects());
		}
		if (currentPage instanceof AddRepositoryLocationPage && 
		    this.connectedPage != null &&
		    this.connectedPage.createUsingProjectSettings()) {
			return currentPage.isPageComplete() && !factory.disallowFinishOnAddRepositoryLocation(this.getProjects());
		}
		if (currentPage instanceof SelectRepositoryLocationPage) {
			return this.selectLocation.useExistingLocation() && !factory.disallowFinishOnSelectRepositoryLocation(this.getProjects());
		}
		return super.canFinish();
	}
	
	public boolean performFinish() {
		IWizardPage currentPage = this.getContainer().getCurrentPage();
		if (currentPage instanceof AddRepositoryLocationPage && 
		    !this.addLocation.performFinish()) {
		    return false;
		}
		
		boolean reconnect = this.connectedPage == null || (!this.connectedPage.useProjectSettings() && !this.connectedPage.createUsingProjectSettings());
		if (this.selectName.getRootProjectName() == null && currentPage instanceof AddRepositoryLocationPage) {
			this.selectName.setProjectsAndLocation(this.getProjects(), this.addLocation.getRepositoryLocation());
		}
		
		final IShareProjectWrapper mainOp = 
			reconnect ? 
			this.getFreshConnectOperation() :
			this.getAlreadyConnectedOperation(this.connectedPage.createUsingProjectSettings());

		final CompositeOperation op = new CompositeOperation(mainOp.getId());

		op.add(new NotifyProjectStatesChangedOperation(mainOp.getProjects(), ProjectStatesChangedEvent.ST_PRE_SHARED));
		
		if (this.addLocation.getOperationToPeform() != null) {
			op.add(this.addLocation.getOperationToPeform());
			op.add(mainOp, new IActionOperation[] {this.addLocation.getOperationToPeform()});
		}
		else {
			op.add(mainOp);			
		}
		op.add(new RefreshResourcesOperation(mainOp, IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL));

		op.add(new NotifyProjectStatesChangedOperation(mainOp.getProjects(), ProjectStatesChangedEvent.ST_POST_SHARED));
		
		if ((!this.alreadyConnected() || reconnect) && (this.commentPage == null || this.commentPage.isShowCommitDialog())) {
			op.add(new PostShareCommitOperation(mainOp));
		}
		
		UIMonitorUtility.doTaskScheduledActive(op);

		return true;
	}
	
	protected boolean isGroupSelectionRequired() {
		return ((SelectProjectsGroupPage)this.projects).isGroupSelectionRequired();
	}
	
	protected IShareProjectWrapper getAlreadyConnectedOperation(boolean create) {
		return 
			create ? 
			new ReconnectProjectOperationImpl(this.getProjects(), this.addLocation.getRepositoryLocation()) :
			new ReconnectProjectOperationImpl(this.getProjects(), this.connectedPage.getSelectedRoot().getRepositoryLocation());
	}
	
	protected IShareProjectWrapper getFreshConnectOperation() {
		IActionOperation addLocationOp = this.addLocation.getOperationToPeform();
		IRepositoryLocation location = null;
		if (addLocationOp != null) {
			location = this.addLocation.getRepositoryLocation();
		}
		else {
			location = this.selectLocation.getRepositoryLocation();
		}
		
		final ShareProjectOperation mainOp = ExtensionsManager.getInstance().getCurrentShareProjectFactory().getShareProjectOperation(this.getProjects(), location, this.selectName, this.commentPage.getCommitComment());
		mainOp.setSharePrompt(new IShareProjectPrompt() {
			public boolean prompt(final IProject []projects) {
				final int []result = new int[1];
				UIMonitorUtility.getDisplay().syncExec(new Runnable() {
					public void run() {
						String projectNames = FileUtility.getNamesListAsString(projects);
						String message = SVNUIMessages.format(projects.length == 1 ? SVNUIMessages.ShareProject_Confirmation_Description_Single : SVNUIMessages.ShareProject_Confirmation_Description_Multiple, new String[] {projectNames});
						MessageDialog dialog = new MessageDialog(
								UIMonitorUtility.getShell(),
								projects.length == 1 ? SVNUIMessages.ShareProject_Confirmation_Title_Single : SVNUIMessages.ShareProject_Confirmation_Title_Multiple, 
								null, message, MessageDialog.WARNING,
								new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL},
								0);
						result[0] = dialog.open();
					}
				});
				return result[0] == 0;
			}
		});
		CompositeOperationImpl op = new CompositeOperationImpl(mainOp.getId());
		
		//drop .svn folders if we want to do a fresh share
		IActionOperation predecessor = null;
		if (this.alreadyConnected()) {
			op.add(predecessor = new AbstractActionOperation("Operation.DropSVNMeta") {
				public ISchedulingRule getSchedulingRule() {
					return MultiRule.combine(ShareProjectWizard.this.getProjects());
				}
				protected void runImpl(IProgressMonitor monitor) throws Exception {
					IProject []projects = ShareProjectWizard.this.getProjects();
					for (int i = 0; i < projects.length && !monitor.isCanceled(); i++) {
						final IProject project = projects[i];
						this.protectStep(new IUnprotectedOperation() {
							public void run(IProgressMonitor monitor) throws Exception {
								FileUtility.removeSVNMetaInformation(project, monitor);
							}
						}, monitor, projects.length);
					}
				}
			});
		}
		if (predecessor != null) {
			op.add(mainOp, new IActionOperation[] {predecessor});
		}
		else {
			op.add(mainOp);
		}

		return op;
	}
	
	public void init(IWorkbench workbench, IProject project) {
		this.init(new IProject[] {project});
	}

	public void init(IProject []projects) {
		this.allProjects = projects;
		if (projects.length > 1) {
			this.setWindowTitle(SVNUIMessages.ShareProjectWizard_Title_Multiple);
		}
		else {
			this.setWindowTitle(SVNUIMessages.ShareProjectWizard_Title_Single);
		}
	}
	
	protected boolean alreadyConnected() {
		return FileUtility.alreadyOnSVN(this.getProjects()[0]);
	}
	
	protected IProject []getProjects() {
		return (IProject [])this.projects.getResources();
	}

	protected class ReconnectProjectOperationImpl extends ReconnectProjectOperation implements IShareProjectWrapper {
		public ReconnectProjectOperationImpl(IProject []projects, IRepositoryLocation location) {
			super(projects, location);
		}
		
		public IResource []getResources() {
			return this.getProjects();
		}

		public IProject []getProjects() {
			return ShareProjectWizard.this.getProjects();
		}
		
	}
	
	protected class CompositeOperationImpl extends CompositeOperation implements IShareProjectWrapper {
		public CompositeOperationImpl(String name) {
			super(name);
		}

		public IResource []getResources() {
			return this.getProjects();
		}

		public IProject []getProjects() {
			return ShareProjectWizard.this.getProjects();
		}
		
	}
	
	protected class PostShareCommitOperation extends AbstractActionOperation {
		protected IActionOperation mainOp;
		
		public PostShareCommitOperation(IActionOperation mainOp) {
			super("Operation.PrepareCommit");
			this.mainOp = mainOp;
		}
		
		protected void runImpl(final IProgressMonitor monitor) throws Exception {
			if (this.mainOp.getExecutionState() != IStatus.OK) {
				return;
			}
			IResourceSelector selector = new IResourceSelector() {
				public IResource []getSelectedResources() {
					return ShareProjectWizard.this.getProjects();
				}
				public IResource []getSelectedResources(IStateFilter filter) {
					return FileUtility.getResourcesRecursive(this.getSelectedResources(), filter, IResource.DEPTH_ZERO);
				}
				public IResource []getSelectedResourcesRecursive(IStateFilter filter) {
					return this.getSelectedResourcesRecursive(filter, IResource.DEPTH_INFINITE);
				}
				public IResource []getSelectedResourcesRecursive(IStateFilter filter, int depth) {
					return FileUtility.getResourcesRecursive(this.getSelectedResources(), filter, depth, PostShareCommitOperation.this, monitor);
				}
			};
			final CommitActionUtility commitUtility = new CommitActionUtility(selector);
			if (commitUtility.getAllResources().length == 0) {
				return;
			}
		    String proposedComment = SVNChangeSetCapability.getProposedComment(commitUtility.getAllResources());
			IResource[] allResources = commitUtility.getAllResources();
		    final CommitPanel commitPanel = new CommitPanel(allResources, allResources, CommitPanel.MSG_COMMIT, proposedComment);
			final CompositeOperation []commitOp = new CompositeOperation[1];
	        UIMonitorUtility.getDisplay().syncExec(new Runnable() {
				public void run() {
					ICommitDialog commitDialog = ExtensionsManager.getInstance().getCurrentCommitFactory().getCommitDialog(ShareProjectWizard.this.getShell(), commitUtility.getAllResourcesSet(), commitPanel);
			        if (commitDialog.open() == 0) {
						commitOp[0] = commitUtility.getCompositeCommitOperation(commitPanel.getSelectedResources(), commitPanel.getNotSelectedResources(), commitDialog.getMessage(), commitPanel.getKeepLocks(), ShareProjectWizard.this.getShell(), UIMonitorUtility.getActivePart(), true);
					}
				}
	        });
			if (commitOp[0] != null) {
				UIMonitorUtility.doTaskScheduledActive(commitOp[0]);
			}
		}
	}
	
}
