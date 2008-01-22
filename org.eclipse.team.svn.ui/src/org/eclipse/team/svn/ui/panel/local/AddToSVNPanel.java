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

package org.eclipse.team.svn.ui.panel.local;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.local.AddToSVNIgnoreOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.SaveProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.refactor.DeleteResourceOperation;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.events.IResourceStatesListener;
import org.eclipse.team.svn.core.resource.events.ResourceStatesChangedEvent;
import org.eclipse.team.svn.core.svnstorage.ResourcesParentsProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DiscardConfirmationDialog;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * Add resources panel implementation
 * 
 * @author Alexander Gurov
 */
public class AddToSVNPanel extends AbstractResourceSelectionPanel {

	protected boolean actionTookEffect;
	protected IResourceStatesListener resourceStatesListener;
	
    public AddToSVNPanel(IResource []resources) {
    	this(resources, null);
    }
    
    public AddToSVNPanel(IResource []resources, IResource []userSelectedResources) {
        super(resources, userSelectedResources, new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL});
        this.actionTookEffect = false;
        this.dialogTitle = SVNTeamUIPlugin.instance().getResource("AddToSVNPanel.Title");
        this.dialogDescription = SVNTeamUIPlugin.instance().getResource("AddToSVNPanel.Description");
        if (resources.length == 1) {
        	this.defaultMessage = SVNTeamUIPlugin.instance().getResource("AddToSVNPanel.Message.Single");
        }
        else {
        	this.defaultMessage = SVNTeamUIPlugin.instance().getResource("AddToSVNPanel.Message.Multi", new String[] {String.valueOf(resources.length)});
        }
    }
    
    protected void addContextMenu() {
		final TableViewer tableViewer = this.selectionComposite.getTableViewer();
		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(tableViewer.getTable());
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				final IStructuredSelection tSelection = (IStructuredSelection)tableViewer.getSelection();
				final IResource[] selectedResources = (IResource[])((List)tSelection.toList()).toArray(new IResource[tSelection.size()]);
				Action tAction = null;
				if (selectedResources.length == 1) {
					manager.add(tAction = new Action(MessageFormat.format(SVNTeamUIPlugin.instance().getResource("AddToSVNPanel.Ignore.Single"), selectedResources[0].getName())) {
						public void run() {
							CompositeOperation op = new CompositeOperation("AddToIgnore");
							op.add(new AddToSVNIgnoreOperation(selectedResources, IRemoteStorage.IGNORE_NAME, null));
							op.add(new RefreshResourcesOperation(new ResourcesParentsProvider(selectedResources), IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL));
							UIMonitorUtility.doTaskNowDefault(op, true);							
						}
					});
					tAction.setEnabled(true);
					String name = selectedResources[0].getName();
					String [] parts = name.split("\\.");
					if ((parts.length != 0)) {
						manager.add(tAction = new Action(MessageFormat.format(SVNTeamUIPlugin.instance().getResource("AddToSVNPanel.Ignore.Single"), "*." + parts[parts.length-1])) {
							public void run() {
								CompositeOperation op = new CompositeOperation("AddToIgnore");
								op.add(new AddToSVNIgnoreOperation(selectedResources, IRemoteStorage.IGNORE_EXTENSION, null));
								op.add(new RefreshResourcesOperation(new ResourcesParentsProvider(selectedResources), IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL));
								UIMonitorUtility.doTaskNowDefault(op, true);
							}
						});
						tAction.setEnabled(true);
					}
				}
				else {
					manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("AddToSVNPanel.IgnoreByNames.Multiple")) {
						public void run() {
							CompositeOperation op = new CompositeOperation("AddToIgnore");
							op.add(new AddToSVNIgnoreOperation(selectedResources, IRemoteStorage.IGNORE_NAME, null));
							op.add(new RefreshResourcesOperation(new ResourcesParentsProvider(selectedResources), IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL));
							UIMonitorUtility.doTaskNowDefault(op, true);
						}
					});
					tAction.setEnabled(tSelection.size() > 0);
					manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("AddToSVNPanel.IgnoreByExtension.Multiple")) {
						public void run() {
							CompositeOperation op = new CompositeOperation("AddToIgnore");
							op.add(new AddToSVNIgnoreOperation(selectedResources, IRemoteStorage.IGNORE_EXTENSION, null));
							op.add(new RefreshResourcesOperation(new ResourcesParentsProvider(selectedResources), IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL));
							UIMonitorUtility.doTaskNowDefault(op, true);							
						}
					});
					tAction.setEnabled(tSelection.size() > 0);
				}
				manager.add(new Separator());
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("AddToSVNPanel.Delete.Action.Lable")) {
					public void run () {
						DiscardConfirmationDialog dialog = new DiscardConfirmationDialog(UIMonitorUtility.getShell(), selectedResources.length == 1, DiscardConfirmationDialog.MSG_RESOURCE);
						if (dialog.open() == 0) {
							DeleteResourceOperation deleteOperation = new DeleteResourceOperation(selectedResources);
							CompositeOperation op = new CompositeOperation(deleteOperation.getId());
							SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(selectedResources);
							RestoreProjectMetaOperation restoreOp = new RestoreProjectMetaOperation(saveOp);
							op.add(saveOp);
							op.add(deleteOperation);
							op.add(restoreOp);
							op.add(new RefreshResourcesOperation(new ResourcesParentsProvider(selectedResources), IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL));
							UIMonitorUtility.doTaskNowDefault(op, true);
						}
					}
				});
				tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/delete.gif"));
				tAction.setEnabled(tSelection.size() > 0);
			}
		});
        menuMgr.setRemoveAllWhenShown(true);
        tableViewer.getTable().setMenu(menu);
    }
    
    public void postInit() {
		super.postInit();
		this.resourceStatesListener = new IResourceStatesListener() {
			public void resourcesStateChanged(ResourceStatesChangedEvent event) {
				AddToSVNPanel.this.updateResources();
			}
		};
		SVNRemoteStorage.instance().addResourceStatesListener(ResourceStatesChangedEvent.class, AddToSVNPanel.this.resourceStatesListener);
	}
    
    public boolean ifActionTookEffect() {
    	return this.actionTookEffect;
    }
    
    public void updateResources() {
    	final TableViewer tableViewer = this.selectionComposite.getTableViewer();
		HashSet toDeleteSet = new HashSet();
		toDeleteSet.addAll(Arrays.asList(this.resources));
		HashSet newResourcesSet = new HashSet();
		newResourcesSet.addAll(Arrays.asList(FileUtility.getResourcesRecursive(this.resources, IStateFilter.SF_UNVERSIONED, IResource.DEPTH_ZERO)));
		List<IResource> ignored = Arrays.asList(FileUtility.getResourcesRecursive(this.resources, IStateFilter.SF_IGNORED, IResource.DEPTH_ZERO));
		if (ignored.size() != 0){
			this.actionTookEffect = true;
		}
		newResourcesSet.removeAll(Arrays.asList(FileUtility.getResourcesRecursive(this.resources, IStateFilter.SF_IGNORED, IResource.DEPTH_ZERO)));
		for (int i = 0; i < resources.length; i++) {
			if (!resources[i].exists()) {
				newResourcesSet.remove(resources[i]);
				this.actionTookEffect = true;
			}
		}
		final IResource[] newResources = (IResource[])newResourcesSet.toArray(new IResource[newResourcesSet.size()]);
		toDeleteSet.removeAll(newResourcesSet);
		final IResource[] toDeleteResources = (IResource[])toDeleteSet.toArray(new IResource[toDeleteSet.size()]);
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				AddToSVNPanel.this.selectionComposite.setResources(newResources);
				if (!tableViewer.getTable().isDisposed()) {
					tableViewer.remove(toDeleteResources);
					tableViewer.refresh();
					AddToSVNPanel.this.selectionComposite.fireSelectionChanged();
				}
			}
		});
		this.resources = newResources;
    }
    
    public void dispose() {
    	super.dispose();
    	SVNRemoteStorage.instance().removeResourceStatesListener(ResourceStatesChangedEvent.class, this.resourceStatesListener);
    }
    
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.addToVCDialogContext";
    }

}
