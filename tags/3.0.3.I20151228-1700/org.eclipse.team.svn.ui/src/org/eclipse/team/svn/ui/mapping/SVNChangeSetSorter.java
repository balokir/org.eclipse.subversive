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

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSet;
import org.eclipse.team.internal.core.subscribers.ChangeSet;
import org.eclipse.team.internal.ui.mapping.ResourceModelSorter;
import org.eclipse.team.svn.core.mapping.SVNIncomingChangeSet;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

public class SVNChangeSetSorter extends ResourceModelSorter implements IPropertyChangeListener {

	// Comment sorting options
	public final static int DATE = 1;
	public final static int COMMENT = 2;
	public final static int USER = 3;
	private ISynchronizePageConfiguration configuration;
	private int reorderingCriteria = SVNChangeSetSorter.DATE;
	
	public SVNChangeSetSorter() {
		super();
	}

	@SuppressWarnings("deprecation")
	public int compare(Viewer viewer, Object o1, Object o2) {
		if (o1 instanceof ChangeSet && o2 instanceof ChangeSet) {
		    if (o1 instanceof ActiveChangeSet && o2 instanceof ActiveChangeSet) {
		        return this.compareNames(((ActiveChangeSet)o1).getTitle(), ((ActiveChangeSet)o2).getTitle());
		    }
		    if (o1 instanceof SVNIncomingChangeSet && o2 instanceof SVNIncomingChangeSet) {
		    	SVNIncomingChangeSet r1 = (SVNIncomingChangeSet)o1;
		    	SVNIncomingChangeSet r2 = (SVNIncomingChangeSet)o2;
				if (this.reorderingCriteria == COMMENT) {
					return this.compareNames(r1.getComment(), r2.getComment());
				}
				else if (this.reorderingCriteria == USER) {
					return this.compareNames(r1.getAuthor(), r2.getAuthor());
				}
				return r1.getDate().compareTo(r2.getDate());
		    }
		    if (o1 instanceof ActiveChangeSet) {
		        return -1;
		    }
		    else if (o2 instanceof ActiveChangeSet) {
		        return 1;
		    }
		    if (o1 instanceof SVNIncomingChangeSet) {
		        return 1;
		    }
		    else if (o2 instanceof SVNIncomingChangeSet) {
		        return -1;
		    }
		}
		return super.compare(viewer, o1, o2);
	}

	@SuppressWarnings({ "deprecation", "unqualified-field-access" })
	private int compareNames(String s1, String s2) {
		s1 = s1 == null ? "" : s1; //$NON-NLS-1$
		s2 = s2 == null ? "" : s2; //$NON-NLS-1$
		return collator.compare(s1, s2);
	}
	
	public void setConfiguration(ISynchronizePageConfiguration configuration) {
		this.configuration = configuration;
		this.configuration.addPropertyChangeListener(this);
		this.reorderingCriteria = SVNChangeSetActionProvider.getSortCriteria(this.configuration);
	}

	public void propertyChange(PropertyChangeEvent event) {
		this.reorderingCriteria = SVNChangeSetActionProvider.getSortCriteria(this.configuration);
	}
	
}
