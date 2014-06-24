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

package org.eclipse.team.svn.core.operation;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.team.svn.core.connector.ISVNProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNCommitStatus;

/**
 * Empty implementation of the IProgressMonitor interface
 * 
 * @author Alexander Gurov
 */
public class SVNNullProgressMonitor implements ISVNProgressMonitor {
	protected ArrayList<SVNCommitStatus> commitStatuses = new ArrayList<SVNCommitStatus>();
	
	public Collection<SVNCommitStatus> getCommitStatuses() {
		return this.commitStatuses;
	}

	public Collection<SVNCommitStatus> getPostCommitErrors() {
		ArrayList<SVNCommitStatus> retVal = new ArrayList<SVNCommitStatus>();
		for (SVNCommitStatus status : this.commitStatuses) {
			retVal.add(status);
		}
		return retVal;
	}

	public void progress(int current, int total, ItemState state) {

	}

	public boolean isActivityCancelled() {
		return false;
	}

	public void commitStatus(SVNCommitStatus status) {
		this.commitStatuses.add(status);
	}
	
}
