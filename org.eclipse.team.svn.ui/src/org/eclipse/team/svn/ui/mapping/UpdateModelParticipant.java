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
import org.eclipse.team.svn.ui.synchronize.action.EditConflictsAction;
import org.eclipse.team.svn.ui.synchronize.action.ExtractOutgoingToAction;
import org.eclipse.team.svn.ui.synchronize.action.ExtractToAction;
import org.eclipse.team.svn.ui.synchronize.action.RevertAction;
import org.eclipse.team.svn.ui.synchronize.action.ShowHistoryAction;
import org.eclipse.team.svn.ui.synchronize.action.logicalmodel.EditConflictsModelAction;
import org.eclipse.team.svn.ui.synchronize.action.logicalmodel.ExtractOutgoingToModelAction;
import org.eclipse.team.svn.ui.synchronize.action.logicalmodel.ExtractToModelAction;
import org.eclipse.team.svn.ui.synchronize.action.logicalmodel.RevertModelAction;
import org.eclipse.team.svn.ui.synchronize.action.logicalmodel.ShowHistoryModelAction;
import org.eclipse.team.svn.ui.synchronize.update.action.AddToSVNAction;
import org.eclipse.team.svn.ui.synchronize.update.action.AddToSVNIgnoreAction;
import org.eclipse.team.svn.ui.synchronize.update.action.logicalmodel.AddToSVNIgnoreModelAction;
import org.eclipse.team.svn.ui.synchronize.update.action.logicalmodel.AddToSVNModelAction;
import org.eclipse.team.svn.ui.synchronize.update.action.logicalmodel.CommitModelAction;
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
			//commit
			CommitModelAction commitAction = new CommitModelAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.Commit"), configuration);
			commitAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/commit.gif"));
			this.appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					UpdateModelActionGroup.GROUP_SYNC_NORMAL,
					commitAction);
			
			//edit conflicts
			EditConflictsModelAction editConflictsAction = new EditConflictsModelAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.EditConflicts"), configuration);
			this.appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					UpdateModelActionGroup.GROUP_SYNC_CONFLICTS,
					editConflictsAction);
			
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
			
			ExtractOutgoingToModelAction extractActionOutgoing = new ExtractOutgoingToModelAction(SVNTeamUIPlugin.instance().getResource("ExtractToAction.Label"), configuration);
			manager.add(extractActionOutgoing);
			
			manager.add(new Separator());
		}
		
		protected void addRemoteActions(IMenuManager manager, ISynchronizePageConfiguration configuration) {
			//TODO add remote actions here
		}
	}
}
