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
import org.eclipse.team.svn.core.client.SVNEntryStatus;
import org.eclipse.team.svn.core.client.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryResource;

/**
 * Merge scope info: merge to, merge from resources and merge start revision
 * 
 * @author Alexander Gurov
 */
public class MergeSet {
    public final IResource []to;
    public final IRepositoryResource []from;
    public final SVNRevision start;
    
    protected SVNEntryStatus []statuses;
    
    public MergeSet(IResource []to, IRepositoryResource []from, SVNRevision start) {
    	this.to = to;
    	this.from = from;
    	this.start = start;
    }

	public SVNEntryStatus []getStatuses() {
		return this.statuses;
	}

	public void setStatuses(SVNEntryStatus []statuses) {
		this.statuses = statuses;
	}
    
}
