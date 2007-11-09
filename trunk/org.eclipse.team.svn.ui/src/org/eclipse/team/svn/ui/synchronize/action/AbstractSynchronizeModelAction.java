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

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.ui.action.IResourceSelector;
import org.eclipse.team.svn.ui.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.ui.synchronize.variant.ResourceVariant;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizeModelAction;
import org.eclipse.team.ui.synchronize.SynchronizeModelOperation;

/**
 * Synchronize view action abstract implementation
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractSynchronizeModelAction extends SynchronizeModelAction {

    public AbstractSynchronizeModelAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
		this.setEnabled(false);
		this.setToolTipText(text);
	}

    public AbstractSynchronizeModelAction(String text, ISynchronizePageConfiguration configuration, ISelectionProvider selectionProvider) {
		super(text, configuration, selectionProvider);
		this.setEnabled(false);
		this.setToolTipText(text);
	}

	protected SynchronizeModelOperation getSubscriberOperation(
			ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		return new FilteredSynchronizeModelOperation(configuration, elements);
	}

	protected abstract IActionOperation execute(FilteredSynchronizeModelOperation operation);

	protected class FilteredSynchronizeModelOperation extends SynchronizeModelOperation implements IResourceSelector {

		public FilteredSynchronizeModelOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
			super(configuration, elements);
		}

		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			IActionOperation op = AbstractSynchronizeModelAction.this.execute(this);
			if (op != null) {
			    UIMonitorUtility.doTaskExternalDefault(op, monitor);
			}
		}
		
		public IResource getSelectedResource() {
			AbstractSVNSyncInfo info = this.getSVNSyncInfo();
			return info != null ? info.getLocal() : null;
		}
		
		public AbstractSVNSyncInfo getSVNSyncInfo() {
		    // this function provide ability to get only a selected resource sync info without it children
		    Object obj = AbstractSynchronizeModelAction.this.getStructuredSelection().getFirstElement();
		    if (obj instanceof SyncInfoModelElement) {
		        return (AbstractSVNSyncInfo)((SyncInfoModelElement)obj).getSyncInfo();
		    }
			return null;
		}
		
		public IResource []getSelectedResources() {
		    return this.getSelectedResources(new ISyncStateFilter.StateFilterWrapper(IStateFilter.SF_ALL));
		}
		
		public IResource []getSelectedResources(ISyncStateFilter filter) {
		    SyncInfo []infos = this.getSyncInfoSet().getSyncInfos();
		    ArrayList retVal = new ArrayList();
		    for (int i = 0; i < infos.length; i++) {
		        AbstractSVNSyncInfo info = (AbstractSVNSyncInfo)infos[i];
		        ILocalResource local = info.getLocalResource();
		        ResourceVariant resource = (ResourceVariant)info.getRemote();
		        if (filter.acceptRemote(resource.getResource().getResource(), resource.getStatus(), resource.getResource().getChangeMask()) ||
		            filter.accept(local.getResource(), local.getStatus(), local.getChangeMask())) {
		            retVal.add(local.getResource());
		        }
		    }
			return (IResource [])retVal.toArray(new IResource[retVal.size()]);
		}

		// recursiveness should be managed by the view
        public IResource[] getSelectedResources(IStateFilter filter) {
            if (filter instanceof ISyncStateFilter) {
    			return this.getSelectedResources((ISyncStateFilter)filter);
            }
			return this.getSelectedResources(new ISyncStateFilter.StateFilterWrapper(filter));
        }
		
		public IResource []getSelectedResourcesRecursive(IStateFilter filter) {
			return this.getSelectedResources(filter);
		}
		
		public IResource []getSelectedResourcesRecursive(IStateFilter filter, int depth) {
			return this.getSelectedResources(filter);
		}
		
		public Shell getShell() {
			return super.getShell();
		}
		
		protected boolean canRunAsJob() {
			return true;
		}
		
		protected String getJobName() {
			return AbstractSynchronizeModelAction.this.getText();
		}

	}

}
