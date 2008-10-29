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

package org.eclipse.team.svn.ui.mapping;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.core.mapping.ISynchronizationScopeManager;
import org.eclipse.team.core.mapping.provider.MergeContext;
import org.eclipse.team.core.mapping.provider.SynchronizationContext;
import org.eclipse.team.internal.ui.synchronize.ChangeSetCapability;
import org.eclipse.team.internal.ui.synchronize.IChangeSetProvider;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.local.UpdateAction;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.extension.impl.synchronize.UpdateActionGroup;
import org.eclipse.team.svn.ui.mapping.UpdateSubscriberContext.ChangeSetSubscriberScopeManager;
import org.eclipse.team.svn.ui.operation.UILoggedOperation;
import org.eclipse.team.svn.ui.synchronize.AbstractSynchronizeActionGroup;
import org.eclipse.team.svn.ui.synchronize.AbstractSynchronizeModelActionGroup;
import org.eclipse.team.svn.ui.synchronize.action.ComparePropertiesAction;
import org.eclipse.team.svn.ui.synchronize.action.CreateBranchAction;
import org.eclipse.team.svn.ui.synchronize.action.CreatePatchFileAction;
import org.eclipse.team.svn.ui.synchronize.action.EditConflictsAction;
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
import org.eclipse.team.svn.ui.synchronize.update.action.AddToSVNAction;
import org.eclipse.team.svn.ui.synchronize.update.action.AddToSVNIgnoreAction;
import org.eclipse.team.svn.ui.synchronize.update.action.CommitAction;
import org.eclipse.team.svn.ui.synchronize.update.action.LockAction;
import org.eclipse.team.svn.ui.synchronize.update.action.OverrideAndCommitAction;
import org.eclipse.team.svn.ui.synchronize.update.action.UnlockAction;
import org.eclipse.team.svn.ui.synchronize.update.action.logicalmodel.AddToSVNIgnoreModelAction;
import org.eclipse.team.svn.ui.synchronize.update.action.logicalmodel.AddToSVNModelAction;
import org.eclipse.team.svn.ui.synchronize.update.action.logicalmodel.CommitModelAction;
import org.eclipse.team.svn.ui.synchronize.update.action.logicalmodel.LockModelAction;
import org.eclipse.team.svn.ui.synchronize.update.action.logicalmodel.OverrideAndCommitModelAction;
import org.eclipse.team.svn.ui.synchronize.update.action.logicalmodel.UnlockModelAction;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.mapping.SynchronizationActionProvider;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ModelSynchronizeParticipantActionGroup;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;

public class UpdateModelParticipant extends AbstractSVNModelParticipant implements IChangeSetProvider {

	private static final String CTX_CONSULT_CHANGE_SETS = "consultChangeSets"; //$NON-NLS-1$
	
	protected ChangeSetCapability capability;
	
	protected boolean isConsultChangeSets;
	
	public UpdateModelParticipant() {
		super();
	}

	public UpdateModelParticipant(SynchronizationContext context) {
		super(context);
		try {
			setInitializationData(TeamUI.getSynchronizeManager().getParticipantDescriptor("org.eclipse.team.svn.ui.synchronize.update.SynchronizeModelParticipant")); //$NON-NLS-1$
		} catch (CoreException e) {			
			UILoggedOperation.reportError(this.getClass().getName(), e);
		}
		setSecondaryId(Long.toString(System.currentTimeMillis()));
		this.isConsultChangeSets = isConsultChangeSets(context.getScopeManager());
	}
	
	protected void initializeConfiguration(ISynchronizePageConfiguration configuration) {
		configuration.setProperty(ISynchronizePageConfiguration.P_VIEWER_ID, "org.eclipse.team.svn.ui.workspaceSynchronization");
		super.initializeConfiguration(configuration);
	}
	
	protected Collection<AbstractSynchronizeActionGroup> getActionGroups() {
		return new ArrayList<AbstractSynchronizeActionGroup>();// ExtensionsManager.getInstance().getCurrentSynchronizeActionContributor().getUpdateContributions();
	}

    protected int getSupportedModes() {
        return ISynchronizePageConfiguration.ALL_MODES;
    }

