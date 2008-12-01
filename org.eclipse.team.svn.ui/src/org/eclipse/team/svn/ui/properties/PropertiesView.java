/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexander Gurov - Initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.ui.properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IResourcePropertyProvider;
import org.eclipse.team.svn.core.operation.local.property.GetPropertiesOperation;
import org.eclipse.team.svn.core.operation.remote.GetRemotePropertiesOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.AbstractSVNView;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.PropertiesComposite;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;

/**
 * Properties view
 *
 * @author Alexander Gurov
 */
public class PropertiesView extends AbstractSVNView {
	
	public static final String VIEW_ID = PropertiesView.class.getName();

	protected PropertiesComposite propertiesComposite;
	protected IResourcePropertyProvider propertyProvider;
	protected IAdaptable adaptable;
	protected Action linkWithEditorAction;
	protected Action linkWithEditorDropDownAction;
	protected boolean backgroundExecution;
	
	public PropertiesView() {
		super(SVNUIMessages.PropertiesView_Description);
	}
	
	public void setFocus() {
	}
	
	public void setResource(IAdaptable resource, IResourcePropertyProvider propertyProvider, boolean backgroundExecution) {
		if (resource instanceof IRepositoryResource) {
			this.repositoryResource = (IRepositoryResource)resource;
			this.wcResource = null;
		}
		else if(resource instanceof IResource) {
			this.wcResource = (IResource)resource;
			this.repositoryResource = null;
		}
		this.adaptable = resource;
		this.propertyProvider = propertyProvider;		
		this.backgroundExecution = backgroundExecution;
		
		this.propertiesComposite.setResource(resource, propertyProvider);
		this.refreshView();
	}
	
	public void refreshView() {
		boolean operationToFollow = this.propertyProvider != null && this.propertyProvider.getExecutionState() != IStatus.OK;
		this.propertiesComposite.setPending(operationToFollow);
		this.getSite().getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				PropertiesView.this.showResourceLabel();
				PropertiesView.this.propertiesComposite.initializeComposite();
			}
		});
		CompositeOperation composite = new CompositeOperation("Operation.ShowProperties");
		if (this.propertyProvider != null && this.propertyProvider.getExecutionState() != IStatus.OK) {
			composite.add(this.propertyProvider);
			composite.add(this.propertiesComposite.getRefreshViewOperation(), new IActionOperation[] {this.propertyProvider});
		}
		else {
			composite.add(this.propertiesComposite.getRefreshViewOperation());
		}
		
		if (this.backgroundExecution) {
			UIMonitorUtility.doTaskScheduledDefault(composite);
		}		
		else {
			UIMonitorUtility.doTaskScheduledDefault(this, composite);
		}
	}

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
	    this.isLinkWithEditorEnabled = SVNTeamPreferences.getPropertiesBoolean(store, SVNTeamPreferences.PROPERTY_LINK_WITH_EDITOR_NAME);
		this.propertiesComposite = new PropertiesComposite(parent); 
		this.propertiesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.propertiesComposite.setPropertiesView(this);
		this.refreshView();
		
		//drop-down menu
        IActionBars actionBars = this.getViewSite().getActionBars();	    
	    IMenuManager actionBarsMenu = actionBars.getMenuManager();
	    
	    this.linkWithEditorDropDownAction = new Action(SVNUIMessages.SVNView_LinkWith_Label, Action.AS_CHECK_BOX) {
	    	public void run() {
	    		PropertiesView.this.linkWithEditor();
	    		PropertiesView.this.linkWithEditorAction.setChecked(PropertiesView.this.isLinkWithEditorEnabled);
	    	}
	    };
	    this.linkWithEditorDropDownAction.setChecked(this.isLinkWithEditorEnabled);
	    
	    actionBarsMenu.add(this.linkWithEditorDropDownAction);

	    IToolBarManager tbm = actionBars.getToolBarManager();
        tbm.removeAll();
        Action action = new Action(SVNUIMessages.SVNView_Refresh_Label) {
        	public void run() {
	    		PropertiesView.this.refreshAction();
	    	}
        };
        action.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/refresh.gif"));
        tbm.add(action);        
        tbm.add(this.getLinkWithEditorAction());
        
        tbm.update(true);
        
        this.getSite().getPage().addSelectionListener(this.selectionListener);

        //Setting context help
	    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.team.svn.help.propertiesViewContext");
	}

	protected void disconnectView() {
		this.propertiesComposite.disconnectComposite();
		this.wcResource = null;
	}
	
	protected void refreshAction() {
		if (PropertiesView.this.repositoryResource != null) {
			PropertiesView.this.propertyProvider = new GetRemotePropertiesOperation(PropertiesView.this.repositoryResource);
			PropertiesView.this.propertiesComposite.setResource(PropertiesView.this.adaptable, PropertiesView.this.propertyProvider);
		}
		PropertiesView.this.refreshView();
	}
	
	protected Action getLinkWithEditorAction() {
		this.linkWithEditorAction = new Action(SVNUIMessages.SVNView_LinkWith_Label, IAction.AS_CHECK_BOX) {
	        public void run() {
	            PropertiesView.this.linkWithEditor();
	            PropertiesView.this.linkWithEditorDropDownAction.setChecked(PropertiesView.this.isLinkWithEditorEnabled);
	        }
	    };
	    this.linkWithEditorAction.setToolTipText(SVNUIMessages.SVNView_LinkWith_ToolTip);
	    this.linkWithEditorAction.setDisabledImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/link_with_disabled.gif"));
	    this.linkWithEditorAction.setHoverImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/link_with.gif"));
	    
	    
	    this.linkWithEditorAction.setChecked(this.isLinkWithEditorEnabled);
	    
	    return this.linkWithEditorAction;
	}
	
	
	protected void linkWithEditor() {
		this.isLinkWithEditorEnabled = !this.isLinkWithEditorEnabled;
        IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
        SVNTeamPreferences.setPropertiesBoolean(store, SVNTeamPreferences.PROPERTY_LINK_WITH_EDITOR_NAME, this.isLinkWithEditorEnabled);
        if (this.isLinkWithEditorEnabled) {
        	this.editorActivated(this.getSite().getPage().getActiveEditor());
		}
	}
	
	protected void updateViewInput(IRepositoryResource resource) {
		if (this.repositoryResource != null && this.repositoryResource.equals(resource)) {
			return;
		}
		if (resource != null) {
			this.setResource(resource, new GetRemotePropertiesOperation(resource), true);
		}
	}
	
	protected void updateViewInput(IResource resource) {
		ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
		if (IStateFilter.SF_EXCLUDE_PREREPLACED_AND_DELETED.accept(local)) {
			if (local.getResource().equals(this.wcResource)) {
				return;
			}
			this.setResource(resource, new GetPropertiesOperation(resource), true);
		}
	}

	protected boolean needsLinkWithEditorAndSelection() {
		return true;
	}

}
