/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.revision.graph.graphic;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphMessages;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**  
 * @author Igor Burilo
 */
public class RevisionGraphEditorInput implements IEditorInput {

	protected IRepositoryResource resource;
	protected Object model;
	
	public RevisionGraphEditorInput(IRepositoryResource resource, Object model) {
		if (resource == null) {
			throw new IllegalArgumentException("resource"); //$NON-NLS-1$
		} 
		if (model == null) {
			throw new IllegalArgumentException("model"); //$NON-NLS-1$
		} 
		
		this.resource = resource;
		this.model = model;
	}

	public String getName() {
		String tooltip = SVNRevisionGraphMessages.format(SVNRevisionGraphMessages.RevisionGraphEditor_EditName, new Object[]{resource.getName(), resource.getSelectedRevision().toString()});
		return tooltip;
	}
	
	public String getToolTipText() {
		return this.getName();
	}
	
	public ImageDescriptor getImageDescriptor() {
		return null;
	}
	
	/**
	 * Current model can be either <class>String</class> or <class>RevisionRootNode</class>
	 * 
	 * String indicates that we can't create model for some reason, e.g.
	 * there's no connection to repository and cache is empty
	 * 
	 * @return model
	 */
	public Object getModel() {
		return this.model;
	}
	
	public void setModel(Object model) {
		this.model = model;
	}
	
	public IRepositoryResource getResource() {
		return this.resource;
	}
	
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} 		
		if (obj instanceof RevisionGraphEditorInput) {
			return this.resource.equals(((RevisionGraphEditorInput) obj).resource);
		}		
		return false;
	}
	
	public boolean exists() {
		return false;
	}
	
	public IPersistableElement getPersistable() {	
		return null;
	}

	public Object getAdapter(Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

}
