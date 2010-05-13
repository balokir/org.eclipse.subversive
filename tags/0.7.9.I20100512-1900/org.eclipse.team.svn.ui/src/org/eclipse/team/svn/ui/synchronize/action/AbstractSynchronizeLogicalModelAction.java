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

package org.eclipse.team.svn.ui.synchronize.action;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.mapping.IResourceDiffTree;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.mapping.ResourceModelParticipantAction;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.core.synchronize.UpdateSubscriber;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.ui.action.IResourceSelector;
import org.eclipse.team.svn.ui.operation.UILoggedOperation;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizeModelAction;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

@SuppressWarnings("restriction")
public abstract class AbstractSynchronizeLogicalModelAction extends ResourceModelParticipantAction {
	
	/**
	 * Provides set of resources filtered by FastSyncInfoFilter(s)
	 */
	protected IResourceSelector syncInfoSelector;
	
    public AbstractSynchronizeLogicalModelAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
		this.setEnabled(false);
		this.setToolTipText(text);		
		this.createSyncInfoSelector();
		//this.createTreeNodeSelector();
	}
    
    protected boolean isEnabledForSelection(IStructuredSelection selection) {
    	return this.getFilteredResources().length > 0;
    }
    
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
				HashSet<IResource> retVal = new HashSet<IResource>();
				try {
					IResource [] filtered = AbstractSynchronizeLogicalModelAction.this.getFilteredResources();
				    for (int i = 0; i < filtered.length; i++) {
				    	AbstractSVNSyncInfo info = (AbstractSVNSyncInfo)UpdateSubscriber.instance().getSyncInfo(filtered[i]);
				        ILocalResource local = info.getLocalResource();
				        ILocalResource remote = info.getRemoteChangeResource();
				        if (remote instanceof IResourceChange && filter.acceptRemote(remote.getResource(), remote.getStatus(), remote.getChangeMask()) || filter.accept(local)) {
				            retVal.add(local.getResource());
				        }
				    }
				    IResource [] filteredResources = retVal.toArray(new IResource[retVal.size()]);
				    /*
				     * filteredResources variable contains now only changed resources. 
				     * But for some operations we need also to include all tree.
				     * Example:
				     * 	Project-1/
				     * 		src/
				     * 			com.polarion/
				     * 				> ClassA.java
				     * 
				     * Here we have changed only ClassA.java and we have it as a resulted selected resource.
				     * But some operations require all changes tree, i.e. we need to 
				     * include also Project-1, src, com.polarion in the result.
				     * 
				     * In order to indicate that operation needs all tree ISyncStateFilter.acceptGroupNodes method
				     * should return true.
				     * 
				     * In order to return all tree it, we process here parents of changed resources
				     */
				    if (filter.acceptGroupNodes()) {
				    	ArrayList<IResource> allSelected = new ArrayList<IResource>(Arrays.asList(AbstractSynchronizeLogicalModelAction.this.getAllSelectedResources()));
				    	for (IResource filteredResource : filteredResources) {
				    		ArrayList<IResource> parents = new ArrayList<IResource>();
				    		IResource parent = filteredResource.getParent();
				    		while (parent != null) {
				    			//As there can be unversioned externals in Sync View, don't process them
				    			AbstractSVNSyncInfo info = (AbstractSVNSyncInfo)UpdateSubscriber.instance().getSyncInfo(parent);
				    			if (info != null) {
							        ILocalResource local = info.getLocalResource();
						    		if (!IStateFilter.SF_UNVERSIONED_EXTERNAL.accept(local)) {
						    			parents.add(parent);
						    		}
				    			}
				    			if (allSelected.contains(parent)) {
				    				retVal.addAll(parents);
				    				break;
				    			}
				    			parent = parent.getParent();
				    		}
				    	}
				    }
				}
			    catch (Exception ex) {
			    	LoggedOperation.reportError(this.getClass().getName(), ex);
			    }
				return retVal.toArray(new IResource[retVal.size()]);
			}
		};
	}
    
    //TODO seems like it is not needed for model content
    /*protected void createTreeNodeSelector() {
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
			
			private IResource[] getSelectedResourcesRecursive(final ISyncStateFilter filter, int depth) {
			    final HashSet<IResource> retVal = new HashSet<IResource>();
			    final IResourceDiffTree diffTree = AbstractSynchronizeLogicalModelAction.this.getSynchronizationContext().getDiffTree();
			    
			    IResource [] allSelected = AbstractSynchronizeLogicalModelAction.this.getAllSelectedResources();
			    for (IResource currentFromSelection : allSelected) {
			    	diffTree.accept(currentFromSelection.getFullPath(), new IDiffVisitor() {
						public boolean visit(IDiff diff) {
							try {
								IResource current = diffTree.getResource(diff);
								if (filter.accept(SVNRemoteStorage.instance().asLocalResource(current))) {
									retVal.add(current);
								}
								else {
									AbstractSVNSyncInfo info = (AbstractSVNSyncInfo)UpdateSubscriber.instance().getSyncInfo(current);
									ILocalResource change = ((ResourceVariant)info.getRemote()).getResource();
									if (change instanceof IResourceChange && filter.acceptRemote(change.getResource(), change.getStatus(), change.getChangeMask())) {
										retVal.add(current);
									}
								}
							}
							catch (Exception ex) {
								LoggedOperation.reportError(this.getClass().getName(), ex);
							}
							return true;
						}
						
					}, depth);
			    }
				return retVal.toArray(new IResource[retVal.size()]);
			}
		};
	}*/
    
    /*
     * Can be overridden in subclasses
     */
    protected FastSyncInfoFilter getSyncInfoFilter() {
    	return new FastSyncInfoFilter();
    }
    
    public AbstractSVNSyncInfo[] getSVNSyncInfos() {
    	List<AbstractSVNSyncInfo> filtered = new ArrayList<AbstractSVNSyncInfo>();
    	try {
    		for (IResource resource : this.getFilteredResources()) {
    			AbstractSVNSyncInfo syncInfo = (AbstractSVNSyncInfo) UpdateSubscriber.instance().getSyncInfo(resource);
    			filtered.add(syncInfo);
    		}    		
    	} catch (TeamException te) {
    		LoggedOperation.reportError(this.getClass().getName(), te);
    	}

		return filtered.toArray(new AbstractSVNSyncInfo[filtered.size()]);
    }
    
    protected IResource [] getFilteredResources() {
		final HashSet<IResource> filtered = new HashSet<IResource>();
		try {
			final IResourceDiffTree diffTree = AbstractSynchronizeLogicalModelAction.this.getSynchronizationContext().getDiffTree();
			IDiff [] affectedDiffs = diffTree.getDiffs(this.getResourceTraversals(this.getStructuredSelection(), new NullProgressMonitor()));
			for (IDiff currentDiff : affectedDiffs) {
				IResource resource = ResourceDiffTree.getResourceFor(currentDiff);
				if (AbstractSynchronizeLogicalModelAction.this.getSyncInfoFilter().select(UpdateSubscriber.instance().getSyncInfo(resource))) {
					filtered.add(resource);
				}
			}
		}
		catch (Exception ex) {
			LoggedOperation.reportError(this.getClass().getName(), ex);
		}
		return filtered.toArray(new IResource[filtered.size()]);
    }
    
    public IResource [] getAllSelectedResources() {
    	IStructuredSelection selection = this.getStructuredSelection();
    	ArrayList<IResource> retVal = new ArrayList<IResource>();
    	for (Iterator<?> it = selection.iterator(); it.hasNext(); ) {
    		Object adapter = Platform.getAdapterManager().getAdapter(it.next(), IResource.class);
    		if (adapter == null) {
    			continue;
    		}
    		retVal.add((IResource)adapter);
    	}
    	return retVal.toArray(new IResource[retVal.size()]);
    } 
    
    public IResource getSelectedResource() {
		IStructuredSelection selection = this.getStructuredSelection();
    	for (Iterator<?> it = selection.iterator(); it.hasNext(); ) {
    		Object adapter = Platform.getAdapterManager().getAdapter(it.next(), IResource.class);
    		if (adapter != null) {
    			return (IResource)adapter;
    		}
    	}
    	return null;
	}
	
	public AbstractSVNSyncInfo getSelectedSVNSyncInfo() {
		IResource resource = this.getSelectedResource();
		try {
			return resource == null ? null : (AbstractSVNSyncInfo)UpdateSubscriber.instance().getSyncInfo(resource);
		}
		catch (TeamException ex) {
			LoggedOperation.reportError(this.getClass().getName(), ex);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		if(needsToSaveDirtyEditors()) {
			if(!saveAllEditors(confirmSaveOfDirtyEditor())) {
				return;
			}
		}
		runOperation();
	}
	
	/**
	 * Returns whether the user should be prompted to save dirty editors. The
	 * default is <code>true</code>.
	 * 
	 * @return whether the user should be prompted to save dirty editors
	 * 
	 * @see SynchronizeModelAction.confirmSaveOfDirtyEditor
	 */
	protected boolean confirmSaveOfDirtyEditor() {
		return true;
	}
	
	/**
	 * Return whether dirty editor should be saved before this action is run.
	 * Default is <code>true</code>.
	 * 
	 * @return whether dirty editor should be saved before this action is run
	 * 
	 * @see SynchronizeModelAction.needsToSaveDirtyEditors
	 */
	protected boolean needsToSaveDirtyEditors() {
		return true;
	}
	
	/**
	 * Save all dirty editors in the workbench that are open on files that may
	 * be affected by this operation. Opens a dialog to prompt the user if
	 * <code>confirm</code> is true. Return true if successful. Return false
	 * if the user has canceled the command. Must be called from the UI thread.
	 * 
	 * @param confirm prompt the user if true
	 * @return boolean false if the operation was canceled.
	 * 
	 * @see SynchronizeModelAction.saveAllEditors
	 */
	public final boolean saveAllEditors(boolean confirm) {
		IResource[] resources = this.getFilteredResources();
		return IDE.saveAllEditors(Utils.getResources(resources), confirm);
	}
	
	protected void runOperation() {
		final IActionOperation op = AbstractSynchronizeLogicalModelAction.this.getOperation();
		if (op == null) {
			return;
		}
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				try {
					PlatformUI.getWorkbench().getProgressService().run(true, true, new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								ProgressMonitorUtility.doTaskExternal(op, monitor);	
							}
						});
				}
				catch (InvocationTargetException ex) {
					UILoggedOperation.reportError(op.getOperationName(), ex);
				}
				catch (InterruptedException ex) {
					LoggedOperation.reportError(AbstractSynchronizeLogicalModelAction.this.getClass().getName(), ex);
				}
			}
		});
	}	

	public IResourceSelector getSyncInfoSelector() {
		return this.syncInfoSelector;
	}
	
	protected abstract IActionOperation getOperation();
	 
}
