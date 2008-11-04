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

package org.eclipse.team.svn.ui.synchronize;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.synchronize.action.logicalmodel.CleanUpModelAction;
import org.eclipse.team.svn.ui.synchronize.action.logicalmodel.ComparePropertiesModelAction;
import org.eclipse.team.svn.ui.synchronize.action.logicalmodel.CreateBranchModelAction;
import org.eclipse.team.svn.ui.synchronize.action.logicalmodel.CreatePatchFileModelAction;
import org.eclipse.team.svn.ui.synchronize.action.logicalmodel.EditConflictsModelAction;
import org.eclipse.team.svn.ui.synchronize.action.logicalmodel.ExtractIncomingToModelAction;
import org.eclipse.team.svn.ui.synchronize.action.logicalmodel.ExtractOutgoingToModelAction;
import org.eclipse.team.svn.ui.synchronize.action.logicalmodel.ExtractToModelAction;
import org.eclipse.team.svn.ui.synchronize.action.logicalmodel.RevertModelAction;
import org.eclipse.team.svn.ui.synchronize.action.logicalmodel.SetKeywordsModelAction;
import org.eclipse.team.svn.ui.synchronize.action.logicalmodel.SetPropertyModelAction;
import org.eclipse.team.svn.ui.synchronize.action.logicalmodel.ShowHistoryModelAction;
import org.eclipse.team.svn.ui.synchronize.action.logicalmodel.ShowIncomingAnnotationModelAction;
import org.eclipse.team.svn.ui.synchronize.action.logicalmodel.ShowIncomingPropertiesModelAction;
import org.eclipse.team.svn.ui.synchronize.action.logicalmodel.ShowOutgoingAnnotationModelAction;
import org.eclipse.team.svn.ui.synchronize.action.logicalmodel.ShowOutgoingPropertiesModelAction;
import org.eclipse.team.svn.ui.synchronize.update.action.logicalmodel.AddToSVNIgnoreModelAction;
import org.eclipse.team.svn.ui.synchronize.update.action.logicalmodel.AddToSVNModelAction;
import org.eclipse.team.svn.ui.synchronize.update.action.logicalmodel.CommitAllModelAction;
import org.eclipse.team.svn.ui.synchronize.update.action.logicalmodel.CommitModelAction;
import org.eclipse.team.svn.ui.synchronize.update.action.logicalmodel.ExpandAllModelAction;
import org.eclipse.team.svn.ui.synchronize.update.action.logicalmodel.LockModelAction;
import org.eclipse.team.svn.ui.synchronize.update.action.logicalmodel.OverrideAndCommitModelAction;
import org.eclipse.team.svn.ui.synchronize.update.action.logicalmodel.UnlockModelAction;
import org.eclipse.team.ui.mapping.SynchronizationActionProvider;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ModelSynchronizeParticipantActionGroup;

/**
 * Synchronize view logical mode update action set
 * 
 * @author Igor Burilo 
 */
public class UpdateModelActionGroup extends AbstractSynchronizeModelActionGroup {

	public static final String GROUP_SYNC_NORMAL = "modelSyncIncomingOutgoing";
	public static final String SVN_SYNC_CONFLICTS = "svnSyncConflicting";
	public static final String GROUP_SYNC_CONFLICTS = "modelSyncConflicting";
	
	protected void configureMergeAction(String mergeActionId, Action action) {			
		if (mergeActionId == SynchronizationActionProvider.MERGE_ACTION_ID) {
			action.setText(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.Update"));
			action.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("/icons/common/actions/update.gif"));
		} else if (mergeActionId == SynchronizationActionProvider.OVERWRITE_ACTION_ID) {
			action.setText(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.OverrideAndUpdate"));
		} else if (mergeActionId == SynchronizationActionProvider.MARK_AS_MERGE_ACTION_ID) {
			action.setText(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.MarkAsMerged"));
		} else if (mergeActionId == MERGE_ALL_ACTION_ID) {
			action.setText(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.UpdateAllIncomingChanges"));
			action.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("/icons/common/actions/update.gif"));
		} else {
			super.configureMergeAction(mergeActionId, action);
		}
	}
	
	protected void addToContextMenu(String mergeActionId, Action action, IMenuManager manager) {
		IContributionItem group = null;;
		if (mergeActionId == SynchronizationActionProvider.MERGE_ACTION_ID) {
			group = manager.find(UpdateModelActionGroup.GROUP_SYNC_NORMAL);
		} else if (mergeActionId == SynchronizationActionProvider.OVERWRITE_ACTION_ID) {
			group = manager.find(UpdateModelActionGroup.GROUP_SYNC_CONFLICTS);
		} else if (mergeActionId == SynchronizationActionProvider.MARK_AS_MERGE_ACTION_ID) {
			group = manager.find(UpdateModelActionGroup.GROUP_SYNC_CONFLICTS);
		} else {
			super.addToContextMenu(mergeActionId, action, manager);
			return;
		}
		if (group != null) {
			manager.appendToGroup(group.getId(), action);
		} else {
			manager.add(action);
		}
	}		
	
	protected void configureActions(ISynchronizePageConfiguration configuration) {
		//commit all
		CommitAllModelAction commitAllAction = new CommitAllModelAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.CommitAllOutgoingChanges"), configuration);
		commitAllAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/commit.gif"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_TOOLBAR_MENU, 
				ModelSynchronizeParticipantActionGroup.MERGE_ACTION_GROUP,
				commitAllAction);			
		
		//expand all
		ExpandAllModelAction expandAllAction = new ExpandAllModelAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.ExpandAll"), configuration);
		expandAllAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/expandall.gif"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_TOOLBAR_MENU, 
				ISynchronizePageConfiguration.MODE_GROUP,
				expandAllAction);
		
		//commit
		CommitModelAction commitAction = new CommitModelAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.Commit"), configuration);
		commitAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/commit.gif"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateModelActionGroup.GROUP_SYNC_NORMAL,
				commitAction);
		
