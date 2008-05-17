/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.local;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.svn.core.connector.SVNMergeStatus;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Merge scope info: merge to, merge from resources and merge start revision
 * 
 * @author Alexander Gurov
 */
public class MergeSet {
    public final IResource []to;
    public final IRepositoryResource []fromStart;
    public final IRepositoryResource []fromEnd;
    public final boolean ignoreAncestry;
    public final int depth;
    
    protected ArrayList<SVNMergeStatus> statuses;
    
    public MergeSet(IResource []to, IRepositoryResource []fromStart, IRepositoryResource []fromEnd, boolean ignoreAncestry, int depth) {
    	this.to = to;
    	this.fromStart = fromStart;
    	this.fromEnd = fromEnd;
    	this.ignoreAncestry = ignoreAncestry;
    	this.depth = depth;
    	this.statuses = new ArrayList<SVNMergeStatus>();
    }

	public SVNMergeStatus []getStatuses() {
		return this.statuses.toArray(new SVNMergeStatus[this.statuses.size()]);
	}

	public void setStatuses(SVNMergeStatus []statuses) {
		this.statuses.clear();
		this.addStatuses(statuses);
	}
    
	public void addStatuses(SVNMergeStatus []statuses) {
		if (statuses != null) {
			this.statuses.addAll(Arrays.asList(statuses));
		}
	}
    
	protected static IRepositoryResource []makeFromStart(IRepositoryResource []fromEnd, SVNRevision start) {
		IRepositoryResource []fromStart = new IRepositoryResource[fromEnd.length];
		for (int i = 0; i < fromEnd.length; i++) {
			fromStart[i] = SVNUtility.copyOf(fromEnd[i]);
			fromStart[i].setSelectedRevision(start);
		}
		return fromStart;
	}
}
