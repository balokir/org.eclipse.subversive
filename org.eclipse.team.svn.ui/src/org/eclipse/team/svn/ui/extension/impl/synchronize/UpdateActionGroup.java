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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.synchronize.AbstractSynchronizeActionGroup;
import org.eclipse.team.svn.ui.synchronize.action.CleanUpAction;
import org.eclipse.team.svn.ui.synchronize.action.ComparePropertiesAction;
import org.eclipse.team.svn.ui.synchronize.action.CreateBranchAction;
import org.eclipse.team.svn.ui.synchronize.action.CreatePatchFileAction;
import org.eclipse.team.svn.ui.synchronize.action.EditConflictsAction;
import org.eclipse.team.svn.ui.synchronize.action.ExpandAllAction;
import org.eclipse.team.svn.ui.synchronize.action.ExtractIncomingToAction;
import org.eclipse.team.svn.ui.synchronize.action.ExtractOutgoingToAction;
import org.eclipse.team.svn.ui.synchronize.action.ExtractToAction;
import org.eclipse.team.svn.ui.synchronize.action.RevertAction;
import org.eclipse.team.svn.ui.synchronize.action.SetKeywordsAction;
import org.eclipse.team.svn.ui.synchronize.action.SetPropertyAction;
import org.eclipse.team.svn.ui.synchronize.action.ShowHistoryAction;
import org.eclipse.team.svn.ui.synchronize.action.ShowIncomingAnnotationAction;
import org.eclipse.team.svn.ui.synchronize.action.ShowIncomingPropertiesAction;
import org.eclipse.team.svn.ui.synchronize.action.ShowOutgoingAnnotationAction;
import org.eclipse.team.svn.ui.synchronize.action.ShowOutgoingPropertiesAction;
import org.eclipse.team.svn.ui.synchronize.update.action.AddToSVNAction;
import org.eclipse.team.svn.ui.synchronize.update.action.AddToSVNIgnoreAction;
import org.eclipse.team.svn.ui.synchronize.update.action.CommitAction;
import org.eclipse.team.svn.ui.synchronize.update.action.LockAction;
import org.eclipse.team.svn.ui.synchronize.update.action.MarkAsMergedAction;
import org.eclipse.team.svn.ui.synchronize.update.action.OverrideAndCommitAction;
import org.eclipse.team.svn.ui.synchronize.update.action.OverrideAndUpdateAction;
import org.eclipse.team.svn.ui.synchronize.update.action.UnlockAction;
import org.eclipse.team.svn.ui.synchronize.update.action.UpdateAction;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Synchronize view update action set
 * 
 * @author Alexander Gurov
 */
public class UpdateActionGroup extends AbstractSynchronizeActionGroup {
	public static final String GROUP_SYNC_NORMAL = "syncIncomingOutgoing"; //$NON-NLS-1$
	public static final String GROUP_SYNC_CONFLICTS = "syncConflicting"; //$NON-NLS-1$
	
