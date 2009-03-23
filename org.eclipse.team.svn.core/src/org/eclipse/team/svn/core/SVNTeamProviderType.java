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

package org.eclipse.team.svn.core;

import org.eclipse.team.core.ProjectSetCapability;
import org.eclipse.team.core.RepositoryProviderType;
import org.eclipse.team.core.subscribers.Subscriber;

/**
 * IProject attachement type
 * 
 * @author Alexander Gurov
 */
public class SVNTeamProviderType extends RepositoryProviderType {

	public SVNTeamProviderType() {
		super();
	}

	public ProjectSetCapability getProjectSetCapability() {
		return new SVNTeamProjectSetCapability();
	}
		
	public Subscriber getSubscriber() {		
		return super.getSubscriber();
	}
	
}
