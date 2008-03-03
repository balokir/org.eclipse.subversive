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
import org.eclipse.team.svn.ui.synchronize.action.CleanUpAction;
import org.eclipse.team.svn.ui.synchronize.action.CreateBranchAction;
import org.eclipse.team.svn.ui.synchronize.action.CreatePatchFileAction;
import org.eclipse.team.svn.ui.synchronize.action.ExpandAllAction;
import org.eclipse.team.svn.ui.synchronize.action.ExtractIncomingToAction;
import org.eclipse.team.svn.ui.synchronize.action.ExtractOutgoingToAction;
import org.eclipse.team.svn.ui.synchronize.action.ExtractToAction;
import org.eclipse.team.svn.ui.synchronize.action.RevertAction;
import org.eclipse.team.svn.ui.synchronize.action.SetKeywordsAction;
import org.eclipse.team.svn.ui.synchronize.action.SetPropertyAction;
import org.eclipse.team.svn.ui.synchronize.action.ShowOutgoingAnnotationAction;
import org.eclipse.team.svn.ui.synchronize.action.ShowOutgoingHistoryAction;
import org.eclipse.team.svn.ui.synchronize.action.ShowOutgoingPropertiesAction;
import org.eclipse.team.svn.ui.synchronize.update.action.AddToSVNAction;
import org.eclipse.team.svn.ui.synchronize.update.action.AddToSVNIgnoreAction;
import org.eclipse.team.svn.ui.synchronize.update.action.CommitAction;
import org.eclipse.team.svn.ui.synchronize.update.action.EditConflictsAction;
import org.eclipse.team.svn.ui.synchronize.update.action.LockAction;
import org.eclipse.team.svn.ui.synchronize.update.action.MarkAsMergedAction;
import org.eclipse.team.svn.ui.synchronize.update.action.OverrideAndCommitAction;
import org.eclipse.team.svn.ui.synchronize.update.action.OverrideAndUpdateAction;
import org.eclipse.team.svn.ui.synchronize.action.ShowIncomingAnnotationAction;
import org.eclipse.team.svn.ui.synchronize.action.ShowIncomingHistoryAction;
import org.eclipse.team.svn.ui.synchronize.action.ShowIncomingPropertiesAction;
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
	
	protected MenuManager outgoing;
	protected MenuManager incoming;
	
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
		this.outgoing.removeAll();
		this.outgoing.dispose();
		
		this.incoming.removeAll();
		this.incoming.dispose();
		
		super.dispose();
	}
	
	protected void configureActions(ISynchronizePageConfiguration configuration) {
		UpdateAction updateAllAction = new UpdateAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.UpdateAllIncomingChanges"), configuration, this.getVisibleRootsSelectionProvider());
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
		ExpandAllAction expandAllAction = new ExpandAllAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.ExpandAll"), configuration, this.getVisibleRootsSelectionProvider());
		expandAllAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/expandall.gif"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_TOOLBAR_MENU, 
				ISynchronizePageConfiguration.MODE_GROUP,
				expandAllAction);
		
		UpdateAction updateAction = new UpdateAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.Update"), configuration);
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

		OverrideAndUpdateAction overrideUpdateAction = new OverrideAndUpdateAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.OverrideAndUpdate"), configuration);
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
		MarkAsMergedAction markMergedAction = new MarkAsMergedAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.MarkAsMerged"), configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_SYNC_CONFLICTS,
				markMergedAction);									
		
		RevertAction revertAction = new RevertAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.Revert"), configuration);
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
		ExtractToAction extractTo = new ExtractToAction(SVNTeamUIPlugin.instance().getResource("ExtractToAction.Label"), configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_MANAGE_LOCALS,
				extractTo);
				
		this.outgoing = new MenuManager(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.Outgoing"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_TEAM, 
				this.outgoing);
		this.incoming = new MenuManager(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.Incoming"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_TEAM, 
				this.incoming);
		
		ShowOutgoingHistoryAction showResourceHistoryAction = new ShowOutgoingHistoryAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.ShowResourceHistory"), configuration);
		showResourceHistoryAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/showhistory.gif"));
		this.outgoing.add(showResourceHistoryAction);
		ShowOutgoingAnnotationAction showAnnotationAction = new ShowOutgoingAnnotationAction(SVNTeamUIPlugin.instance().getResource("ShowAnnotationCommand.label"), configuration);
		this.outgoing.add(showAnnotationAction);
		ShowOutgoingPropertiesAction showPropertiesAction = new ShowOutgoingPropertiesAction(SVNTeamUIPlugin.instance().getResource("ShowPropertiesAction.label"), configuration);
		showPropertiesAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/propertiesedit.gif"));
		this.outgoing.add(showPropertiesAction);
		SetPropertyAction setPropAction = new SetPropertyAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.SetProperty"), configuration);
		this.outgoing.add(setPropAction);
		SetKeywordsAction setKeywordsAction = new SetKeywordsAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.SetKeywords"), configuration);
		this.outgoing.add(setKeywordsAction);
		
		this.outgoing.add(new Separator());
		
		LockAction lockAction = new LockAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.Lock"), configuration);
		lockAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/lock.gif"));
		this.outgoing.add(lockAction);
		UnlockAction unlockAction = new UnlockAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.Unlock"), configuration);
		unlockAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/unlock.gif"));
		this.outgoing.add(unlockAction);
		
		this.outgoing.add(new Separator());
		
		CreatePatchFileAction patchAction = new CreatePatchFileAction(SVNTeamUIPlugin.instance().getResource("CreatePatchCommand.label"), configuration);
		this.outgoing.add(patchAction);
		CreateBranchAction branchAction = new CreateBranchAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.Branch"), configuration);
		branchAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/branch.gif"));
		this.outgoing.add(branchAction);
		ExtractOutgoingToAction extractActionOutgoing = new ExtractOutgoingToAction(SVNTeamUIPlugin.instance().getResource("ExtractToAction.Label"), configuration);
		this.outgoing.add(extractActionOutgoing);
		
		this.outgoing.add(new Separator());
		
		CleanUpAction cleanUpAction = new CleanUpAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.Cleanup"), configuration);
		this.outgoing.add(cleanUpAction);
		
		ShowIncomingHistoryAction showIncomingHistoryAction = new ShowIncomingHistoryAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.ShowResourceHistory"), configuration);
		showIncomingHistoryAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/showhistory.gif"));
		this.incoming.add(showIncomingHistoryAction);
		ShowIncomingAnnotationAction showIncomingAnnotationAction = new ShowIncomingAnnotationAction(SVNTeamUIPlugin.instance().getResource("ShowAnnotationAction.label"), configuration);
		this.incoming.add(showIncomingAnnotationAction);
		
		
		ShowIncomingPropertiesAction showIncomingPropertiesAction = new ShowIncomingPropertiesAction(SVNTeamUIPlugin.instance().getResource("ShowPropertiesAction.label"), configuration);
		showIncomingPropertiesAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/propertiesedit.gif"));
		this.incoming.add(showIncomingPropertiesAction);
		
		this.incoming.add(new Separator());
		
		ExtractIncomingToAction extractActionIncoming = new ExtractIncomingToAction(SVNTeamUIPlugin.instance().getResource("ExtractToAction.Label"), configuration);
		this.incoming.add(extractActionIncoming);
	}
	
}
