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
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

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
		RepositoryRevisions.REVISIONS_NAME = SVNTeamUIPlugin.instance().getResource("RepositoriesView.Model.Revisions");
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

	public IRepositoryResource []getLinks(SVNRevision revision) {
		IRepositoryResource []resources = this.location.getRevisionLinks();
		ArrayList<IRepositoryResource> retVal = new ArrayList<IRepositoryResource>();
		for (int i = 0; i < resources.length; i++) {
			if (resources[i].getSelectedRevision().equals(revision)) {
				retVal.add(resources[i]);
			}
		}
		return retVal.toArray(new IRepositoryResource[retVal.size()]);
	}
	
	public Object []getChildren(Object o) {
		if (this.children == null) {
			IRepositoryResource []resources = this.location.getRevisionLinks();

			HashSet<RevisionWrapper> revisions = new HashSet<RevisionWrapper>();
			for (int i = 0; i < resources.length; i++) {
				revisions.add(new RevisionWrapper(resources[i].getSelectedRevision()));
			}
			
			this.children = new RepositoryRevision[revisions.size()];
			int i = 0;
			for (Iterator<RevisionWrapper> it = revisions.iterator(); i < this.children.length; i++) {
				this.children[i] = new RepositoryRevision(this, it.next().revision);
			}
		}
		return this.children;
	}
	
	public ImageDescriptor getImageDescriptor(Object o) {
		return SVNTeamUIPlugin.instance().getImageDescriptor("icons/objects/revisions.gif");
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
			return this.revision.getKind();
		}
		
		public boolean equals(Object obj) {
			if (obj instanceof RevisionWrapper) {
				return this.revision.equals(((RevisionWrapper)obj).revision);
			}
			return false;
		}
	}
}
