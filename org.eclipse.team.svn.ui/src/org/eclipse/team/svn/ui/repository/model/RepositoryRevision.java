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

import java.text.MessageFormat;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Repository revision node representation 
 * 
 * @author Alexander Gurov
 */
public class RepositoryRevision extends RepositoryFictiveNode implements IParentTreeNode, IDataTreeNode {
	protected static String REVISION_NAME;
	protected IRepositoryResource []resources;
	protected RepositoryResource []wrappers;
	protected RepositoryRevisions parent;
	protected SVNRevision revision;
	
	public RepositoryRevision(RepositoryRevisions parent, SVNRevision revision) {
		RepositoryRevision.REVISION_NAME = SVNTeamUIPlugin.instance().getResource("RepositoriesView.Model.Revision");
		this.parent = parent;
		this.revision = revision;
		this.refresh();
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
    
	public IRepositoryResource []getRepositoryResources() {
		return this.resources;
	}
	
	public void refresh() {
		this.resources = this.parent.getLinks(this.revision);
		this.wrappers = RepositoryFolder.wrapChildren(null, this.resources, null);
	}
	
	public Object getData() {
		return null;
	}
	
	public boolean hasChildren() {
		return true;
	}
	
	public String getLabel(Object o) {
		return MessageFormat.format(RepositoryRevision.REVISION_NAME, new Object[] {this.revision.toString()});
	}

	public Object[] getChildren(Object o) {
		return this.wrappers;
	}
	
	public ImageDescriptor getImageDescriptor(Object o) {
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
	}

	public boolean equals(Object obj) {
		if (obj != null && obj instanceof RepositoryRevision) {
			RepositoryRevision other = (RepositoryRevision)obj;
			return this.parent.equals(other.parent) && this.revision.equals(other.revision);
		}
		return super.equals(obj);
	}
	
}
