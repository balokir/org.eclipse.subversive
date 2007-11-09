/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.properties;

import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IResourcePropertyProvider;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * Properties editor input
 *
 * @author Sergiy Logvin
 */
public class PropertiesEditorInput implements IEditorInput {
	
	protected IRepositoryResource remoteResource;
	protected IResource localResource;
	protected IResourcePropertyProvider propertyProvider;

	public PropertiesEditorInput(IAdaptable resource, IResourcePropertyProvider propertyProvider) {
		if (resource instanceof IRepositoryResource) {
			this.remoteResource = (IRepositoryResource)resource;
		}
		else if(resource instanceof IResource) {
			this.localResource = (IResource)resource;
		}
		this.propertyProvider = propertyProvider;
	}

	public boolean exists() {
		return this.remoteResource != null || this.localResource != null;
	}
	
	public ImageDescriptor getImageDescriptor() {
		return SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/propertiesedit.gif");
	}
	
	public String getName() {
		if (this.localResource != null) {
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(this.localResource);
			if (local == null) {
				return this.localResource.getName();
			}
			return local.getName() + (IStateFilter.SF_ADDED.accept(local.getResource(), local.getStatus(), local.getChangeMask()) ? "" : (" " + local.getRevision()));
		}
		
		String name = SVNTeamUIPlugin.instance().getResource("PropertiesEditor.Name");
		return MessageFormat.format(name, new String[] {remoteResource.getName(), String.valueOf(remoteResource.getSelectedRevision())});
	}
	
	public IPersistableElement getPersistable() {
		return null;
	}
	
	public String getToolTipText() {
		return this.getName();
	}
	
	public Object getAdapter(Class adapter) {
		return null;
	}
	
	public IAdaptable getResource() {
		if (this.localResource == null) {
			return this.remoteResource;
		}
		else {
			return this.localResource;
		}
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof PropertiesEditorInput) {
			return this.getResource().equals(((PropertiesEditorInput)obj).getResource());
		}
		else {
			return false;
		}
	}

	public IResourcePropertyProvider getPropertyProvider() {
		return this.propertyProvider;
	}

}
