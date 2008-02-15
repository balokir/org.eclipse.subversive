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

package org.eclipse.team.svn.ui.synchronize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.subscribers.SubscriberChangeEvent;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariantComparator;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNEntryStatus;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.operation.local.IRemoteStatusOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.resource.events.IResourceStatesListener;
import org.eclipse.team.svn.core.resource.events.ResourceStatesChangedEvent;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ILoggedOperationFactory;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Abstract subscriber class. Can be implemented as synchronize or merge subscriber.
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractSVNSubscriber extends Subscriber implements IResourceStatesListener {
	protected static final IResourceVariantComparator RV_COMPARATOR = new ResourceVariantComparator();
	
    protected RemoteStatusCache statusCache;
    protected Set<IResource> oldResources;
    
    public AbstractSVNSubscriber() {
        super();
		this.statusCache = new RemoteStatusCache();
		SVNRemoteStorage.instance().addResourceStatesListener(ResourceStatesChangedEvent.class, this);
		this.oldResources = new HashSet();
    }

    public boolean isSynchronizedWithRepository() {
    	return this.statusCache.containsData();
    }
    
    public String getName() {
        return this.getClass().getName();
    }

    public boolean isSupervised(IResource resource) {
		return FileUtility.isConnected(resource) && !FileUtility.isSVNInternals(resource) && !FileUtility.isLinked(resource);
    }

    public IResource []members(IResource resource) {
    	ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
    	if (local == null || IStateFilter.SF_IGNORED.accept(local) || IStateFilter.SF_NOTEXISTS.accept(local)) {
    		return FileUtility.NO_CHILDREN;
    	}
    	return this.statusCache.allMembers(resource);
    }

    public IResource []roots() {
        ArrayList roots = new ArrayList();
		IProject []projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			if (FileUtility.isConnected(projects[i])) {
				roots.add(projects[i]);
			}
		}
		return (IResource [])roots.toArray(new IResource[roots.size()]);
    }

    public SyncInfo getSyncInfo(IResource resource) throws TeamException {
		if (!this.isSupervised(resource)) {
			return null;
		}
		IResourceChange remoteStatus = SVNRemoteStorage.instance().resourceChangeFromBytes(this.statusCache.getBytes(resource));
    	// incoming additions shouldn't call WC access
		ILocalResource localStatus = SVNRemoteStorage.instance().asLocalResourceDirty(resource);
		if (localStatus != null || remoteStatus != null) {
			SyncInfo info = this.getSVNSyncInfo(localStatus, remoteStatus);
			if (info != null) {
				info.init();
				int kind = info.getKind();
				if (SyncInfo.getChange(kind) == SyncInfo.DELETION && (SyncInfo.getDirection(kind) & SyncInfo.OUTGOING) != 0 && !resource.exists()) {
					synchronized (this.oldResources) {
						this.oldResources.add(resource);
					}
				}
			}
			return info;
		}
		return null;
    }

    public IResourceVariantComparator getResourceComparator() {
		return AbstractSVNSubscriber.RV_COMPARATOR;
    }

    public void refresh(final IResource []resources, final int depth, IProgressMonitor monitor) {
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		boolean contiguousReportMode = SVNTeamPreferences.getSynchronizeBoolean(store, SVNTeamPreferences.SYNCHRONIZE_SHOW_REPORT_CONTIGUOUS_NAME);

		HashSet refreshScope = this.clearRemoteStatusesImpl(resources);
		AbstractSVNSubscriber.this.resourcesStateChangedImpl((IResource [])refreshScope.toArray(new IResource[refreshScope.size()]));
		
		if (contiguousReportMode) {
			IActionOperation op = new UpdateStatusOperation(resources, depth);
	        UIMonitorUtility.doTaskExternalDefault(op, monitor);
		}
		else {
			this.resourcesStateChangedImpl(this.findChanges(resources, depth, monitor, UIMonitorUtility.DEFAULT_FACTORY));
		}
    }

	public void clearRemoteStatuses(IResource []resources) {
	    HashSet refreshScope = this.clearRemoteStatusesImpl(resources);
		this.resourcesStateChangedImpl((IResource [])refreshScope.toArray(new IResource[refreshScope.size()]));
	}
	
    public void resourcesStateChanged(ResourceStatesChangedEvent event) {
    	if (event.type == ResourceStatesChangedEvent.CHANGED_NODES) {
    		this.resourcesStateChangedImpl(event.getResourcesRecursivelly());
    	}
    }
    
	protected HashSet clearRemoteStatusesImpl(IResource []resources) {
		return this.clearRemoteStatusesImpl(this.statusCache, resources);
	}
	
	protected HashSet clearRemoteStatusesImpl(RemoteStatusCache cache, IResource []resources) {
		final HashSet refreshSet = new HashSet();
		cache.traverse(resources, IResource.DEPTH_INFINITE, new RemoteStatusCache.ICacheVisitor() {
			public void visit(IPath current, byte []data) {
				IResource resource = SVNRemoteStorage.instance().resourceChangeFromBytes(data).getResource();
				if (resource != null) {
					refreshSet.add(resource);
				}
			}
		});
		for (int i = 0; i < resources.length; i++) {
			cache.flushBytes(resources[i], IResource.DEPTH_INFINITE);
		}
		return refreshSet;
	}
	
    protected void resourcesStateChangedImpl(IResource []resources) {
    	Set allResources = new HashSet(Arrays.asList(resources));
    	for (int i = 0; i < resources.length; i++) {
    		allResources.addAll(Arrays.asList(this.statusCache.allMembers(resources[i])));
    	}
    	synchronized (this.oldResources) {
        	IResource []refreshSet = (IResource [])allResources.toArray(new IResource[allResources.size()]);
        	// ensure we cached all locally-known resources
        	if (CoreExtensionsManager.instance().getOptionProvider().isSVNCacheEnabled()) {
        		IResource []parents = FileUtility.getParents(refreshSet, false);
            	for (int i = 0; i < parents.length; i++) {
            		try {
    					SVNRemoteStorage.instance().getRegisteredChildren((IContainer)parents[i]);
    				}
    				catch (Exception ex) {
    					LoggedOperation.reportError(SVNTeamUIPlugin.instance().getResource("Error.CheckCache"), ex);
    				}
            	}
        	}
    		for (Iterator it = this.oldResources.iterator(); it.hasNext(); ) {
				IResource resource = (IResource)it.next();
				ILocalResource local = SVNRemoteStorage.instance().asLocalResourceDirty(resource);
				if (local == null || IStateFilter.SF_NOTEXISTS.accept(local)) {
					it.remove();
					allResources.add(resource);
				}
			}
        	this.fireTeamResourceChange(SubscriberChangeEvent.asSyncChangedDeltas(this, refreshSet));
    	}
  	}
    
	protected IResource []findChanges(IResource []resources, int depth, IProgressMonitor monitor, ILoggedOperationFactory operationWrapperFactory) {
		final IRemoteStatusOperation rStatusOp = this.getStatusOperation(resources, depth);
		if (rStatusOp == null) {
    		return FileUtility.NO_CHILDREN;
		}
		final ArrayList changes = new ArrayList();
		
		CompositeOperation op = new CompositeOperation(rStatusOp.getId());
		op.add(rStatusOp);
		op.add(new AbstractActionOperation("Operation.FetchChanges") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				SVNEntryStatus []statuses = rStatusOp.getStatuses();
				if (statuses != null) {
					for (int i = 0; i < statuses.length && !monitor.isCanceled(); i++) {
						if (AbstractSVNSubscriber.this.isIncomig(statuses[i])) {
							IResourceChange resourceChange = AbstractSVNSubscriber.this.handleResourceChange(rStatusOp, statuses[i]);
							if (resourceChange != null) {
								ProgressMonitorUtility.setTaskInfo(monitor, this, resourceChange.getResource().getFullPath().toString());
								AbstractSVNSubscriber.this.statusCache.setBytes(resourceChange.getResource(), SVNRemoteStorage.instance().resourceChangeAsBytes(resourceChange));
								changes.add(resourceChange.getResource());
							}
						}
						ProgressMonitorUtility.progress(monitor, i, statuses.length);
					}
				}
			}
		}, new IActionOperation[] {rStatusOp});
		ProgressMonitorUtility.doTaskExternal(op, monitor, operationWrapperFactory);
		
		return (IResource [])changes.toArray(new IResource[changes.size()]);
	}
	
	protected abstract boolean isIncomig(SVNEntryStatus status);
	protected abstract IResourceChange handleResourceChange(IRemoteStatusOperation rStatusOp, SVNEntryStatus status);
    protected abstract SyncInfo getSVNSyncInfo(ILocalResource localStatus, IResourceChange remoteStatus);
    protected abstract IRemoteStatusOperation getStatusOperation(IResource []resources, int depth);

    public class UpdateStatusOperation extends AbstractActionOperation implements ILoggedOperationFactory {
    	protected IResource []resources;
    	protected int depth;
    	
    	public UpdateStatusOperation(IResource []resources, int depth) {
    		super("Operation.UpdateStatus");
    		this.resources = resources;
    		this.depth = depth;
    	}
    	
		public IActionOperation getLogged(IActionOperation operation) {
			return new LoggedOperation(operation) {
				protected void handleError(IStatus errorStatus) {
					UpdateStatusOperation.this.reportStatus(errorStatus);
				}
			};
		}
		
    	protected void runImpl(IProgressMonitor monitor) throws Exception {
    		Map project2Resources = SVNUtility.splitWorkingCopies(this.resources);
            for (Iterator it = project2Resources.values().iterator(); it.hasNext() && !monitor.isCanceled(); ) {
            	List entry = (List)it.next();
    			final IResource []wcResources = (IResource [])entry.toArray(new IResource[entry.size()]);
    			this.protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws Exception {
						AbstractSVNSubscriber.this.resourcesStateChangedImpl(AbstractSVNSubscriber.this.findChanges(wcResources, depth, monitor, UpdateStatusOperation.this)); 
					}
				}, monitor, project2Resources.size());
            }                
        }
      	
    };
}