		//override and commit			
		OverrideAndCommitModelAction overrideCommitAction = new OverrideAndCommitModelAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.OverrideAndCommit"), configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateModelActionGroup.GROUP_SYNC_CONFLICTS,
				overrideCommitAction);
		
		
		//edit conflicts
		EditConflictsModelAction editConflictsAction = new EditConflictsModelAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.EditConflicts"), configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateModelActionGroup.SVN_SYNC_CONFLICTS,
				editConflictsAction);
		
		//compare properties
		ComparePropertiesModelAction comparePropsAction = new ComparePropertiesModelAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.CompareProperties"), configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateModelActionGroup.SVN_SYNC_CONFLICTS,
				comparePropsAction);
		
		//revert
		RevertModelAction revertAction = new RevertModelAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.Revert"), configuration);
		revertAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/revert.gif"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateModelActionGroup.GROUP_MANAGE_LOCALS,
				revertAction);
		
		//show history
		ShowHistoryModelAction showHistoryAction = new ShowHistoryModelAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.ShowResourceHistory"), configuration);
		showHistoryAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/showhistory.gif"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateModelActionGroup.GROUP_MANAGE_LOCALS,
				showHistoryAction);	
		
		//add to SVN
		AddToSVNModelAction addToSVNAction = new AddToSVNModelAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.AddToVersionControl"), configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateModelActionGroup.GROUP_MANAGE_LOCALS,
				addToSVNAction);
		
		//add to SVN ignore
		AddToSVNIgnoreModelAction addToSVNIgnoreAction = new AddToSVNIgnoreModelAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.AddToIgnore"), configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateModelActionGroup.GROUP_MANAGE_LOCALS,
				addToSVNIgnoreAction);
		
		//extract to
		ExtractToModelAction extractTo = new ExtractToModelAction(SVNTeamUIPlugin.instance().getResource("ExtractAllToAction.Label"), configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				UpdateModelActionGroup.GROUP_MANAGE_LOCALS,
				extractTo);					
		
		this.addSpecificActions(extractTo, configuration);
	}	
	
	protected void addLocalActions(IMenuManager manager, ISynchronizePageConfiguration configuration) {				
		//show properties
		ShowOutgoingPropertiesModelAction showPropertiesAction = new ShowOutgoingPropertiesModelAction(SVNTeamUIPlugin.instance().getResource("ShowPropertiesAction.label"), configuration);
		showPropertiesAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/propertiesedit.gif"));
		manager.add(showPropertiesAction);						
		
		//set property
		SetPropertyModelAction setPropAction = new SetPropertyModelAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.SetProperty"), configuration);
		manager.add(setPropAction);
		
		//set keywords
		SetKeywordsModelAction setKeywordsAction = new SetKeywordsModelAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.SetKeywords"), configuration);
		manager.add(setKeywordsAction);
		
		//show annotation
		ShowOutgoingAnnotationModelAction showAnnotationAction = new ShowOutgoingAnnotationModelAction(SVNTeamUIPlugin.instance().getResource("ShowAnnotationCommand.label"), configuration);
		manager.add(showAnnotationAction);
		
		manager.add(new Separator());
		
		//lock
		LockModelAction lockAction = new LockModelAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.Lock"), configuration);
		lockAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/lock.gif"));
		manager.add(lockAction);
		
		//unlock
		UnlockModelAction unlockAction = new UnlockModelAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.Unlock"), configuration);
		unlockAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/unlock.gif"));
		manager.add(unlockAction);
		
		manager.add(new Separator());
		
		//create patch
		CreatePatchFileModelAction patchAction = new CreatePatchFileModelAction(SVNTeamUIPlugin.instance().getResource("CreatePatchCommand.label"), configuration);
		manager.add(patchAction);
					
		//create branch
		CreateBranchModelAction branchAction = new CreateBranchModelAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.Branch"), configuration);
		branchAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/branch.gif"));
		manager.add(branchAction);
		
		//extract
		ExtractOutgoingToModelAction extractActionOutgoing = new ExtractOutgoingToModelAction(SVNTeamUIPlugin.instance().getResource("ExtractToAction.Label"), configuration);
		manager.add(extractActionOutgoing);
		
		manager.add(new Separator());
		
		//cleanup
		CleanUpModelAction cleanUpAction = new CleanUpModelAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.Cleanup"), configuration);
		manager.add(cleanUpAction);
	}
	
	protected void addRemoteActions(IMenuManager manager, ISynchronizePageConfiguration configuration) {			
		//show properties
		ShowIncomingPropertiesModelAction showIncomingPropertiesAction = new ShowIncomingPropertiesModelAction(SVNTeamUIPlugin.instance().getResource("ShowPropertiesAction.label"), configuration);
		showIncomingPropertiesAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/propertiesedit.gif"));
		manager.add(showIncomingPropertiesAction);
		
		//show annotation
		ShowIncomingAnnotationModelAction showIncomingAnnotationAction = new ShowIncomingAnnotationModelAction(SVNTeamUIPlugin.instance().getResource("ShowAnnotationAction.label"), configuration);
		manager.add(showIncomingAnnotationAction);
					
		manager.add(new Separator());
		
		//extract
		ExtractIncomingToModelAction extractActionIncoming = new ExtractIncomingToModelAction(SVNTeamUIPlugin.instance().getResource("ExtractToAction.Label"), configuration);
		manager.add(extractActionIncoming);
	}
}
