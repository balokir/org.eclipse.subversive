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

package org.eclipse.team.svn.ui.synchronize.action;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.ui.action.IResourceSelector;
import org.eclipse.team.svn.ui.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.ui.synchronize.variant.ResourceVariant;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizeModelAction;
import org.eclipse.team.ui.synchronize.SynchronizeModelOperation;

/**
 * Synchronize view action abstract implementation
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractSynchronizeModelAction extends SynchronizeModelAction {
	/**
	 * Provides set of resources filtered by FastSyncInfoFilter(s)
	 */
	protected IResourceSelector syncInfoSelector;
	/**
	 * Handles tree-based selection without applying of FastSyncInfoFilter(s).
	 * Group nodes are provided by default, in order to disallow group nodes please use code <code>new ISyncStateFilter.StateFilterWrapper(filter, false)</code>.
	 */
	protected IResourceSelector treeNodeSelector;

    public AbstractSynchronizeModelAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
		this.setEnabled(false);
		this.setToolTipText(text);
		
		this.createSyncInfoSelector();
		this.createTreeNodeSelector();
	}

    public AbstractSynchronizeModelAction(String text, ISynchronizePageConfiguration configuration, ISelectionProvider selectionProvider) {
		super(text, configuration, selectionProvider);
		this.setEnabled(false);
		this.setToolTipText(text);
		
		this.createSyncInfoSelector();
		this.createTreeNodeSelector();
	}

	protected final SynchronizeModelOperation getSubscriberOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		IActionOperation op = this.getOperation(configuration, elements);
		return new FilteredSynchronizeModelOperation(configuration, elements, op);
	}

	protected abstract IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements);

	protected void createSyncInfoSelector() {
		this.syncInfoSelector = new IResourceSelector() {
			public IResource []getSelectedResources() {
			    return this.getSelectedResources(new ISyncStateFilter.StateFilterWrapper(IStateFilter.SF_ALL, false));
			}
			
	        public IResource[] getSelectedResources(IStateFilter filter) {
	            if (filter instanceof ISyncStateFilter) {
	    			return this.getSelectedResources((ISyncStateFilter)filter);
	            }
				return this.getSelectedResources(new ISyncStateFilter.StateFilterWrapper(filter, false));
	        }
			
			public IResource []getSelectedResourcesRecursive(IStateFilter filter) {
				return this.getSelectedResources(filter);
			}
			
			public IResource []getSelectedResourcesRecursive(IStateFilter filter, int depth) {
				return this.getSelectedResources(filter);
			}
			
			private IResource []getSelectedResources(ISyncStateFilter filter) {
				AbstractSVNSyncInfo []infos = AbstractSynchronizeModelAction.this.getSVNSyncInfos();
			    HashSet<IResource> retVal = new HashSet<IResource>();
			    for (int i = 0; i < infos.length; i++) {
			        ILocalResource local = infos[i].getLocalResource();
			        ILocalResource remote = ((ResourceVariant)infos[i].getRemote()).getResource();
			        if (remote instanceof IResourceChange && filter.acceptRemote(remote.getResource(), remote.getStatus(), remote.getChangeMask()) || filter.accept(local)) {
			            retVal.add(local.getResource());
			        }
			    }
			    if (filter.acceptGroupNodes()) {
			    	HashSet<ISynchronizeModelElement> selection = new HashSet<ISynchronizeModelElement>(Arrays.asList(AbstractSynchronizeModelAction.this.getSelectedElements()));
			    	for (IDiffElement element : AbstractSynchronizeModelAction.this.getFilteredDiffElements()) {
			    		if (element instanceof ISynchronizeModelElement && retVal.contains(((ISynchronizeModelElement)element).getResource())) {
				    		IDiffContainer parent = element.getParent();
				    		ArrayList<IResource> parents = new ArrayList<IResource>();
				    		while (parent != null && parent instanceof ISynchronizeModelElement && ((ISynchronizeModelElement)parent).getResource() != null) {
				    			parents.add(((ISynchronizeModelElement)parent).getResource());
				    			if (selection.contains(parent)) {
				    				retVal.addAll(parents);
				    				break;
				    			}
					    		parent = parent.getParent();
				    		}
			    		}
			    	}
			    }
				return retVal.toArray(new IResource[retVal.size()]);
			}
		};
	}
	
	protected void createTreeNodeSelector() {
		this.treeNodeSelector = new IResourceSelector() {
			public IResource[] getSelectedResources() {
			    return this.getSelectedResources(new ISyncStateFilter.StateFilterWrapper(IStateFilter.SF_ALL, true));
			}
			
			public IResource[] getSelectedResources(IStateFilter filter) {
				return this.getSelectedResourcesRecursive(filter, IResource.DEPTH_ZERO);
			}
			
			public IResource[] getSelectedResourcesRecursive(IStateFilter filter) {
				return this.getSelectedResourcesRecursive(filter, IResource.DEPTH_INFINITE);
			}
			
			public IResource[] getSelectedResourcesRecursive(IStateFilter filter, int depth) {
	            if (filter instanceof ISyncStateFilter) {
	    			return this.getSelectedResourcesRecursive((ISyncStateFilter)filter, depth);
	            }
				return this.getSelectedResourcesRecursive(new ISyncStateFilter.StateFilterWrapper(filter, true), depth);
			}
			
			private IResource[] getSelectedResourcesRecursive(ISyncStateFilter filter, int depth) {
			    HashSet<IResource> retVal = new HashSet<IResource>();
				for (ISynchronizeModelElement element : AbstractSynchronizeModelAction.this.getSelectedElements()) {
	    			this.fetchSelectedNodes(retVal, element, filter, depth);
				}
				return retVal.toArray(new IResource[retVal.size()]);
			}
			
			private void fetchSelectedNodes(Set<IResource> nodes, ISynchronizeModelElement node, ISyncStateFilter filter, int depth) {
				IResource resource = node.getResource();
				ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
				if (local != null && filter.accept(local)) {
					nodes.add(resource);
				}
				else if (node instanceof SyncInfoModelElement) {
					ILocalResource change = ((ResourceVariant)((AbstractSVNSyncInfo)((SyncInfoModelElement)node).getSyncInfo()).getRemote()).getResource();
					if (change instanceof IResourceChange && filter.acceptRemote(change.getResource(), change.getStatus(), change.getChangeMask())) {
						nodes.add(resource);
					}
				}
				if (depth != IResource.DEPTH_ZERO) {
					int sizeBefore = nodes.size();
		    		for (IDiffElement element : node.getChildren()) {
		    			this.fetchSelectedNodes(nodes, (ISynchronizeModelElement)element, filter, depth == IResource.DEPTH_INFINITE ? IResource.DEPTH_INFINITE : IResource.DEPTH_ZERO);
		    		}
		    		if (sizeBefore != nodes.size() && filter.acceptGroupNodes()) {
		    			nodes.add(resource);
		    		}
				}
			}
		};
	}
	
	protected IResource getSelectedResource() {
		ISynchronizeModelElement []selection = this.getSelectedElements();
		return selection.length == 0 ? null : this.getSelectedElements()[0].getResource();
	}
	
	protected AbstractSVNSyncInfo getSelectedSVNSyncInfo() {
		ISynchronizeModelElement []selection = this.getSelectedElements();
		if (selection.length == 0 || !(selection[0] instanceof SyncInfoModelElement)) {
			return null;
		}
		return (AbstractSVNSyncInfo)((SyncInfoModelElement)selection[0]).getSyncInfo();
	}
	
	protected AbstractSVNSyncInfo[] getSVNSyncInfos() {
		List<AbstractSVNSyncInfo> filtered = new ArrayList<AbstractSVNSyncInfo>();
		for (IDiffElement e : this.getFilteredDiffElements()) {
			filtered.add((AbstractSVNSyncInfo)((SyncInfoModelElement)e).getSyncInfo());
		}
		return filtered.toArray(new AbstractSVNSyncInfo[filtered.size()]);
	}
	
	protected ISynchronizeModelElement []getSelectedElements() {
		ArrayList<ISynchronizeModelElement> retVal = new ArrayList<ISynchronizeModelElement>();
	    IStructuredSelection selection = AbstractSynchronizeModelAction.this.getStructuredSelection();
		for (Iterator it = selection.iterator(); it.hasNext(); ) {
			Object element = it.next();
			if (element instanceof ISynchronizeModelElement) {
				retVal.add((ISynchronizeModelElement)element);
			}
		}
		return retVal.toArray(new ISynchronizeModelElement[retVal.size()]);
	}
	
	protected static class FilteredSynchronizeModelOperation extends SynchronizeModelOperation {
		protected IActionOperation executable;
		
		public FilteredSynchronizeModelOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements, IActionOperation executable) {
			super(configuration, elements);
			this.executable = executable;
		}

		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			if (this.executable != null) {
			    ProgressMonitorUtility.doTaskExternal(this.executable, monitor);
			}
		}
		
		protected boolean canRunAsJob() {
			return true;
		}
		
		protected String getJobName() {
			return this.executable == null ? super.getJobName() : this.executable.getOperationName();
		}

	}

}
