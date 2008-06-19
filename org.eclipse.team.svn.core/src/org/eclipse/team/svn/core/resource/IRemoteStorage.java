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

package org.eclipse.team.svn.core.resource;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.resource.events.IResourceStatesListener;
import org.eclipse.team.svn.core.resource.events.ResourceStatesChangedEvent;

/**
 * Remote storage interace
 * 
 * @author Alexander Gurov
 */
public interface IRemoteStorage extends ISVNStorage {
	public void addResourceStatesListener(Class eventClass, IResourceStatesListener listener);
	public void removeResourceStatesListener(Class eventClass, IResourceStatesListener listener);
    public void fireResourceStatesChangedEvent(ResourceStatesChangedEvent event);
	
	public IResourceChange asResourceChange(IChangeStateProvider changeState, boolean update);
	public ILocalResource asLocalResource(IResource resource);
	public ILocalResource asLocalResourceAccessible(IResource resource);
	public ILocalResource asLocalResourceDirty(IResource resource);
	public void refreshLocalResources(IResource []resources, int depth);
	public IRepositoryResource asRepositoryResource(IRepositoryLocation location, String url, boolean isFile);
	public IRepositoryResource asRepositoryResource(IResource resource);
	public ILocalResource asLocalResource(IProject project, String url, int kind);
	public IRepositoryLocation getRepositoryLocation(IResource resource);
	
	public byte []resourceChangeAsBytes(IResourceChange resource);
	public IResourceChange resourceChangeFromBytes(byte []bytes);

}
