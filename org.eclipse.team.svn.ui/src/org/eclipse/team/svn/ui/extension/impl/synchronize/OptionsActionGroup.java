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

package org.eclipse.team.svn.ui.extension.impl.synchronize;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
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

	protected IPropertyChangeListener configurationListener;
	
	public void dispose() {
		if (this.configurationListener != null) {
			SVNTeamUIPlugin.instance().getPreferenceStore().removePropertyChangeListener(this.configurationListener);
		}
		super.dispose();
	}

	public void configureMenuGroups(ISynchronizePageConfiguration configuration) {
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_VIEW_MENU, 
				OptionsActionGroup.GROUP_SYNCH_OPTIONS);
	}
	
	protected void configureActions(ISynchronizePageConfiguration configuration) {
		this.contiguousOptionAction = new Action(SVNTeamUIPlugin.instance().getResource("OptionsActionGroup.ShowInfoContiguous"), IAction.AS_CHECK_BOX) {
			public void run() {
				IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
				boolean fastReportMode = SVNTeamPreferences.getSynchronizeBoolean(store, SVNTeamPreferences.SYNCHRONIZE_SHOW_REPORT_CONTIGUOUS_NAME);
				SVNTeamPreferences.setSynchronizeBoolean(store, SVNTeamPreferences.SYNCHRONIZE_SHOW_REPORT_CONTIGUOUS_NAME, !fastReportMode);
				SVNTeamUIPlugin.instance().savePluginPreferences();
			}
		};
		this.refreshOptionButtons();
		this.appendToGroup(
				ISynchronizePageConfiguration.P_VIEW_MENU, 
				OptionsActionGroup.GROUP_SYNCH_OPTIONS,
				this.contiguousOptionAction);
		
		SVNTeamUIPlugin.instance().getPreferenceStore().addPropertyChangeListener(this.configurationListener);
	}
	
    protected void refreshOptionButtons() {
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		this.contiguousOptionAction.setChecked(SVNTeamPreferences.getSynchronizeBoolean(store, SVNTeamPreferences.SYNCHRONIZE_SHOW_REPORT_CONTIGUOUS_NAME));
    }
}
