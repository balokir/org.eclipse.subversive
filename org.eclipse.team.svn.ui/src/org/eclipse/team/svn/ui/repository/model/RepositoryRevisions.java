/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.repository.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRevisionLink;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;

/**
 * Repository revisions node representation 
 * 
 * @author Alexander Gurov
 */
public class RepositoryRevisions extends RepositoryFictiveNode implements IParentTreeNode, IDataTreeNode {
	protected static String REVISIONS_NAME;
	protected IRepositoryLocation location;
	protected RepositoryRevision []children;
	
	public RepositoryRevisions(IRepositoryLocation location) {
		this.location = location;
		RepositoryRevisions.REVISIONS_NAME = SVNUIMessages.RepositoriesView_Model_Revisions;
	}
	
	public IRepositoryLocation getRepositoryLocation() {
		return this.location;
	}

    public RGB getForeground(Object element) {
    	return RepositoryResource.NOT_RELATED_NODES_FOREGROUND;
    }
    
    public RGB getBackground(Object element) {
    	return RepositoryResource.NOT_RELATED_NODES_BACKGROUND;
    }
    
    public FontData getFont(Object element) {
    	return RepositoryResource.NOT_RELATED_NODES_FONT.getFontData()[0];
    }
    
	public void refresh() {
		this.children = null;
	}
	
	public Object getData() {
		return null;
	}
	
	public boolean hasChildren() {
		return this.location.getRevisionLinks().length > 0;
	}
	
	public String getLabel(Object o) {
		return RepositoryRevisions.REVISIONS_NAME;
	}

	public IRevisionLink []getLinks(SVNRevision revision) {
		IRevisionLink []links = this.location.getRevisionLinks();
		ArrayList<IRevisionLink> retVal = new ArrayList<IRevisionLink>();
		for (int i = 0; i < links.length; i++) {
			if (links[i].getRepositoryResource().getSelectedRevision().equals(revision)) {
				retVal.add(links[i]);
			}
		}
		return retVal.toArray(new IRevisionLink[retVal.size()]);
	}
	
	public Object []getChildren(Object o) {
		if (this.children == null) {
			IRevisionLink[] links = this.location.getRevisionLinks();				
			this.children = new RepositoryRevision[links.length];
			for (int i = 0; i < links.length; i++) {
				this.children[i] = new RepositoryRevision(this, links[i]);
			}
			Arrays.sort(this.children, new Comparator<RepositoryRevision>() {
				public int compare(RepositoryRevision o1, RepositoryRevision o2) {			
					IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
					if (SVNTeamPreferences.getBehaviourBoolean(store, SVNTeamPreferences.BEHAVIOUR_CASE_INSENSITIVE_TABLE_SORTING_NAME)) {
						return o1.getLabel().compareToIgnoreCase(o2.getLabel());
					}
					return o1.getLabel().compareTo(o2.getLabel());
				}				
			});		
		}
		return this.children;
	}
	
	public ImageDescriptor getImageDescriptor(Object o) {
		return SVNTeamUIPlugin.instance().getImageDescriptor("icons/objects/revisions.gif"); //$NON-NLS-1$
	}

	public boolean equals(Object obj) {
		if (obj != null && obj instanceof RepositoryRevisions) {
			return ((RepositoryRevisions)obj).location.equals(this.location);
		}
		return super.equals(obj);
	}
	
	protected class RevisionWrapper {
		public SVNRevision revision;
		
		public RevisionWrapper(SVNRevision revision) {
			this.revision = revision;
		}

		public int hashCode() {
			return this.revision.getKind().id;
		}
		
		public boolean equals(Object obj) {
			if (obj instanceof RevisionWrapper) {
				return this.revision.equals(((RevisionWrapper)obj).revision);
			}
			return false;
		}
	}
}
