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

package org.eclipse.team.svn.ui.panel.participant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.panel.local.CommitPanel;
import org.eclipse.team.svn.ui.synchronize.AbstractSynchronizeActionGroup;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeModelAction;
import org.eclipse.team.svn.ui.synchronize.action.CleanUpAction;
import org.eclipse.team.svn.ui.synchronize.action.CompareWithLatestRevisionPaneAction;
import org.eclipse.team.svn.ui.synchronize.action.CompareWithRevisionPaneAction;
import org.eclipse.team.svn.ui.synchronize.action.CompareWithWorkingCopyPaneAction;
import org.eclipse.team.svn.ui.synchronize.action.CreateBranchAction;
import org.eclipse.team.svn.ui.synchronize.action.CreatePatchFileAction;
import org.eclipse.team.svn.ui.synchronize.action.DeletePaneAction;
import org.eclipse.team.svn.ui.synchronize.action.EditConflictsAction;
import org.eclipse.team.svn.ui.synchronize.action.ExtractOutgoingToAction;
import org.eclipse.team.svn.ui.synchronize.action.OpenInComparePaneAction;
import org.eclipse.team.svn.ui.synchronize.action.ReplaceWithLatestRevisionPaneAction;
import org.eclipse.team.svn.ui.synchronize.action.ReplaceWithRevisionPaneAction;
import org.eclipse.team.svn.ui.synchronize.action.RevertAction;
import org.eclipse.team.svn.ui.synchronize.update.action.LockAction;
import org.eclipse.team.svn.ui.synchronize.update.action.MarkAsMergedAction;
import org.eclipse.team.svn.ui.synchronize.update.action.UnlockAction;

import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizeScope;

/**
 * Commit pane participant
 * 
 * @author Igor Burilo
 */
public class CommitPaneParticipant extends BasePaneParticipant {

	protected CommitPanel commitPanel;
	
	public CommitPaneParticipant(ISynchronizeScope scope, CommitPanel commitPanel) {
		super(scope, commitPanel);	   
		this.commitPanel = commitPanel;
	}
	
    protected Collection<AbstractSynchronizeActionGroup> getActionGroups() {
    	List<AbstractSynchronizeActionGroup> actionGroups = new ArrayList<AbstractSynchronizeActionGroup>();
    	actionGroups.add(new CommitPaneActionGroup(this.commitPanel));
    	return actionGroups;
    }
    
    /**
     * Commit pane's action set
     *
     * 
     * @author Igor Burilo
     */
    protected static class CommitPaneActionGroup extends BasePaneActionGroup {    
    	
    	protected MenuManager compareWithGroup;
    	protected MenuManager replaceWithGroup;
    	
    	protected CommitPanel commitPanel;
    	
    	public CommitPaneActionGroup(CommitPanel commitPanel) {
    		super(commitPanel);
    		this.commitPanel = commitPanel;
    	}    			   
		
		protected void configureActions(ISynchronizePageConfiguration configuration) {
			super.configureActions(configuration);
			
			//Open in compare editor				
			final OpenInComparePaneAction openInCompareAction = new OpenInComparePaneAction("", configuration);
			this.appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					ISynchronizePageConfiguration.FILE_GROUP,
					openInCompareAction);
			/*
			TODO handle double click ?
			For more details see ModelSynchronizeParticipantActionGroup
			
			configuration.setProperty(SynchronizePageConfiguration.P_OPEN_ACTION, new Action() {
				public void run() {
					openInCompareAction.run();
				}
			});			
			*/
			
