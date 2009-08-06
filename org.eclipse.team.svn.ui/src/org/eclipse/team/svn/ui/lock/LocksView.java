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

package org.eclipse.team.svn.ui.lock;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.AbstractSVNView;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.IActionBars;

/**
 * SVN Lock View
 * 
 * Shows locks on files
 * 
 * @author Igor Burilo
 */
public class LocksView extends AbstractSVNView {

	public static final String VIEW_ID = LocksView.class.getName();
	
	protected LocksComposite locksComposite;
	protected boolean backgroundExecution;
	
	protected Action linkWithEditorAction;
	protected Action linkWithEditorDropDownAction;
	
	public LocksView() {
		super(SVNUIMessages.LocksView_SVNLocks);
	}	
	
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		
	    this.isLinkWithEditorEnabled = SVNTeamPreferences.getPropertiesBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.LOCKS_LINK_WITH_EDITOR_NAME);	    		 
			
		this.locksComposite = new LocksComposite(parent, this); 
		this.locksComposite.setLayoutData(new GridData(GridData.FILL_BOTH));		
		this.refreshView();
		
		this.createActionBars();

        //TODO Setting context help
	    //PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.team.svn.help.propertiesViewContext"); //$NON-NLS-1$
	}
	
	protected void createActionBars() {
		//drop-down menu
        IActionBars actionBars = this.getViewSite().getActionBars();	    
	    IMenuManager actionBarsMenu = actionBars.getMenuManager();
	    
	    this.linkWithEditorDropDownAction = new Action(SVNUIMessages.SVNView_LinkWith_Label, Action.AS_CHECK_BOX) {
	    	public void run() {
	    		LocksView.this.linkWithEditor();
	    		LocksView.this.linkWithEditorAction.setChecked(LocksView.this.isLinkWithEditorEnabled);
	    	}
	    };
	    this.linkWithEditorDropDownAction.setChecked(this.isLinkWithEditorEnabled);
	    
	    actionBarsMenu.add(this.linkWithEditorDropDownAction);

	    IToolBarManager tbm = actionBars.getToolBarManager();
        tbm.removeAll();
        Action action = new Action(SVNUIMessages.SVNView_Refresh_Label) {
        	public void run() {
        		LocksView.this.refreshAction();
	    	}
        };
        action.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/refresh.gif")); //$NON-NLS-1$
        tbm.add(action);        
        tbm.add(this.getLinkWithEditorAction());
        
        tbm.update(true);
        
        this.getSite().getPage().addSelectionListener(this.selectionListener);
	}
	
	protected Action getLinkWithEditorAction() {
		this.linkWithEditorAction = new Action(SVNUIMessages.SVNView_LinkWith_Label, IAction.AS_CHECK_BOX) {
	        public void run() {
	            LocksView.this.linkWithEditor();
	            LocksView.this.linkWithEditorDropDownAction.setChecked(LocksView.this.isLinkWithEditorEnabled);
	        }
	    };
	    this.linkWithEditorAction.setToolTipText(SVNUIMessages.SVNView_LinkWith_ToolTip);
	    this.linkWithEditorAction.setDisabledImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/link_with_disabled.gif")); //$NON-NLS-1$
	    this.linkWithEditorAction.setHoverImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/link_with.gif")); //$NON-NLS-1$	    
	    
	    this.linkWithEditorAction.setChecked(this.isLinkWithEditorEnabled);
	    
	    return this.linkWithEditorAction;
	}
	
	protected void refreshAction() {
		this.refreshView();				
	}
	
	protected void linkWithEditor() {
		this.isLinkWithEditorEnabled = !this.isLinkWithEditorEnabled;
        IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
        SVNTeamPreferences.setPropertiesBoolean(store, SVNTeamPreferences.LOCKS_LINK_WITH_EDITOR_NAME, this.isLinkWithEditorEnabled);
        if (this.isLinkWithEditorEnabled) {
        	this.editorActivated(this.getSite().getPage().getActiveEditor());
		}
	}
	
	protected void updateViewInput(IResource resource) {		
		ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
		if (IStateFilter.SF_VERSIONED.accept(local)) {
			if (resource.equals(this.wcResource)) {
				return;
			}
			this.setResource(resource, true);
		}
	}
	
	public void setResource(IResource resource, boolean backgroundExecution) {		
		this.wcResource = resource;					
		this.backgroundExecution = backgroundExecution;		
		this.locksComposite.setResource(resource);		
		this.refreshView();
	}
	
	protected void refreshView() {
		if (this.wcResource != null) {
			RetrieveLocksOperation mainOp = new RetrieveLocksOperation(this.wcResource, this.locksComposite);
			
			this.locksComposite.setPending(true);
			this.getSite().getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					LocksView.this.showResourceLabel();
					LocksView.this.locksComposite.initializeComposite();
				}
			});
			
			if (this.backgroundExecution) {
				UIMonitorUtility.doTaskScheduledDefault(mainOp);
			}		
			else {
				UIMonitorUtility.doTaskScheduledDefault(this, mainOp);
			}	
		}
	}
	
	protected void disconnectView() {
		this.locksComposite.disconnectComposite();
		this.wcResource = null;
	}

	protected boolean needsLinkWithEditorAndSelection() {
		return true;
	}

	public void setFocus() {
		
	}
}
