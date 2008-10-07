/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *    Alessandro Nistico - [patch] Change Set's implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.AddToSVNIgnoreOperation;
import org.eclipse.team.svn.core.operation.local.CreatePatchOperation;
import org.eclipse.team.svn.core.operation.local.ExportOperation;
import org.eclipse.team.svn.core.operation.local.LockOperation;
import org.eclipse.team.svn.core.operation.local.MarkAsMergedOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.SaveProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.UnlockOperation;
import org.eclipse.team.svn.core.operation.local.management.CleanupOperation;
import org.eclipse.team.svn.core.operation.local.property.GetPropertiesOperation;
import org.eclipse.team.svn.core.operation.local.refactor.DeleteResourceOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.events.IResourceStatesListener;
import org.eclipse.team.svn.core.resource.events.ResourceStatesChangedEvent;
import org.eclipse.team.svn.core.svnstorage.ResourcesParentsProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.local.AddToSVNIgnoreAction;
import org.eclipse.team.svn.ui.action.local.BranchTagAction;
import org.eclipse.team.svn.ui.action.local.CompareWithWorkingCopyAction;
import org.eclipse.team.svn.ui.action.local.ReplaceWithLatestRevisionAction;
import org.eclipse.team.svn.ui.action.local.ReplaceWithRevisionAction;
import org.eclipse.team.svn.ui.action.local.RevertAction;
import org.eclipse.team.svn.ui.composite.CommentComposite;
import org.eclipse.team.svn.ui.composite.ResourceSelectionComposite;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.dialog.DiscardConfirmationDialog;
import org.eclipse.team.svn.ui.dialog.UnlockResourcesDialog;
import org.eclipse.team.svn.ui.event.IResourceSelectionChangeListener;
import org.eclipse.team.svn.ui.event.ResourceSelectionChangedEvent;
import org.eclipse.team.svn.ui.extension.factory.ICommentDialogPanel;
import org.eclipse.team.svn.ui.operation.CompareResourcesOperation;
import org.eclipse.team.svn.ui.operation.ShowConflictEditorOperation;
import org.eclipse.team.svn.ui.panel.common.CommentPanel;
import org.eclipse.team.svn.ui.panel.remote.ComparePanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.properties.bugtraq.BugtraqModel;
import org.eclipse.team.svn.ui.propfind.BugtraqPropFindVisitor;
import org.eclipse.team.svn.ui.propfind.CompositePropFindVisitor;
import org.eclipse.team.svn.ui.propfind.IPropFindVisitor;
import org.eclipse.team.svn.ui.propfind.LogTemplatesPropFindVisitor;
import org.eclipse.team.svn.ui.propfind.MaxLogWidthPropFindVisitor;
import org.eclipse.team.svn.ui.propfind.MinLockSizePropFindVisitor;
import org.eclipse.team.svn.ui.propfind.MinLogSizePropFindVisitor;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * Commit panel 
 * 
 * @author Sergiy Logvin
 */
public class CommitPanel extends CommentPanel implements ICommentDialogPanel {
	public static final int MSG_COMMIT = 0;
	public static final int MSG_OVER_AND_COMMIT = 1;
	public static final int MAXIMUM_CHECKS_SIZE = 100;
	
	protected ResourceSelectionComposite selectionComposite;
	protected Button keepLocksButton;
	protected Button pasteNamesButton;
	protected SashForm sForm;
	protected IResource []resources;
	protected boolean keepLocks;
	protected List<IResourceSelectionChangeListener> changeListenerList;
	protected IResource[] userSelectedResources;
	protected int minLogSize;
	protected int maxLogWidth;
	
	protected final String proposedComment;
	
	protected IResourceStatesListener resourceStatesListener;
	protected boolean resourcesChanged;
	
	public CommitPanel(IResource []resources, int msgType) {
		this(resources, msgType, null);
	}
	
	public CommitPanel(IResource []resources, int msgType, String proposedComment) {
		this(resources, null, msgType, proposedComment);
    }
	
	public CommitPanel(IResource []resources, IResource[] userSelectedResources, int msgType, String proposedComment) {
		super(SVNTeamUIPlugin.instance().getResource("CommitPanel.Title"));
		this.proposedComment = proposedComment;
		this.resources = resources;
		if (msgType == CommitPanel.MSG_OVER_AND_COMMIT) {
			this.defaultMessage = SVNTeamUIPlugin.instance().getResource("CommitPanel.Message");
			this.dialogDescription = SVNTeamUIPlugin.instance().getResource("CommitPanel.Description");
		}
		this.changeListenerList = new ArrayList<IResourceSelectionChangeListener>();
		this.userSelectedResources = userSelectedResources;
	}
    
