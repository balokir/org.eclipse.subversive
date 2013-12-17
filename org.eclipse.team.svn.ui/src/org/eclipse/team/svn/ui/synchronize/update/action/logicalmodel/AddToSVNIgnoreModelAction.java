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

package org.eclipse.team.svn.ui.synchronize.update.action.logicalmodel;

import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeLogicalModelAction;
import org.eclipse.team.svn.ui.synchronize.update.action.AddToSVNIgnoreActionHelper;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Synchronize view add to svn:ignore logical model action implementation
 * 
 * @author Igor Burilo
 */
public class AddToSVNIgnoreModelAction extends AbstractSynchronizeLogicalModelAction {

	protected AddToSVNIgnoreActionHelper actionHelper;
	
	public AddToSVNIgnoreModelAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
		this.actionHelper = new AddToSVNIgnoreActionHelper(this, configuration);
	}

	protected boolean needsToSaveDirtyEditors() {
		return false;
	}
			
	protected FastSyncInfoFilter getSyncInfoFilter() {
		return this.actionHelper.getSyncInfoFilter();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeLogicalModelAction#getOperation()
	 */
	protected IActionOperation getOperation() {
		return this.actionHelper.getOperation();
	}

}
