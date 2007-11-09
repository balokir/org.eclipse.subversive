/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Vladimir Bykov - Initial API and implementation
 *    Alessandro Nistico - [patch] Change Set's implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.client.PropertyData;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.SaveProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.property.GetPropertiesOperation;
import org.eclipse.team.svn.core.operation.local.refactor.DeleteResourceOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.events.IResourceStatesListener;
import org.eclipse.team.svn.core.resource.events.ResourceStatesChangedEvent;
import org.eclipse.team.svn.core.svnstorage.ResourcesParentsProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.local.RevertAction;
import org.eclipse.team.svn.ui.composite.CommentComposite;
import org.eclipse.team.svn.ui.composite.ResourceSelectionComposite;
import org.eclipse.team.svn.ui.dialog.DiscardConfirmationDialog;
import org.eclipse.team.svn.ui.event.IResourceSelectionChangeListener;
import org.eclipse.team.svn.ui.event.ResourceSelectionChangedEvent;
import org.eclipse.team.svn.ui.extension.factory.ICommentDialogPanel;
import org.eclipse.team.svn.ui.panel.common.CommentPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.properties.bugtraq.BugtraqModel;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;

/**
 * Commit panel 
 * 
 * @author Bykov Vladimir
 */
public class CommitPanel extends CommentPanel implements ICommentDialogPanel {
	public static final int MSG_COMMIT = 0;
	public static final int MSG_OVER_AND_COMMIT = 1;
	// Restricting the size of log templates set in order to prevent prolonged commit operation execution.
	// Set -1 as MAXIMUM_LOG_TEMPLATE_SIZE to refuse from restricting.
	public static final int MAXIMUM_LOG_TEMPLATE_SIZE = 20;
	public static final int MAXIMUM_CHECKS_SIZE = 100;
	
