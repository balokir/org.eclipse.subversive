/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alessandro Nistico - [patch] Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.core.subscribers.CheckedInChangeSet;
import org.eclipse.team.internal.ui.synchronize.SyncInfoSetChangeSetCollector;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.ui.synchronize.variant.ResourceVariant;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Change Set collector implementation.
 * 
 * @author Alessandro Nistico
 */
public class SVNChangeSetCollector extends SyncInfoSetChangeSetCollector {
	public SVNChangeSetCollector(ISynchronizePageConfiguration configuration) {
		super(configuration);
	}
	
	public void dispose() {
		SVNChangeSetCapability.isEnabled = false;
		super.dispose();
	}
	
	protected void add(SyncInfo[] infos) {
		if (infos == null || infos.length == 0) {
			return;
		}
		final Map sets = new HashMap();
		// change set name format is: revisionNum (date) [author] ...comment...
		DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());
		String svnAuthor = SVNTeamPlugin.instance().getResource("SVNInfo.Author");
		String svnDate = SVNTeamPlugin.instance().getResource("SVNInfo.Date");
		String svnNoAuthor = SVNTeamPlugin.instance().getResource("SVNInfo.NoAuthor");
		String svnNoDate = SVNTeamPlugin.instance().getResource("SVNInfo.NoDate");
		for (int i = 0; i < infos.length; i++) {
			if ((infos[i].getKind() & SyncInfo.OUTGOING) != 0) {
				continue;
			}
			ResourceVariant remote = (ResourceVariant) infos[i].getRemote();
			String id = remote.getContentIdentifier();
			SVNCheckedInChangeSet set = (SVNCheckedInChangeSet)sets.get(id);
			if (set == null) {
				ILocalResource resource = remote.getResource();
				set = new SVNCheckedInChangeSet();
				set.author = resource.getAuthor();
				set.date = resource.getLastCommitDate() == 0 ? null : new Date(resource.getLastCommitDate());
				if (resource instanceof IResourceChange) {
					set.comment = ((IResourceChange)resource).getComment();
				}
				String name = 
					String.valueOf(resource.getRevision()) + " " + 
					(set.date == null ? svnNoDate : MessageFormat.format(svnDate, new String[] {dateTimeFormat.format(set.date)})) + " " + 
					(resource.getAuthor() == null ? svnNoAuthor : MessageFormat.format(svnAuthor, new String[] {resource.getAuthor()}));
				if (set.comment != null) {
					name += " " + set.comment;
				}
				set.setName(name);
				sets.put(id, set);
			}
			set.add(infos[i]);
		}
		
		// lesser UI blinking and thread safe...
		this.performUpdate(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				for (Iterator it = sets.values().iterator(); it.hasNext(); ) {
					SVNCheckedInChangeSet set = (SVNCheckedInChangeSet)it.next();
					SVNChangeSetCollector.this.add(set);
				}
			}
		}, true, new NullProgressMonitor());
	}

	protected void initializeSets() {
	}
	
	public static class SVNCheckedInChangeSet extends CheckedInChangeSet {
		private String author;

		private String comment;

		private Date date;

		public String getAuthor() {
			return this.author;
		}

		public Date getDate() {
			return this.date;
		}

		public String getComment() {
			return this.comment;
		}
		
		public void setName(String name) {
			super.setName(name);
		}
	}
	
}
