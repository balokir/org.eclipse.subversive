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

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.AddToSVNWithPropertiesOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.ui.action.QueryResourceAddition;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeModelAction;
import org.eclipse.team.svn.ui.synchronize.update.UpdateSyncInfo;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Synchronize view add to version control action implementation
 * 
 * @author Alexander Gurov
 */
public class AddToSVNAction extends AbstractSynchronizeModelAction {
	public AddToSVNAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter() {
			public boolean select(SyncInfo info) {
				UpdateSyncInfo sync = (UpdateSyncInfo)info;
				return IStateFilter.SF_NEW.accept(sync.getLocalResource()) || IStateFilter.SF_IGNORED.accept(sync.getLocalResource());
			}
		};
	}

	protected IActionOperation execute(final FilteredSynchronizeModelOperation operation) {
		final IResource [][]resources = new IResource[1][];
		operation.getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
			    QueryResourceAddition query = new QueryResourceAddition(operation, operation.getShell());
			    resources[0] = query.queryAddition();
			}
		});
		if (resources[0] == null) {
			return null;
		}
		AddToSVNWithPropertiesOperation mainOp = new AddToSVNWithPropertiesOperation(resources[0], false);
		
		CompositeOperation op = new CompositeOperation(mainOp.getId());

		op.add(mainOp);
		op.add(new RefreshResourcesOperation(resources[0]/*, IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL*/));

		return op;
	}

}