    protected int getDefaultMode() {
        return ISynchronizePageConfiguration.BOTH_MODE;
    }

	public ChangeSetCapability getChangeSetCapability() {
		if (this.capability == null) {
			this.capability = new SVNModelParticipantChangeSetCapability();
        }
        return this.capability;
	}
	
	protected MergeContext restoreContext(ISynchronizationScopeManager manager) {		
		return UpdateSubscriberContext.createContext(manager, ISynchronizationContext.THREE_WAY);
	}
	
	protected ISynchronizationScopeManager createScopeManager(ResourceMapping[] mappings) {
		return UpdateSubscriberContext.createWorkspaceScopeManager(mappings, true, this.isConsultChangeSets);
	}
	
	public void saveState(IMemento memento) {
		super.saveState(memento);
	   	memento.putString(CTX_CONSULT_CHANGE_SETS, Boolean.toString(this.isConsultChangeSets));
	}
	
    public void init(String secondaryId, IMemento memento) throws PartInitException {
    	try {
    		String consult = memento.getString(CTX_CONSULT_CHANGE_SETS);
    		if (consult != null)
    			this.isConsultChangeSets = Boolean.valueOf(consult).booleanValue();
    	} finally {
    		super.init(secondaryId, memento);
    	}
    }
	    
	protected boolean isConsultChangeSets(ISynchronizationScopeManager manager) {
		if (manager instanceof ChangeSetSubscriberScopeManager) {
			ChangeSetSubscriberScopeManager man = (ChangeSetSubscriberScopeManager) manager;
			return man.isConsultSets();
		}
		return false;
	}
	
	protected ModelSynchronizeParticipantActionGroup createMergeActionGroup() {
		return new UpdateModelActionGroup();		
	}
	
	/**
	 * Synchronize view logical mode update action set
	 * 
	 * @author Igor Burilo
	 * 
	 * TODO
	 * Externalize class.
	 * Don't use any references, i.e. constants from UpdateActionGroup class 
	 */
	public class UpdateModelActionGroup extends AbstractSynchronizeModelActionGroup {
		
		public static final String GROUP_SYNC_NORMAL = "modelSyncIncomingOutgoing";
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
			/*
			 * 
			 * TODO Correctly implement. See WorkspaceCommitAction
			 * 
			//commit all
			CommitModelAction commitAllAction = new CommitModelAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.CommitAllOutgoingChanges"), configuration, this.getVisibleRootsSelectionProvider());
			commitAllAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/commit.gif"));
			this.appendToGroup(
					ISynchronizePageConfiguration.P_TOOLBAR_MENU, 
					ModelSynchronizeParticipantActionGroup.MERGE_ACTION_GROUP,
					commitAllAction);
			*/
			
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
					UpdateModelActionGroup.GROUP_SYNC_CONFLICTS,
					editConflictsAction);
			
			//compare properties
			ComparePropertiesModelAction comparePropsAction = new ComparePropertiesModelAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.CompareProperties"), configuration);
			this.appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					UpdateModelActionGroup.GROUP_SYNC_CONFLICTS,
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
			
			//TODO add actions here
			
			this.addSpecificActions(extractTo, configuration);
		}	
		
		protected void addLocalActions(IMenuManager manager, ISynchronizePageConfiguration configuration) {
			//TODO add local actions here
			
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
			
			//create patch
			CreatePatchFileModelAction patchAction = new CreatePatchFileModelAction(SVNTeamUIPlugin.instance().getResource("CreatePatchCommand.label"), configuration);
			manager.add(patchAction);
			
			/* TODO incorrectly works
			//create branch
			CreateBranchModelAction branchAction = new CreateBranchModelAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.Branch"), configuration);
			branchAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/branch.gif"));
			manager.add(branchAction);
			*/
			//extract
			ExtractOutgoingToModelAction extractActionOutgoing = new ExtractOutgoingToModelAction(SVNTeamUIPlugin.instance().getResource("ExtractToAction.Label"), configuration);
			manager.add(extractActionOutgoing);
			
			manager.add(new Separator());
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
}
