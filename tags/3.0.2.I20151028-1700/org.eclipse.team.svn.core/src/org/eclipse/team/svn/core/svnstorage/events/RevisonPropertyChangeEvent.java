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

import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;

/**
 * This event is sent when the revision property is changed.
 * 
 * @author Alexei Goncharov
 */
public class RevisonPropertyChangeEvent {

	public static final int SET = 0;
	public static final int REMOVED = 1;
	
	protected int action;
	protected SVNRevision revision;
	protected SVNProperty property;
	protected IRepositoryLocation location;
	
	public RevisonPropertyChangeEvent(int action,
								SVNRevision revision,
								IRepositoryLocation location,
								SVNProperty property){
		this.revision = revision;
		this.property = property;
		this.action = action;
		this.location = location;
	}
	
	public int getAction() {
		return this.action;
	}
	
	public SVNRevision getRevision() {
		return this.revision;
	}
	
	public SVNProperty getProperty() {
		return this.property;
	}
	
	public IRepositoryLocation getLocation() {
		return this.location;
	}
}
