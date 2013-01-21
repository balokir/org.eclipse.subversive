/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.mylyn;

import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.team.ui.AbstractActiveChangeSetProvider;
import org.eclipse.mylyn.team.ui.IContextChangeSet;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSetManager;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * Provides access to SVN ActiveChangeSet's 
 * 
 * @author Alexander Gurov
 */
public class SVNActiveChangeSetProvider extends AbstractActiveChangeSetProvider {
	public ActiveChangeSetManager getActiveChangeSetManager() {
		return SVNTeamUIPlugin.instance().getModelChangeSetManager();
	}
	
	public IContextChangeSet createChangeSet(ITask task) {
		return new SVNContextChangeSet(task, getActiveChangeSetManager());
	}
	
}