	public void configureMenuGroups(ISynchronizePageConfiguration configuration) {
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_SYNC_NORMAL);
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_SYNC_CONFLICTS);
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				AbstractSynchronizeActionGroup.GROUP_MANAGE_LOCALS);
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_TEAM);
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_TOOLBAR_MENU, 
				AbstractSynchronizeActionGroup.GROUP_PROCESS_ALL);
	}
	
	protected void configureActions(ISynchronizePageConfiguration configuration) {
		UpdateAction updateAllAction = new UpdateAction(SVNUIMessages.SynchronizeActionGroup_UpdateAllIncomingChanges, configuration, this.getVisibleRootsSelectionProvider());
		updateAllAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/update.gif")); //$NON-NLS-1$
		this.appendToGroup(
				ISynchronizePageConfiguration.P_TOOLBAR_MENU, 
				UpdateActionGroup.GROUP_PROCESS_ALL,
				updateAllAction);
		CommitAction commitAllAction = new CommitAction(SVNUIMessages.UpdateActionGroup_CommitAllOutgoingChanges, configuration, this.getVisibleRootsSelectionProvider());
		commitAllAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/commit.gif")); //$NON-NLS-1$
		this.appendToGroup(
				ISynchronizePageConfiguration.P_TOOLBAR_MENU, 
				UpdateActionGroup.GROUP_PROCESS_ALL,
				commitAllAction);
		ExpandAllAction expandAllAction = new ExpandAllAction(SVNUIMessages.SynchronizeActionGroup_ExpandAll, configuration, this.getVisibleRootsSelectionProvider());
		expandAllAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/expandall.gif")); //$NON-NLS-1$
		this.appendToGroup(
				ISynchronizePageConfiguration.P_TOOLBAR_MENU, 
				ISynchronizePageConfiguration.MODE_GROUP,
				expandAllAction);
		
		CommitAction commitAction = new CommitAction(SVNUIMessages.UpdateActionGroup_Commit, configuration);
		commitAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/commit.gif")); //$NON-NLS-1$
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_SYNC_NORMAL,
				commitAction);

		UpdateAction updateAction = new UpdateAction(SVNUIMessages.SynchronizeActionGroup_Update, configuration);
		updateAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/update.gif")); //$NON-NLS-1$
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_SYNC_NORMAL,
				updateAction);
		
		OverrideAndCommitAction overrideCommitAction = new OverrideAndCommitAction(SVNUIMessages.UpdateActionGroup_OverrideAndCommit, configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_SYNC_CONFLICTS,
				overrideCommitAction);
		OverrideAndUpdateAction overrideUpdateAction = new OverrideAndUpdateAction(SVNUIMessages.SynchronizeActionGroup_OverrideAndUpdate, configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_SYNC_CONFLICTS,
				overrideUpdateAction);
		MarkAsMergedAction markMergedAction = new MarkAsMergedAction(SVNUIMessages.SynchronizeActionGroup_MarkAsMerged, configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_SYNC_CONFLICTS,
				markMergedAction);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_SYNC_CONFLICTS,
				new Separator());
		EditConflictsAction editConflictsAction = new EditConflictsAction(SVNUIMessages.UpdateActionGroup_EditConflicts, configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_SYNC_CONFLICTS,
				editConflictsAction);
		ComparePropertiesAction comparePropsAction = new ComparePropertiesAction(SVNUIMessages.SynchronizeActionGroup_CompareProperties, configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_SYNC_CONFLICTS,
				comparePropsAction);
		
		RevertAction revertAction = new RevertAction(SVNUIMessages.SynchronizeActionGroup_Revert, configuration);
		revertAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/revert.gif")); //$NON-NLS-1$
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_MANAGE_LOCALS,
				revertAction);
		ShowHistoryAction showHistoryAction = new ShowHistoryAction(SVNUIMessages.SynchronizeActionGroup_ShowResourceHistory, configuration);
		showHistoryAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/showhistory.gif")); //$NON-NLS-1$
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_MANAGE_LOCALS,
				showHistoryAction);
		AddToSVNAction addToSVNAction = new AddToSVNAction(SVNUIMessages.UpdateActionGroup_AddToVersionControl, configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_MANAGE_LOCALS,
				addToSVNAction);
		AddToSVNIgnoreAction addToSVNIgnoreAction = new AddToSVNIgnoreAction(SVNUIMessages.UpdateActionGroup_AddToIgnore, configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_MANAGE_LOCALS,
				addToSVNIgnoreAction);
		ExtractToAction extractTo = new ExtractToAction(SVNUIMessages.ExtractAllToAction_Label, configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateActionGroup.GROUP_MANAGE_LOCALS,
				extractTo);
		
		this.addSpecificActions(extractTo, configuration);
	}
	
	protected void addLocalActions(IMenuManager manager, ISynchronizePageConfiguration configuration) {
		ShowOutgoingPropertiesAction showPropertiesAction = new ShowOutgoingPropertiesAction(SVNUIMessages.ShowPropertiesAction_label, configuration);
		showPropertiesAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/propertiesedit.gif")); //$NON-NLS-1$
		manager.add(showPropertiesAction);
		SetPropertyAction setPropAction = new SetPropertyAction(SVNUIMessages.SynchronizeActionGroup_SetProperty, configuration);
		manager.add(setPropAction);
		SetKeywordsAction setKeywordsAction = new SetKeywordsAction(SVNUIMessages.SynchronizeActionGroup_SetKeywords, configuration);
		manager.add(setKeywordsAction);
		ShowOutgoingAnnotationAction showAnnotationAction = new ShowOutgoingAnnotationAction(SVNUIMessages.ShowAnnotationCommand_label, configuration);
		manager.add(showAnnotationAction);
		
		manager.add(new Separator());
		
		LockAction lockAction = new LockAction(SVNUIMessages.UpdateActionGroup_Lock, configuration);
		lockAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/lock.gif")); //$NON-NLS-1$
		manager.add(lockAction);
		UnlockAction unlockAction = new UnlockAction(SVNUIMessages.UpdateActionGroup_Unlock, configuration);
		unlockAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/unlock.gif")); //$NON-NLS-1$
		manager.add(unlockAction);
		
		manager.add(new Separator());
		
		CreatePatchFileAction patchAction = new CreatePatchFileAction(SVNUIMessages.CreatePatchCommand_label, configuration);
		manager.add(patchAction);
		CreateBranchAction branchAction = new CreateBranchAction(SVNUIMessages.SynchronizeActionGroup_Branch, configuration);
		branchAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/branch.gif")); //$NON-NLS-1$
		manager.add(branchAction);
		ExtractOutgoingToAction extractActionOutgoing = new ExtractOutgoingToAction(SVNUIMessages.ExtractToAction_Label, configuration);
		manager.add(extractActionOutgoing);
		
		manager.add(new Separator());
		
		CleanUpAction cleanUpAction = new CleanUpAction(SVNUIMessages.SynchronizeActionGroup_Cleanup, configuration);
		manager.add(cleanUpAction);
	}
	
	protected void addRemoteActions(IMenuManager manager, ISynchronizePageConfiguration configuration) {
		ShowIncomingPropertiesAction showIncomingPropertiesAction = new ShowIncomingPropertiesAction(SVNUIMessages.ShowPropertiesAction_label, configuration);
		showIncomingPropertiesAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/propertiesedit.gif")); //$NON-NLS-1$
		manager.add(showIncomingPropertiesAction);
		ShowIncomingAnnotationAction showIncomingAnnotationAction = new ShowIncomingAnnotationAction(SVNUIMessages.ShowAnnotationAction_label, configuration);
		manager.add(showIncomingAnnotationAction);
		
		manager.add(new Separator());
		
		ExtractIncomingToAction extractActionIncoming = new ExtractIncomingToAction(SVNUIMessages.ExtractToAction_Label, configuration);
		manager.add(extractActionIncoming);
	}
	
}
