/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.svnstorage.events;

import org.eclipse.team.svn.core.resource.IRepositoryLocation;

/**
 * Event is sent when the repository location is added or removed.
 * 
 * @author Alexei Goncharov
 */
public class RepositoriesStateChangedEvent {
	
	public static final int ADDED = 0;
	public static final int REMOVED = 1;
	
	protected int eventType;
	protected IRepositoryLocation location;
	
	public RepositoriesStateChangedEvent(IRepositoryLocation location, int action) {
		this.location = location;
		this.eventType = action;
	}
	
	public int getAction() {
		return this.eventType;
	}
	
	public IRepositoryLocation getLocation() {
		return this.location;
	}

}
