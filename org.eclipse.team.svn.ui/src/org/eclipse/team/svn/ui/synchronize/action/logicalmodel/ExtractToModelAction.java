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

package org.eclipse.team.svn.ui.synchronize.action.logicalmodel;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeLogicalModelAction;
import org.eclipse.team.svn.ui.synchronize.action.ExtractToActionHelper;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Extract To logical model action for Synchronize View (both incoming and outgoing - 
 * the conflicting resources are ignored)
 * 
 * @author Igor Burilo
 *
 */
public class ExtractToModelAction extends AbstractSynchronizeLogicalModelAction {

	protected ExtractToActionHelper actionHelper;
	
	public ExtractToModelAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
		this.actionHelper = new ExtractToActionHelper(this, configuration);
	}

	protected FastSyncInfoFilter getSyncInfoFilter() {
		return this.actionHelper.getSyncInfoFilter();
	}
	
	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		AbstractSVNSyncInfo [] infos = this.getSVNSyncInfos();
		for (int i = 0; i < infos.length; i++) {
			if (SyncInfo.getDirection(infos[i].getKind()) == SyncInfo.CONFLICTING) {
				return false;
			}
		}
		return infos.length > 0;
	}
	
	protected IActionOperation getOperation() {
		return this.actionHelper.getOperation();
	}

}
