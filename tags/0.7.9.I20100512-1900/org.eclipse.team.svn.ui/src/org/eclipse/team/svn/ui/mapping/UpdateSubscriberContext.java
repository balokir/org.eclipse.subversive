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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.mapping.ISynchronizationScopeManager;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.subscribers.SubscriberMergeContext;
import org.eclipse.team.core.subscribers.SubscriberScopeManager;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.operation.local.ClearLocalStatusesOperation;
import org.eclipse.team.svn.core.operation.local.MarkAsMergedOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RemoveNonVersionedResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.RevertOperation;
import org.eclipse.team.svn.core.operation.local.SaveProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.UpdateOperation;
import org.eclipse.team.svn.core.resource.ILocalFile;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.svnstorage.ResourcesParentsProvider;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.core.synchronize.UpdateSubscriber;
import org.eclipse.team.svn.core.synchronize.UpdateSyncInfo;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.action.local.UpdateAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.operation.ClearUpdateStatusesOperation;
import org.eclipse.team.svn.ui.operation.NotifyUnresolvedConflictOperation;
import org.eclipse.team.svn.ui.panel.local.OverrideResourcesPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.synchronize.action.ISyncStateFilter;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.utility.UnacceptableOperationNotificator;

public class UpdateSubscriberContext extends SubscriberMergeContext {

	protected int type;
	
	public UpdateSubscriberContext(Subscriber subscriber, ISynchronizationScopeManager manager, int type) {
		super(subscriber, manager);	
		this.type = type;
	}
	
