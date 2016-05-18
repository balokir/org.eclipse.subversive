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

package org.eclipse.team.svn.core.resource.events;

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

}
