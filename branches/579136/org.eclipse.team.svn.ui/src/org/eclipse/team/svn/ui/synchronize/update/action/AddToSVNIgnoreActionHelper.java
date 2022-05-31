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

package org.eclipse.team.svn.ui.synchronize.update.action;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.AddToSVNIgnoreOperation;
import org.eclipse.team.svn.core.operation.local.AddToSVNOperation;
import org.eclipse.team.svn.core.operation.local.ClearLocalStatusesOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.synchronize.UpdateSyncInfo;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.local.AddToSVNPanel;
import org.eclipse.team.svn.ui.panel.local.IgnoreMethodPanel;
import org.eclipse.team.svn.ui.synchronize.action.AbstractActionHelper;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Synchronize view add to svn:ignore action helper
 * 
 * @author Igor Burilo
 */
public class AddToSVNIgnoreActionHelper extends AbstractActionHelper {
	
	protected static IStateFilter SF_NEW_AND_PARENT_VERSIONED = new IStateFilter.AbstractStateFilter() {
        protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
            if (state == IStateFilter.ST_NEW) {
    			return IStateFilter.SF_VERSIONED.accept(SVNRemoteStorage.instance().asLocalResource(resource.getParent()));
            }
            return false;
        }
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return true;
		}
    };
    
	public AddToSVNIgnoreActionHelper(IAction action, ISynchronizePageConfiguration configuration) {
		super(action, configuration);
	}

	public FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter() {
			public boolean select(SyncInfo info) {
				UpdateSyncInfo sync = (UpdateSyncInfo)info;
				return AddToSVNIgnoreActionHelper.SF_NEW_AND_PARENT_VERSIONED.accept(sync.getLocalResource());
			}
		};
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.ui.synchronize.action.AbstractActionHelper#getOperation()
	 */
	public IActionOperation getOperation() {
		//FIXME add unversioned parents if required
		IResource []resources = FileUtility.shrinkChildNodes(this.getSyncInfoSelector().getSelectedResources());
		IResource []operableParents = FileUtility.getOperableParents(resources, IStateFilter.SF_UNVERSIONED);
		if (operableParents.length > 0) {
		    final AddToSVNPanel panel = new AddToSVNPanel(operableParents);
			final DefaultDialog dialog1 = new DefaultDialog(this.configuration.getSite().getShell(), panel);
		    if (dialog1.open() != 0) {
		        return null;
		    }
		    operableParents = panel.getSelectedResources();
		}

		IgnoreMethodPanel panel = new IgnoreMethodPanel(resources);
		DefaultDialog dialog = new DefaultDialog(this.configuration.getSite().getShell(), panel);
		if (dialog.open() != 0) {
			return null;
		}
		
		AddToSVNIgnoreOperation mainOp = new AddToSVNIgnoreOperation(resources, panel.getIgnoreType(), panel.getIgnorePattern());
		
		CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());

		if (operableParents.length > 0) {
			op.add(new AddToSVNOperation(operableParents));
			op.add(new ClearLocalStatusesOperation(operableParents));
		}

		op.add(mainOp);
		HashSet<IResource> tmp = new HashSet<IResource>(Arrays.asList(resources));
		for (int i = 0; i < resources.length; i++) {
			tmp.add(resources[i].getParent());
		}
		IResource []resourcesAndParents = tmp.toArray(new IResource[tmp.size()]);
		op.add(new RefreshResourcesOperation(resourcesAndParents, IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL));

		return op;
	}

}
