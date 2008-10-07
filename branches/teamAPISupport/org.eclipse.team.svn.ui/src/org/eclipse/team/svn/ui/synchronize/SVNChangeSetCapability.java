/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alessandro Nistico - [patch] Initial API and implementation
 *    Thomas Champagne - Bug 217561 : additional date formats for label decorations
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.window.Window;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSet;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSetManager;
import org.eclipse.team.internal.core.subscribers.ChangeSet;
import org.eclipse.team.internal.ui.synchronize.ChangeSetCapability;
import org.eclipse.team.internal.ui.synchronize.SyncInfoSetChangeSetCollector;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.local.CommitSetPanel;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Change Set capability implementation 
 * 
 * @author Alessandro Nistico
 */
public class SVNChangeSetCapability extends ChangeSetCapability {
	public static boolean isEnabled = false;
	
	public static String getProposedComment(IResource []resourcesToCommit) {
		String retVal = null;
		ChangeSet []sets = SVNTeamUIPlugin.instance().getChangeSetManager().getSets();
		for (int i = 0; i < sets.length; i++) {
			if (SVNChangeSetCapability.containsOneOf(sets[i], resourcesToCommit)) {
				String comment = sets[i].getComment();
				retVal = retVal == null ? comment : (retVal + "\n" + comment);
			}
		}
		return retVal;
	}
	
	public static boolean containsOneOf(ChangeSet set, IResource []resourcesToCommit) {
		for (int i = 0; i < resourcesToCommit.length; i++) {
			if (set.contains(resourcesToCommit[i])) {
				return true;
			}
		}
		return false;
	}

	public boolean supportsCheckedInChangeSets() {
		return true;
	}

	public SyncInfoSetChangeSetCollector createSyncInfoSetChangeSetCollector(ISynchronizePageConfiguration configuration) {
		SVNChangeSetCapability.isEnabled = true;
		return new SVNChangeSetCollector(configuration);
	}

	public boolean supportsActiveChangeSets() {
		return true;
	}

	public ActiveChangeSet createChangeSet(ISynchronizePageConfiguration configuration, IDiff[] diffs) {
        ActiveChangeSet set = this.getActiveChangeSetManager().createSet(SVNTeamUIPlugin.instance().getResource("ChangeSet.NewSet"), new IDiff[0]); 
		CommitSetPanel panel = new CommitSetPanel(set, getResources(diffs), CommitSetPanel.MSG_CREATE);  
		DefaultDialog dialog = new DefaultDialog(configuration.getSite().getShell(), panel);
		dialog.open();
		if (dialog.getReturnCode() != Window.OK) {
			return null;
		}
		set.add(diffs);
		return set;
	}

    private IResource[] getResources(IDiff[] diffs) {
    	Set<IResource> result = new HashSet<IResource>();
    	for (int i = 0; i < diffs.length; i++) {
			IDiff diff = diffs[i];
			IResource resource = ResourceDiffTree.getResourceFor(diff);
			if (resource != null) {
				result.add(resource);
			}
		}
        return result.toArray(new IResource[result.size()]);
    }

	public void editChangeSet(ISynchronizePageConfiguration configuration, ActiveChangeSet set) {
		CommitSetPanel panel = new CommitSetPanel(set, set.getResources(), CommitSetPanel.MSG_EDIT);  
		DefaultDialog dialog = new DefaultDialog(configuration.getSite().getShell(), panel);
		dialog.open();
	}

	public ActiveChangeSetManager getActiveChangeSetManager() {
		return SVNTeamUIPlugin.instance().getChangeSetManager();
	}
	
	public boolean enableActiveChangeSetsFor(ISynchronizePageConfiguration configuration) {
		return this.supportsActiveChangeSets() && configuration.getMode() != ISynchronizePageConfiguration.INCOMING_MODE;
	}
	
	public boolean enableCheckedInChangeSetsFor(ISynchronizePageConfiguration configuration) {
		return this.supportsCheckedInChangeSets() && configuration.getMode() != ISynchronizePageConfiguration.OUTGOING_MODE;
	}

}
