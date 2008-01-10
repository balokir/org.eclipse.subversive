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

package org.eclipse.team.svn.ui.synchronize.update.action;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
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
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.local.AddToSVNPanel;
import org.eclipse.team.svn.ui.panel.local.IgnoreMethodPanel;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeModelAction;
import org.eclipse.team.svn.ui.synchronize.update.UpdateSyncInfo;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Synchronize view add to svn:ignore action implementation
 * 
 * @author Alexander Gurov
 */
public class AddToSVNIgnoreAction extends AbstractSynchronizeModelAction {

	public AddToSVNIgnoreAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected static IStateFilter SF_NEW_AND_PARENT_VERSIONED = new IStateFilter.AbstractStateFilter() {
        protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
            if (state == IStateFilter.ST_NEW) {
            	IContainer parent = resource.getParent();
            	if (resource != null && parent != null) {
            		ILocalResource localParent = SVNRemoteStorage.instance().asLocalResource(parent);
            		if (localParent != null) {
            			return IStateFilter.SF_VERSIONED.accept(localParent);
            		}
                }
            }
            return false;
        }
		protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
			return true;
		}
    };
		
	protected FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter() {
			public boolean select(SyncInfo info) {
				UpdateSyncInfo sync = (UpdateSyncInfo)info;
				return AddToSVNIgnoreAction.SF_NEW_AND_PARENT_VERSIONED.accept(sync.getLocalResource());
			}
		};
	}

	protected IActionOperation execute(final FilteredSynchronizeModelOperation operation) {
		final IResource [][]resources = new IResource[][] {FileUtility.shrinkChildNodes(operation.getSelectedResources(AddToSVNIgnoreAction.SF_NEW_AND_PARENT_VERSIONED))};
		
		final IResource [][]operableParents = new IResource[][] {FileUtility.getOperableParents(resources[0], IStateFilter.SF_UNVERSIONED)};
		if (operableParents[0].length > 0) {
		    final AddToSVNPanel panel = new AddToSVNPanel(operableParents[0]);
			final DefaultDialog dialog1 = new DefaultDialog(operation.getShell(), panel);
			operation.getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
				    operableParents[0] = dialog1.open() != 0 ? null : panel.getSelectedResources();
				}
			});
		    if (operableParents[0] == null) {
		        return null;
		    }
		}

		IgnoreMethodPanel panel = new IgnoreMethodPanel(resources[0]);
		final DefaultDialog dialog = new DefaultDialog(operation.getShell(), panel);
		operation.getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				if (dialog.open() != 0) {
					resources[0] = null;
				}
			}
		});
		
		if (resources[0] == null) {
			return null;
		}
		
		AddToSVNIgnoreOperation mainOp = new AddToSVNIgnoreOperation(resources[0], panel.getIgnoreType(), panel.getIgnorePattern());
		
		CompositeOperation op = new CompositeOperation(mainOp.getId());

		if (operableParents[0].length > 0) {
			op.add(new AddToSVNOperation(operableParents[0]));
			op.add(new ClearLocalStatusesOperation(operableParents[0]));
		}

		op.add(mainOp);
		HashSet tmp = new HashSet(Arrays.asList(resources[0]));
		for (int i = 0; i < resources[0].length; i++) {
			tmp.add(resources[0][i].getParent());
		}
		IResource []resourcesAndParents = (IResource [])tmp.toArray(new IResource[tmp.size()]);
		op.add(new RefreshResourcesOperation(resourcesAndParents/*, IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL*/));

		return op;
	}

}