	public void createControlsImpl(Composite parent) {
    	GridData data = null;
    	GridLayout layout = null;

    	layout = (GridLayout)parent.getLayout();
    	layout.marginHeight = 3;
    	
    	this.sForm = new SashForm(parent, SWT.VERTICAL);
    	layout = new GridLayout();
    	layout.marginHeight = layout.marginWidth = 0;
    	layout.verticalSpacing = 3;
    	this.sForm.setLayout(layout);
    	data = new GridData(GridData.FILL_BOTH);
    	data.heightHint = 400;
    	this.sForm.setLayoutData(data);
    	
    	Composite composite = new Composite(this.sForm, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		layout = new GridLayout();
		layout.verticalSpacing = 4;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
    	    	
		Group group = new Group(composite, SWT.NULL);
    	layout = new GridLayout();
		group.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		group.setLayoutData(data);
		group.setText(SVNTeamUIPlugin.instance().getResource("CommitPanel.Comment"));
		
		CommitPanel.CollectPropertiesOperation op = new CollectPropertiesOperation(this.resources);
    	UIMonitorUtility.doTaskNowDefault(op, true);
		
		this.bugtraqModel = op.getBugtraqModel();
		this.minLogSize = op.getMinLogSize();
		this.maxLogWidth = op.getMaxLogWidth();
    	this.comment = new CommentComposite(group, this.proposedComment, this, op.getLogTemplates(), this.bugtraqModel, this.minLogSize, this.maxLogWidth);
		data = new GridData(GridData.FILL_BOTH);
		this.comment.setLayoutData(data);
		
		Composite middleComposite = new Composite(composite, SWT.NULL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 2;
		layout.numColumns = 2;
		middleComposite.setLayoutData(data);
		middleComposite.setLayout(layout);
		
		this.keepLocksButton = new Button(middleComposite, SWT.CHECK);
		data = new GridData(GridData.BEGINNING | GridData.FILL_HORIZONTAL);
		this.keepLocksButton.setLayoutData(data);
		this.keepLocksButton.setText(SVNTeamUIPlugin.instance().getResource("CommitPanel.KeepLocks"));
		this.keepLocksButton.setSelection(false);
		this.keepLocksButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CommitPanel.this.keepLocks = CommitPanel.this.keepLocksButton.getSelection();
			}
		});
		
