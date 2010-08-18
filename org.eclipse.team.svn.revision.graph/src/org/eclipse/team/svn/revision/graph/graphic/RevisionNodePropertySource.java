/*******************************************************************************
 * Copyright (c) 2005-2010 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo (Polarion Software) - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.revision.graph.graphic;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphMessages;
import org.eclipse.team.svn.revision.graph.graphic.figure.RevisionFigure;
import org.eclipse.team.svn.ui.utility.DateFormatter;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * Contribute revision node to Properties view
 * 
 * @author Igor Burilo
 */
public class RevisionNodePropertySource implements IPropertySource {

	protected static final String PATH_PROPERTY = "path"; 	 //$NON-NLS-1$
	protected static final String REVISION_PROPERTY = "revision";	 //$NON-NLS-1$
	protected static final String ACTION_PROPERTY = "action";	 //$NON-NLS-1$
	protected static final String AUTHOR_PROPERTY = "author";	 //$NON-NLS-1$
	protected static final String DATE_PROPERTY = "date"; //$NON-NLS-1$
	protected static final String COMMENT_PROPERTY = "comment";	 //$NON-NLS-1$
	protected static final String COPIED_FROM_PROPERTY = "copiedFrom"; //$NON-NLS-1$
	protected static final String CHANGED_PATHS_PROPERTY = "changedPaths";	 //$NON-NLS-1$
	protected static final String INCOMING_MERGES_PROPERTY = "incomingMerges"; //$NON-NLS-1$
	protected static final String OUTGOING_MERGES_PROPERTY = "outgoingMerges"; //$NON-NLS-1$
	
	protected final RevisionNode node;
	
	protected IPropertyDescriptor[] descriptors;
	
	public RevisionNodePropertySource(RevisionNode node) {
		this.node = node;
	}
	
	public IPropertyDescriptor[] getPropertyDescriptors() {
		if (this.descriptors == null) {
			List<IPropertyDescriptor> list = new ArrayList<IPropertyDescriptor>();
			list.add(new PropertyDescriptor(PATH_PROPERTY, SVNRevisionGraphMessages.RevisionNodePropertySource_Path));
			list.add(new PropertyDescriptor(REVISION_PROPERTY, SVNRevisionGraphMessages.RevisionNodePropertySource_Revision));
			list.add(new PropertyDescriptor(ACTION_PROPERTY, SVNRevisionGraphMessages.RevisionNodePropertySource_Action));
			list.add(new PropertyDescriptor(AUTHOR_PROPERTY, SVNRevisionGraphMessages.RevisionNodePropertySource_Author));
			list.add(new PropertyDescriptor(DATE_PROPERTY, SVNRevisionGraphMessages.RevisionNodePropertySource_Date));
			list.add(new PropertyDescriptor(COMMENT_PROPERTY, SVNRevisionGraphMessages.RevisionNodePropertySource_Comment));
			if (this.node.getCopiedFrom() != null) {
				list.add(new PropertyDescriptor(COPIED_FROM_PROPERTY, SVNRevisionGraphMessages.RevisionNodePropertySource_CopiedFrom));
			}			
			list.add(new PropertyDescriptor(CHANGED_PATHS_PROPERTY, SVNRevisionGraphMessages.RevisionNodePropertySource_ChangedPaths));
			if (this.node.hasIncomingMerges()) {
				list.add(new PropertyDescriptor(INCOMING_MERGES_PROPERTY, SVNRevisionGraphMessages.RevisionNodePropertySource_IncomingMerges));
			}
			if (this.node.hasOutgoingMerges()) {
				list.add(new PropertyDescriptor(OUTGOING_MERGES_PROPERTY, SVNRevisionGraphMessages.RevisionNodePropertySource_OutgoingMerges));
			}
			this.descriptors = list.toArray(new IPropertyDescriptor[0]);
		}
		return this.descriptors;
	}
	
	public Object getPropertyValue(Object id) {
		if (PATH_PROPERTY.equals(id)) {
			return this.node.getPath();
		} else if (REVISION_PROPERTY.equals(id)) {
			return this.node.getRevision();
		} else if (ACTION_PROPERTY.equals(id)) {						
			return RevisionFigure.getActionAsString(this.node.getAction());
		} else if (AUTHOR_PROPERTY.equals(id)) {
			return this.node.getAuthor();
		} else if (DATE_PROPERTY.equals(id)) {			
			long date = this.node.getDate(); 
			return date == 0 ? SVNMessages.SVNInfo_NoDate : DateFormatter.formatDate(date);
		} else if (COMMENT_PROPERTY.equals(id)) {
			return this.node.getMessage();
		} else if (COPIED_FROM_PROPERTY.equals(id)) {
			RevisionNode copiedFrom = this.node.getCopiedFrom();
			if (copiedFrom != null) {
				return copiedFrom.getPath() + "@" + copiedFrom.getRevision(); //$NON-NLS-1$
			}
		} else if (CHANGED_PATHS_PROPERTY.equals(id)) {
			return RevisionFigure.getChangedPathsAsString(this.node);
		} else if (INCOMING_MERGES_PROPERTY.equals(id)) {
			return RevisionFigure.getIncomingMergesAsString(this.node);
		} else if (OUTGOING_MERGES_PROPERTY.equals(id)) {
			return RevisionFigure.getOutgoingMergesAsString(this.node);
		}
		return null;
	}
	
	public Object getEditableValue() {	
		return null;
	}

	public boolean isPropertySet(Object id) {		
		return false;
	}

	public void resetPropertyValue(Object id) {
	}

	public void setPropertyValue(Object id, Object value) {
	}
}
