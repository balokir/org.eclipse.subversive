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

package org.eclipse.team.svn.ui.operation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;

/**
 * Allows us to get revision in the separate thread
 * 
 * @author Alexander Gurov
 */
public class GetRemoteResourceRevisionOperation extends AbstractActionOperation {
	protected IRepositoryResource resource;
	protected long revision;

	public GetRemoteResourceRevisionOperation(IRepositoryResource resource) {
		super("Operation.GetRemoteRevision");
		this.resource = resource;
		this.revision = SVNRevision.INVALID_REVISION_NUMBER;
	}

	public long getRevision() {
		return this.revision;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		this.revision = this.resource.getRevision();
	}

}
