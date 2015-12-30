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

package org.eclipse.team.svn.ui.synchronize.action;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.synchronize.UpdateSyncInfo;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.action.IResourceSelector;
import org.eclipse.team.svn.ui.dialog.TagModifyWarningDialog;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.extension.factory.ICommitDialog;
import org.eclipse.team.svn.ui.mapping.ModelHelper;
import org.eclipse.team.svn.ui.mapping.SVNModelParticipantChangeSetCapability;
import org.eclipse.team.svn.ui.panel.local.CommitPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.synchronize.SVNChangeSetCapability;
import org.eclipse.team.svn.ui.utility.CommitActionUtility;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Synchronize view commit action helper
 * 
 * @author Igor Burilo
 *
 */
public class CommitActionHelper extends AbstractActionHelper {

	public CommitActionHelper(IAction action, ISynchronizePageConfiguration configuration) {
		super(action, configuration);		
	}
	
	public IActionOperation getOperation() {
		return CommitActionHelper.getCommitOperation(this.getSyncInfoSelector(), this.configuration);	}

	public FastSyncInfoFilter getSyncInfoFilter() {
		return CommitActionHelper.getCommitSyncInfoFilter();
	}
	
	public static FastSyncInfoFilter getCommitSyncInfoFilter() {
		return new FastSyncInfoFilter.SyncInfoDirectionFilter(new int[] {SyncInfo.OUTGOING}) {
            public boolean select(SyncInfo info) {
                UpdateSyncInfo sync = (UpdateSyncInfo)info;
                return super.select(info) && !IStateFilter.SF_OBSTRUCTED.accept(sync.getLocalResource());
            }		    
		};
	}
	
	public static IActionOperation getCommitOperation(IResourceSelector resourceSelector, ISynchronizePageConfiguration configuration) {
		CommitActionUtility commitUtility = new CommitActionUtility(resourceSelector);
		IResource[] resources = commitUtility.getAllResources();
		
		IProject[] tagOperatedProjects = SVNUtility.getTagOperatedProjects(resources);
		if (tagOperatedProjects.length != 0) {
			TagModifyWarningDialog dlg = new TagModifyWarningDialog(configuration.getSite().getShell(), tagOperatedProjects);
        	if (dlg.open() != 0) {
        		return null;
        	}
		}
		
	    String proposedComment = ModelHelper.isShowModelSync() ? SVNModelParticipantChangeSetCapability.getProposedComment(resources) : SVNChangeSetCapability.getProposedComment(resources);                
	    CommitPanel commitPanel = new CommitPanel(resources, resources, CommitPanel.MSG_COMMIT, proposedComment); 
        ICommitDialog dialog = ExtensionsManager.getInstance().getCurrentCommitFactory().getCommitDialog(configuration.getSite().getShell(), commitUtility.getAllResourcesSet(), commitPanel);				
		if (dialog.open() != 0) {
			return null;
		}
		
		return commitUtility.getCompositeCommitOperation(commitPanel.getSelectedResources(), commitPanel.getTreatAsEdits(), dialog.getMessage(), commitPanel.getKeepLocks(), configuration.getSite().getShell(), configuration.getSite().getPart());
	}
	
	/**
	 * Ask preferences in order to determine whether to consult change sets
	 * during commit, synchronize or not
	 */
	public static boolean isIncludeChangeSets(final String message) {		
		if (SVNTeamPlugin.instance().getModelChangeSetManager().getSets().length == 0)
			return false;
		
		final IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		String consultChangeSetsOption = SVNTeamPreferences.getConsultChangeSetsInCommit(store, SVNTeamPreferences.CONSULT_CHANGE_SETS_IN_COMMIT);		
		if (consultChangeSetsOption.equals(SVNTeamPreferences.CONSULT_CHANGE_SETS_IN_COMMIT_ALWAYS)) {
			return true;
		} else if (consultChangeSetsOption.equals(SVNTeamPreferences.CONSULT_CHANGE_SETS_IN_COMMIT_NEVER)) {
			return false;
		}
		
		final int[] result = new int[] { 0 };
		// Ask the user whether to switch
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				Shell shell = UIMonitorUtility.getShell();
				
				MessageDialogWithToggle m = MessageDialogWithToggle.openYesNoQuestion(
						shell,
						SVNUIMessages.ConsultChangeSets_toggleDialog_title, 
						message, 
						SVNUIMessages.ConsultChangeSets_toggleDialog_toggleMessage, 
						false /* toggle state */,
						store,
						SVNTeamPreferences.fullPromptName(SVNTeamPreferences.CONSULT_CHANGE_SETS_IN_COMMIT));
				m.getReturnCode();	
				result[0] = m.getReturnCode();
			}
		});
							
		switch (result[0]) {
			// yes
			case IDialogConstants.YES_ID:
			case IDialogConstants.OK_ID :
				return true;
			// no
			case IDialogConstants.NO_ID :
				return false;
		}
		return false;				
	}
}
