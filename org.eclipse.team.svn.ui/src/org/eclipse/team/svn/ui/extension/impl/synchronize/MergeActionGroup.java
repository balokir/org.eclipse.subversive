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

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.synchronize.AbstractSynchronizeActionGroup;
import org.eclipse.team.svn.ui.synchronize.action.CleanUpAction;
import org.eclipse.team.svn.ui.synchronize.action.CreateBranchAction;
import org.eclipse.team.svn.ui.synchronize.action.CreatePatchFileAction;
import org.eclipse.team.svn.ui.synchronize.action.EditConflictsAction;
import org.eclipse.team.svn.ui.synchronize.action.ExpandAllAction;
import org.eclipse.team.svn.ui.synchronize.action.MergePropertiesAction;
import org.eclipse.team.svn.ui.synchronize.action.RevertAction;
import org.eclipse.team.svn.ui.synchronize.action.SetKeywordsAction;
import org.eclipse.team.svn.ui.synchronize.action.SetPropertyAction;
import org.eclipse.team.svn.ui.synchronize.action.ShowHistoryAction;
import org.eclipse.team.svn.ui.synchronize.action.ShowIncomingAnnotationAction;
import org.eclipse.team.svn.ui.synchronize.action.ShowIncomingPropertiesAction;
import org.eclipse.team.svn.ui.synchronize.action.ShowOutgoingAnnotationAction;
import org.eclipse.team.svn.ui.synchronize.action.ShowOutgoingPropertiesAction;
import org.eclipse.team.svn.ui.synchronize.merge.action.MarkAsMergedAction;
import org.eclipse.team.svn.ui.synchronize.merge.action.OverrideAndUpdateAction;
import org.eclipse.team.svn.ui.synchronize.merge.action.UpdateAction;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Merge action group
 * 
 * @author Alexander Gurov
 */
public class MergeActionGroup extends AbstractSynchronizeActionGroup {
	public static final String GROUP_MERGE_CHANGES = "mergeChanges";
	public static final String GROUP_MANAGE_LOCALS = "manageLocalChanges";
	public static final String GROUP_TEAM = "team";
	public static final String GROUP_PROCESS_ALL = "processAllItems";
	
	protected MenuManager outgoing;
	protected MenuManager incoming;
	
	public void configureMenuGroups(ISynchronizePageConfiguration configuration) {
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_MERGE_CHANGES);
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_MANAGE_LOCALS);
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_TEAM);
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_TOOLBAR_MENU, 
				MergeActionGroup.GROUP_PROCESS_ALL);
	}
	
	public void dispose() {
		this.outgoing.removeAll();
		this.outgoing.dispose();
		
		this.incoming.removeAll();
		this.incoming.dispose();
		
		super.dispose();
	}
	
	public void configureActions(ISynchronizePageConfiguration configuration) {
		UpdateAction updateAllAction = new UpdateAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.AcceptAllIncomingChanges"), configuration, this.getVisibleRootsSelectionProvider());
		updateAllAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/update.gif"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_TOOLBAR_MENU, 
				MergeActionGroup.GROUP_PROCESS_ALL,
				updateAllAction);
		ExpandAllAction expandAllAction = new ExpandAllAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.ExpandAll"), configuration, this.getVisibleRootsSelectionProvider());
		expandAllAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/expandall.gif"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_TOOLBAR_MENU, 
				ISynchronizePageConfiguration.MODE_GROUP,
				expandAllAction);

		UpdateAction updateAction = new UpdateAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.Accept"), configuration);
		updateAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/update.gif"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_MERGE_CHANGES,
				updateAction);
		OverrideAndUpdateAction overrideAndUpdateAction = new OverrideAndUpdateAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.OverrideAndUpdate"), configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_MERGE_CHANGES,
				overrideAndUpdateAction);
		EditConflictsAction editConflictsAction = new EditConflictsAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.EditConflicts"), configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_MERGE_CHANGES,
				editConflictsAction);
		MergePropertiesAction mergePropertiesAction = new MergePropertiesAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.MergeProperties"), configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_MERGE_CHANGES,
				mergePropertiesAction);
		MarkAsMergedAction markAsMergedAction = new MarkAsMergedAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.MarkAsMerged"), configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_MERGE_CHANGES,
				markAsMergedAction);

		RevertAction revertAction = new RevertAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.Revert"), configuration);
		revertAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/revert.gif"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_MANAGE_LOCALS,
				revertAction);
		ShowHistoryAction showHistoryAction = new ShowHistoryAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.ShowResourceHistory"), configuration);
		showHistoryAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/showhistory.gif"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_MANAGE_LOCALS,
				showHistoryAction);

		this.outgoing = new MenuManager(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.Outgoing"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_TEAM, 
				this.outgoing);
		this.incoming = new MenuManager(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.Incoming"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_TEAM, 
				this.incoming);
		
		ShowOutgoingPropertiesAction showPropertiesAction = new ShowOutgoingPropertiesAction(SVNTeamUIPlugin.instance().getResource("ShowPropertiesAction.label"), configuration);
		showPropertiesAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/propertiesedit.gif"));
		this.outgoing.add(showPropertiesAction);
		SetPropertyAction setPropAction = new SetPropertyAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.SetProperty"), configuration);
		this.outgoing.add(setPropAction);
		SetKeywordsAction setKeywordsAction = new SetKeywordsAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.SetKeywords"), configuration);
		this.outgoing.add(setKeywordsAction);
		ShowOutgoingAnnotationAction showAnnotationAction = new ShowOutgoingAnnotationAction(SVNTeamUIPlugin.instance().getResource("ShowAnnotationCommand.label"), configuration);
		this.outgoing.add(showAnnotationAction);
		
		this.outgoing.add(new Separator());
		
		CreatePatchFileAction patchAction = new CreatePatchFileAction(SVNTeamUIPlugin.instance().getResource("CreatePatchCommand.label"), configuration);
		this.outgoing.add(patchAction);
		CreateBranchAction branchAction = new CreateBranchAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.Branch"), configuration);
		branchAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/branch.gif"));
		this.outgoing.add(branchAction);
		
		this.outgoing.add(new Separator());
		
		CleanUpAction cleanUpAction = new CleanUpAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.Cleanup"), configuration);
		this.outgoing.add(cleanUpAction);
		
		ShowIncomingPropertiesAction showIncomingPropertiesAction = new ShowIncomingPropertiesAction(SVNTeamUIPlugin.instance().getResource("ShowPropertiesAction.label"), configuration);
		showIncomingPropertiesAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/propertiesedit.gif"));
		this.incoming.add(showIncomingPropertiesAction);
		ShowIncomingAnnotationAction showIncomingAnnotationAction = new ShowIncomingAnnotationAction(SVNTeamUIPlugin.instance().getResource("ShowAnnotationAction.label"), configuration);
		this.incoming.add(showIncomingAnnotationAction);
		
		this.incoming.add(new Separator());
	}
	
}
