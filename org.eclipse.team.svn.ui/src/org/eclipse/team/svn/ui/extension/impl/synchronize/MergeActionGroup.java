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

import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.synchronize.AbstractSynchronizeActionGroup;
import org.eclipse.team.svn.ui.synchronize.merge.action.MarkAsMergedAction;
import org.eclipse.team.svn.ui.synchronize.merge.action.OverrideAndUpdateAction;
import org.eclipse.team.svn.ui.synchronize.merge.action.ShowAnnotationAction;
import org.eclipse.team.svn.ui.synchronize.merge.action.ShowResourceHistoryAction;
import org.eclipse.team.svn.ui.synchronize.merge.action.UpdateAction;
import org.eclipse.team.svn.ui.synchronize.update.action.RevertAction;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Merge action group
 * 
 * @author Alexander Gurov
 */
public class MergeActionGroup extends AbstractSynchronizeActionGroup {
	public static final String GROUP_MERGE_CHANGES = "mergeChanges";
	public static final String GROUP_MANAGE_LOCALS = "manageLocalChanges";
	public static final String GROUP_CHANGES_AUDIT = "changesAudit";
	public static final String GROUP_PROCESS_ALL = "processAllItems";
	
	public void configureMenuGroups(ISynchronizePageConfiguration configuration) {
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_MERGE_CHANGES);
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_MANAGE_LOCALS);
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_CHANGES_AUDIT);
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_TOOLBAR_MENU, 
				MergeActionGroup.GROUP_PROCESS_ALL);
	}
	
	public void configureActions(ISynchronizePageConfiguration configuration) {
		UpdateAction updateAllAction = new UpdateAction(SVNTeamUIPlugin.instance().getResource("MergeActionGroup.UpdateAllIncomingChanges"), configuration, this.getVisibleRootsSelectionProvider());
		updateAllAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/update.gif"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_TOOLBAR_MENU, 
				MergeActionGroup.GROUP_PROCESS_ALL,
				updateAllAction);

		UpdateAction updateAction = new UpdateAction(SVNTeamUIPlugin.instance().getResource("MergeActionGroup.Update"), configuration);
		updateAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/update.gif"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_MERGE_CHANGES,
				updateAction);
		OverrideAndUpdateAction overrideAndUpdateAction = new OverrideAndUpdateAction(SVNTeamUIPlugin.instance().getResource("MergeActionGroup.OverrideAndUpdate"), configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_MERGE_CHANGES,
				overrideAndUpdateAction);
		MarkAsMergedAction markAsMergedAction = new MarkAsMergedAction(SVNTeamUIPlugin.instance().getResource("MergeActionGroup.MarkAsMerged"), configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_MERGE_CHANGES,
				markAsMergedAction);

		RevertAction revertAction = new RevertAction(SVNTeamUIPlugin.instance().getResource("MergeActionGroup.Revert"), configuration);
		revertAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/revert.gif"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_MANAGE_LOCALS,
				revertAction);

		ShowResourceHistoryAction showResourceHistoryAction = new ShowResourceHistoryAction(SVNTeamUIPlugin.instance().getResource("MergeActionGroup.ShowResourceHistory"), configuration);
		showResourceHistoryAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/showhistory.gif"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_CHANGES_AUDIT,
				showResourceHistoryAction);
		ShowAnnotationAction showAnnotationAction = new ShowAnnotationAction(SVNTeamUIPlugin.instance().getResource("MergeActionGroup.ShowAnnotation"), configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_CHANGES_AUDIT,
				showAnnotationAction);
	}
	
}
