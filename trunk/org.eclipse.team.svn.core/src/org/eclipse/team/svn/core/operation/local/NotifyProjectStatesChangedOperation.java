/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.local;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.resource.events.ProjectStatesChangedEvent;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;

/**
 * Fire project state changed event
 * 
 * @author Alexander Gurov
 */
public class NotifyProjectStatesChangedOperation extends AbstractActionOperation {
	protected IProject []projects;
	protected int eventType;

	public NotifyProjectStatesChangedOperation(IProject []projects, int eventType) {
		super("Operation.NotifyProjectChange");
		this.projects = projects;
		this.eventType = eventType;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		SVNRemoteStorage.instance().fireResourceStatesChangedEvent(new ProjectStatesChangedEvent(this.projects, this.eventType));		
	}

}
