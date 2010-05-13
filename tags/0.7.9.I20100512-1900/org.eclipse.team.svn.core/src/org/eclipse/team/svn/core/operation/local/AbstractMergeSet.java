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

/**
 * Merge scope info: merge to, merge from resources and merge start revision
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractMergeSet {
    public final IResource []to;
    
    protected ArrayList<SVNMergeStatus> statuses;
    
    public AbstractMergeSet(IResource []to) {
    	this.to = to;
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
    
}