		this.pasteNamesButton = new Button(middleComposite, SWT.PUSH | SWT.END);
		data = new GridData();
		this.pasteNamesButton.setLayoutData(data);
		this.pasteNamesButton.setText(SVNTeamUIPlugin.instance().getResource("CommitPanel.PasteNames.Button"));
		this.pasteNamesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CommitPanel.this.pasteNames();
			}
		});
		
		Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.selectionComposite = new ResourceSelectionComposite(this.sForm, SWT.NONE, this.resources, true, this.userSelectedResources);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 175;
		this.selectionComposite.setLayoutData(data);
		
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		int first = SVNTeamPreferences.getDialogInt(store, SVNTeamPreferences.COMMIT_DIALOG_WEIGHT_NAME);
		this.sForm.setWeights(new int[] {first, 100 - first});
		
		this.selectionComposite.addResourcesSelectionChangedListener(new IResourceSelectionChangeListener() {
			public void resourcesSelectionChanged(ResourceSelectionChangedEvent event) {
				CommitPanel.this.fireResourcesSelectionChanged(event);
			}
		});
		this.attachTo(this.selectionComposite, new AbstractVerifier() {
			protected String getErrorMessage(Control input) {
				IResource []selection = CommitPanel.this.getSelectedResources();
				if (selection == null || selection.length == 0) {
					return SVNTeamUIPlugin.instance().getResource("ResourceSelectionComposite.Verifier.Error");
				}
				if (FileUtility.checkForResourcesPresenceRecursive(selection, IStateFilter.SF_CONFLICTING)) {
					return SVNTeamUIPlugin.instance().getResource("CommitPanel.Conflicting.Error");
				}
				return null;
			}
			protected String getWarningMessage(Control input) {
				return null;
			}
		});
		this.addContextMenu();
    }
	
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.commitDialogContext";
    }
	
	public void postInit() {
		super.postInit();
		this.resourceStatesListener = new IResourceStatesListener() {
			public void resourcesStateChanged(ResourceStatesChangedEvent event) {
				CommitPanel.this.updateResources(event);
			}
		};
		SVNRemoteStorage.instance().addResourceStatesListener(ResourceStatesChangedEvent.class, CommitPanel.this.resourceStatesListener);
	}
	
	protected void saveChangesImpl() {
		super.saveChangesImpl();
		this.retainWeights();
	}
	
	protected void cancelChangesImpl() {
		super.cancelChangesImpl();
		this.retainWeights();
	}
	
	public boolean canClose() {
		final boolean []commit = new boolean[] {true};
		if (this.bugtraqModel != null && 
				this.bugtraqModel.getMessage() != null &&
				this.bugtraqModel.isWarnIfNoIssue() &&
				this.comment.getBugID() != null &&
				this.comment.getBugID().trim().length() == 0) {
			UIMonitorUtility.getDisplay().syncExec(new Runnable() {
				public void run() {
					MessageDialog dlg = new MessageDialog(
							UIMonitorUtility.getShell(), 
							SVNTeamUIPlugin.instance().getResource("CommitPanel.NoBugId.Title"), 
							null, 
							SVNTeamUIPlugin.instance().getResource("CommitPanel.NoBugId.Message"), 
							MessageDialog.WARNING, 
							new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}, 
							0);
					commit[0] = dlg.open() == 0;
				}
			});
		}
		return commit[0];
	}
	
	protected void pasteNames() {
		List selectedResources = this.selectionComposite.getCurrentSelection();
		String namesString = "";
		for (Iterator iter = selectedResources.iterator(); iter.hasNext();) {
			IResource resource = (IResource) iter.next();
			namesString += resource.getName() + "\n";
		}
		this.comment.insertText(namesString);
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
				
				//paste selected names action
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("CommitPanel.PasteNames.Action")) {
					public void run() {
						CommitPanel.this.pasteNames();
					}
				});
				tAction.setEnabled(tSelection.size() > 0);
				
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
				
				//Revert action
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("CommitPanel.Revert.Action")) {
					public void run() {
						IResource[] changedResources = FileUtility.getResourcesRecursive(selectedResources, RevertAction.SF_REVERTABLE_OR_NEW);
						CompositeOperation revertOp = RevertAction.getRevertOperation(UIMonitorUtility.getShell(), changedResources, selectedResources);
						if (revertOp != null) {
							UIMonitorUtility.doTaskNowDefault(revertOp, true);
						}
					}
				});
				tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/revert.gif"));
				tAction.setEnabled(tSelection.size() > 0);
				
				//Ignore resources group
				if (tSelection.size() > 0 && selectedResources.length == FileUtility.getResourcesRecursive(selectedResources, AddToSVNIgnoreAction.SF_NEW_AND_PARENT_VERSIONED).length) {				
					MenuManager subMenu = new MenuManager(SVNTeamUIPlugin.instance().getResource("CommitPanel.Ignore.Group"));
					if (tSelection.size() > 1) {
						subMenu.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("CommitPanel.IgnoreByName.Multiple.Action")) {
							public void run() {
								CompositeOperation op = new CompositeOperation("AddToIgnore");
								op.add(new AddToSVNIgnoreOperation(selectedResources, IRemoteStorage.IGNORE_NAME, null));
								op.add(new RefreshResourcesOperation(new ResourcesParentsProvider(CommitPanel.this.resources), IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL));
								UIMonitorUtility.doTaskNowDefault(op, true);
							}
						});
						tAction.setEnabled(true);
						subMenu.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("CommitPanel.IgnoreByExtension.Multiple.Action")) {
							public void run() {
								CompositeOperation op = new CompositeOperation("AddToIgnore");
								op.add(new AddToSVNIgnoreOperation(selectedResources, IRemoteStorage.IGNORE_EXTENSION, null));
								op.add(new RefreshResourcesOperation(new ResourcesParentsProvider(CommitPanel.this.resources), IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL));
								UIMonitorUtility.doTaskNowDefault(op, true);							
							}
						});
						tAction.setEnabled(true);
					}
					else {
						subMenu.add(tAction = new Action(selectedResources[0].getName()) {
							public void run() {
								CompositeOperation op = new CompositeOperation("AddToIgnore");
								op.add(new AddToSVNIgnoreOperation(selectedResources, IRemoteStorage.IGNORE_NAME, null));
								op.add(new RefreshResourcesOperation(new ResourcesParentsProvider(CommitPanel.this.resources), IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL));
								UIMonitorUtility.doTaskNowDefault(op, true);							
							}
						});
						tAction.setEnabled(true);
						String name = selectedResources[0].getName();
						String [] parts = name.split("\\.");
						if ((parts.length != 0)) {
							subMenu.add(tAction = new Action("*." + parts[parts.length-1]) {
								public void run() {
									CompositeOperation op = new CompositeOperation("AddToIgnore");
									op.add(new AddToSVNIgnoreOperation(selectedResources, IRemoteStorage.IGNORE_EXTENSION, null));
									op.add(new RefreshResourcesOperation(new ResourcesParentsProvider(CommitPanel.this.resources), IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL));
									UIMonitorUtility.doTaskNowDefault(op, true);
								}
							});
							tAction.setEnabled(true);
						}
					}
					manager.add(subMenu);
				}
				
				//Edit conflicts action
				manager.add (tAction = new Action(SVNTeamUIPlugin.instance().getResource("EditConflictsAction.label")) {
					public void run() {
						UIMonitorUtility.doTaskScheduledDefault(new ShowConflictEditorOperation(FileUtility.getResourcesRecursive(selectedResources, IStateFilter.SF_CONFLICTING), true));
					}
				});
				tAction.setEnabled(FileUtility.checkForResourcesPresenceRecursive(selectedResources, IStateFilter.SF_CONFLICTING));
				
				//Mark as merged action
				manager.add (tAction = new Action(SVNTeamUIPlugin.instance().getResource("CommitPanel.MarkAsMerged.Action")) {
					public void run() {
						MarkAsMergedOperation mainOp = new MarkAsMergedOperation(selectedResources, false, null);
						CompositeOperation op = new CompositeOperation(mainOp.getId());
						op.add(mainOp);
						op.add(new RefreshResourcesOperation(FileUtility.getParents(selectedResources, false)));
						UIMonitorUtility.doTaskNowDefault(op, false);
					}
				});
				tAction.setEnabled(FileUtility.checkForResourcesPresenceRecursive(selectedResources, IStateFilter.SF_CONFLICTING) && selectedResources.length == 1);
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
						if (!IStateFilter.SF_INTERNAL_INVALID.accept(local)) {
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
						if (!IStateFilter.SF_INTERNAL_INVALID.accept(local)) {
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
						if (!IStateFilter.SF_INTERNAL_INVALID.accept(local)) {
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
				if (!CommitPanel.this.selectionComposite.isDisposed()) {
					CommitPanel.this.selectionComposite.setResources(newResources);
					CommitPanel.this.selectionComposite.fireSelectionChanged();
				}
			}
		});
		
		this.resources = newResources;
		this.resourcesChanged = true;
	}
    
    public boolean getResourcesChanged() {
    	return this.resourcesChanged;
    }
    
	public IResource []getSelectedResources() {
    	return this.selectionComposite.getSelectedResources();
    }
    
    public IResource []getNotSelectedResources() {
    	return this.selectionComposite.getNotSelectedResources();
    }
    
    public void addResourcesSelectionChangedListener(IResourceSelectionChangeListener listener) {
		this.changeListenerList.add(listener);
	}
	
	public void removeResourcesSelectionChangedListener(IResourceSelectionChangeListener listener) {
		this.changeListenerList.remove(listener);
	}
	
	public void fireResourcesSelectionChanged(ResourceSelectionChangedEvent event) {
		this.validateContent();
		IResourceSelectionChangeListener []listeners = this.changeListenerList.toArray(new IResourceSelectionChangeListener[this.changeListenerList.size()]);
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].resourcesSelectionChanged(event);
		}
	}
    
    public boolean getKeepLocks() {
    	return this.keepLocks;
    }
    
    protected Point getPrefferedSizeImpl() {
    	return new Point(600, SWT.DEFAULT);
    }
    
    protected void retainWeights() {
    	int []weights = this.sForm.getWeights();
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		SVNTeamPreferences.setDialogInt(store, SVNTeamPreferences.COMMIT_DIALOG_WEIGHT_NAME, weights[0] / 10);
    }
         
    public void dispose() {
    	super.dispose();
    	SVNRemoteStorage.instance().removeResourceStatesListener(ResourceStatesChangedEvent.class, this.resourceStatesListener);
    }
    
    public static class CollectPropertiesOperation extends AbstractActionOperation {
    	protected IResource []resources;
    	protected MinLogSizePropFindVisitor minLogVisitor;
    	protected LogTemplatesPropFindVisitor logTemplateVisitor;
    	protected BugtraqPropFindVisitor bugtraqVisitor;
    	protected MaxLogWidthPropFindVisitor maxWidthVisitor;
    	protected MinLockSizePropFindVisitor minLockVisitor;
    	protected CompositePropFindVisitor compositeVisitor;
    	
    	public CollectPropertiesOperation(IResource []resources) {
    		super("Operation.CollectProperties");
    		this.resources = resources;
    		this.logTemplateVisitor = new LogTemplatesPropFindVisitor();
    		this.bugtraqVisitor = new BugtraqPropFindVisitor();
    		this.minLogVisitor = new MinLogSizePropFindVisitor();
    		this.maxWidthVisitor = new MaxLogWidthPropFindVisitor();
    		this.minLockVisitor = new MinLockSizePropFindVisitor();
    		if (SVNTeamPreferences.getCommentTemplatesBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.COMMENT_LOG_TEMPLATES_ENABLED_NAME)) {
    			this.compositeVisitor = new CompositePropFindVisitor(new IPropFindVisitor [] {this.logTemplateVisitor, this.bugtraqVisitor, this.minLogVisitor, this.maxWidthVisitor, this.minLockVisitor});
        	}
    		else {
    			this.compositeVisitor = new CompositePropFindVisitor(new IPropFindVisitor [] {this.bugtraqVisitor, this.minLogVisitor, this.maxWidthVisitor, this.minLockVisitor});
    		}
    	}
    	
    	protected void runImpl(IProgressMonitor monitor) throws Exception {
    		ArrayList<IResource> parentProperties = new ArrayList<IResource>();
			
			int length = this.resources.length < CommitPanel.MAXIMUM_CHECKS_SIZE ? this.resources.length : CommitPanel.MAXIMUM_CHECKS_SIZE;
	    	for (int i = 0; i < length && !monitor.isCanceled(); i++) {
	    		ProgressMonitorUtility.setTaskInfo(monitor, this, this.resources[i].getFullPath().toString());
	    		
	    		ILocalResource local = SVNRemoteStorage.instance().asLocalResourceAccessible(this.resources[i]);
    			IResource resourceToProcess = this.resources[i];
    			while (IStateFilter.SF_UNVERSIONED.accept(local)) {
    				resourceToProcess = resourceToProcess.getParent();
    				local = SVNRemoteStorage.instance().asLocalResourceAccessible(resourceToProcess);
    			}
				if (!this.processProperty(resourceToProcess, parentProperties, monitor)) {
    				break;
    			}
	    		
	    		ProgressMonitorUtility.progress(monitor, i, length);
			}
		}
    	
    	protected boolean processProperty(IResource resource, ArrayList<IResource> parentProperties, IProgressMonitor monitor) {
    		if (parentProperties.contains(resource) || monitor.isCanceled()) {
				return true;
			}
    		
    		GetPropertiesOperation op = new GetPropertiesOperation(resource);
    		ProgressMonitorUtility.doTaskExternalDefault(op, monitor);
        	if (op.getExecutionState() == IStatus.OK) {
        		SVNProperty []properties = op.getProperties();
        		if (properties != null) {
        			for (int i = 0; i < properties.length; i++) {
    					if (!this.compositeVisitor.visit(properties[i])) {
    						return false;
    					}
    				}
        		}
        	}
    		
    		parentProperties.add(resource);
    		
    		IResource parent = resource.getParent();
        	if (parent != null && !(parent instanceof IWorkspaceRoot) && !monitor.isCanceled()) {
        		return this.processProperty(parent, parentProperties, monitor);
        	}        	
        	return true;
    	}
    	
		public HashSet getLogTemplates() {
			return this.logTemplateVisitor.getLogTemplates();
		}
    	
    	public BugtraqModel getBugtraqModel() {
    		return this.bugtraqVisitor.getBugtraqModel();
    	}
    	
    	public int getMinLogSize() {
    		return this.minLogVisitor.getMinLogSize();
    	}
    	
    	public int getMinLockSize() {
    		return this.minLockVisitor.getMinLockSize();
    	}
    	
    	public int getMaxLogWidth() {
    		return this.maxWidthVisitor.getMaxLogWidth();
    	}
    }
    
}
