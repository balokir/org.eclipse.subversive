/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.client.Status;
import org.eclipse.team.svn.core.resource.IRepositoryResource;

/**
 * Merge scope info: merge to, merge from resources and merge start revision
 * 
 * @author Alexander Gurov
 */
public class MergeSet {
    public final IResource []to;
    public final IRepositoryResource []from;
    public final Revision start;
    
    protected Status []statuses;
    
    public MergeSet(IResource []to, IRepositoryResource []from, Revision start) {
    	this.to = to;
    	this.from = from;
    	this.start = start;
    }

	public Status []getStatuses() {
		return this.statuses;
	}

	public void setStatuses(Status []statuses) {
		this.statuses = statuses;
	}
    
}
