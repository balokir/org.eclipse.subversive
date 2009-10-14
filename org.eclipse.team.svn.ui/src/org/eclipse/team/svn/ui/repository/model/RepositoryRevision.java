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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRevisionLink;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Repository revision node representation 
 * 
 * @author Alexander Gurov
 */
public class RepositoryRevision extends RepositoryFictiveNode implements IParentTreeNode, IDataTreeNode, IToolTipProvider {	
	protected RepositoryRevisions parent;
	protected RepositoryResource []wrappers;
	protected IRevisionLink link;
	protected SVNRevision revision;
	
	public RepositoryRevision(RepositoryRevisions parent, IRevisionLink link) {
		this.parent = parent;
		this.link = link;
		this.revision = this.link.getRepositoryResource().getSelectedRevision();				
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
    
    public SVNRevision getRevision() {
    	return this.revision;
    }
    
	public IRevisionLink getRevisionLink() {
		return this.link;
	}
	
	public void refresh() {
		this.wrappers = RepositoryFolder.wrapChildren(null, new IRepositoryResource[] {this.link.getRepositoryResource()}, null);
	}
	
	public Object getData() {
		return null;
	}
	
	public boolean hasChildren() {
		return true;
	}
	
	public String getLabel() {
		/*
		 * Show resource url relative to repository root
		 * 
		 * Note how repository root is calculated: 
		 * parent.getRepositoryLocation().getRepositoryRootUrl()
		 * this is done instead of
		 * this.link.getRepositoryResource().getRepositoryLocation().getRepositoryRootUrl()
		 * in order externals to different repositories were not shown
		 * as relative to repository root, so they're shown with full url
		 */
		IRepositoryResource resource = this.link.getRepositoryResource();		
		IPath rootPath = new Path(this.parent.getRepositoryLocation().getRepositoryRootUrl());
		IPath resourcePath = new Path(resource.getUrl());
		if (rootPath.isPrefixOf(resourcePath)) {
			IPath relativePath = resourcePath.makeRelativeTo(rootPath);
			return "^" + (relativePath.isEmpty() ? "" : ("/" + relativePath.toString())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else {
			return resourcePath.toString();
		}								
	}
	
	public String getLabel(Object o) {		
		return this.getLabel() + " " + this.revision.toString(); //$NON-NLS-1$
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
			return this.revision.equals(other.revision);
		}
		return super.equals(obj);
	}

	public String getToolTipMessage(String formatString) {	
		return this.link.getComment();
	}
	
}
