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

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.ui.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Synchronize view revert action implementation
 * 
 * @author Alexander Gurov
 */
public class RevertAction extends AbstractSynchronizeModelAction {
	public RevertAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter() {
			public boolean select(SyncInfo info) {
				return org.eclipse.team.svn.ui.action.local.RevertAction.SF_REVERTABLE_OR_NEW.accept(((AbstractSVNSyncInfo)info).getLocalResource());
			}
		};
	}

	protected IActionOperation execute(final FilteredSynchronizeModelOperation operation) {
		final CompositeOperation []op = new CompositeOperation[1];
		operation.getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				IResource []changedResources = operation.getSelectedResourcesRecursive(org.eclipse.team.svn.ui.action.local.RevertAction.SF_REVERTABLE_OR_NEW);
				IResource []userSelectedResources = operation.getSelectedResourcesRecursive();
				op[0] = org.eclipse.team.svn.ui.action.local.RevertAction.getRevertOperation(operation.getShell(), changedResources, userSelectedResources);
			}
		});
		
		if (op[0] == null) {
			return null;
		}

		return op[0];
	}

}
