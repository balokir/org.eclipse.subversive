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
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.history.data.RootHistoryCategory;

/**
 * Plain text nodes for History View
 * 
 * @author Alexander Gurov
 */
public class PlainTextLogNode extends AbstractLogNode {
	protected String label;

	public PlainTextLogNode(String label, ILogNode parent) {
		super(parent);
		this.label = label;
	}
	
	public int getType() {
		return ILogNode.TYPE_NONE;
	}
	
	public Object getEntity() {
		return this.label;
	}
	
	public boolean requiresBoldFont(long currentRevision) {
		return true;
	}
	
	public ImageDescriptor getImageDescriptor() {
		if (this.label.equals(RootHistoryCategory.PENDING[0])) {
			return SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/repositories/browser_pending.gif");
		}
		return null;
	}
	
	public String getLabel(int columnIndex, int labelType, long currentRevision) {
		if (columnIndex == ILogNode.COLUMN_REVISION) {
			return this.label;
		}
		return "";
	}
	
	public ILogNode []getChildren() {
		return null;
	}
	
	public boolean hasChildren() {
		return false;
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
		return this.label.hashCode();
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof PlainTextLogNode) {
			return this.label.equals(((PlainTextLogNode)obj).label);
		}
		return false;
	}
	
}