			//separator
			this.appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CommitPaneActionGroup.GROUP_SYNC_NORMAL,
					new Separator());		
			
			
			//paste selected names
			AbstractSynchronizeModelAction pasteSelectedNamesAction = new AbstractSynchronizeModelAction(SVNTeamUIPlugin.instance().getResource("CommitPanel.PasteNames.Action"), configuration) {
				protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {					
					CommitPaneActionGroup.this.commitPanel.pasteNames();					
					return null;
				}				
			}; 
			this.appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CommitPaneActionGroup.GROUP_SYNC_NORMAL,
					pasteSelectedNamesAction);		 
			 
			
	        //create patch
	        final CreatePatchFileAction patchAction = new CreatePatchFileAction(SVNTeamUIPlugin.instance().getResource("CreatePatchCommand.label"), configuration);
	        this.appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CommitPaneActionGroup.GROUP_SYNC_NORMAL,
					patchAction);		        
	        	        
			//create branch
			CreateBranchAction branchAction = new CreateBranchAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.Branch"), configuration);
			branchAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/branch.gif"));
			this.appendToGroup(
						ISynchronizePageConfiguration.P_CONTEXT_MENU, 
						CommitPaneActionGroup.GROUP_SYNC_NORMAL,
						branchAction);				 			
			 
			//separator
			this.appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CommitPaneActionGroup.GROUP_SYNC_NORMAL,
					new Separator());				
			
			//revert
			RevertAction revertAction = new RevertAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.Revert"), configuration);
			revertAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/revert.gif"));
				this.appendToGroup(
						ISynchronizePageConfiguration.P_CONTEXT_MENU, 
						CommitPaneActionGroup.GROUP_SYNC_NORMAL,
						revertAction);
				
			//edit conflicts	
			EditConflictsAction editConflictsAction = new EditConflictsAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.EditConflicts"), configuration);
			this.appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CommitPaneActionGroup.GROUP_SYNC_NORMAL,
					editConflictsAction);				
			
			//mark as merged
			MarkAsMergedAction markMergedAction = new MarkAsMergedAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.MarkAsMerged"), configuration);
			this.appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CommitPaneActionGroup.GROUP_SYNC_NORMAL,
					markMergedAction);				
			
			//separator
			this.appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CommitPaneActionGroup.GROUP_SYNC_NORMAL,
					new Separator());			
			
			//lock
			LockAction lockAction = new LockAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.Lock"), configuration);
			lockAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/lock.gif"));
			this.appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CommitPaneActionGroup.GROUP_SYNC_NORMAL,
					lockAction);			
			
			//unlock
			UnlockAction unlockAction = new UnlockAction(SVNTeamUIPlugin.instance().getResource("UpdateActionGroup.Unlock"), configuration);
			unlockAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/unlock.gif"));
			this.appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CommitPaneActionGroup.GROUP_SYNC_NORMAL,
					unlockAction);	
			
			//separator
			this.appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CommitPaneActionGroup.GROUP_SYNC_NORMAL,
					new Separator());		
			
			//Compare With group 
			this.compareWithGroup = new MenuManager(SVNTeamUIPlugin.instance().getResource("CommitPanel.CompareWith.Group"));
			this.appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CommitPaneActionGroup.GROUP_SYNC_NORMAL, 
					this.compareWithGroup);
						
			//compare with working copy
			CompareWithWorkingCopyPaneAction compareWithWorkingCopyAction = new CompareWithWorkingCopyPaneAction(SVNTeamUIPlugin.instance().getResource("CompareWithWorkingCopyAction.label"), configuration);
			this.compareWithGroup.add(compareWithWorkingCopyAction);						
			
			//compare with latest revision
			CompareWithLatestRevisionPaneAction compareWithLatestRevisionPaneAction = new CompareWithLatestRevisionPaneAction(SVNTeamUIPlugin.instance().getResource("CompareWithLatestRevisionAction.label"), configuration);
			this.compareWithGroup.add(compareWithLatestRevisionPaneAction);			
						
			//compare with revision
			CompareWithRevisionPaneAction compareWithRevisionPaneAction = new CompareWithRevisionPaneAction(SVNTeamUIPlugin.instance().getResource("CompareWithRevisionAction.label"), configuration);
			this.compareWithGroup.add(compareWithRevisionPaneAction);			
			
			//Replace with group
			this.replaceWithGroup = new MenuManager(SVNTeamUIPlugin.instance().getResource("CommitPanel.ReplaceWith.Group"));
			this.appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CommitPaneActionGroup.GROUP_SYNC_NORMAL, 
					this.replaceWithGroup);
			
			//replace with latest revision
			ReplaceWithLatestRevisionPaneAction replaceWithLatestRevisionPaneAction = new ReplaceWithLatestRevisionPaneAction(SVNTeamUIPlugin.instance().getResource("ReplaceWithLatestRevisionAction.label"), configuration);
			this.replaceWithGroup.add(replaceWithLatestRevisionPaneAction);
			
			//replace with revision
			ReplaceWithRevisionPaneAction replaceWithRevisionPaneAction = new ReplaceWithRevisionPaneAction(SVNTeamUIPlugin.instance().getResource("ReplaceWithRevisionAction.label"), configuration);
			this.replaceWithGroup.add(replaceWithRevisionPaneAction);
			
			//separator
			this.appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CommitPaneActionGroup.GROUP_SYNC_NORMAL,
					new Separator());	
			
			//export
			ExtractOutgoingToAction extractActionOutgoing = new ExtractOutgoingToAction(SVNTeamUIPlugin.instance().getResource("ExtractToAction.Label"), configuration);
			this.appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CommitPaneActionGroup.GROUP_SYNC_NORMAL,
					extractActionOutgoing);	
			
			//cleanup
			CleanUpAction cleanUpAction = new CleanUpAction(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.Cleanup"), configuration);
			this.appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CommitPaneActionGroup.GROUP_SYNC_NORMAL,
					cleanUpAction);	
			
			//separator
			this.appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CommitPaneActionGroup.GROUP_SYNC_NORMAL,
					new Separator());		
			
			//delete
			DeletePaneAction deleteAction = new DeletePaneAction(SVNTeamUIPlugin.instance().getResource("CommitPanel.Delete.Action"), configuration);
			deleteAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/delete.gif"));
			this.appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CommitPaneActionGroup.GROUP_SYNC_NORMAL,
					deleteAction);	
		}
		
		public void dispose() {
			if (this.compareWithGroup != null) {
				this.compareWithGroup.removeAll();
				this.compareWithGroup.dispose();
			}
			
			if (this.replaceWithGroup != null) {
				this.replaceWithGroup.removeAll();
				this.replaceWithGroup.dispose();
			}
			
			super.dispose();
		}
    }
}
