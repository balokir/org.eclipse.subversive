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
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.synchronize.AbstractSVNParticipant;
import org.eclipse.team.svn.ui.synchronize.AbstractSynchronizeActionGroup;

/**
 * Synchronize options action set
 * 
 * @author Alexander Gurov
 */
public class OptionsActionGroup extends AbstractSynchronizeActionGroup {
	public static final String GROUP_SYNCH_OPTIONS = "synchronizeViewOptions";
	
	protected IAction changeOptionAction;
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
		if (CoreExtensionsManager.instance().getSVNClientWrapperFactory().isReportRevisionChangeAllowed()) {
			this.changeOptionAction = new Action(SVNTeamUIPlugin.instance().getResource("OptionsActionGroup.ReportFolderChanges"), IAction.AS_CHECK_BOX) {
				public void run() {
					IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
					boolean reportRevisionChange = SVNTeamPreferences.getSynchronizeBoolean(store, SVNTeamPreferences.SYNCHRONIZE_REPORT_REVISION_CHANGE_NAME);
					SVNTeamPreferences.setSynchronizeBoolean(store, SVNTeamPreferences.SYNCHRONIZE_REPORT_REVISION_CHANGE_NAME, !reportRevisionChange);
					SVNTeamUIPlugin.instance().savePluginPreferences();
				}
			};
		}
		this.contiguousOptionAction = new Action(SVNTeamUIPlugin.instance().getResource("OptionsActionGroup.ShowInfoContiguous"), IAction.AS_CHECK_BOX) {
			public void run() {
				IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
				boolean fastReportMode = SVNTeamPreferences.getSynchronizeBoolean(store, SVNTeamPreferences.SYNCHRONIZE_SHOW_REPORT_CONTIGUOUS_NAME);
				SVNTeamPreferences.setSynchronizeBoolean(store, SVNTeamPreferences.SYNCHRONIZE_SHOW_REPORT_CONTIGUOUS_NAME, !fastReportMode);
				SVNTeamUIPlugin.instance().savePluginPreferences();
			}
		};
		this.refreshOptionButtons();
		if (CoreExtensionsManager.instance().getSVNClientWrapperFactory().isReportRevisionChangeAllowed()) {
			this.appendToGroup(
					ISynchronizePageConfiguration.P_VIEW_MENU, 
					OptionsActionGroup.GROUP_SYNCH_OPTIONS,
					this.changeOptionAction);
		}
		this.appendToGroup(
				ISynchronizePageConfiguration.P_VIEW_MENU, 
				OptionsActionGroup.GROUP_SYNCH_OPTIONS,
				this.contiguousOptionAction);
		
		this.configurationListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().startsWith(SVNTeamPreferences.SYNCHRONIZE_BASE)) {
					OptionsActionGroup.this.refreshOptionButtons();
					AbstractSVNParticipant paticipant = (AbstractSVNParticipant)OptionsActionGroup.this.getConfiguration().getParticipant();
					if (event.getProperty().endsWith(SVNTeamPreferences.SYNCHRONIZE_REPORT_REVISION_CHANGE_NAME) && 
						paticipant.getMatchingSubscriber().isSynchronizedWithRepository()) {
						SVNRemoteStorage.instance().reconfigureLocations();
						paticipant.run(null);
					}
				}
			}
		};
		SVNTeamUIPlugin.instance().getPreferenceStore().addPropertyChangeListener(this.configurationListener);
	}
	
    protected void refreshOptionButtons() {
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		if (CoreExtensionsManager.instance().getSVNClientWrapperFactory().isReportRevisionChangeAllowed()) {
			this.changeOptionAction.setChecked(SVNTeamPreferences.getSynchronizeBoolean(store, SVNTeamPreferences.SYNCHRONIZE_REPORT_REVISION_CHANGE_NAME));
		}
		this.contiguousOptionAction.setChecked(SVNTeamPreferences.getSynchronizeBoolean(store, SVNTeamPreferences.SYNCHRONIZE_SHOW_REPORT_CONTIGUOUS_NAME));
    }
}
