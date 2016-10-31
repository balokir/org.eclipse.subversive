/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Andrey Loskutov - [scalability] SVN update takes hours if "Synchronize" view is opened
 *******************************************************************************/

package org.eclipse.team.svn.core.resource.events;

import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * Project state changed event implementation
 * 
 * @author Alexander Gurov
 */
public class ProjectStatesChangedEvent extends ResourceStatesChangedEvent {
	public static final int ST_POST_SHARED = 0;
	public static final int ST_POST_DISCONNECTED = 1;
	public static final int ST_PRE_SHARED = 2;
	public static final int ST_PRE_DISCONNECTED = 3;
	// POST_CLOSE/POST_DELETE events are not provided by Eclipse Platform
	public static final int ST_PRE_CLOSED = 4;
	public static final int ST_PRE_DELETED = 5;
	
	public final int newState;

	public ProjectStatesChangedEvent(IProject []projects, int newState) {
		super(projects, IResource.DEPTH_ZERO, ResourceStatesChangedEvent.CHANGED_NODES);
		this.newState = newState;
	}

	@Override
	public boolean canMerge(ResourceStatesChangedEvent e) {
		if (e instanceof ProjectStatesChangedEvent) {			
			return super.canMerge(e) && this.newState == ((ProjectStatesChangedEvent)e).newState;
		}
		return false;
	}
	
	@Override
	public ProjectStatesChangedEvent merge(ResourceStatesChangedEvent event) {
		IProject [] arr = new IProject[this.resources.length + event.resources.length];
		System.arraycopy(this.resources, 0, arr, 0, this.resources.length);
		System.arraycopy(event.resources, 0, arr, this.resources.length, event.resources.length);
		return new ProjectStatesChangedEvent(arr, this.newState);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.newState;
		result = prime * result + Arrays.hashCode(this.resources);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ProjectStatesChangedEvent)) {
			return false;
		}
		ProjectStatesChangedEvent other = (ProjectStatesChangedEvent) obj;
		if (this.newState != other.newState) {
			return false;
		}
		if (!Arrays.equals(this.resources, other.resources)) {
			return false;
		}
		return true;
	}
}
