/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.mapping;

import java.util.HashSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.window.Window;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSet;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSetManager;
import org.eclipse.team.internal.core.subscribers.ChangeSet;
import org.eclipse.team.internal.ui.synchronize.ChangeSetCapability;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.synchronize.UpdateSubscriber;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.local.CommitSetPanel;
import org.eclipse.team.svn.ui.synchronize.SVNChangeSetCapability;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

public class SVNModelParticipantChangeSetCapability extends ChangeSetCapability {

	public static String getProposedComment(IResource []resourcesToCommit) {
		String retVal = null;
		ChangeSet []sets = SVNTeamPlugin.instance().getModelChangeSetManager().getSets();
		for (int i = 0; i < sets.length; i++) {
			if (SVNChangeSetCapability.containsOneOf(sets[i], resourcesToCommit)) {
				String comment = sets[i].getComment();
				retVal = retVal == null ? comment : (retVal + "\n" + comment); //$NON-NLS-1$
			}
		}
		return retVal;
	}
	
	public boolean supportsCheckedInChangeSets() {
		return true;
	}
	
	public boolean enableCheckedInChangeSetsFor(ISynchronizePageConfiguration configuration) {
	    return this.supportsCheckedInChangeSets() && configuration.getMode() != ISynchronizePageConfiguration.OUTGOING_MODE;
	}

	public boolean supportsActiveChangeSets() {
		return true;
	}
	
	public boolean enableActiveChangeSetsFor(ISynchronizePageConfiguration configuration) {
        return this.supportsActiveChangeSets() && configuration.getMode() != ISynchronizePageConfiguration.INCOMING_MODE;
	}

	public ActiveChangeSetManager getActiveChangeSetManager() {
		return SVNTeamPlugin.instance().getModelChangeSetManager();
    }
	
	public SVNIncomingChangeSetCollector createIncomingChangeSetCollector(ISynchronizePageConfiguration configuration) {
		return new SVNIncomingChangeSetCollector(configuration, UpdateSubscriber.instance());
	}
	
	public ActiveChangeSet createChangeSet(ISynchronizePageConfiguration configuration, IDiff[] infos) {
        ActiveChangeSet set = this.getActiveChangeSetManager().createSet(SVNUIMessages.ChangeSet_NewSet, new IDiff[0]); 
		CommitSetPanel panel = new CommitSetPanel(set, this.getResources(infos), CommitSetPanel.MSG_CREATE);  
		DefaultDialog dialog = new DefaultDialog(configuration.getSite().getShell(), panel);
		dialog.open();
		if (dialog.getReturnCode() != Window.OK) {
			return null;
		}
		set.add(infos);
		return set;
    }
	
	public void editChangeSet(ISynchronizePageConfiguration configuration, ActiveChangeSet set) {
		CommitSetPanel panel = new CommitSetPanel(set, set.getResources(), CommitSetPanel.MSG_EDIT);  
		DefaultDialog dialog = new DefaultDialog(configuration.getSite().getShell(), panel);
		dialog.open(); 
    }
	
	private IResource[] getResources(IDiff[] diffs) {
    	HashSet<IResource> result = new HashSet<IResource>();
    	for (int i = 0; i < diffs.length; i++) {
			IDiff diff = diffs[i];
			IResource resource = ResourceDiffTree.getResourceFor(diff);
			if (resource != null) {
				result.add(resource);
			}
		}
        return result.toArray(new IResource[result.size()]);
    }
	
}
