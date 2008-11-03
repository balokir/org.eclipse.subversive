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
import org.eclipse.team.svn.ui.synchronize.action.CommitActionHelper;
import org.eclipse.team.svn.ui.synchronize.action.logicalmodel.AbstractModelToolbarAction;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Synchronize view commit all action logical model implementation
 * 
 * @author Igor Burilo
 */
public class CommitAllModelAction extends AbstractModelToolbarAction {	
	
	public CommitAllModelAction(String text, ISynchronizePageConfiguration configuration) {		
		super(text, configuration);
	}
	
	public FastSyncInfoFilter getSyncInfoFilter() {
		return CommitActionHelper.getCommitSyncInfoFilter();
	}

	protected IActionOperation getOperation() {
		return CommitActionHelper.getCommitOperation(this.getSyncInfoSelector(), this.getConfiguration());
	}
}
