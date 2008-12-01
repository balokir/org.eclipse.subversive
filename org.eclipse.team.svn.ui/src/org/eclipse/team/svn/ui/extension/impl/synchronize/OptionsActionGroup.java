/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.extension.impl.synchronize;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSubscriber;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.synchronize.AbstractSynchronizeActionGroup;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Synchronize options action set
 * 
 * @author Alexander Gurov
 */
public class OptionsActionGroup extends AbstractSynchronizeActionGroup {
	public static final String GROUP_SYNCH_OPTIONS = "synchronizeViewOptions";
	
	protected IAction contiguousOptionAction;

	public void configureMenuGroups(ISynchronizePageConfiguration configuration) {
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_VIEW_MENU, 
				OptionsActionGroup.GROUP_SYNCH_OPTIONS);
	}
	
	protected void configureActions(ISynchronizePageConfiguration configuration) {
		this.contiguousOptionAction = new Action(SVNUIMessages.OptionsActionGroup_ShowInfoContiguous, IAction.AS_CHECK_BOX) {
			public void run() {
				boolean fastReportMode = AbstractSVNSubscriber.getSynchInfoContigous();
				AbstractSVNSubscriber.setSynchInfoContigous(!fastReportMode);
				SVNTeamPlugin.instance().savePluginPreferences();
			}
		};
		this.refreshOptionButtons();
		this.appendToGroup(
				ISynchronizePageConfiguration.P_VIEW_MENU, 
				OptionsActionGroup.GROUP_SYNCH_OPTIONS,
				this.contiguousOptionAction);
	}
	
    protected void refreshOptionButtons() {
		this.contiguousOptionAction.setChecked(AbstractSVNSubscriber.getSynchInfoContigous());
    }
}
