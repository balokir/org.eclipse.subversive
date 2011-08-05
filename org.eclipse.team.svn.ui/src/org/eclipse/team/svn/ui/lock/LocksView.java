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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.ui.AbstractSVNView;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.operation.ScanLocksOperation;
import org.eclipse.team.svn.ui.operation.ScanLocksOperation.CreateLockResourcesHierarchyOperation;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

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
	
	protected Action linkWithEditorAction;
	protected Action linkWithEditorDropDownAction;
	
	public LocksView() {
		super(SVNUIMessages.LocksView_SVNLocks);
	}	
	
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		
	    this.isLinkWithEditorEnabled = SVNTeamPreferences.getPropertiesBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.LOCKS_LINK_WITH_EDITOR_NAME);	    		 
			
		this.locksComposite = new LocksComposite(parent); 
		this.locksComposite.setLayoutData(new GridData(GridData.FILL_BOTH));		
		this.refresh();
		
		this.createActionBars();

        //Setting context help
	    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.team.svn.help.locksViewContext"); //$NON-NLS-1$
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
        		LocksView.this.refresh();
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
			this.setResource(resource);
		}
	}
	
	public void setResourceWithoutActionExecution(IResource resource) {
		this.wcResource = resource;
		this.locksComposite.setResource(resource);
	}
	
	public void setResource(IResource resource) {
		this.setResourceWithoutActionExecution(resource);
		this.refresh();
	}
	
	public IActionOperation getUpdateViewOperation() {
		CompositeOperation op = null;
		if (this.wcResource != null) {
			ScanLocksOperation mainOp = new ScanLocksOperation(new IResource[]{this.wcResource});
			op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
			
			op.add(new AbstractActionOperation("", SVNUIMessages.class) { //$NON-NLS-1$
				protected void runImpl(IProgressMonitor monitor) throws Exception {										
					//set pending
					LocksView.this.locksComposite.setPending(true);
					LocksView.this.getSite().getShell().getDisplay().syncExec(new Runnable() {
						public void run() {
							LocksView.this.showResourceLabel();
							LocksView.this.locksComposite.initializeComposite();
						}
					});
				}
			});
			
			op.add(mainOp);
			/*
			 * As we don't want that scan locks operation to write in console, pass console stream as null.
			 * Scan locks operation writes only last notification in status, which is not useful info
			 * so we disable it.
			 */
			mainOp.setConsoleStream(null);
			
			final CreateLockResourcesHierarchyOperation createHierarchyOp = new CreateLockResourcesHierarchyOperation(mainOp);
			op.add(createHierarchyOp, new IActionOperation[]{mainOp});
			
			//update composite
			op.add(new AbstractActionOperation("", SVNUIMessages.class) {				 //$NON-NLS-1$
				protected void runImpl(IProgressMonitor monitor) throws Exception {
					LocksView.this.locksComposite.setRootLockResource(createHierarchyOp.getLockResourceRoot());
					UIMonitorUtility.getDisplay().syncExec(new Runnable() {
						public void run() {
							LocksView.this.locksComposite.setPending(false);
							LocksView.this.locksComposite.initializeComposite();
						}
					});
				}
			}, new IActionOperation[]{createHierarchyOp});									
		}
		return op;
	}
	
	public void refresh() {
		IActionOperation op = this.getUpdateViewOperation();
		if (op != null) {
			ProgressMonitorUtility.doTaskScheduled(op, false);	
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
	
	public static LocksView instance() {
		final LocksView []view = new LocksView[1];
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = SVNTeamUIPlugin.instance().getWorkbench().getActiveWorkbenchWindow();
				if (window != null && window.getActivePage() != null) {
					view[0] = (LocksView)window.getActivePage().findView(LocksView.VIEW_ID);
				}
			}
		});
		return view[0];
	}
}
