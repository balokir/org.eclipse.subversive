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

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.synchronize.AbstractSynchronizeActionGroup;
import org.eclipse.team.svn.ui.synchronize.update.action.AddToSVNAction;
import org.eclipse.team.svn.ui.synchronize.update.action.AddToSVNIgnoreAction;
import org.eclipse.team.svn.ui.synchronize.update.action.CommitAction;
import org.eclipse.team.svn.ui.synchronize.update.action.CreatePatchFileAction;
import org.eclipse.team.svn.ui.synchronize.update.action.EditConflictsAction;
import org.eclipse.team.svn.ui.synchronize.update.action.ExpandAllAction;
import org.eclipse.team.svn.ui.synchronize.update.action.LockAction;
import org.eclipse.team.svn.ui.synchronize.update.action.MarkAsMergedAction;
import org.eclipse.team.svn.ui.synchronize.update.action.OverrideAndCommitAction;
import org.eclipse.team.svn.ui.synchronize.update.action.OverrideAndUpdateAction;
import org.eclipse.team.svn.ui.synchronize.update.action.RevertAction;
import org.eclipse.team.svn.ui.synchronize.update.action.SetKeywordsAction;
import org.eclipse.team.svn.ui.synchronize.update.action.SetPropertyAction;
import org.eclipse.team.svn.ui.synchronize.update.action.ShowAnnotationAction;
import org.eclipse.team.svn.ui.synchronize.update.action.ShowPropertiesAction;
import org.eclipse.team.svn.ui.synchronize.update.action.ShowResourceHistoryAction;
import org.eclipse.team.svn.ui.synchronize.update.action.UnlockAction;
import org.eclipse.team.svn.ui.synchronize.update.action.UpdateAction;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Synchronize view update action set
 * 
 * @author Alexander Gurov
 */
public class UpdateActionGroup extends AbstractSynchronizeActionGroup {
	public static final String GROUP_SYNC_NORMAL = "syncIncomingOutgoing";
	public static final String GROUP_SYNC_CONFLICTS = "syncConflicting";
	public static final String GROUP_MANAGE_LOCALS = "manageLocalChanges";
	public static final String GROUP_TEAM = "team";
	public static final String GROUP_PROCESS_ALL = "processAllItems";
	
	protected MenuManager addOns;
	
	public void configureMenuGroups(ISynchronizePageConfiguration configuration) {
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_SYNC_NORMAL);
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_SYNC_CONFLICTS);
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_MANAGE_LOCALS);
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_TEAM);
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_TOOLBAR_MENU, 
				UpdateActionGroup.GROUP_PROCESS_ALL);
	}
	
	public void dispose() {
		this.addOns.removeAll();
		this.addOns.dispose();
		
		super.dispose();
	}
	
	protected void configureActions(ISynchronizePageConfiguration configuration) {
		UpdateAction updateAllAction = new UpdateAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.UpdateAllIncomingChanges"), configuration, this.getVisibleRootsSelectionProvider());
		updateAllAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/update.gif"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_TOOLBAR_MENU, 
				UpdateActionGroup.GROUP_PROCESS_ALL,
				updateAllAction);
		CommitAction commitAllAction = new CommitAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.CommitAllOutgoingChanges"), configuration, this.getVisibleRootsSelectionProvider());
		commitAllAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/commit.gif"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_TOOLBAR_MENU, 
				UpdateActionGroup.GROUP_PROCESS_ALL,
				commitAllAction);
		ExpandAllAction expandAllAction = new ExpandAllAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.ExpandAll"), configuration, this.getVisibleRootsSelectionProvider());
		expandAllAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/expandall.gif"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_TOOLBAR_MENU, 
				ISynchronizePageConfiguration.MODE_GROUP,
				expandAllAction);
		
		UpdateAction updateAction = new UpdateAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.Update"), configuration);
		updateAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/update.gif"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_SYNC_NORMAL,
				updateAction);
		CommitAction commitAction = new CommitAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.Commit"), configuration);
		commitAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/commit.gif"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_SYNC_NORMAL,
				commitAction);

		OverrideAndUpdateAction overrideUpdateAction = new OverrideAndUpdateAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.OverrideAndUpdate"), configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_SYNC_CONFLICTS,
				overrideUpdateAction);
		OverrideAndCommitAction overrideCommitAction = new OverrideAndCommitAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.OverrideAndCommit"), configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_SYNC_CONFLICTS,
				overrideCommitAction);
		EditConflictsAction editConflictsAction = new EditConflictsAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.EditConflicts"), configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_SYNC_CONFLICTS,
				editConflictsAction);									
		MarkAsMergedAction markMergedAction = new MarkAsMergedAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.MarkAsMerged"), configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_SYNC_CONFLICTS,
				markMergedAction);									
		
		RevertAction revertAction = new RevertAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.Revert"), configuration);
		revertAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/revert.gif"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_MANAGE_LOCALS,
				revertAction);
		AddToSVNAction addToSVNAction = new AddToSVNAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.AddToVersionControl"), configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_MANAGE_LOCALS,
				addToSVNAction);
		AddToSVNIgnoreAction addToSVNIgnoreAction = new AddToSVNIgnoreAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.AddToIgnore"), configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_MANAGE_LOCALS,
				addToSVNIgnoreAction);
		
		this.addOns = new MenuManager(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.Team"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_TEAM, 
				this.addOns);
		
		CreatePatchFileAction patchAction = new CreatePatchFileAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.Patch"), configuration);
		this.addOns.add(patchAction);
		
		this.addOns.add(new Separator());
		
		ShowResourceHistoryAction showResourceHistoryAction = new ShowResourceHistoryAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.ShowResourceHistory"), configuration);
		showResourceHistoryAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/showhistory.gif"));
		this.addOns.add(showResourceHistoryAction);
		ShowAnnotationAction showAnnotationAction = new ShowAnnotationAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.ShowAnnotation"), configuration);
		this.addOns.add(showAnnotationAction);
		
		this.addOns.add(new Separator());
		
		ShowPropertiesAction showPropertiesAction = new ShowPropertiesAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.ShowProperties"), configuration);
		showPropertiesAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/propertiesedit.gif"));
		this.addOns.add(showPropertiesAction);
		SetPropertyAction setPropAction = new SetPropertyAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.SetProperty"), configuration);
		this.addOns.add(setPropAction);
		SetKeywordsAction setKeywordsAction = new SetKeywordsAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.SetKeywords"), configuration);
		this.addOns.add(setKeywordsAction);
		
		this.addOns.add(new Separator());
		
		LockAction lockAction = new LockAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.Lock"), configuration);
		lockAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/lock.gif"));
		this.addOns.add(lockAction);
		UnlockAction unlockAction = new UnlockAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.Unlock"), configuration);
		unlockAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/unlock.gif"));
		this.addOns.add(unlockAction);
	}
	
}
