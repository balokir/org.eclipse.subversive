/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.history.model;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.history.data.HistoryCategory;
import org.eclipse.team.svn.ui.history.data.RootHistoryCategory;
import org.eclipse.team.svn.ui.history.data.SVNLocalFileRevision;

/**
 * History View category UI node
 * 
 * @author Alexander Gurov
 */
public class CategoryLogNode extends AbstractLogNode {
	protected HistoryCategory category;
	
	public CategoryLogNode(RootHistoryCategory category) {
		this(category, null);
	}

	public CategoryLogNode(HistoryCategory category, ILogNode parent) {
		super(parent);
		this.category = category;
	}

	public ILogNode[] getChildren() {
		Object []entries = this.category.getEntries();
		ILogNode []children = new ILogNode[entries.length];
		for (int i = 0; i < entries.length; i++) {
			if (entries[i] instanceof SVNLogEntry) {
				children[i] = new SVNLogNode((SVNLogEntry)entries[i], this);
			}
			else if (entries[i] instanceof SVNLocalFileRevision) {
				children[i] = new LocalLogNode((SVNLocalFileRevision)entries[i], this);
			}
			else if (entries[i] instanceof HistoryCategory) {
				children[i] = new CategoryLogNode((HistoryCategory)entries[i], this);
			}
			else if (entries[i] instanceof String) {
				children[i] = new PlainTextLogNode((String)entries[i], this);
			}
		}
		return children;
	}

	public Object getEntity() {
		return this.category;
	}

	public boolean requiresBoldFont(long currentRevision) {
		return true;
	}
	
	public ImageDescriptor getImageDescriptor() {
		return SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/group_by_date.gif");
	}

	public String getLabel(int columnIndex, int labelType, long currentRevision) {
		if (columnIndex == ILogNode.COLUMN_REVISION) {
			return this.category.getName();
		}
		return "";
	}
	
	public int getType() {
		return ILogNode.TYPE_CATEGORY;
	}

	public boolean hasChildren() {
		return true;
	}

	public String getAuthor() {
		return "";
	}

	public int getChangesCount() {
		return 0;
	}

	public String getComment() {
		return "";
	}

	public long getRevision() {
		return SVNRevision.INVALID_REVISION_NUMBER;
	}

	public long getTimeStamp() {
		return 0;
	}
	
	public int hashCode() {
		return this.category.hashCode();
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof CategoryLogNode) {
			return this.category.equals(((CategoryLogNode)obj).category);
		}
		return false;
	}
	
}
