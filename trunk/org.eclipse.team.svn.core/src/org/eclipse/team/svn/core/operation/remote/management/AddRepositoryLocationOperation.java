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

package org.eclipse.team.svn.core.operation.remote.management;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;

/**
 * Add repository location implementation
 * 
 * @author Alexander Gurov
 */
public class AddRepositoryLocationOperation extends AbstractActionOperation {
	protected IRepositoryLocation location;

	public AddRepositoryLocationOperation(IRepositoryLocation location) {
		super("Operation.AddRepositoryLocation");
		this.location = location;
	}

	public ISchedulingRule getSchedulingRule() {
		return null;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		SVNRemoteStorage.instance().addRepositoryLocation(this.location);
		//try to fetch repository root URL and UUID
		this.location.getRepositoryRootUrl();
	}
	
	protected String getShortErrorMessage(Throwable t) {
		return MessageFormat.format(super.getShortErrorMessage(t), new Object[] {this.location.getUrl()});
	}

}
