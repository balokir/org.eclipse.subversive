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
import org.eclipse.team.svn.ui.synchronize.update.action.OverrideAndCommitModelActionHelper;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Override and commit conflicting files logical model action
 * 
 * @author Igor Burilo
 */
public class OverrideAndCommitModelAction extends AbstractSynchronizeLogicalModelAction {

	protected OverrideAndCommitModelActionHelper actionHelper;
	
	public OverrideAndCommitModelAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
		this.actionHelper = new OverrideAndCommitModelActionHelper(this, configuration);
	}

	public FastSyncInfoFilter getSyncInfoFilter() {
		return this.actionHelper.getSyncInfoFilter();
	}
	
	protected IActionOperation getOperation() {
		return this.actionHelper.getOperation();
	}

}
