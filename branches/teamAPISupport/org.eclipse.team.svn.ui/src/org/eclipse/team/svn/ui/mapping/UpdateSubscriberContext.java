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

package org.eclipse.team.svn.ui.mapping;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.mapping.ISynchronizationScopeManager;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.subscribers.SubscriberMergeContext;
import org.eclipse.team.core.subscribers.SubscriberScopeManager;
import org.eclipse.team.svn.core.synchronize.UpdateSubscriber;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * @author Igor Burilo
 *
 */
public class UpdateSubscriberContext extends SubscriberMergeContext {

	protected int type;
	
	public UpdateSubscriberContext(Subscriber subscriber, ISynchronizationScopeManager manager, int type) {
		super(subscriber, manager);	
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.provider.MergeContext#makeInSync(org.eclipse.team.core.diff.IDiff, org.eclipse.core.runtime.IProgressMonitor)
	 */	
	protected void makeInSync(IDiff diff, IProgressMonitor monitor) throws CoreException {
		System.out.println("makeInSync");		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IMergeContext#markAsMerged(org.eclipse.team.core.diff.IDiff, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void markAsMerged(IDiff node, boolean inSyncHint, IProgressMonitor monitor) throws CoreException {
		System.out.println("markAsMerged");		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IMergeContext#reject(org.eclipse.team.core.diff.IDiff, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void reject(IDiff diff, IProgressMonitor monitor) throws CoreException {
		System.out.println("reject");		
	}
	
	public static SubscriberScopeManager createWorkspaceScopeManager(ResourceMapping[] mappings, boolean consultModels, final boolean consultChangeSets) {
		return new ChangeSetSubscriberScopeManager(UpdateSubscriber.instance().getName(), mappings, UpdateSubscriber.instance(), consultModels, consultChangeSets);
	}
	
	/*
	 * TODO
	 * It's called from CVS's ModelUpdateOperation, but we don't have such operation
	 */
	public static SubscriberScopeManager createUpdateScopeManager(ResourceMapping[] mappings, boolean consultModels) {
		/*
		 * name
		 * 
		 * TODO see CVS how to correctly retrieve name
		 */
		String name = "workspaceSubscriber";
		
		SubscriberScopeManager manager = new SubscriberScopeManager(name, 
			mappings, UpdateSubscriber.instance(), consultModels);
		return manager;
	}
	
	public static UpdateSubscriberContext createContext(ISynchronizationScopeManager manager, int type) {
		UpdateSubscriber subscriber = UpdateSubscriber.instance();
		UpdateSubscriberContext mergeContext = new UpdateSubscriberContext(subscriber, manager, type);
		mergeContext.initialize();
		return mergeContext;
	}
	
	public static final class ChangeSetSubscriberScopeManager extends SubscriberScopeManager {
		private final boolean consultSets;

		private ChangeSetSubscriberScopeManager(String name, ResourceMapping[] mappings, Subscriber subscriber, boolean consultModels, boolean consultSets) {
			super(name, mappings, subscriber, consultModels);
			this.consultSets = consultSets;
		}

		protected ResourceTraversal[] adjustInputTraversals(ResourceTraversal[] traversals) {
			if (isConsultSets())
				return SVNTeamUIPlugin.instance().getModelCangeSetManager().adjustInputTraversals(traversals);
			return super.adjustInputTraversals(traversals);
		}

		public boolean isConsultSets() {
			return this.consultSets;
		}
	}

}
