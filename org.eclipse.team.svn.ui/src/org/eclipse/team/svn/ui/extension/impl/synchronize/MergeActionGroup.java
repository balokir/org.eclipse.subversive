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
import org.eclipse.team.svn.ui.synchronize.action.CreateBranchAction;
import org.eclipse.team.svn.ui.synchronize.action.CreatePatchFileAction;
import org.eclipse.team.svn.ui.synchronize.action.EditConflictsAction;
import org.eclipse.team.svn.ui.synchronize.action.EditTreeConflictsAction;
import org.eclipse.team.svn.ui.synchronize.action.ExpandAllAction;
import org.eclipse.team.svn.ui.synchronize.action.MergePropertiesAction;
import org.eclipse.team.svn.ui.synchronize.action.OpenInExternalCompareEditorAction;
import org.eclipse.team.svn.ui.synchronize.action.RevertAction;
import org.eclipse.team.svn.ui.synchronize.action.SetExternalDefinitionAction;
import org.eclipse.team.svn.ui.synchronize.action.SetKeywordsAction;
import org.eclipse.team.svn.ui.synchronize.action.SetPropertyAction;
import org.eclipse.team.svn.ui.synchronize.action.ShowHistoryAction;
import org.eclipse.team.svn.ui.synchronize.action.ShowOutgoingAnnotationAction;
import org.eclipse.team.svn.ui.synchronize.action.ShowOutgoingPropertiesAction;
import org.eclipse.team.svn.ui.synchronize.merge.action.MarkAsMergedAction;
import org.eclipse.team.svn.ui.synchronize.merge.action.OverrideAndUpdateAction;
import org.eclipse.team.svn.ui.synchronize.merge.action.ShowIncomingAnnotationAction;
import org.eclipse.team.svn.ui.synchronize.merge.action.ShowIncomingPropertiesAction;
import org.eclipse.team.svn.ui.synchronize.merge.action.UpdateAction;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Merge action group
 * 
 * @author Alexander Gurov
 */
public class MergeActionGroup extends AbstractSynchronizeActionGroup {
	public static final String GROUP_MERGE_CHANGES = "mergeChanges"; //$NON-NLS-1$
	
	public void configureMenuGroups(ISynchronizePageConfiguration configuration) {
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_MERGE_CHANGES);
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				AbstractSynchronizeActionGroup.GROUP_MANAGE_LOCALS);
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				AbstractSynchronizeActionGroup.GROUP_TEAM);
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_TOOLBAR_MENU, 
				AbstractSynchronizeActionGroup.GROUP_PROCESS_ALL);
	}
	
	public void configureActions(ISynchronizePageConfiguration configuration) {
		OpenInExternalCompareEditorAction externalCompareAction = new OpenInExternalCompareEditorAction(SVNUIMessages.OpenInExternalCompareEditor_Action, configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				ISynchronizePageConfiguration.FILE_GROUP,
				externalCompareAction);
		
		UpdateAction updateAllAction = new UpdateAction(SVNUIMessages.SynchronizeActionGroup_AcceptAllIncomingChanges, configuration, this.getVisibleRootsSelectionProvider());
		updateAllAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/update.gif")); //$NON-NLS-1$
		this.appendToGroup(
				ISynchronizePageConfiguration.P_TOOLBAR_MENU, 
				MergeActionGroup.GROUP_PROCESS_ALL,
				updateAllAction);
		ExpandAllAction expandAllAction = new ExpandAllAction(SVNUIMessages.SynchronizeActionGroup_ExpandAll, configuration, this.getVisibleRootsSelectionProvider());
		expandAllAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/expandall.gif")); //$NON-NLS-1$
		this.appendToGroup(
				ISynchronizePageConfiguration.P_TOOLBAR_MENU, 
				ISynchronizePageConfiguration.MODE_GROUP,
				expandAllAction);

		UpdateAction updateAction = new UpdateAction(SVNUIMessages.SynchronizeActionGroup_Accept, configuration);
		updateAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/update.gif")); //$NON-NLS-1$
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_MERGE_CHANGES,
				updateAction);
		OverrideAndUpdateAction overrideAndUpdateAction = new OverrideAndUpdateAction(SVNUIMessages.MergeActionGroup_OverrideAndUpdate, configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_MERGE_CHANGES,
				overrideAndUpdateAction);
		EditConflictsAction editConflictsAction = new EditConflictsAction(SVNUIMessages.UpdateActionGroup_EditConflicts, configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_MERGE_CHANGES,
				editConflictsAction);
		EditTreeConflictsAction editTreeConflictsAction = new EditTreeConflictsAction(SVNUIMessages.UpdateActionGroup_EditTreeConflicts, configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_MERGE_CHANGES,
				editTreeConflictsAction);
		MergePropertiesAction mergePropertiesAction = new MergePropertiesAction(SVNUIMessages.SynchronizeActionGroup_MergeProperties, configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_MERGE_CHANGES,
				mergePropertiesAction);
		MarkAsMergedAction markAsMergedAction = new MarkAsMergedAction(SVNUIMessages.SynchronizeActionGroup_MarkAsMerged, configuration);
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_MERGE_CHANGES,
				markAsMergedAction);

		RevertAction revertAction = new RevertAction(SVNUIMessages.SynchronizeActionGroup_Revert, configuration);
		revertAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/revert.gif")); //$NON-NLS-1$
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_MANAGE_LOCALS,
				revertAction);
		ShowHistoryAction showHistoryAction = new ShowHistoryAction(SVNUIMessages.SynchronizeActionGroup_ShowResourceHistory, configuration);
		showHistoryAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/showhistory.gif")); //$NON-NLS-1$
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				MergeActionGroup.GROUP_MANAGE_LOCALS,
				showHistoryAction);

		this.addSpecificActions(showHistoryAction, configuration);
	}
	
	protected void addLocalActions(IMenuManager manager, ISynchronizePageConfiguration configuration) {
		ShowOutgoingPropertiesAction showPropertiesAction = new ShowOutgoingPropertiesAction(SVNUIMessages.ShowPropertiesAction_label, configuration);
		showPropertiesAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/propertiesedit.gif")); //$NON-NLS-1$
		manager.add(showPropertiesAction);
		SetPropertyAction setPropAction = new SetPropertyAction(SVNUIMessages.SynchronizeActionGroup_SetProperty, configuration);
		manager.add(setPropAction);
		
		SetKeywordsAction setKeywordsAction = new SetKeywordsAction(SVNUIMessages.SynchronizeActionGroup_SetKeywords, configuration);
		manager.add(setKeywordsAction);
		
		SetExternalDefinitionAction setExternalsAction = new SetExternalDefinitionAction(SVNUIMessages.Action_SetExternals, configuration);
		manager.add(setExternalsAction);
		
		ShowOutgoingAnnotationAction showAnnotationAction = new ShowOutgoingAnnotationAction(SVNUIMessages.ShowAnnotationCommand_label, configuration);
		manager.add(showAnnotationAction);
		
		manager.add(new Separator());
		
		CreatePatchFileAction patchAction = new CreatePatchFileAction(SVNUIMessages.CreatePatchCommand_label, configuration);
		manager.add(patchAction);
		CreateBranchAction branchAction = new CreateBranchAction(SVNUIMessages.SynchronizeActionGroup_Branch, configuration);
		branchAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/branch.gif")); //$NON-NLS-1$
		manager.add(branchAction);
		
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
	}
	
}
