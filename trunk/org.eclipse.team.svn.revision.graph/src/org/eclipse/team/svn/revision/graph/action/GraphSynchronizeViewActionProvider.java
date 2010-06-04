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
package org.eclipse.team.svn.revision.graph.action;

import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphMessages;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphPlugin;
import org.eclipse.team.svn.revision.graph.operation.RevisionGraphUtility;
import org.eclipse.team.svn.ui.synchronize.UpdateModelActionGroup;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.ObjectPluginAction;
import org.eclipse.ui.navigator.CommonActionProvider;

/**
 * Contribute revision graph action to model-aware synchronize view 
 * 
 * @author Igor Burilo
 */
public class GraphSynchronizeViewActionProvider extends CommonActionProvider {

	protected final ShowRevisionGraphAction showRevisionGraph = new ShowRevisionGraphAction();
	
	protected static class ShowRevisionGraphAction extends Action implements IViewActionDelegate {

		protected IViewPart viewPart;
		
		protected IStructuredSelection selection = new StructuredSelection(); 
		
		public ShowRevisionGraphAction() {
			this.setText(SVNRevisionGraphMessages.ShowRevisionGraphAction);
			this.setToolTipText(SVNRevisionGraphMessages.ShowRevisionGraphAction);
			this.setImageDescriptor(SVNRevisionGraphPlugin.instance().getImageDescriptor("icons/showgraph.png")); //$NON-NLS-1$
		}
		
		public void init(IViewPart viewPart) {
			this.viewPart = viewPart;
		}

		@Override
		public void run() {
			if (this.selection instanceof IStructuredSelection) {
				this.doRun();
			}
		} 
		
		public void run(IAction action) {						
			if (action instanceof ObjectPluginAction) {
				ObjectPluginAction objectAction = (ObjectPluginAction) action;
				if (objectAction.getSelection() instanceof IStructuredSelection) {
					this.selection = (IStructuredSelection) objectAction.getSelection();					
					this.doRun();
				}
			} 
		}
		
		protected void doRun() { 			
			IResource resource = this.getSelectedResource();
			IRepositoryResource reposResource = SVNRemoteStorage.instance().asRepositoryResource(resource);
			IActionOperation op = RevisionGraphUtility.getRevisionGraphOperation(reposResource);
			if (op != null) {
				UIMonitorUtility.doTaskScheduledDefault(this.viewPart, op);	
			}
		}

		public void selectionChanged(IAction action, ISelection selection) {
			if (selection instanceof IStructuredSelection) {
				this.selection = (IStructuredSelection) selection;				
			} else {
				this.selection = StructuredSelection.EMPTY;
			}
			this.setEnabled(this.isEnabledForSelection());
		}
		
		protected boolean isEnabledForSelection() {
			if (this.selection.size() == 1) {
				IResource resource = this.getSelectedResource();
				//allow if resource is remotely deleted
				if (resource != null && FileUtility.checkForResourcesPresence(new IResource[]{ resource }, IStateFilter.SF_ONREPOSITORY, IResource.DEPTH_ZERO)) {
					return true;
				}				
			}
			return false;
		}
		
	    protected IResource getSelectedResource() {
			IStructuredSelection selection = this.selection;
	    	for (Iterator<?> it = selection.iterator(); it.hasNext(); ) {
	    		Object adapter = Platform.getAdapterManager().getAdapter(it.next(), IResource.class);
	    		if (adapter != null) {
	    			return (IResource)adapter;
	    		}
	    	}
	    	return null;
		}		
	}			
	
	@Override
	public void fillContextMenu(IMenuManager menuManager) {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		this.showRevisionGraph.selectionChanged(this.showRevisionGraph, selection);		
		menuManager.insertAfter(UpdateModelActionGroup.GROUP_MANAGE_LOCALS, this.showRevisionGraph);
	}

}
