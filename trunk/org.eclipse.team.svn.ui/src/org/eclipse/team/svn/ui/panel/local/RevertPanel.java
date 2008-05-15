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

package org.eclipse.team.svn.ui.panel.local;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.CreatePatchOperation;
import org.eclipse.team.svn.core.operation.local.ExportOperation;
import org.eclipse.team.svn.core.operation.local.LockOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.SaveProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.UnlockOperation;
import org.eclipse.team.svn.core.operation.local.management.CleanupOperation;
import org.eclipse.team.svn.core.operation.local.refactor.DeleteResourceOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.events.IResourceStatesListener;
import org.eclipse.team.svn.core.resource.events.ResourceStatesChangedEvent;
import org.eclipse.team.svn.core.svnstorage.ResourcesParentsProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.local.BranchTagAction;
import org.eclipse.team.svn.ui.action.local.CompareWithWorkingCopyAction;
import org.eclipse.team.svn.ui.action.local.ReplaceWithLatestRevisionAction;
import org.eclipse.team.svn.ui.action.local.ReplaceWithRevisionAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.dialog.DiscardConfirmationDialog;
import org.eclipse.team.svn.ui.dialog.UnlockResourcesDialog;
import org.eclipse.team.svn.ui.operation.CompareResourcesOperation;
import org.eclipse.team.svn.ui.panel.remote.ComparePanel;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * Revert resources state panel implementation
 * 
 * @author Alexander Gurov
 */
public class RevertPanel extends AbstractResourceSelectionPanel {

    protected boolean removeNonVersioned;
    protected boolean disableRemoveNonVersionedChange;
    
	protected IResourceStatesListener resourceStatesListener;
	
	public RevertPanel(IResource []resources) {
    	this(resources, null);
    }
    