	protected Composite parent;
	protected ResourceSelectionComposite selectionComposite;
	protected Button keepLocksButton;
	protected Button pasteNamesButton;
	protected SashForm sForm;
	protected IResource []resources;
	protected boolean keepLocks;
	protected List changeListenerList;
	protected IResource[] userSelectedResources;
	
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
		this.changeListenerList = new ArrayList();
		this.userSelectedResources = userSelectedResources;
	}
    
	public void createControls(Composite parent) {
		this.parent = parent;
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
		
		CommitPanel.GetLogTemplatesOperation logTemplatesOp = new CommitPanel.GetLogTemplatesOperation(this.resources);
		CommitPanel.GetBugTraqPropertiesModelOperation bugtraqOp = new GetBugTraqPropertiesModelOperation(this.resources);
		CommitPanel.runPropertiesOperations(logTemplatesOp, bugtraqOp);
		
		this.bugtraqModel = bugtraqOp.getBugtraqModel();
    	this.comment = new CommentComposite(group, this.proposedComment, this, logTemplatesOp.getLogTemplates(), this.bugtraqModel);
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
		int first = SVNTeamPreferences.getCommitDialogInt(store, SVNTeamPreferences.COMMIT_DIALOG_WEIGHT_NAME);
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
				CommitPanel.this.updateResources();
			}
		};
		SVNRemoteStorage.instance().addResourceStatesListener(ResourceStatesChangedEvent.class, CommitPanel.this.resourceStatesListener);
	}
	
	protected void saveChanges() {
		super.saveChanges();
		this.retainSizeAndWeights();
	}
	
	protected void cancelChanges() {
		super.cancelChanges();
		this.retainSizeAndWeights();
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
				final IResource[] selectedResources = (IResource[])((List)tSelection.toList()).toArray(new IResource[tSelection.size()]);
				Action tAction = null;
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("CommitPanel.PasteNames.Action")) {
					public void run() {
						CommitPanel.this.pasteNames();
					}
				});
				tAction.setEnabled(tSelection.size() > 0);
				
				manager.add(new Separator());
				
				manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("CommitPanel.Revert.Action")) {
					public void run() {
						IResource[] changedResources = FileUtility.getResourcesRecursive(selectedResources, RevertAction.SF_REVERTABLE_OR_NEW);
						Shell shell = UIMonitorUtility.getShell();
						CompositeOperation revertOp = RevertAction.getRevertOperation(shell, changedResources, selectedResources);
						if (revertOp != null) {
							UIMonitorUtility.doTaskNowDefault(revertOp, true);
						}
					}
				});
				tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/revert.gif"));
				tAction.setEnabled(tSelection.size() > 0);
				
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
	
	protected void updateResources() {
		final TableViewer tableViewer = this.selectionComposite.getTableViewer();
		HashSet toDeleteSet = new HashSet();
		toDeleteSet.addAll(Arrays.asList(this.resources));
		HashSet newResourcesSet = new HashSet();
		newResourcesSet.addAll(Arrays.asList(FileUtility.getResourcesRecursive(this.resources, IStateFilter.SF_COMMITABLE, IResource.DEPTH_ZERO)));
		newResourcesSet.addAll(Arrays.asList(FileUtility.getResourcesRecursive(this.resources, IStateFilter.SF_ADDED, IResource.DEPTH_ZERO)));
		final IResource[] newResources = (IResource[])newResourcesSet.toArray(new IResource[newResourcesSet.size()]);
		toDeleteSet.removeAll(newResourcesSet);
		final IResource[] toDeleteResources = (IResource[])toDeleteSet.toArray(new IResource[toDeleteSet.size()]);
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				CommitPanel.this.selectionComposite.setResources(newResources);
				//FIXME isDisposed() test is necessary as dispose() method is not called from FastTrack Commit Dialog
				if (!tableViewer.getTable().isDisposed()) {
					tableViewer.remove(toDeleteResources);
					tableViewer.refresh();
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
		IResourceSelectionChangeListener []listeners = (IResourceSelectionChangeListener [])this.changeListenerList.toArray(new IResourceSelectionChangeListener[this.changeListenerList.size()]);
		for (int i = 0; i < listeners.length; i++) {
			((IResourceSelectionChangeListener)listeners[i]).resourcesSelectionChanged(event);
		}
	}
    
    public boolean getKeepLocks() {
    	return this.keepLocks;
    }
    
    public Point getPrefferedSize() {
    	IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
    	
    	return new Point(SVNTeamPreferences.getCommitDialogInt(store, SVNTeamPreferences.COMMIT_DIALOG_WIDTH_NAME), 
    			SVNTeamPreferences.getCommitDialogInt(store, SVNTeamPreferences.COMMIT_DIALOG_HEIGHT_NAME));
    }
    
    protected void retainSizeAndWeights() {
    	int []weights = this.sForm.getWeights();
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		Point size = this.parent.getSize();
		SVNTeamPreferences.setCommitDialogInt(store, SVNTeamPreferences.COMMIT_DIALOG_WIDTH_NAME, size.x);
		SVNTeamPreferences.setCommitDialogInt(store, SVNTeamPreferences.COMMIT_DIALOG_HEIGHT_NAME, size.y);
		SVNTeamPreferences.setCommitDialogInt(store, SVNTeamPreferences.COMMIT_DIALOG_WEIGHT_NAME, weights[0] / 10);
    }
    
    public static void runPropertiesOperations(CommitPanel.GetLogTemplatesOperation logOperation, CommitPanel.GetBugTraqPropertiesModelOperation bugtraqOp) {
    	CompositeOperation composite = new CompositeOperation("Operation.ScanForProperties");
    	if (SVNTeamPreferences.getCommentTemplatesBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.COMMENT_LOG_TEMPLATES_ENABLED_NAME)) {
    		composite.add(logOperation);
    	}
    	composite.add(bugtraqOp);
    	UIMonitorUtility.doTaskNowDefault(composite, true);
    }
    
    protected static String getLogTemplateProperty(IResource resource, Map parentTemplates) {
    	if (parentTemplates.containsKey(resource)) {
    		// if resource already in the map this mean that:
    		//	1) comment template already added and we do not need additional search
    		//	2) comment template does not exist on all levels and search have no sense
    		return (String)parentTemplates.get(resource);
    	}
    	
		String value = null;
    	GetPropertiesOperation op = new GetPropertiesOperation(resource);
    	ProgressMonitorUtility.doTaskExternalDefault(op, new NullProgressMonitor());
    	if (op.getExecutionState() == IStatus.OK) {
    		PropertyData []logTemplateProperties = op.getProperties();
    		if (logTemplateProperties != null && logTemplateProperties.length > 0) {
    			for (int i = 0; i < logTemplateProperties.length; i++) {
					if (logTemplateProperties[i].name.equals("tsvn:logtemplate")) {
						value = logTemplateProperties[i].value;
						break;
					}
				}
    		}
    	}
		parentTemplates.put(resource, value);
		if (value != null) {
			return value;
		}
    	
    	IResource parent = resource.getParent();
    	if (parent != null && !(parent instanceof IWorkspaceRoot)) {
    		return CommitPanel.getLogTemplateProperty(parent, parentTemplates);
    	}
    	
    	return null;
    }
    
    public void dispose() {
    	super.dispose();
    	SVNRemoteStorage.instance().removeResourceStatesListener(ResourceStatesChangedEvent.class, this.resourceStatesListener);
    }
    
    public static class GetLogTemplatesOperation extends AbstractNonLockingOperation {
    	
    	protected HashSet logTemplates;
    	protected IResource []resources;
    	
    	public GetLogTemplatesOperation(IResource []resources) {
    		super("Operation.CollectLogTemplates");
    		this.logTemplates = new HashSet();
    		this.resources = resources;
    	}

		protected void runImpl(IProgressMonitor monitor) throws Exception {
			HashMap parentTemplates = new HashMap();
			int length = resources.length < CommitPanel.MAXIMUM_CHECKS_SIZE ? resources.length : CommitPanel.MAXIMUM_CHECKS_SIZE;
	    	for (int i = 0; i < length && !monitor.isCanceled(); i++) {
	    		ProgressMonitorUtility.setTaskInfo(monitor, this, resources[i].getFullPath().toString());
	    		
	    		ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resources[i]);
	    		if (local != null && !IStateFilter.SF_UNVERSIONED.accept(resources[i], local.getStatus(), local.getChangeMask())) {
	    			String logTemplate = CommitPanel.getLogTemplateProperty(resources[i], parentTemplates);
	    			if (logTemplate != null) {
	    				this.logTemplates.add(logTemplate);
	    				if (this.logTemplates.size() == CommitPanel.MAXIMUM_LOG_TEMPLATE_SIZE) {
	    					break;
	    				}
	    			}
	    		}
	    		
	    		ProgressMonitorUtility.progress(monitor, i, length);
			}
		}

		public HashSet getLogTemplates() {
			return this.logTemplates;
		}
    }
    
    public static class GetBugTraqPropertiesModelOperation extends AbstractNonLockingOperation {
    	
    	protected IResource[] resources;
    	protected BugtraqModel model;
    	
    	public GetBugTraqPropertiesModelOperation(IResource []resources) {
    		super("Operation.CollectBugTraq");
    		this.resources = resources;
    		this.model = new BugtraqModel();
		}
    	
    	public BugtraqModel getBugtraqModel() {
    		return this.model;
    	}

		protected void runImpl(IProgressMonitor monitor) throws Exception {
			Set bugtraqProperties = new HashSet(
					Arrays.asList(new String[] {"bugtraq:url",
												 "bugtraq:logregex",
												 "bugtraq:label",
												 "bugtraq:message",
												 "bugtraq:number",
												 "bugtraq:warnifnoissue",
												 "bugtraq:append"}));
			IResource versionedResource = null;
			for (int i = 0; i < this.resources.length && !monitor.isCanceled(); i++) {
				ILocalResource local = SVNRemoteStorage.instance().asLocalResource(this.resources[i]);
	    		if (local != null && !IStateFilter.SF_UNVERSIONED.accept(this.resources[i], local.getStatus(), local.getChangeMask())) {
	    			versionedResource = this.resources[i];
	    		}
			}
			if (versionedResource != null) {
				this.getBugtraqProperties(bugtraqProperties, versionedResource);
			}
		}
		
		protected Set getBugtraqProperties(Set bugtraqProperties, IResource resource) {
	    	GetPropertiesOperation op = new GetPropertiesOperation(resource);
	    	ProgressMonitorUtility.doTaskExternalDefault(op, new NullProgressMonitor());
	    	if (op.getExecutionState() == IStatus.OK) {
	    		PropertyData []resourceProperties = op.getProperties();
	    		if (resourceProperties != null && resourceProperties.length > 0) {
	    			for (int i = 0; i < resourceProperties.length; i++) {
						if (bugtraqProperties.contains(resourceProperties[i].name)) {
							this.processBugtraqProperty(resourceProperties[i].name, resourceProperties[i].value);
							bugtraqProperties.remove(resourceProperties[i].name);
						}
					}
	    		}
	    	}
	    	
	    	IResource parent = resource.getParent();
	    	if (parent != null && !(parent instanceof IWorkspaceRoot)) {
	    		if (!bugtraqProperties.isEmpty()) {
	    			return this.getBugtraqProperties(bugtraqProperties, parent);
	    		}
	    	}
			return bugtraqProperties;
		}
		
		protected void processBugtraqProperty(String name, String value) {
			if (name.equals("bugtraq:url")) {
				this.model.setUrl(value);
			}
			else if (name.equals("bugtraq:logregex")) {
				this.model.setLogregex(value);
			}
			else if (name.equals("bugtraq:label")) {
				this.model.setLabel(value);
			}
			else if (name.equals("bugtraq:message")) {
				this.model.setMessage(value);
			}
			else if (name.equals("bugtraq:number")) {
				boolean number = value == null || !(value.trim().equals("false") || value.trim().equals("no"));
				this.model.setNumber(number);
			}
			else if (name.equals("bugtraq:warnifnoissue")) {
				boolean warn = value != null && (value.trim().equals("yes") || value.trim().equals("true"));
				this.model.setWarnIfNoIssue(warn);
			}
			else if (name.equals("bugtraq:append")) {
				boolean append = value == null || !(value.trim().equals("false") || value.trim().equals("no"));
				this.model.setAppend(append);
			}
 		}
    }
    
}
