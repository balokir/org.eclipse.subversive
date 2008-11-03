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

import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.core.synchronize.variant.RemoteResourceVariant;
import org.eclipse.team.svn.ui.operation.ShowHistoryViewOperation;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Show resource history logical model action helper
 * 
 * @author Igor Burilo
 */
public class ShowHistoryActionHelper extends AbstractActionHelper {

	public ShowHistoryActionHelper(IAction action, ISynchronizePageConfiguration configuration) {
		super(action, configuration);
	}

	public IActionOperation getOperation() {
		AbstractSVNSyncInfo info = this.getSelectedSVNSyncInfo();
		if (info != null ) {
			RemoteResourceVariant variant = (RemoteResourceVariant)info.getRemote();
			if (variant.getResource() instanceof IResourceChange) {
				return new ShowHistoryViewOperation(((IResourceChange)variant.getResource()).getOriginator(), 0, 0);
			}
		}
		return new ShowHistoryViewOperation(this.getSelectedResource(), 0, 0);
	}

}