	public IStatus merge(IDiff[] deltas, boolean force, IProgressMonitor monitor) throws CoreException {
		if (force) {
			final IResource [][]resources = new IResource[1][];
			ArrayList<IResource> obstructedList = new ArrayList<IResource>();
			ArrayList<IResource> overrideList = new ArrayList<IResource>();
			for (IDiff diff : deltas) {
				try {
					IResource current = this.getDiffTree().getResource(diff);
					AbstractSVNSyncInfo info = (AbstractSVNSyncInfo)UpdateSubscriber.instance().getSyncInfo(current);
					ILocalResource local = info.getLocalResource();
			        ILocalResource remote = info.getRemoteChangeResource();
			        if (remote instanceof IResourceChange && ISyncStateFilter.SF_OVERRIDE.acceptRemote(remote.getResource(), remote.getStatus(), remote.getChangeMask()) || ISyncStateFilter.SF_OVERRIDE.accept(local)) {
			            overrideList.add(current);
			        }
					if (IStateFilter.SF_OBSTRUCTED.accept(local)) {
						obstructedList.add(current);
					}
				}
				catch (Exception ex) {
					LoggedOperation.reportError(this.getClass().getName(), ex);
				}
			}
			IResource[] obstructedResources = FileUtility.addOperableParents(obstructedList.toArray(new IResource[obstructedList.size()]), IStateFilter.SF_OBSTRUCTED);
			HashSet<IResource> allResources = new HashSet<IResource>(Arrays.asList(obstructedResources));
			final IResource [][] changedResources = new IResource [1][];
			changedResources[0] = overrideList.toArray(new IResource[overrideList.size()]);
			UIMonitorUtility.getDisplay().syncExec(new Runnable() {
				public void run() {
					changedResources[0] = UnacceptableOperationNotificator.shrinkResourcesWithNotOnRespositoryParents(UIMonitorUtility.getShell(), changedResources[0]); 					
				}
			});
			
			ArrayList<IResource> affected = new ArrayList<IResource>();
	 		if (changedResources[0] != null) {
				IResource [] changedWithOperableParents = FileUtility.addOperableParents(changedResources[0], IStateFilter.SF_NOTONREPOSITORY);
				ArrayList<IResource> changedList = new ArrayList<IResource>(Arrays.asList(changedResources[0]));
				for (IResource current : changedWithOperableParents) {
					if (!changedList.contains(current)) {
						changedList.add(current);
						IResource [] currentAffectedArray = FileUtility.getResourcesRecursive(new IResource [] {current}, IStateFilter.SF_ANY_CHANGE);
						for (IResource currentAffected : currentAffectedArray) {
							if (!changedList.contains(currentAffected)) {
								affected.add(currentAffected);
							}
						}
					}
				}
				changedResources [0] = changedWithOperableParents;
	 			allResources.addAll(Arrays.asList(changedResources[0]));
	 		}
			if (allResources.size() > 0) {
				IResource []fullSet = allResources.toArray(new IResource[allResources.size()]);
				final OverrideResourcesPanel panel = new OverrideResourcesPanel(fullSet, fullSet, OverrideResourcesPanel.MSG_UPDATE, affected.toArray(new IResource [affected.size()]));
				final int [] dialogRetVal = new int [1];
				UIMonitorUtility.getDisplay().syncExec(new Runnable() {
					public void run() {
						DefaultDialog dialog = new DefaultDialog(UIMonitorUtility.getShell(), panel);
						dialogRetVal[0] = dialog.open();
					}
				});
				if (dialogRetVal[0] != 0) {
					return Status.OK_STATUS;
				}
				resources[0] = panel.getSelectedResources();
			} else {
				return Status.OK_STATUS;
			}						
			
			CompositeOperation op = new CompositeOperation("Operation_UOverrideAndUpdate", SVNUIMessages.class); //$NON-NLS-1$
			SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(resources[0]);
			op.add(saveOp);

			/*
			 * We should call RemoveNonVersionedResourcesOperation before revert operation, because we don't want 
			 * to delete ignored resources (revert operation makes 'ignored' resource as 'new' in case if ignore properties were not committed)
			 * 
			 * Probably there are case where we need to call RemoveNonVersionedResourcesOperation once again after revert operation,
			 * but I didn't find them
			 */
			RemoveNonVersionedResourcesOperation removeNonVersionedResourcesOp = new RemoveNonVersionedResourcesOperation(resources[0], true);
			op.add(removeNonVersionedResourcesOp);			
			RevertOperation revertOp = new RevertOperation(FileUtility.getResourcesRecursive(resources[0], IStateFilter.SF_REVERTABLE, IResource.DEPTH_ZERO), true);
			op.add(revertOp);
			op.add(new ClearLocalStatusesOperation(resources[0]));
			// Obstructed resources are deleted now. So, try to revert all corresponding entries
			RevertOperation revertOp1 = new RevertOperation(FileUtility.getResourcesRecursive(resources[0], IStateFilter.SF_OBSTRUCTED, IResource.DEPTH_ZERO), true);
			op.add(revertOp1);
			op.add(new ClearLocalStatusesOperation(resources[0]));
			
			//TODO split by peg revision
			Map<SVNRevision, Set<IResource>> splitted = new HashMap<SVNRevision, Set<IResource>>();
			splitted.put(SVNRevision.HEAD, new HashSet<IResource>(Arrays.asList(resources[0])));
			for (Map.Entry<SVNRevision, Set<IResource>> entry : splitted.entrySet()) {
				final IResource []toUpdate = entry.getValue().toArray(new IResource[0]);
				boolean ignoreExternals = SVNTeamPreferences.getBehaviourBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BEHAVIOUR_IGNORE_EXTERNALS_NAME);
				UpdateOperation mainOp = new UpdateOperation(new IResourceProvider() {
					public IResource[] getResources() {
						return 
							FileUtility.getResourcesRecursive(toUpdate, new IStateFilter.AbstractStateFilter() {
								protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
									return IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask) || IStateFilter.SF_NOTEXISTS.accept(resource, state, mask);
								}
								protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
									return true;
								}
							}, IResource.DEPTH_ZERO);
					}
				}, entry.getKey(), ignoreExternals);
				op.add(mainOp, new IActionOperation[] {revertOp, revertOp1, removeNonVersionedResourcesOp});
				op.add(new ClearUpdateStatusesOperation(mainOp), new IActionOperation[]{mainOp});
			}
			op.add(new RestoreProjectMetaOperation(saveOp));
			op.add(new RefreshResourcesOperation(resources[0]/*, IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL*/));
			ProgressMonitorUtility.doTaskExternal(op, monitor);
		}
		else {
			final ArrayList<IResource> resourcesList = new ArrayList<IResource>();
			for (IDiff diff : deltas) {
				try {
					IResource current = this.getDiffTree().getResource(diff);
					AbstractSVNSyncInfo info = (AbstractSVNSyncInfo)UpdateSubscriber.instance().getSyncInfo(current);
					if (SyncInfo.getDirection(info.getKind()) == SyncInfo.INCOMING || SyncInfo.getDirection(info.getKind()) == SyncInfo.CONFLICTING && !IStateFilter.SF_OBSTRUCTED.accept(((UpdateSyncInfo)info).getLocalResource())) {
						resourcesList.add(current);
					}
				}
				catch (Exception ex) {
					LoggedOperation.reportError(this.getClass().getName(), ex);
				}
			}
			if (resourcesList.isEmpty()) {
				return Status.OK_STATUS;
			}
			final IResource [][] resources = new IResource[1][];
			resources[0] = resourcesList.toArray(new IResource[resourcesList.size()]);
			UIMonitorUtility.getDisplay().syncExec(new Runnable() {
				public void run() {
					resources[0] = UnacceptableOperationNotificator.shrinkResourcesWithNotOnRespositoryParents(UIMonitorUtility.getShell(), resources[0]); 
				}
				
			}); 
			if (resources[0] == null || resources[0].length == 0) {
				return Status.OK_STATUS;
			}			
			resources[0] = FileUtility.addOperableParents(resources[0], IStateFilter.SF_UNVERSIONED);
			final IResource []missing = FileUtility.getResourcesRecursive(resources[0], UpdateAction.SF_MISSING_RESOURCES);
			final boolean [] missingRetVal = new boolean[1];
			if (missing.length > 0) {
				UIMonitorUtility.getDisplay().syncExec(new Runnable() {
					public void run() {
						missingRetVal[0] = org.eclipse.team.svn.ui.action.local.UpdateAction.updateMissing(UIMonitorUtility.getShell().getShell(), missing);
					}
				});
				if (!missingRetVal[0]) {
					return Status.OK_STATUS;
				}
			}
			
			CompositeOperation op = new CompositeOperation("Operation_Update", SVNMessages.class); //$NON-NLS-1$
			SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(resources[0]);
			op.add(saveOp);
			
			//TODO split by peg revision
			Map<SVNRevision, Set<IResource>> splitted = new HashMap<SVNRevision, Set<IResource>>();
			splitted.put(SVNRevision.HEAD, new HashSet<IResource>(Arrays.asList(resources[0])));
			for (Map.Entry<SVNRevision, Set<IResource>> entry : splitted.entrySet()) {
				boolean ignoreExternals = SVNTeamPreferences.getBehaviourBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BEHAVIOUR_IGNORE_EXTERNALS_NAME);
				UpdateOperation mainOp = new UpdateOperation(entry.getValue().toArray(new IResource[0]), entry.getKey(), ignoreExternals);
				op.add(mainOp);
				op.add(new ClearUpdateStatusesOperation(mainOp), new IActionOperation[]{mainOp});
				op.add(new NotifyUnresolvedConflictOperation(mainOp));
			}
			op.add(new RestoreProjectMetaOperation(saveOp));
			op.add(new RefreshResourcesOperation(new ResourcesParentsProvider(resources[0])/*, IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL*/));
			ProgressMonitorUtility.doTaskExternal(op, monitor);
		}
		return Status.OK_STATUS;
	}
	
	public void markAsMerged(IDiff [] nodes, boolean inSyncHint, IProgressMonitor monitor) throws CoreException {
		ArrayList<IResource> resourceList = new ArrayList<IResource>(); 
		for (IDiff node : nodes) {
			IResource current = this.getDiffTree().getResource(node);
			try {
				AbstractSVNSyncInfo info = (AbstractSVNSyncInfo)UpdateSubscriber.instance().getSyncInfo(current);
				boolean localIsFile = info.getLocalResource().getResource() instanceof IFile;
                boolean remoteIsFile = info.getRemoteChangeResource() instanceof ILocalFile;
                if (!IStateFilter.SF_OBSTRUCTED.accept(info.getLocalResource()) && localIsFile && remoteIsFile) {
                	resourceList.add(current);
                }
			}
			catch (Exception ex) {
				LoggedOperation.reportError(this.getClass().getName(), ex);
			}
		}
		IResource []resources =  resourceList.toArray(new IResource[resourceList.size()]);
		if (resources == null || resources.length == 0) {
			return;
		}
		boolean ignoreExternals = SVNTeamPreferences.getBehaviourBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BEHAVIOUR_IGNORE_EXTERNALS_NAME);
		MarkAsMergedOperation mainOp = new MarkAsMergedOperation(resources, false, null, ignoreExternals);
		CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
		op.add(mainOp);
		op.add(new ClearUpdateStatusesOperation(resources), new IActionOperation[]{mainOp});
		op.add(new RefreshResourcesOperation(FileUtility.getParents(resources, false)));
		ProgressMonitorUtility.doTaskExternal(op, monitor);
	}
	
	public ISchedulingRule getMergeRule(IDiff diff) {
		return null;
	}
	
	public ISchedulingRule getMergeRule(IDiff[] deltas) {
		return null;
	}
	
	/* stubs */
	
	protected void makeInSync(IDiff diff, IProgressMonitor monitor) throws CoreException {
		//stub		
	}
	
	public void markAsMerged(IDiff node, boolean inSyncHint, IProgressMonitor monitor) throws CoreException {
		// stub
	}
	
	public void reject(IDiff diff, IProgressMonitor monitor) throws CoreException {
		//stub		
	}
	
	public static SubscriberScopeManager createWorkspaceScopeManager(ResourceMapping[] mappings, boolean consultModels, boolean consultChangeSets) {
		return new ChangeSetSubscriberScopeManager(UpdateSubscriber.instance().getName(), mappings, UpdateSubscriber.instance(), consultModels, consultChangeSets);
	}
	
	public static SubscriberScopeManager createUpdateScopeManager(ResourceMapping[] mappings, boolean consultModels) {		
		SubscriberScopeManager manager = new SubscriberScopeManager(UpdateSubscriber.instance().getName(), 
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
			if (this.isConsultSets()) {
				return SVNTeamUIPlugin.instance().getModelCangeSetManager().adjustInputTraversals(traversals);
			}
			return super.adjustInputTraversals(traversals);
		}

		public boolean isConsultSets() {
			return this.consultSets;
		}
	}

}
