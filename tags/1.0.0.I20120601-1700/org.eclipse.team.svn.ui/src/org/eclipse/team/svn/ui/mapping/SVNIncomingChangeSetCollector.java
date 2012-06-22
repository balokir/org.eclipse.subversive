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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IDiffChangeEvent;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.core.subscribers.ChangeSet;
import org.eclipse.team.internal.core.subscribers.ChangeSetManager;
import org.eclipse.team.internal.core.subscribers.DiffChangeSet;
import org.eclipse.team.internal.ui.synchronize.ChangeSetCapability;
import org.eclipse.team.internal.ui.synchronize.IChangeSetProvider;
import org.eclipse.team.svn.core.BaseMessages;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.mapping.SVNIncomingChangeSet;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.utility.DateFormatter;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;

public class SVNIncomingChangeSetCollector extends ChangeSetManager {
	
	protected ISynchronizePageConfiguration configuration;
	protected Subscriber subscriber;
	
	public SVNIncomingChangeSetCollector(ISynchronizePageConfiguration configuration, Subscriber subscriber) {
		this.configuration = configuration;
		this.subscriber = subscriber;
	}
	
	protected void initializeSets() {
		//do nothing
	}
	
	public void add(IDiff[] diffs) {
		if (diffs == null || diffs.length == 0) {
			return;
		}
		
		//format strings
		String svnAuthor = SVNMessages.SVNInfo_Author;
		String svnDate = SVNMessages.SVNInfo_Date;
		String svnNoAuthor = SVNMessages.SVNInfo_NoAuthor;
		String svnNoDate = SVNMessages.SVNInfo_NoDate;
		//
		
		HashMap<Long, SVNIncomingChangeSet> sets = new HashMap<Long, SVNIncomingChangeSet>();
		final Set<SVNIncomingChangeSet> added = new HashSet<SVNIncomingChangeSet>();
		for (ChangeSet set : this.getSets()) {
			SVNIncomingChangeSet svnSet = (SVNIncomingChangeSet)set;
			sets.put(svnSet.getRevision(), svnSet);
		}
		try {
			for (IDiff diff : diffs) {
				SyncInfo info = this.subscriber.getSyncInfo(ResourceDiffTree.getResourceFor(diff));
				if (info == null || (info.getKind() & SyncInfo.INCOMING) == 0) {
					continue;
				}
				ILocalResource resource = ((AbstractSVNSyncInfo) info).getRemoteChangeResource();
				long revision = resource.getRevision();
				SVNIncomingChangeSet set = sets.get(revision);
				boolean updateName = false;
				if (set == null) {
					set = new SVNIncomingChangeSet();
					set.setAuthor(resource.getAuthor());
					set.setDate(new Date(resource.getLastCommitDate()));
					set.setRevision(revision);
					if (resource instanceof IResourceChange) {
						set.setComment(((IResourceChange)resource).getComment());
					}
					updateName = true;
					sets.put(revision, set);
					added.add(set);
				}
				else if (set.getDate().getTime() == 0) {
					updateName = true;
					set.setDate(new Date(resource.getLastCommitDate()));
				}
				else if (set.getAuthor() == null) {
					updateName = true;
					set.setAuthor(resource.getAuthor());
				}
				if (updateName) {
					// rebuild name
					String name = 
						String.valueOf(revision) + " " +  //$NON-NLS-1$
						(resource.getLastCommitDate() == 0 ? svnNoDate : BaseMessages.format(svnDate, new Object[] {DateFormatter.formatDate(set.getDate())})) + " " +  //$NON-NLS-1$
						(resource.getAuthor() == null ? svnNoAuthor : BaseMessages.format(svnAuthor, new Object[] {resource.getAuthor()}));
					if (set.getComment() != null) {
						String comment = set.getComment();
						if (FileUtility.isWindows()) {
							comment = comment.replaceAll("\r\n|\r|\n", " "); //$NON-NLS-1$ //$NON-NLS-2$
						}
						name += " " + comment; //$NON-NLS-1$
					}
					set.setName(name);
				}
				
				set.add(diff);
			}
			for (SVNIncomingChangeSet set : added) {
				this.add(set);
			}
		}
		catch (TeamException ex) {
			LoggedOperation.reportError(this.getClass().getName(), ex);
		}
	}
	
    public void handleChange(IDiffChangeEvent event) {
        ArrayList<IPath> removals = new ArrayList<IPath>(Arrays.asList(event.getRemovals()));
        ArrayList<IDiff> additions = new ArrayList<IDiff>(Arrays.asList(event.getAdditions()));
        IDiff[] changed = event.getChanges();
        for (int i = 0; i < changed.length; i++) {
            IDiff diff = changed[i];
            additions.add(diff);
            removals.add(diff.getPath());
        }
        if (!removals.isEmpty()) {
            this.remove(removals.toArray(new IPath[removals.size()]));
        }
        if (!additions.isEmpty()) {
            this.add(additions.toArray(new IDiff[additions.size()]));
        }
    }
    
    protected void remove(IPath[] paths) {
    	ChangeSet[] sets = this.getSets();
        for (int i = 0; i < sets.length; i++) {
        	DiffChangeSet set = (DiffChangeSet)sets[i];
            set.remove(paths);
        }
    }
	
	public Subscriber getSubscriber() {
		return this.subscriber;
	}
	
	public ChangeSetCapability getChangeSetCapability() {
        ISynchronizeParticipant participant = this.configuration.getParticipant();
        if (participant instanceof IChangeSetProvider) {
            IChangeSetProvider provider = (IChangeSetProvider)participant;
            return provider.getChangeSetCapability();
        }
        return null;
    }

}
