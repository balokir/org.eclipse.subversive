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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSet;
import org.eclipse.team.internal.core.subscribers.ChangeSet;
import org.eclipse.team.internal.ui.mapping.ResourceModelSorter;
import org.eclipse.team.svn.ui.synchronize.SVNChangeSetCollector.SVNCheckedInChangeSet;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

public class SVNChangeSetSorter extends ResourceModelSorter {

	// Comment sorting options
	public final static int DATE = 1;
	public final static int COMMENT = 2;
	public final static int USER = 3;
	private ISynchronizePageConfiguration configuration;
	
	public SVNChangeSetSorter() {
		super();
	}

	@SuppressWarnings("deprecation")
	public int compare(Viewer viewer, Object o1, Object o2) {
		if (o1 instanceof  ChangeSet && o2 instanceof ChangeSet) {
		    ChangeSet s1 = (ChangeSet) o1;
		    ChangeSet s2 = (ChangeSet) o2;
		    if (s1 instanceof ActiveChangeSet && s2 instanceof ActiveChangeSet) {
		        return this.compareNames(((ActiveChangeSet)s1).getTitle(), ((ActiveChangeSet)s2).getTitle());
		    }
		    if (s1 instanceof SVNCheckedInChangeSet && s2 instanceof SVNCheckedInChangeSet) {
		    	SVNCheckedInChangeSet r1 = (SVNCheckedInChangeSet)s1;
		    	SVNCheckedInChangeSet r2 = (SVNCheckedInChangeSet)s2;
				if (getCommentCriteria() == DATE) {
					return r1.getDate().compareTo(r2.getDate());
				}
				else if (getCommentCriteria() == COMMENT) {
					return this.compareNames(r1.getComment(), r2.getComment());
				}
				else if (getCommentCriteria() == USER) {
					return this.compareNames(r1.getAuthor(), r2.getAuthor());
				}
				else
					return 0;
		    }
		    if (s1 instanceof ActiveChangeSet) {
		        return -1;
		    }
		    else if (s2 instanceof ActiveChangeSet) {
		        return 1;
		    }
		    if (s1 instanceof SVNCheckedInChangeSet) {
		        return -1;
		    } else if (s2 instanceof SVNCheckedInChangeSet) {
		        return 1;
		    }
		}
		return super.compare(viewer, o1, o2);
	}

	@SuppressWarnings({ "deprecation", "unqualified-field-access" })
	private int compareNames(String s1, String s2) {
		return collator.compare(s1, s2);
	}
	
	public int getCommentCriteria() {
		return SVNChangeSetActionProvider.getSortCriteria(this.configuration);
	}

	public void setConfiguration(ISynchronizePageConfiguration configuration) {
		this.configuration = configuration;
	}
	
}
