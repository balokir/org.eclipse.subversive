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

import java.util.Collection;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphMessages;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphPlugin;
import org.eclipse.team.svn.revision.graph.operation.RevisionGraphUtility;
import org.eclipse.team.svn.ui.extension.impl.DefaultSynchronizeViewActionContributor;
import org.eclipse.team.svn.ui.extension.impl.synchronize.UpdateActionGroup;
import org.eclipse.team.svn.ui.synchronize.AbstractSynchronizeActionGroup;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeModelAction;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Contribute revision graph action to not-model-aware synchronize view 
 * 
 * @author Igor Burilo
 */
public class GraphSynchronizeViewActionContributor extends DefaultSynchronizeViewActionContributor {

	protected static class ShowRevisionGraphAction extends AbstractSynchronizeModelAction {

		public ShowRevisionGraphAction(String text, ISynchronizePageConfiguration configuration) {
			super(text, configuration);
			this.setImageDescriptor(SVNRevisionGraphPlugin.instance().getImageDescriptor("icons/showgraph.png")); //$NON-NLS-1$
		}
		
		@Override
		protected boolean updateSelection(IStructuredSelection selection) {
			if (super.updateSelection(selection)) {
				IResource[] resources = this.getAllSelectedResources();
				if (FileUtility.checkForResourcesPresence(resources, IStateFilter.SF_ONREPOSITORY, IResource.DEPTH_ZERO)) {
					return true;
				}
			}
			return false;
		}
		
		protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
			IResource[] resources = this.getAllSelectedResources();
			resources = FileUtility.getResourcesRecursive(resources, IStateFilter.SF_ONREPOSITORY, IResource.DEPTH_ZERO);
			IRepositoryResource[] reposResources = new IRepositoryResource[resources.length];
			for (int i = 0; i < resources.length; i ++) {
				reposResources[i] = SVNRemoteStorage.instance().asRepositoryResource(resources[i]);
			}		
			return RevisionGraphUtility.getRevisionGraphOperation(reposResources);			
		}
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Collection getUpdateContributions() {
		Collection contributions = super.getUpdateContributions();
		contributions.add(new AbstractSynchronizeActionGroup() {						
			protected void configureActions(ISynchronizePageConfiguration configuration) {
				ShowRevisionGraphAction showGraphAction = new ShowRevisionGraphAction(SVNRevisionGraphMessages.ShowRevisionGraphAction, configuration);
				this.appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					UpdateActionGroup.GROUP_MANAGE_LOCALS,
					showGraphAction);				
			}
			public void configureMenuGroups(ISynchronizePageConfiguration configuration) {
			}						
		});
		return contributions;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection getMergeContributions() {
		Collection contributions = super.getMergeContributions();
		contributions.add(new AbstractSynchronizeActionGroup() {						
			protected void configureActions(ISynchronizePageConfiguration configuration) {
				ShowRevisionGraphAction showGraphAction = new ShowRevisionGraphAction(SVNRevisionGraphMessages.ShowRevisionGraphAction, configuration);
				this.appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					UpdateActionGroup.GROUP_MANAGE_LOCALS,
					showGraphAction);				
			}
			public void configureMenuGroups(ISynchronizePageConfiguration configuration) {
			}						
		});
		return contributions;
	}
}