    public RevertPanel(IResource[] resources, IResource[] userSelectedResources) {
        super(resources, userSelectedResources, new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL});
        this.dialogTitle = SVNTeamUIPlugin.instance().getResource("RevertPanel.Title");
        this.dialogDescription = SVNTeamUIPlugin.instance().getResource("RevertPanel.Description");
        this.defaultMessage = SVNTeamUIPlugin.instance().getResource("RevertPanel.Message");
        IResource[] nonVersionedResources = FileUtility.getResourcesRecursive(resources, IStateFilter.SF_NEW, IResource.DEPTH_ZERO);
        this.disableRemoveNonVersionedChange = nonVersionedResources.length == resources.length;
    	this.removeNonVersioned = this.disableRemoveNonVersionedChange;
    }
    
    public void createControlsImpl(Composite parent) {
    	GridLayout layout = null;
    	GridData data = null;
    	
    	Composite composite = new Composite(parent, SWT.NONE);
    	layout = new GridLayout();
    	layout.horizontalSpacing = 0;
    	layout.verticalSpacing = 0;
    	layout.marginWidth = 0;
    	layout.marginHeight = 0;
    	composite.setLayout(layout);
    	data = new GridData(GridData.FILL_BOTH);
    	composite.setLayoutData(data);
    	
    	super.createControlsImpl(composite);
    	
    	this.createVerticalStrut(composite, 4);
    	
    	Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
    	data = new GridData(GridData.FILL_HORIZONTAL);
    	separator.setLayoutData(data);
    	
    	this.createVerticalStrut(composite, 7);
    	
    	final Button removeNonVersionedButton = new Button(composite, SWT.CHECK);
    	data = new GridData();
    	removeNonVersionedButton.setLayoutData(data);
    	removeNonVersionedButton.setText(SVNTeamUIPlugin.instance().getResource("RevertPanel.Button.RemoveNonVersioned"));
    	removeNonVersionedButton.setSelection(this.removeNonVersioned);
    	removeNonVersionedButton.setEnabled(!this.disableRemoveNonVersionedChange);
    	removeNonVersionedButton.addSelectionListener(new SelectionAdapter() {
    		public void widgetSelected(SelectionEvent e) {
    			RevertPanel.this.removeNonVersioned = removeNonVersionedButton.getSelection();
    		}
    	});
    	this.addContextMenu();
    }
    
    public void postInit() {
		super.postInit();
		this.resourceStatesListener = new IResourceStatesListener() {
			public void resourcesStateChanged(ResourceStatesChangedEvent event) {
				RevertPanel.this.updateResources(event);
			}
		};
		SVNRemoteStorage.instance().addResourceStatesListener(ResourceStatesChangedEvent.class, RevertPanel.this.resourceStatesListener);
	}
    
    public void dispose() {
    	super.dispose();
    	SVNRemoteStorage.instance().removeResourceStatesListener(ResourceStatesChangedEvent.class, this.resourceStatesListener);
    }
    
    protected void addContextMenu() {
		final TableViewer tableViewer = this.selectionComposite.getTableViewer();
		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(tableViewer.getTable());
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				final IStructuredSelection tSelection = (IStructuredSelection)tableViewer.getSelection();
				final IResource[] selectedResources = (IResource[])tSelection.toList().toArray(new IResource[tSelection.size()]);
				Action tAction = null;

				//Create Patch File action
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("CreatePatchCommand.label")) {
					public void run() {
						FileDialog dlg = new FileDialog(UIMonitorUtility.getShell(), SWT.PRIMARY_MODAL | SWT.SAVE);
						dlg.setText(SVNTeamUIPlugin.instance().getResource("SelectPatchFilePage.SavePatchAs"));
						dlg.setFileName(selectedResources[0].getName() + ".patch");
						dlg.setFilterExtensions(new String[] {"patch", "*.*"});
						String file = dlg.open();
						if (file != null) {
							CreatePatchOperation mainOp = new CreatePatchOperation(new IResource[] {selectedResources[0]}, file, true, true, true, true);
							UIMonitorUtility.doTaskNowDefault(mainOp, false);
						}
					}
				});
				tAction.setEnabled(tSelection.size() == 1 && FileUtility.checkForResourcesPresence(selectedResources, IStateFilter.SF_VERSIONED, IResource.DEPTH_ZERO));
				
				//Create Branch action
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("BranchAction.label")) {
					public void run() {
						IResource [] resources = FileUtility.getResourcesRecursive(selectedResources, IStateFilter.SF_EXCLUDE_DELETED, IResource.DEPTH_INFINITE);
						IActionOperation op = BranchTagAction.getBranchTagOperation(UIMonitorUtility.getShell(), BranchTagAction.BRANCH_ACTION, resources);
						if (op != null) {
							UIMonitorUtility.doTaskNowDefault(op, true);
						}
					}
				});
				tAction.setEnabled(tSelection.size() > 0  && FileUtility.checkForResourcesPresence(selectedResources, IStateFilter.SF_EXCLUDE_DELETED, IResource.DEPTH_ZERO));
				tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/branch.gif"));
				manager.add(new Separator());
				
				//Lock action
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("LockAction.label")) {
					public void run() {
						boolean containsFolder = false;
						for (int i = 0; i < selectedResources.length; i++) {
							if (selectedResources[i] instanceof IContainer) {
								containsFolder = true;
								break;
							}
						}
						CommitPanel.CollectPropertiesOperation cop = new CommitPanel.CollectPropertiesOperation(selectedResources);
						ProgressMonitorUtility.doTaskExternal(cop, null);
						LockPanel commentPanel = new LockPanel(!containsFolder, cop.getMinLockSize());
						DefaultDialog dialog = new DefaultDialog(UIMonitorUtility.getShell(), commentPanel);
						if (dialog.open() == 0) {
						    IResource []resources = FileUtility.getResourcesRecursive(selectedResources, IStateFilter.SF_READY_TO_LOCK, commentPanel.isRecursive() ? IResource.DEPTH_INFINITE : IResource.DEPTH_ONE); 
						    LockOperation mainOp = new LockOperation(resources, commentPanel.getMessage(), commentPanel.getForce());
						    CompositeOperation op = new CompositeOperation(mainOp.getId());
							op.add(mainOp);
							op.add(new RefreshResourcesOperation(resources));
							UIMonitorUtility.doTaskNowDefault(op, false);
						}
					}
				});
				tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/lock.gif"));
				tAction.setEnabled(FileUtility.checkForResourcesPresenceRecursive(selectedResources, IStateFilter.SF_READY_TO_LOCK));
				
				//Unlock action
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("UnlockAction.label")) {
					public void run() {
						boolean recursive = false;
						for (int i = 0; i < selectedResources.length; i++) {
							if (selectedResources[i].getType() != IResource.FILE) {
								recursive = true;
								break;
							}
						}
						UnlockResourcesDialog dialog = new UnlockResourcesDialog(UIMonitorUtility.getShell(), recursive);
						if (dialog.open() == 0) {
							IResource []resources = FileUtility.getResourcesRecursive(selectedResources, IStateFilter.SF_LOCKED, dialog.isRecursive() ? IResource.DEPTH_INFINITE : IResource.DEPTH_ONE);
						    UnlockOperation mainOp = new UnlockOperation(resources);
							CompositeOperation op = new CompositeOperation(mainOp.getId());
							op.add(mainOp);
							op.add(new RefreshResourcesOperation(resources));
							UIMonitorUtility.doTaskNowDefault(op, false);
						}
					}
				});
				tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/unlock.gif"));
				tAction.setEnabled(FileUtility.checkForResourcesPresenceRecursive(selectedResources, IStateFilter.SF_LOCKED));
				manager.add(new Separator());
				
				//Compare With group 
				MenuManager subMenu = new MenuManager(SVNTeamUIPlugin.instance().getResource("CommitPanel.CompareWith.Group"));
				subMenu.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("CompareWithWorkingCopyAction.label")) {
					public void run() {
						IResource resource = selectedResources[0];
						ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
						if (local != null) {
							IRepositoryResource remote = local.isCopied() ? SVNUtility.getCopiedFrom(resource) : SVNRemoteStorage.instance().asRepositoryResource(resource);
							remote.setSelectedRevision(SVNRevision.BASE);
							UIMonitorUtility.doTaskScheduledDefault(new CompareResourcesOperation(local, remote, false, true));
						}
					}
				});
				tAction.setEnabled(tSelection.size() == 1 && FileUtility.checkForResourcesPresence(selectedResources, CompareWithWorkingCopyAction.COMPARE_FILTER, IResource.DEPTH_ZERO));
				subMenu.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("CompareWithLatestRevisionAction.label")) {
					public void run() {
						IResource resource = selectedResources[0];
						ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
						if (local != null) {
							IRepositoryResource remote = local.isCopied() ? SVNUtility.getCopiedFrom(resource) : SVNRemoteStorage.instance().asRepositoryResource(resource);
							remote.setSelectedRevision(SVNRevision.HEAD);
							UIMonitorUtility.doTaskScheduledDefault(new CompareResourcesOperation(local, remote, false, true));
						}
					}
				});
				tAction.setEnabled(tSelection.size() == 1 && 
						(CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() == ISVNConnectorFactory.APICompatibility.SVNAPI_1_5_x || 
						selectedResources[0].getType() == IResource.FILE) && FileUtility.checkForResourcesPresenceRecursive(selectedResources, CompareWithWorkingCopyAction.COMPARE_FILTER));
				subMenu.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("CompareWithRevisionAction.label")) {
					public void run() {
						IResource resource = selectedResources[0];
						ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
						if (local != null) {
							IRepositoryResource remote = local.isCopied() ? SVNUtility.getCopiedFrom(resource) : SVNRemoteStorage.instance().asRepositoryResource(resource);
							ComparePanel panel = new ComparePanel(remote, local.getRevision());
							DefaultDialog dlg = new DefaultDialog(UIMonitorUtility.getShell(), panel);
							if (dlg.open() == 0) {
								remote = panel.getSelectedResource();
								UIMonitorUtility.doTaskScheduledDefault(new CompareResourcesOperation(local, remote, false, true));
							}
						}
					}
				});
				tAction.setEnabled(tSelection.size() == 1 && 
						(CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() == ISVNConnectorFactory.APICompatibility.SVNAPI_1_5_x || 
						selectedResources[0].getType() == IResource.FILE) && FileUtility.checkForResourcesPresenceRecursive(selectedResources, CompareWithWorkingCopyAction.COMPARE_FILTER));
				manager.add(subMenu);
				
				//Replace with group
				subMenu = new MenuManager(SVNTeamUIPlugin.instance().getResource("CommitPanel.ReplaceWith.Group"));
				subMenu.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("ReplaceWithLatestRevisionAction.label")) {
					public void run() {
						IResource []resources = FileUtility.getResourcesRecursive(selectedResources, IStateFilter.SF_ONREPOSITORY, IResource.DEPTH_ZERO);
						IActionOperation op = ReplaceWithLatestRevisionAction.getReplaceOperation(resources, UIMonitorUtility.getShell());
						if (op != null) {
							UIMonitorUtility.doTaskNowDefault(op, true);
						}
					}
				});
				tAction.setEnabled(FileUtility.checkForResourcesPresenceRecursive(selectedResources, IStateFilter.SF_ONREPOSITORY));
				subMenu.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("ReplaceWithRevisionAction.label")) {
					public void run() {
						IActionOperation op = ReplaceWithRevisionAction.getReplaceOperation(selectedResources, UIMonitorUtility.getShell());
						if (op != null) {
							UIMonitorUtility.doTaskNowDefault(op, true);
						}
					}
				});
				tAction.setEnabled(tSelection.size() == 1 && FileUtility.checkForResourcesPresence(selectedResources, IStateFilter.SF_ONREPOSITORY, IResource.DEPTH_ZERO));
				manager.add(subMenu);				
				manager.add(new Separator());
				
				//Export action
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("ExportCommand.label")) {
					public void run() {
						DirectoryDialog fileDialog = new DirectoryDialog(UIMonitorUtility.getShell());
						fileDialog.setText(SVNTeamUIPlugin.instance().getResource("ExportAction.Select.Title"));
						fileDialog.setMessage(SVNTeamUIPlugin.instance().getResource("ExportAction.Select.Description"));
						String path = fileDialog.open();
						if (path != null) {
							UIMonitorUtility.doTaskScheduledDefault(new ExportOperation(FileUtility.getResourcesRecursive(selectedResources, IStateFilter.SF_EXCLUDE_DELETED, IResource.DEPTH_ZERO) , path, SVNRevision.WORKING));
						}
					}
				});
				tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/export.gif"));
				tAction.setEnabled(tSelection.size() > 0 && FileUtility.checkForResourcesPresence(selectedResources, IStateFilter.SF_EXCLUDE_DELETED, IResource.DEPTH_ZERO));
				
				//Clean-up action
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("CleanupCommand.label")) {
					public void run() {
						IResource []resources = FileUtility.getResourcesRecursive(selectedResources, IStateFilter.SF_VERSIONED_FOLDERS, IResource.DEPTH_ZERO);
						CleanupOperation mainOp = new CleanupOperation(resources);
						CompositeOperation op = new CompositeOperation(mainOp.getId());
						op.add(mainOp);
						op.add(new RefreshResourcesOperation(resources));
						UIMonitorUtility.doTaskNowDefault(op, false);						
					}
				});
				tAction.setEnabled(tSelection.size() > 0 && FileUtility.checkForResourcesPresence(selectedResources, IStateFilter.SF_VERSIONED_FOLDERS, IResource.DEPTH_ZERO));
				manager.add(new Separator());
				
				//Delete action
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("CommitPanel.Delete.Action")) {
					public void run() {
						DiscardConfirmationDialog dialog = new DiscardConfirmationDialog(UIMonitorUtility.getShell(), selectedResources.length == 1, DiscardConfirmationDialog.MSG_RESOURCE);
						if (dialog.open() == 0) {
							DeleteResourceOperation deleteOperation = new DeleteResourceOperation(selectedResources);
							CompositeOperation op = new CompositeOperation(deleteOperation.getId());
							SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(selectedResources);
							RestoreProjectMetaOperation restoreOp = new RestoreProjectMetaOperation(saveOp);
							op.add(saveOp);
							op.add(deleteOperation);
							op.add(restoreOp);
							op.add(new RefreshResourcesOperation(new ResourcesParentsProvider(selectedResources), IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_CHANGES));
							UIMonitorUtility.doTaskNowDefault(op, true);
						}
					}
				});
				tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/delete.gif"));
				tAction.setEnabled(tSelection.size() > 0 && !FileUtility.checkForResourcesPresence(selectedResources, IStateFilter.SF_DELETED, IResource.DEPTH_ZERO));
			}
		});
        menuMgr.setRemoveAllWhenShown(true);
        tableViewer.getTable().setMenu(menu);
	}
    
	protected void updateResources(ResourceStatesChangedEvent event) {
		HashSet<IResource> allResources = new HashSet<IResource>(Arrays.asList(this.resources));
		
		HashSet<IResource> toDeleteSet = new HashSet<IResource>();
		toDeleteSet.addAll(Arrays.asList(FileUtility.getResourcesRecursive(event.resources, IStateFilter.SF_NOTMODIFIED, IResource.DEPTH_ZERO)));
		toDeleteSet.addAll(Arrays.asList(FileUtility.getResourcesRecursive(event.resources, IStateFilter.SF_NOTEXISTS, IResource.DEPTH_ZERO)));
		toDeleteSet.addAll(Arrays.asList(FileUtility.getResourcesRecursive(event.resources, IStateFilter.SF_IGNORED, IResource.DEPTH_ZERO)));
		
		allResources.removeAll(toDeleteSet);
		
		final IResource[] newResources = allResources.toArray(new IResource[allResources.size()]);
		
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				//FIXME isDisposed() test is necessary as dispose() method is not called from FastTrack Commit Dialog
				if (!RevertPanel.this.selectionComposite.isDisposed()) {
					RevertPanel.this.selectionComposite.setResources(newResources);
					RevertPanel.this.selectionComposite.fireSelectionChanged();
				}
			}
		});
		
		this.resources = newResources;
	}
    
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.revertDialogContext";
    }
    
    public boolean getRemoveNonVersioned() {
    	return this.removeNonVersioned;
    }
    
    protected void createVerticalStrut(Composite parent, int height) {
    	Label strut = new Label(parent, SWT.NONE);
    	GridData data = new GridData();
    	data.heightHint = height;
    	strut.setLayoutData(data);
    }
    
}
