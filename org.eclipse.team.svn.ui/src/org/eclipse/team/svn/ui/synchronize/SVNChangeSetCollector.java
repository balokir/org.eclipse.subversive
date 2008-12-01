/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alessandro Nistico - [patch] Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.core.subscribers.ChangeSet;
import org.eclipse.team.internal.core.subscribers.CheckedInChangeSet;
import org.eclipse.team.internal.ui.synchronize.SyncInfoSetChangeSetCollector;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.synchronize.variant.ResourceVariant;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.utility.DateFormatter;
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
		Map<Long, SVNCheckedInChangeSet> sets = new HashMap<Long, SVNCheckedInChangeSet>();
		final Set<SVNCheckedInChangeSet> added = new HashSet<SVNCheckedInChangeSet>();
		for (ChangeSet set : this.getSets()) {
			SVNCheckedInChangeSet svnSet = (SVNCheckedInChangeSet)set;
			sets.put(svnSet.getRevision(), svnSet);
		}
		// change set name format is: revisionNum (date) [author] ...comment...
		String svnAuthor = SVNMessages.SVNInfo_Author;
		String svnDate = SVNMessages.SVNInfo_Date;
		String svnNoAuthor = SVNMessages.SVNInfo_NoAuthor;
		String svnNoDate = SVNMessages.SVNInfo_NoDate;
		for (SyncInfo info : infos) {
			if ((info.getKind() & SyncInfo.INCOMING) == 0) {
				continue;
			}
			ResourceVariant remote = (ResourceVariant) info.getRemote();
			ILocalResource resource = remote.getResource();
			long revision = resource.getRevision();
			SVNCheckedInChangeSet set = sets.get(revision);
			boolean updateName = false;
			if (set == null) {
				set = new SVNCheckedInChangeSet();
				set.author = resource.getAuthor();
				set.date = new Date(resource.getLastCommitDate());
				set.revision = revision;
				if (resource instanceof IResourceChange) {
					set.comment = ((IResourceChange)resource).getComment();
				}
				updateName = true;
				sets.put(revision, set);
				added.add(set);
			}
			else if (set.date.getTime() == 0) {
				updateName = true;
				set.date = new Date(resource.getLastCommitDate());
			}
			else if (set.author == null) {
				updateName = true;
				set.author = resource.getAuthor();
			}
			if (updateName) {
				// rebuild name
				String name = 
					String.valueOf(revision) + " " + 
					(resource.getLastCommitDate() == 0 ? svnNoDate : MessageFormat.format(svnDate, new Object[] {DateFormatter.formatDate(set.date)})) + " " + 
					(resource.getAuthor() == null ? svnNoAuthor : MessageFormat.format(svnAuthor, new Object[] {resource.getAuthor()}));
				if (set.comment != null) {
					String comment = set.comment;
					if (FileUtility.isWindows()) {
						comment = comment.replaceAll("\r\n|\r|\n", " ");
					}
					name += " " + comment;
				}
				set.setName(name);
			}
			
			set.add(info);
		}
		
		// lesser UI blinking and thread safe...
		this.performUpdate(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				for (SVNCheckedInChangeSet set : added) {
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
		
		private long revision;

		public long getRevision() {
			return this.revision;
		}

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
